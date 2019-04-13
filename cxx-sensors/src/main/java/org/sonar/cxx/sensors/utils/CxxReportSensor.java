/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;

/**
 * This class is used as base for all sensors which import reports. It hosts common logic such as finding the reports.
 */
public abstract class CxxReportSensor implements Sensor {

  private static final Logger LOG = Loggers.get(CxxReportSensor.class);

  private final CxxLanguage language;
  private final String propertiesKeyPathToReports;
  private final Set<String> notFoundFiles = new HashSet<>();

  /**
   * {@inheritDoc}
   */
  protected CxxReportSensor(CxxLanguage language, String propertiesKeyPathToReports) {
    this.language = language;
    this.propertiesKeyPathToReports = language.getPluginProperty(propertiesKeyPathToReports);
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
   * Use the given {@link Configuration} object in order to get a list of Ant patterns referenced by key
   * <code>reportPathKey</code>. Apply <code>moduleBaseDir</code> in order to make relative Ant patterns to absolute
   * ones. Resolve Ant patterns and returns the list of existing files.
   *
   * @param settings project (module) configuration
   * @param moduleBaseDir project (module) base directory
   * @param reportPathKey configuration key for the external reports (CSV list of Ant patterns)
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
   * @param property String with comma separated items
   * @return
   */
  public static String[] splitProperty(String property) {
    return property.split(",");
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

  private InputFile getInputFileTryRealPath(SensorContext sensorContext, String path) {
    final Path absolutePath = sensorContext.fileSystem().baseDir().toPath().resolve(path);
    Path realPath;
    try {
      realPath = absolutePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
    } catch (IOException | RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unable to get the real path: module '{}', baseDir '{}', path '{}', exception '{}'",
                sensorContext.module().key(), sensorContext.fileSystem().baseDir(), path, e.getMessage());
      }
      return null;
    }

    // if the real path is equal to the given one - skip search; we already
    // tried such path
    //
    // IMPORTANT: don't use Path::equals(), since it's dependent on a file-system.
    // SonarQube plugin API works with string paths, so the equality of strings
    // is important
    final String realPathString = realPath.toString();
    if (absolutePath.toString().equals(realPathString)) {
      return null;
    }

    return sensorContext.fileSystem()
            .inputFile(sensorContext.fileSystem().predicates().hasAbsolutePath(realPathString));
  }

  public InputFile getInputFileIfInProject(SensorContext sensorContext, String path) {
    if (notFoundFiles.contains(path)) {
      return null;
    }

    // 1. try the most generic search predicate first; usually it's the right
    // one
    InputFile inputFile = sensorContext.fileSystem()
            .inputFile(sensorContext.fileSystem().predicates().hasPath(path));

    // 2. if there was nothing found, try to normalize the path by means of
    // Path::toRealPath(). This helps if some 3rd party tools obfuscate the
    // paths. E.g. the MS VC compiler tends to transform file paths to the lower
    // case in its logs.
    //
    // IMPORTANT: SQ plugin API allows creation of NewIssue only on locations,
    // which belong to the module. This internal check is performed by means
    // of comparison of the paths. The paths which are managed by the framework
    // (the reference paths) are NOT stored in the canonical form.
    // E.g. the plugin API neither resolves symbolic links nor performs
    // case-insensitive path normalization (could be relevant on Windows)
    //
    // Normalization by means of File::getCanonicalFile() or Path::toRealPath()
    // can produce paths, which don't pass the mentioned check. E.g. resolution
    // of symbolic links or letter case transformation
    // might lead to the paths, which don't belong to the module's base
    // directory (at least not in terms of parent-child semantic). This is the
    // reason why we should avoid the resolution of symbolic links and not use
    // the Path::toRealPath() as the only search predicate.
    if (inputFile == null) {
      inputFile = getInputFileTryRealPath(sensorContext, path);
    }

    if (inputFile == null) {
      LOG.warn("Cannot find the file '{}' in module '{}' base dir '{}', skipping violations.",
              path, sensorContext.module().key(), sensorContext.fileSystem().baseDir());
      notFoundFiles.add(path);
    }
    return inputFile;
  }

  /**
   * override always executeImpl instead of execute
   */
  protected abstract void executeImpl(SensorContext context);

  @Override
  public void execute(SensorContext context) {
    notFoundFiles.clear();
    executeImpl(context);
  }

  public CxxLanguage getLanguage() {
    return language;
  }

  public String getReportPathKey() {
    return propertiesKeyPathToReports;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
