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

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.SonarException; //@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import org.sonar.plugins.cxx.CxxLanguage;

/**
 * This class is used as base for all sensors which import reports.
 * It hosts common logic such as finding the reports and saving issues
 * in SonarQube
 */
public abstract class CxxReportSensor implements Sensor {
  private ResourcePerspectives perspectives;
  private Set<String> notFoundFiles = new HashSet<String>();
  private Set<String> uniqueIssues = new HashSet<String>();
  private final Metric metric;
  private int violationsCount;

  protected FileSystem fs;
  protected Settings conf;
  private final ProjectReactor reactor;

  /**
   * Use this constructor if you dont have to save violations aka issues
   *
   * @param conf the Settings object used to access the configuration properties
   * @param fs   file system access layer
   * @param reactor
   */
  protected CxxReportSensor(Settings conf, FileSystem fs, ProjectReactor reactor) {
    this(null, conf, fs, reactor, null);
  }

  /**
   * Use this constructor if your sensor implementation saves violations aka issues
   *
   * @param perspectives used to create issuables
   * @param conf         the Settings object used to access the configuration properties
   * @param fs           file system access layer
   * @param metric       this metrics will be used to save a measure of the overall
   *                     issue count. Pass 'null' to skip this.
   * @param reactor
   */
  protected CxxReportSensor(ResourcePerspectives perspectives, Settings conf, FileSystem fs, ProjectReactor reactor, Metric metric) {
    this.conf = conf;
    this.fs = fs;
    this.metric = metric;
    this.reactor = reactor;
    this.perspectives = perspectives;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(fs.predicates().hasLanguage(CxxLanguage.KEY))
      && conf.hasKey(reportPathKey());
  }
  
  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    if (!CxxUtils.isReactorProject(project)) {
      try {
        List<File> reports = getReports(conf,
          reactor.getRoot().getBaseDir().getCanonicalPath(),
          fs.baseDir().getPath(),
          reportPathKey());
        violationsCount = 0;

        for (File report : reports) {
          CxxUtils.LOG.info("Processing report '{}'", report);
          try {
            int prevViolationsCount = violationsCount;
            processReport(project, context, report);
            CxxUtils.LOG.info("{} processed = {}", metric == null ? "Issues" : metric.getName(),
              violationsCount - prevViolationsCount);
          } catch (EmptyReportException e) {
            CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
          }
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
        throw new SonarException(msg, e); //@todo SonarException has been deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  protected String getStringProperty(String name, String def) {
      String value = conf.getString(name);
      if (value == null)
          value = def;
      return value;
  }

  public static List<File> getReports(Settings conf,
    String baseDirPath1,
    String baseDirPath2,
    String reportPathPropertyKey) {
    String reportPath = conf.getString(reportPathPropertyKey);
    List<File> reports = new ArrayList<File>();
    if (reportPath != null && !reportPath.isEmpty()) {
      reportPath = FilenameUtils.normalize(reportPath);
      File singleFile = new File(reportPath);
      if (singleFile.exists()) {
        reports.add(singleFile);
      } else {
        CxxUtils.LOG.debug("Using pattern '{}' to find reports", reportPath);
  
        DirectoryScanner scanner = new DirectoryScanner();
        String[] includes = new String[1];
        includes[0] = reportPath;
        scanner.setIncludes(includes);
        String baseDirPath = baseDirPath1;
        scanner.setBasedir(new File(baseDirPath));
        String[] relPaths = new String[0];
        try {
          scanner.scan();
          relPaths = scanner.getIncludedFiles();
        } catch (IllegalStateException e) {
          CxxUtils.LOG.error("Invalid report baseDir '{}'", baseDirPath);
        }
        if (relPaths.length < 1 && !baseDirPath2.isEmpty()) {
          baseDirPath = baseDirPath2;
          scanner.setBasedir(new File(baseDirPath));
          try {
            scanner.scan();
            relPaths = scanner.getIncludedFiles();
          } catch (IllegalStateException e) {
            CxxUtils.LOG.error("Invalid report baseDir '{}'", baseDirPath);
          }
        }
        
        for (String relPath : relPaths) {
          String path = CxxUtils.normalizePath(new File(baseDirPath, relPath).getAbsolutePath());
          try {
            File reportFile = new File(path);
            if (reportFile.exists()) {
              reports.add(reportFile);
            } else {
              CxxUtils.LOG.error("Can't read report '{}'", path);
            }
          } catch (SecurityException e) {
            CxxUtils.LOG.error("Read access to report '{}' denied", path);
          }
        }
        if (reports.isEmpty()) {
          CxxUtils.LOG.warn("Cannot find a report for '{}={}'", reportPathPropertyKey, reportPath);
        }
      }
    } else {
      CxxUtils.LOG.error("Undefined report path value for key '{}'", reportPathPropertyKey);
    }

    return reports;
  }

  /**
   * Saves code violation only if unique.
   * Compares file, line, ruleId and msg.
   */
  public void saveUniqueViolation(Project project, SensorContext context, String ruleRepoKey,
    String file, String line, String ruleId, String msg) {

    if (uniqueIssues.add(file + line + ruleId + msg)) { // StringBuilder is slower
      saveViolation(project, context, ruleRepoKey, file, line, ruleId, msg);
    }
  }

  /**
   * Saves a code violation which is detected in the given file/line and has
   * given ruleId and message. Saves it to the given project and context.
   * Project or file-level violations can be saved by passing null for the
   * according parameters ('file' = null for project level, 'line' = null for
   * file-level)
   */
  private void saveViolation(Project project, SensorContext context, String ruleRepoKey,
    String filename, String line, String ruleId, String msg) {
    Issuable issuable = null;
    int lineNr = 0;
    // handles file="" situation -- file level
    if ((filename != null) && (filename.length() > 0)) {
      String root = reactor.getRoot().getBaseDir().getAbsolutePath();
      String normalPath = CxxUtils.normalizePathFull(filename, root);
      if (normalPath != null && !notFoundFiles.contains(normalPath)) {
        InputFile inputFile = fs.inputFile(fs.predicates().is(new File(normalPath)));
        if (inputFile != null) {
          lineNr = getLineAsInt(line, inputFile.lines());
          issuable = perspectives.as(Issuable.class, inputFile);
        } else {
          CxxUtils.LOG.warn("Cannot find the file '{}', skipping violations", normalPath);
          notFoundFiles.add(normalPath);
        }
      }
    } else { // project level
      issuable = perspectives.as(Issuable.class, (Resource) project);
    }

    if (issuable != null) {
      addIssue(issuable, lineNr, RuleKey.of(ruleRepoKey, ruleId), msg);
    }
  }

  private void addIssue(Issuable issuable, int lineNr, RuleKey rule, String msg) {
    Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder()
      .ruleKey(rule)
      .message(msg);
    if (lineNr > 0) {
      issueBuilder = issueBuilder.line(lineNr);
    }
    Issue issue = issueBuilder.build();
    try {
      if (issuable.addIssue(issue)) {
        violationsCount++;
      }
    } catch (org.sonar.api.utils.MessageException me) {
      CxxUtils.LOG.error("Could not add the issue, details: '{}'", me.toString());
    }
  }

  private int getLineAsInt(String line, int maxLine) {
    int lineNr = 0;
    if (line != null) {
      try {
        lineNr = Integer.parseInt(line);
        if (lineNr < 1) {
          lineNr = 1;
        } else if (lineNr > maxLine) { // https://jira.sonarsource.com/browse/SONAR-6792
          lineNr = maxLine;
        }
      } catch (java.lang.NumberFormatException nfe) {
        CxxUtils.LOG.warn("Skipping invalid line number: {}", line);
        lineNr = -1;
      }
    }
    return lineNr;
  }

  protected void processReport(final Project project, final SensorContext context, File report)
      throws Exception
  {
  }

  protected String reportPathKey() {
    return "";
  }
}
