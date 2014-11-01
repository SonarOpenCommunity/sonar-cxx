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
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.sonar.api.resources.Resource;

/**
 * {@inheritDoc}
 */
public abstract class CxxReportSensor implements Sensor {
  private ResourcePerspectives perspectives;
  protected Settings conf;
  private HashSet<String> notFoundFiles = new HashSet<String>();
  private HashSet<String> uniqueIssues = new HashSet<String>();
  protected ModuleFileSystem fs;
  private final ProjectReactor reactor;

  private final Metric metric;
  private int violationsCount;

  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(Settings conf, ModuleFileSystem fs, ProjectReactor reactor) {
    this(null, conf, fs, reactor);
  }

  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(ResourcePerspectives perspectives, Settings conf, ModuleFileSystem fs, ProjectReactor reactor) {
    this(perspectives, conf, fs, reactor, null);
  }

  /**
   * {@inheritDoc}
   */
  public CxxReportSensor(ResourcePerspectives perspectives, Settings conf, ModuleFileSystem fs, ProjectReactor reactor, Metric metric) {
    this.perspectives = perspectives;
    this.conf = conf;
    this.fs = fs;
    this.reactor = reactor;
    this.metric = metric;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
  return !fs.files(FileQuery.onSource().onLanguage(CxxLanguage.KEY)).isEmpty();
  }
  
  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    if (!CxxUtils.isReactorProject(project)){
    try {
        List<File> reports = getReports(conf, reactor.getRoot().getBaseDir().getCanonicalPath(), reportPathKey(), defaultReportPath());
        if (reports.isEmpty()) {
          reports = getReports(conf, fs.baseDir().getPath(), reportPathKey(), defaultReportPath());
        }

        violationsCount = 0;

        for (File report : reports) {
          CxxUtils.LOG.info("Processing report '{}'", report);
          try {
            int prevViolationsCount = violationsCount;
            processReport(project, context, report);
            CxxUtils.LOG.info("{} processed = {}", metric == null ? "Issues" : metric.getName(), violationsCount - prevViolationsCount);
          } catch (EmptyReportException e) {
            CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
          }
        }

        if (reports.isEmpty()) {
          handleNoReportsCase(context);
        }

        if (metric != null) {
          Measure measure = new Measure(metric);
          measure.setIntValue(violationsCount);
          context.saveMeasure(measure);
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
  public boolean saveViolation(Project project, SensorContext context,
      String ruleRepoKey, String filename, String line, String ruleId,
      String msg) {
    boolean add = false;
    Resource resource = null;
    int lineNr = 0;
    // handles file="" situation -- file level
    if ((filename != null) && (filename.length() > 0)) {
      String normalPath = CxxUtils.normalizePathList(filename, fs.baseDir().getAbsolutePath());
      if ((normalPath != null) && !notFoundFiles.contains(normalPath)) {
          org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File
              .fromIOFile(new File(normalPath), project);
          if (context.getResource(sonarFile) != null) {
            lineNr = getLineAsInt(line);
            resource = sonarFile;
            add = true;
          } else {
            CxxUtils.LOG.warn("Cannot find the file '{}', skipping violations", normalPath);
            notFoundFiles.add(normalPath);
          }
      }
    } else {
      // project level violation
      resource = project;
      add = true;
    }

    if (add) {
      add = contextSaveViolation(resource, lineNr, RuleKey.of(ruleRepoKey, ruleId), msg);
    }

    return add;
  }

  private boolean contextSaveViolation(Resource resource, int lineNr, RuleKey rule, String msg) {
    Issuable issuable = perspectives.as(Issuable.class, resource);
    boolean result = false;
    if (issuable != null) {
      Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder()
          .ruleKey(rule)
          .message(msg);
      if (lineNr > 0) {
        issueBuilder = issueBuilder.line(lineNr);
      }
      Issue issue = issueBuilder.build();
      result = issuable.addIssue(issue);
      if (result)
        violationsCount++;
    }
    return result;
  }

  private int getLineAsInt(String line) {
    int lineNr = 0;
    if (line != null) {
      try {
        lineNr = Integer.parseInt(line);
        lineNr = lineNr == 0 ? 1 : lineNr;
      } catch (java.lang.NumberFormatException nfe) {
        CxxUtils.LOG.warn("Skipping invalid line number: {}", line);
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
