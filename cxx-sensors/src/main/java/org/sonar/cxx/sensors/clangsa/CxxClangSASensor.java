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
package org.sonar.cxx.sensors.clangsa;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import java.io.File;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * Sensor for Clang Static Analyzer.
 *
 */
public class CxxClangSASensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxClangSASensor.class);
  public static final String REPORT_PATH_KEY = "clangsa.reportPath";
  public static final String KEY = "ClangSA";

  /**
   * CxxClangSASensor for Clang Static Analyzer Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxClangSASensor(CxxLanguage language) {
    super(language);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " ClangSASensor")
      .onlyOnLanguage(this.language.getKey())
      .createIssuesForRuleRepository(CxxClangSARuleRepository.KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {

    LOG.debug("Processing clangsa report '{}''", report.getName());

    try {
      File f = new File(report.getPath());

      NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(f);

      // Array of file paths where an issue was detected.
      NSObject[] sourceFiles = ((NSArray) rootDict.objectForKey("files")).getArray();

      NSObject[] diagnostics = ((NSArray) rootDict.objectForKey("diagnostics")).getArray();

      for (NSObject diagnostic : diagnostics) {
        NSDictionary diag = (NSDictionary) diagnostic;

        NSString desc = (NSString) diag.get("description");
        String description = desc.getContent();

        String checkerName = ((NSString) diag.get("check_name")).getContent();

        NSDictionary location = (NSDictionary) diag.get("location");

        Integer line = ((NSNumber) location.get("line")).intValue();

        NSNumber fileIndex = (NSNumber) location.get("file");

        NSObject filePath = sourceFiles[fileIndex.intValue()];

        saveUniqueViolation(context,
          CxxClangSARuleRepository.KEY,
          ((NSString) filePath).getContent(),
          line.toString(),
          checkerName,
          description);

      }
    } catch (final java.io.IOException
      | java.text.ParseException
      | javax.xml.parsers.ParserConfigurationException
      | org.xml.sax.SAXException
      | com.dd.plist.PropertyListFormatException e) {

      LOG.error("Failed to parse clangsa report: {}", e.getMessage());

    }
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }
}
