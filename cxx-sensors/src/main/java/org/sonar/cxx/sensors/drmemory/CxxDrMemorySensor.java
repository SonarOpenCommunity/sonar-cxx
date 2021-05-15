/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.sensors.drmemory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.drmemory.DrMemoryParser.DrMemoryError;
import org.sonar.cxx.sensors.drmemory.DrMemoryParser.DrMemoryError.Location;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Dr. Memory is a memory monitoring tool capable of identifying memory-related programming errors such as accesses of
 * uninitialized memory, accesses to not addressable memory (including outside of allocated heap units and heap
 * underflow and overflow), accesses to freed memory, double frees, memory leaks, and (on Windows) handle leaks, GDI API
 * usage errors, and accesses to unreserved thread local storage slots. See also: http://drmemory.org
 *
 * @author asylvestre
 */
public class CxxDrMemorySensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.drmemory.reportPaths";
  public static final String REPORT_ENCODING_DEF = "sonar.cxx.drmemory.encoding";
  private static final String DEFAULT_ENCODING_DEF = StandardCharsets.UTF_8.name();
  private static final Logger LOG = Loggers.get(CxxDrMemorySensor.class);

  public static List<PropertyDefinition> properties() {
    var category = "CXX External Analyzers";
    var subcategory = "Dr. Memory";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Dr. Memory Report(s)")
        .description(
          "Comma-separated paths (absolute or relative to the project base directory) to `*.txt` files with"
            + " `Dr. Memory` issues. Ant patterns are accepted for relative paths."
        )
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(REPORT_ENCODING_DEF)
        .defaultValue(DEFAULT_ENCODING_DEF)
        .name("Dr. Memory Report Encoding")
        .description(
          "Defines the encoding to be used to read the files from `sonar.cxx.drmemory.reportPaths` (default is `UTF-8`)."
        )
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  private static String getFrameText(Location frame, int frameNr) {
    var sb = new StringBuilder(512);
    sb.append("#").append(frameNr).append(" ").append(frame.getFile()).append(":").append(frame.getLine());
    return sb.toString();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Dr. Memory report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  private boolean frameIsInProject(Location frame) {
    return getInputFileIfInProject(frame.getFile()) != null;
  }

  @CheckForNull
  private Location getLastOwnFrame(DrMemoryError error) {
    for (var frame : error.getStackTrace()) {
      if (frameIsInProject(frame)) {
        return frame;
      }
    }
    return null;
  }

  @Override
  protected void processReport(File report) {
    String reportEncoding = context.config().get(REPORT_ENCODING_DEF).orElse(DEFAULT_ENCODING_DEF);
    LOG.debug("Encoding='{}'", reportEncoding);

    for (var error : DrMemoryParser.parse(report, reportEncoding)) {
      if (error.getStackTrace().isEmpty()) {
        var moduleIssue = new CxxReportIssue(error.getType().getId(), null, null, null, error.getMessage());
        saveUniqueViolation(moduleIssue);
      } else {
        Location lastOwnFrame = getLastOwnFrame(error);
        if (lastOwnFrame == null) {
          LOG.warn("Cannot find a file to assign the DrMemory error '{}' to", error);
          continue;
        }
        var fileIssue = new CxxReportIssue(error.getType().getId(),
                                       lastOwnFrame.getFile(), lastOwnFrame.getLine().toString(), null,
                                       error.getMessage());

        // add all frames as secondary locations
        var frameNr = 0;
        for (var frame : error.getStackTrace()) {
          boolean frameIsInProject = frameIsInProject(frame);
          String mappedPath = (frameIsInProject) ? frame.getFile() : lastOwnFrame.getFile();
          Integer mappedLine = (frameIsInProject) ? frame.getLine() : lastOwnFrame.getLine();
          fileIssue.addLocation(mappedPath, mappedLine.toString(), null, getFrameText(frame, frameNr));
          ++frameNr;
        }
        saveUniqueViolation(fileIssue);
      }
    }
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxDrMemoryRuleRepository.KEY;
  }

}
