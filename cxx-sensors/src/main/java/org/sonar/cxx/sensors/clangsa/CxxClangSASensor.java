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
package org.sonar.cxx.sensors.clangsa;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import java.io.File;
import javax.annotation.Nullable;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Sensor for Clang Static Analyzer.
 *
 */
public class CxxClangSASensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "clangsa.reportPath";

  private static final Logger LOG = Loggers.get(CxxClangSASensor.class);

  /**
   * CxxClangSASensor for Clang Static Analyzer Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxClangSASensor(CxxLanguage language) {
    super(language, REPORT_PATH_KEY, CxxClangSARuleRepository.getRepositoryKey(language));
  }

  private static NSObject require(@Nullable NSObject object, String errorMsg) {
    if (object == null) {
      throw new IllegalArgumentException(errorMsg);
    }
    return object;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " ClangSASensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {

    LOG.debug("Processing clangsa report '{}''", report.getName());

    try {
      File f = new File(report.getPath());

      NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(f);

      NSObject[] diagnostics = ((NSArray) require(rootDict.objectForKey("diagnostics"),
        "Missing mandatory entry 'diagnostics'")).getArray();
      NSObject[] sourceFiles = ((NSArray) require(rootDict.objectForKey("files"),
        "Missing mandatory entry 'files'")).getArray();

      for (NSObject diagnostic : diagnostics) {
        NSDictionary diag = (NSDictionary) diagnostic;

        String description = ((NSString) require(diag.get("description"),
          "Missing mandatory entry 'diagnostics/description'")).getContent();
        String checkerName = ((NSString) require(diag.get("check_name"),
          "Missing mandatory entry 'diagnostics/check_name'")).getContent();
        NSDictionary location = (NSDictionary) require(diag.get("location"),
          "Missing mandatory entry 'diagnostics/location'");
        int line = ((NSNumber) require(location.get("line"),
          "Missing mandatory entry 'diagnostics/location/line'")).intValue();
        int fileIndex = ((NSNumber) require(location.get("file"),
          "Missing mandatory entry 'diagnostics/location/file'")).intValue();

        if (fileIndex < 0 || fileIndex >= sourceFiles.length) {
          throw new IllegalArgumentException("Invalid file index");
        }
        String filePath = ((NSString) sourceFiles[fileIndex]).getContent();

        CxxReportIssue issue = new CxxReportIssue(checkerName, filePath, Integer.toString(line), description);

        addFlowToIssue(diag, sourceFiles, issue);

        saveUniqueViolation(context, issue);
      }
    } catch (Exception e) {
      LOG.error("Failed to parse clangsa report: {}", e.getMessage());
    }
  }

  @Override
  protected CxxMetricsFactory.Key getMetricKey() {
    return CxxMetricsFactory.Key.CLANG_SA_SENSOR_ISSUES_KEY;
  }

  private enum PathElementKind {
    CONTROL, EVENT, UNKNOWN
  }

  private class PathElement {
    private final NSDictionary pathDict;

    public PathElement(NSObject pathObject) {
      pathDict = (NSDictionary) pathObject;
    }

    public PathElementKind getKind() {
      String kind = ((NSString) require(pathDict.get("kind"), "Missing mandatory entry 'kind'")).getContent();
      if ("event".equals(kind)) {
        return PathElementKind.EVENT;
      } else if ("control".equals(kind)) {
        return PathElementKind.CONTROL;
      } else {
        return PathElementKind.UNKNOWN;
      }
    }
  }

  private class PathEvent {
    private final NSDictionary eventDict;
    private final NSObject[] sourceFiles;

    public PathEvent(final NSObject eventDict, final NSObject[] sourceFiles) {
      this.eventDict = (NSDictionary) eventDict;
      this.sourceFiles = sourceFiles;
    }

    public String getExtendedMessage() {
      return ((NSString) require(eventDict.get("extended_message"), "Missing mandatory entry 'extended_message'"))
          .getContent();
    }

    public String getLineNumber() {
      int lineNumber = ((NSNumber) require(getLocation().get("line"), "Missing mandatory entry 'line'")).intValue();
      return Integer.toString(lineNumber);
    }

    public String getFilePath() {
      int fileIndex = ((NSNumber) require(getLocation().get("file"), "Missing mandatory entry 'file'")).intValue();
      if (fileIndex < 0 || fileIndex >= sourceFiles.length) {
        throw new IllegalArgumentException("Invalid file index");
      }
      return ((NSString) sourceFiles[fileIndex]).getContent();
    }

    private NSDictionary getLocation() {
      return (NSDictionary) require(eventDict.get("location"), "Missing mandatory entry 'location'");
    }
  }

  private void addFlowToIssue(final NSDictionary diagnostic, final NSObject[] sourceFiles, final CxxReportIssue issue) {
    NSObject[] path = ((NSArray) require(diagnostic.objectForKey("path"), "Missing mandatory entry 'path'")).getArray();
    for (NSObject pathObject : path) {
      PathElement pathElement = new PathElement(pathObject);
      if (pathElement.getKind() != PathElementKind.EVENT) {
        continue;
      }

      PathEvent event = new PathEvent(pathObject, sourceFiles);
      issue.addFlowElement(event.getFilePath(), event.getLineNumber(), event.getExtendedMessage());
    }
  }
}
