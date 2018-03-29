/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxReportLocation;

/**
 * This class is used as base for all sensors which import reports. It hosts common logic such as finding the reports
 * and saving issues in SonarQube
 */
public abstract class CxxReportSensor implements Sensor {

  private static final Logger LOG = Loggers.get(CxxReportSensor.class);
  private final Set<String> notFoundFiles = new HashSet<>();
  private final Set<CxxReportIssue> uniqueIssues = new HashSet<>();
  private final Map<InputFile, Integer> violationsPerFileCount = new HashMap<>();
  private int violationsPerModuleCount;
  protected final CxxLanguage language;

  /**
   * {@inheritDoc}
   */
  protected CxxReportSensor(CxxLanguage language) {
    this.language = language;
  }

  /**
   * Get string property from configuration. If the string is not set or empty, return the default value.
   *
   * @param context sensor context
   * @param name Name of the property
   * @param def Default value
   * @return Value of the property if set and not empty, else default value.
   */
  public static String getContextStringProperty(SensorContext context, String name, String def) {
    String s = context.config().get(name).orElse(null);
    if (s == null || s.isEmpty()) {
      return def;
    }
    return s;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {
    try {
      LOG.info("Searching reports by relative path with basedir '{}' and search prop '{}'",
        context.fileSystem().baseDir(), getReportPathKey());
      List<File> reports = getReports(context.config(), context.fileSystem().baseDir(), getReportPathKey());
      violationsPerFileCount.clear();
      violationsPerModuleCount = 0;

      for (File report : reports) {
        int prevViolationsCount = violationsPerModuleCount;
        LOG.info("Processing report '{}'", report);
        executeReport(context, report, prevViolationsCount);
      }

      LOG.info("{} processed = {}", CxxMetrics.getKey(this.getSensorKey(), language), violationsPerModuleCount);

      String metricKey = CxxMetrics.getKey(this.getSensorKey(), language);
      Metric<Integer> metric = this.language.getMetric(metricKey);

      if (metric != null) {
        for (Map.Entry<InputFile, Integer> entry : violationsPerFileCount.entrySet()) {
          context.<Integer>newMeasure()
            .forMetric(metric)
            .on(entry.getKey())
            .withValue(entry.getValue())
            .save();
        }
        context.<Integer>newMeasure()
          .forMetric(metric)
          .on(context.module())
          .withValue(violationsPerModuleCount)
          .save();
      }
    } catch (Exception e) {
      String msg = new StringBuilder()
        .append("Cannot feed the data into sonar, details: '")
        .append(e)
        .append("'")
        .toString();
      LOG.error(msg);
      CxxUtils.validateRecovery(e, this.language);
    }
  }

  /**
   * @param context
   * @param report
   * @param prevViolationsCount
   * @throws Exception
   */
  private void executeReport(SensorContext context, File report, int prevViolationsCount) throws Exception {
    try {
      processReport(context, report);
      if (LOG.isDebugEnabled()) {
        LOG.debug("{} processed = {}", CxxMetrics.getKey(this.getSensorKey(), language),
          violationsPerModuleCount - prevViolationsCount);
      }
    } catch (EmptyReportException e) {
      LOG.warn("The report '{}' seems to be empty, ignoring.", report);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot read report", e);
      }
      CxxUtils.validateRecovery(e, language);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /**
   * resolveFilename normalizes the report full path
   *
   * @param baseDir of the project
   * @param filename of the report
   * @return String
   */
  @Nullable
  public static String resolveFilename(final String baseDir, @Nullable final String filename) {

    if (filename != null) {
      // Normalization can return null if path is null, is invalid, 
      // or is a path with back-ticks outside known directory structure
      String normalizedPath = FilenameUtils.normalize(filename);
      if ((normalizedPath != null) && (new File(normalizedPath).isAbsolute())) {
        return normalizedPath;
      }

      // Prefix with absolute module base directory, attempt normalization again -- can still get null here
      normalizedPath = FilenameUtils.normalize(baseDir + File.separator + filename);
      if (normalizedPath != null) {
        return normalizedPath;
      }
    }
    return null;
  }

  /**
   * getReports
   *
   * @param settings of the C++ project
   * @param moduleBaseDir location of sonar properties file
   * @param genericReportKeyData full path of XML report
   * @return File
   */
  public static List<File> getReports(Configuration settings,
    final File moduleBaseDir,
    @Nullable String genericReportKeyData) {

    List<File> reports = new ArrayList<>();

    if (Strings.isNullOrEmpty(genericReportKeyData)) {
      return reports;
    }

    String reportPathString = settings.get(genericReportKeyData).orElse("");
    if (reportPathString.isEmpty()) {
      LOG.info("Undefined report path value for key '{}'", genericReportKeyData);
    } else {
      List<String> reportPaths = Arrays.asList(splitProperty(reportPathString));

      List<String> includes = normalizeReportPaths(moduleBaseDir, reportPaths);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scanner uses report paths: '{}'", includes);
      }
      // Includes array cannot contain null elements
      DirectoryScanner directoryScanner = new DirectoryScanner();
      directoryScanner.setIncludes(includes.toArray(new String[includes.size()]));
      directoryScanner.scan();

      String[] includeFiles = directoryScanner.getIncludedFiles();
      LOG.info("Scanner found '{}' report files", includeFiles.length);
      for (String found : includeFiles) {
        reports.add(new File(found));
      }

      if (reports.isEmpty() && !includes.isEmpty()) {
        LOG.warn("Cannot find a report for '{}={}'", genericReportKeyData, includes.get(0));
      } else {
        LOG.info("Parser will parse '{}' report files", reports.size());
      }
    }

    return reports;
  }

  /**
   * @param moduleBaseDir
   * @param reportPaths
   * @return
   */
  private static List<String> normalizeReportPaths(final File moduleBaseDir, List<String> reportPaths) {
    List<String> includes = new ArrayList<>();
    for (String reportPath : reportPaths) {

      String normalizedPath = resolveFilename(moduleBaseDir.getAbsolutePath(), reportPath.trim());
      if (normalizedPath != null) {
        includes.add(normalizedPath);
        continue;
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Not a valid report path '{}'", reportPath);
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Normalized report includes to '{}'", includes);
    }
    return includes;
  }

  /**
   * Saves code violation only if unique. Compares file, line, ruleId and msg.
   *
   * @param sensorContext
   * @param ruleRepoKey
   * @param file
   * @param line
   * @param ruleId
   * @param msg
   */
  public void saveUniqueViolation(SensorContext sensorContext, String ruleRepoKey, @Nullable String file,
      @Nullable String line, String ruleId, String msg) {
    CxxReportLocation location = new CxxReportLocation(file, line, msg);
    CxxReportIssue issue = new CxxReportIssue(ruleRepoKey, ruleId, location);
    saveUniqueViolation(sensorContext, issue);
  }

  /**
   * Saves code violation only if unique. Compares file, line, ruleId and msg or the first given location
   *
   * @param sensorContext
   * @param ruleRepoKey
   * @param ruleId
   * @param locations
   */
  public void saveUniqueViolation(SensorContext sensorContext, CxxReportIssue issue) {
    if (uniqueIssues.add(issue)) {
      saveViolation(sensorContext, issue);
    }
  }

  private NewIssueLocation createNewIssueLocationFile(SensorContext sensorContext, NewIssue newIssue,
      CxxReportLocation location, Set<InputFile> affectedFiles) {
    String root = sensorContext.fileSystem().baseDir().getAbsolutePath();
    String normalPath = CxxUtils.normalizePathFull(location.getFile(), root);
    if (normalPath != null && !notFoundFiles.contains(normalPath)) {
      InputFile inputFile = sensorContext.fileSystem()
          .inputFile(sensorContext.fileSystem().predicates().hasAbsolutePath(normalPath));
      if (inputFile != null) {
        int lines = inputFile.lines();
        int lineNr = getLineAsInt(location.getLine(), lines);
        NewIssueLocation newIssueLocation = newIssue.newLocation().on(inputFile)
            .at(inputFile.selectLine(lineNr > 0 ? lineNr : 1)).message(location.getInfo());

        affectedFiles.add(inputFile);

        return newIssueLocation;
      } else {
        LOG.warn("Cannot find the file '{}', skipping violations", normalPath);
        notFoundFiles.add(normalPath);
      }
    }
    return null;
  }

  private NewIssueLocation createNewIssueLocationModule(SensorContext sensorContext, NewIssue newIssue,
      CxxReportLocation location) {
    NewIssueLocation newIssueLocation = newIssue.newLocation().on(sensorContext.module()).message(location.getInfo());
    return newIssueLocation;
  }

  /**
   * Saves a code violation which is detected in the given file/line and has given ruleId and message. Saves it to the
   * given project and context. Project or file-level violations can be saved by passing null for the according
   * parameters ('file' = null for project level, 'line' = null for file-level)
   */
  private void saveViolation(SensorContext sensorContext, CxxReportIssue issue) {
    String repoKey = issue.getRuleRepoKey() + this.language.getRepositorySuffix();
    NewIssue newIssue = sensorContext.newIssue().forRule(RuleKey.of(repoKey, issue.getRuleId()));

    Set<InputFile> affectedFiles = new HashSet<>();
    List<NewIssueLocation> newIssueLocations = new ArrayList<>();

    for (CxxReportLocation location : issue.getLocations()) {
      if (location.getFile() != null && !location.getFile().isEmpty()) {
        NewIssueLocation newIssueLocation = createNewIssueLocationFile(sensorContext, newIssue, location,
            affectedFiles);
        if (newIssueLocation != null) {
          newIssueLocations.add(newIssueLocation);
        }
      } else {
        NewIssueLocation newIssueLocation = createNewIssueLocationModule(sensorContext, newIssue, location);
        newIssueLocations.add(newIssueLocation);
      }
    }

    if (!newIssueLocations.isEmpty()) {
      try {
        newIssue.at(newIssueLocations.get(0));
        for (int i = 1; i < newIssueLocations.size(); i++) {
          newIssue.addLocation(newIssueLocations.get(i));
        }
        newIssue.save();

        for (InputFile affectedFile : affectedFiles) {
          violationsPerFileCount.merge(affectedFile, 1, Integer::sum);
        }
        violationsPerModuleCount++;
      } catch (RuntimeException ex) {
        LOG.error("Could not add the issue '{}':{}', skipping issue", issue.toString(), ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }
    }
  }

  private int getLineAsInt(@Nullable String line, int maxLine) {
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
        LOG.warn("Skipping invalid line number: {}", line);
        CxxUtils.validateRecovery(nfe, this.language);
        lineNr = -1;
      }
    }
    return lineNr;
  }

  /**
   * @param property String with comma separated items
   * @return
   */
  public static String[] splitProperty(String property) {
    return Iterables.toArray(Splitter.on(',').split(property), String.class);
  }

  protected void processReport(final SensorContext context, File report)
    throws Exception {
  }

  public abstract String getReportPathKey();

  protected abstract String getSensorKey();
}
