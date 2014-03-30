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
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.cxx.CxxPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class CxxReportSensor implements Sensor {
  private RuleFinder ruleFinder;
  protected Settings conf;
  private HashSet<String> uniqueFileName = new HashSet<String>();
  protected ModuleFileSystem fs;
  
  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(Settings conf, ModuleFileSystem fs) {
    this(null, conf, fs);
  }

  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(RuleFinder ruleFinder, Settings conf, ModuleFileSystem fs) {
    this.ruleFinder = ruleFinder;
    this.conf = conf;
    this.fs = fs;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return !project.getFileSystem().mainFiles(CxxLanguage.KEY).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    try {
      List<File> reports = getReports(conf, fs.baseDir().getPath(),
          reportPathKey(), defaultReportPath());
      for (File report : reports) {
        CxxUtils.LOG.info("Processing report '{}'", report);
        try{
          processReport(project, context, report);
        }
        catch(EmptyReportException e){
          CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
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
      if (value == null)
          value = def;
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

    CxxUtils.LOG.debug("Using pattern '{}' to find reports", reportPath);

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
   * Saves a code violation which is detected in the given file/line
   * and has given ruleId and message. Saves it to the given project and context.
   * Project or file-level violations can be saved by passing null for the according parameters
   * ('file' = 'line' = null for project level, 'line' = null for file-level)
   */
  protected void saveViolation(Project project, SensorContext context, String ruleRepoKey,
                               String file, String line, String ruleId, String msg) {
    RuleQuery ruleQuery = RuleQuery.create()
      .withRepositoryKey(ruleRepoKey)
      .withKey(ruleId);
    Rule rule = ruleFinder.find(ruleQuery);
    if (rule != null) {
      Violation violation = null;
      // handles file="" situation
      if ((file != null) && (file.length() > 0)){
        org.sonar.api.resources.File resource =
          org.sonar.api.resources.File.fromIOFile(new File(file), project);
        if (context.getResource(resource) != null) {
          // file level violation
          violation = Violation.create(rule, resource);

          // considering the line information for file level violations only
          if (line != null){
            try{
              int linenr = Integer.parseInt(line);
              linenr = linenr == 0 ? 1 : linenr;
              violation.setLineId(linenr);
            } catch(java.lang.NumberFormatException nfe){
              CxxUtils.LOG.warn("Skipping invalid line number: {}", line);
            }
          }
        } else {
          if (uniqueFileName.add(file)) {
          CxxUtils.LOG.warn("Cannot find the file '{}', skipping violations", file);
          }
        }
      } else {
        // project level violation
        violation = Violation.create(rule, project);
      }

      if (violation != null){
        violation.setMessage(msg);
        context.saveViolation(violation);
      }
    } else {
      CxxUtils.LOG.warn("Cannot find the rule {}, skipping violation", ruleId);
    }
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
