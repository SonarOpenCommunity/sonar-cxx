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
import com.google.common.collect.Iterables;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;

/**
 * This class is used as base for all sensors which import reports. It hosts
 * common logic such as finding the reports.
 */
public abstract class CxxReportSensor implements Sensor {

  private static final Logger LOG = Loggers.get(CxxReportSensor.class);
  private final CxxLanguage language;
  private final String propertiesKeyPathToReports;

  /**
   * {@inheritDoc}
   */
  protected CxxReportSensor(CxxLanguage language, String propertiesKeyPathToReports) {
    this.language = language;
    this.propertiesKeyPathToReports = language.getPluginProperty(propertiesKeyPathToReports);
  }

  public CxxLanguage getLanguage() {
    return language;
  }

  public String getReportPathKey()
  {
    return propertiesKeyPathToReports;
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
   * Use the given {@link Configuration} object in order to get a list of Ant
   * patterns referenced by key <code>reportPathKey</code>. Apply
   * <code>moduleBaseDir</code> in order to make relative Ant patterns to
   * absolute ones. Resolve Ant patterns and returns the list of existing files.
   *
   * @param settings
   *          project (module) configuration
   * @param moduleBaseDir
   *          project (module) base directory
   * @param reportPathKey
   *          configuration key for the external reports (CSV list of Ant
   *          patterns)
   * @return List<File> list of report paths
   */
  public static List<File> getReports(Configuration settings, final File moduleBaseDir, String reportPathKey) {
    String[] reportPaths = settings.getStringArray(reportPathKey);
    if (reportPaths == null || reportPaths.length == 0) {
      LOG.info("Undefined report path value for key '{}'", reportPathKey);
      return Collections.emptyList();
    }

    List<String> normalizedReportPaths = normalizeReportPaths(moduleBaseDir, reportPaths);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Scanner uses normalized report path(s): '{}'", String.join(", ", normalizedReportPaths));
    }
    // Includes array cannot contain null elements
    DirectoryScanner directoryScanner = new DirectoryScanner();
    directoryScanner.setIncludes(normalizedReportPaths.toArray(new String[normalizedReportPaths.size()]));
    directoryScanner.scan();
    String[] existingReportPaths = directoryScanner.getIncludedFiles();

    if (existingReportPaths.length == 0) {
      LOG.warn("Property '{}': cannot find any files matching the Ant pattern(s) '{}'", reportPathKey,
          String.join(", ", normalizedReportPaths));
      return Collections.emptyList();
    }

    LOG.info("Parser will parse '{}' report file(s)", existingReportPaths.length);
    return Arrays.stream(existingReportPaths).map(File::new).collect(Collectors.toList());
  }

  /**
   * @param moduleBaseDir
   * @param reportPaths
   * @return
   */
  private static List<String> normalizeReportPaths(final File moduleBaseDir, String[] reportPaths) {
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
   * @param property String with comma separated items
   * @return
   */
  public static String[] splitProperty(String property) {
    return Iterables.toArray(Splitter.on(',').split(property), String.class);
  }

}
