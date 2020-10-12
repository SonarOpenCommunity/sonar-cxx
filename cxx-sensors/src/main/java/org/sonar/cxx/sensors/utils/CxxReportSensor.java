/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * This class is used as base for all sensors which import reports. It hosts common logic such as finding the reports.
 */
public abstract class CxxReportSensor implements ProjectSensor {

  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  private static final Logger LOG = Loggers.get(CxxReportSensor.class);

  protected static final String USE_ANT_STYLE_WILDCARDS
                                  = " Use <a href='"
                                      + "https://ant.apache.org/manual/dirtasks.html"
                                      + "'>Ant-style wildcards</a> if neccessary.";

  private final Set<String> notFoundFiles = new HashSet<>();

  protected SensorContext context;

  /**
   * {@inheritDoc}
   */
  protected CxxReportSensor() {
  }

  public List<File> getReports(String reportPathsKey) {
    return CxxUtils.getFiles(context, reportPathsKey);
  }

  /**
   * Get InputFile for path.
   *
   * IMPORTANT: SQ allows creation of NewIssue only if InputFile exists. This internal check is performed by a simple
   * string comparison of the absolute path (relative paths are made absolute to baseDir first and forward/back slashes
   * are also normalized. The resolution of symbolic links and case-sensitive paths is not supported by SQ. In the case
   * of reports that contain case-insensitive paths (e.g. Visual Studio warnings are always lowercase), the function
   * must normalize them.
   *
   * @param path relative or absolute path
   * @return InputFile if path is part of project, otherwise none
   */
  @CheckForNull
  public InputFile getInputFileIfInProject(String path) {
    InputFile inputFile = null;

    // in case previous search failed don't search again
    if (!notFoundFiles.contains(path)) {

      // try the most generic search predicate first; usually it's the right one
      inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(path));

      // if there was nothing found, try to normalize the path: resolve symbolic links, make path case-sensitive
      if (inputFile == null) {
        inputFile = getInputFileTryRealPath(path);

        if (inputFile == null) {
          LOG.warn("Cannot find the file '{}' in project '{}' with baseDir '{}', skipping.",
                   path, context.project().key(), context.fileSystem().baseDir());
          notFoundFiles.add(path);
        }
      }
    }

    return inputFile;
  }

  @Override
  public void execute(SensorContext context) {
    this.context = context;
    notFoundFiles.clear();
    executeImpl();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /**
   * Get InputFile for path.
   *
   * Resolution of symbolic links and case-sensitive paths.
   *
   * @param path relative or absolute path
   * @return InputFile if path is part of project, otherwise none
   */
  @CheckForNull
  private InputFile getInputFileTryRealPath(String path) {

    // create absolute path (relative to baseDir)
    Path absPath = context.fileSystem().baseDir().toPath().resolve(path);
    try {
      // resolve symbolic links
      Path realPath = absPath.toRealPath(LinkOption.NOFOLLOW_LINKS);

      // if the real path is equal to the given one - skip search: we already tried such path
      // IMPORTANT: SQ works with string paths, so the equality of strings is important
      if (!absPath.toString().equals(realPath.toString())) {
        return context.fileSystem().inputFile(
          context.fileSystem().predicates().hasAbsolutePath(realPath.toString()));
      }
    } catch (IOException | RuntimeException e) {
      LOG.debug("Unable to get the real path: project='{}' baseDir='{}' path='{}' absPath='{}' exception='{}'",
                context.project().key(), context.fileSystem().baseDir(), path, absPath, e.toString());
    }
    return null;
  }

  /**
   * override always executeImpl instead of execute
   */
  protected abstract void executeImpl();

}
