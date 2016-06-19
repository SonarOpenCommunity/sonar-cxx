/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.Sensor; //@todo deprecated
import org.sonar.api.batch.SensorContext; //@todo deprecated
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives; //@todo deprecated
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure; //@todo deprecated
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project; //@todo deprecated
import org.sonar.api.resources.Resource; //@todo deprecated
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.api.config.Settings;

/**
 * This class is used as base for all sensors which import reports. It hosts
 * common logic such as finding the reports and saving issues in SonarQube
 */
public abstract class CxxReportSensor implements Sensor {

  private ResourcePerspectives perspectives;
  private final Set<String> notFoundFiles = new HashSet<>();
  private final Set<String> uniqueIssues = new HashSet<>();
  private final Metric metric;
  private int violationsCount;

  protected FileSystem fs;
  protected Settings settings;

  /**
   * Use this constructor if you dont have to save violations aka issues
   *
   * @param settings the Settings object used to access the configuration
   * properties
   * @param fs file system access layer
   */
  protected CxxReportSensor(Settings settings, FileSystem fs) {
    this(null, settings, fs, null);
  }

  /**
   * Use this constructor if your sensor implementation saves violations aka
   * issues
   *
   * @param perspectives used to create issuables
   * @param settings the Settings object used to access the configuration
   * properties
   * @param fs file system access layer
   * @param metric this metrics will be used to save a measure of the overall
   * issue count. Pass 'null' to skip this.
   */
  protected CxxReportSensor(ResourcePerspectives perspectives, Settings settings, FileSystem fs, Metric metric) {
    this.settings = settings;
    this.fs = fs;
    this.metric = metric;
    this.perspectives = perspectives;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(fs.predicates().hasLanguage(CxxLanguage.KEY))
      && settings.hasKey(reportPathKey());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    try {
      List<File> reports = getReports(settings, fs.baseDir(), reportPathKey());
      violationsCount = 0;
      
      for (File report : reports) {
        int prevViolationsCount = violationsCount;
        CxxUtils.LOG.info("Processing report '{}'", report);
        try {
          processReport(project, context, report);
          CxxUtils.LOG.debug("{} processed = {}", metric == null ? "Issues" : metric.getName(),
             violationsCount - prevViolationsCount);
        } catch (EmptyReportException e) {
          CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
        }
      }

      CxxUtils.LOG.info("{} processed = {}", metric == null ? "Issues" : metric.getName(),
        violationsCount);
          
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
      throw new IllegalStateException(msg, e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  protected String getStringProperty(String name, String def) {
    String value = settings.getString(name);
    if (value == null) {
      value = def;
    }
    return value;
  }

  public static List<File> getReports(Settings settings, final File moduleBaseDir,
      String reportPathPropertyKey) {

    List<File> reports = new ArrayList<>();

    List<String> reportPaths = Arrays.asList(settings.getStringArray(reportPathPropertyKey));
    if (!reportPaths.isEmpty()) {
      List<String> includes = new ArrayList<>();
      for (String reportPath : reportPaths) {
        // Normalization can return null if path is null, is invalid, or is a path with back-ticks outside known directory structure
        String normalizedPath = FilenameUtils.normalize(reportPath);
        if (normalizedPath != null && new File(normalizedPath).isAbsolute()) {
          includes.add(normalizedPath);
          continue;
        }

        // Prefix with absolute module base dir, attempt normalization again -- can still get null here
        normalizedPath = FilenameUtils.normalize(moduleBaseDir.getAbsolutePath() + File.separator + reportPath);
        if (normalizedPath != null) {
          includes.add(normalizedPath);
          continue;
        }

        CxxUtils.LOG.debug("Not a valid report path '{}'", reportPath);
      }

      CxxUtils.LOG.debug("Normalized report includes to '{}'", includes);

      // Includes array cannot contain null elements
      DirectoryScanner directoryScanner = new DirectoryScanner();
      directoryScanner.setIncludes(includes.toArray(new String[includes.size()]));
      directoryScanner.scan();

      String [] includeFiles = directoryScanner.getIncludedFiles();
      CxxUtils.LOG.info("Scanner found '{}' report files", includeFiles.length);
      for (String found : includeFiles) {        
        reports.add(new File(found));
      }

      if (reports.isEmpty()) {
        CxxUtils.LOG.warn("Cannot find a report for '{}'", reportPathPropertyKey);
      } else {
        CxxUtils.LOG.info("Parser will parse '{}' report files", reports.size());
      }
    } else {
      CxxUtils.LOG.error("Undefined report path value for key '{}'", reportPathPropertyKey);
    }

    return reports;
  }

  /**
   * Saves code violation only if unique. Compares file, line, ruleId and msg.
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
    if ((filename != null) && (!filename.isEmpty())) {
      String root = fs.baseDir().getAbsolutePath();
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
      .message(msg); //@todo deprecated message
    if (lineNr > 0) {
      issueBuilder = issueBuilder.line(lineNr); //@todo deprecated line
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
    throws Exception {
  }

  protected String reportPathKey() {
    return "";
  }
}
