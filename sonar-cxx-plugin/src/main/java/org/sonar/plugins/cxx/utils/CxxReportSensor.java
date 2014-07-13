/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.utils;

import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.sonar.api.resources.Resource;

/**
 * {@inheritDoc}
 */
public abstract class CxxReportSensor implements Sensor {
  private RuleFinder ruleFinder;
  protected Settings conf;
  private HashSet<String> notFoundFiles = new HashSet<String>();
  private HashSet<String> uniqueIssues = new HashSet<String>();
  private HashMap<String, Rule> ruleCache = new HashMap<String, Rule>();
  protected ModuleFileSystem fs;
    private final ProjectReactor reactor;

  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(Settings conf, ModuleFileSystem fs, ProjectReactor reactor) {
    this(null, conf, fs, reactor);
  }

  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(RuleFinder ruleFinder, Settings conf, ModuleFileSystem fs, ProjectReactor reactor) {
    this.ruleFinder = ruleFinder;
    this.conf = conf;
    this.fs = fs;
    this.reactor = reactor;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
//    return !project.getFileSystem().mainFiles(CxxLanguage.KEY).isEmpty();
  return !fs.files(FileQuery.onSource().onLanguage(CxxLanguage.KEY)).isEmpty();
  }
  
  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    try {
      List<File> reports = getReports(conf, reactor.getRoot().getBaseDir().getAbsolutePath(),
          reportPathKey(), defaultReportPath());
      for (File report : reports) {
        CxxUtils.LOG.info("Processing report '" + report + "'");
        try{
          processReport(project, context, report);
        } catch(EmptyReportException e) {
          CxxUtils.LOG.warn("The report " + report + " seems to be empty, ignoring.");
        }
      }

      if (reports.isEmpty()) {
        handleNoReportsCase(context);
      }
    } catch (Exception e) {
      String msg = new StringBuilder()
          .append("Cannot feed the data into sonar, details: '")
          .append(e)
          .append("'")
          .toString();
      throw new SonarException(msg, e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public String getStringProperty(String name, String def) {
    String value = conf.getString(name);
    if (value == null) {
      value = def;
    }
    return value;
  }

  protected List<File> getReports(Settings conf,
      String baseDirPath,
      String reportPathPropertyKey,
      String defaultReportPath) {
    String reportPath = conf.getString(reportPathPropertyKey);
    if (reportPath == null) {
      reportPath = defaultReportPath;
    }

    CxxUtils.LOG.debug("Using pattern "  + reportPath + " to find reports");

    DirectoryScanner scanner = new DirectoryScanner();
    String[] includes = new String[1];
    includes[0] = reportPath;
    scanner.setIncludes(includes);
    scanner.setBasedir(new File(baseDirPath));
    scanner.scan();
    String[] relPaths = scanner.getIncludedFiles();

    List<File> reports = new ArrayList<File>();
    for (String relPath : relPaths) {
      reports.add(new File(baseDirPath, relPath));
    }

    return reports;
  }

  /**
   * Saves code violation only if unique.
   * Compares file, line, ruleId and msg.
   */
  public boolean saveUniqueViolation(Project project, SensorContext context, String ruleRepoKey,
                                        String file, String line, String ruleId, String msg) {

    if (uniqueIssues.add(file + line + ruleId + msg)) { // StringBuilder is slower
      return saveViolation(project, context, ruleRepoKey, file, line, ruleId, msg);
    }
    return false;
  }

  /**
   * Saves a code violation which is detected in the given file/line and has
   * given ruleId and message. Saves it to the given project and context.
   * Project or file-level violations can be saved by passing null for the
   * according parameters ('file' = null for project level, 'line' = null for
   * file-level)
   */

  public boolean saveViolation(Project module, SensorContext context,
      String ruleRepoKey, String filename, String line, String ruleId,
      String msg) {
    boolean add = false;
    Resource resource = null;
    int lineNr = 0;
    // handles file="" situation -- file level
    if ((filename != null) && (filename.length() > 0)) {
      String normalPath = CxxUtils.getCaseSensitiveFileName(filename, fs);
      if ((normalPath != null) && !notFoundFiles.contains(normalPath)) {
          org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File
              .fromIOFile(new File(normalPath), module);

          if (context.getResource(sonarFile) != null) {
            lineNr = getLineAsInt(line);
            resource = sonarFile;
            add = true;
          } else {
            CxxUtils.LOG.warn("Cannot find the file '" + normalPath + "', skipping violations");
            notFoundFiles.add(normalPath);
          }
      }
    } else {
      // project level violation
      resource = module;
      add = true;
    }

    if (add) {
      Rule rule = getRule(ruleRepoKey, ruleId);
      if (rule != null) {
        contextSaveViolation(context, resource, lineNr, rule, msg);
      }
    }

    return add;
  }

  private void contextSaveViolation(SensorContext context, Resource resource, int lineNr, Rule rule, String msg) {
    Violation violation = Violation.create(rule, resource);
    if (lineNr > 0) {
      violation.setLineId(lineNr);
    }
    violation.setMessage(msg);
    context.saveViolation(violation);
  }

  private Rule getRule(String ruleRepoKey, String ruleId) {
    String key = ruleRepoKey + ruleId; // StringBuilder is slower
    Rule rule = ruleCache.get(key);
    if (rule == null) {
      RuleQuery ruleQuery = RuleQuery.create()
        .withRepositoryKey(ruleRepoKey)
        .withKey(ruleId);
      rule = ruleFinder.find(ruleQuery);
      ruleCache.put(key, rule);
      if (rule == null) {
        CxxUtils.LOG.warn("Cannot find the rule {}, skipping violation", ruleId);
      }
    }
    return rule;
  }

  private int getLineAsInt(String line) {
    int lineNr = 0;
    if (line != null) {
      try {
        lineNr = Integer.parseInt(line);
        lineNr = lineNr == 0 ? 1 : lineNr;
      } catch (java.lang.NumberFormatException nfe) {
        CxxUtils.LOG.warn("Skipping invalid line number: " + line);
        lineNr = -1;
      }
    }
    return lineNr;
  }

  protected void processReport(Project project, SensorContext context, File report)
      throws Exception
  {
  }

  protected void handleNoReportsCase(SensorContext context) {
  }

  protected String reportPathKey() {
    return "";
  };

  protected String defaultReportPath() {
    return "";
  };
}
