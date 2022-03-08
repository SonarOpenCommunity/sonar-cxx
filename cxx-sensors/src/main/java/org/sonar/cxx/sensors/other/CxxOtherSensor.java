/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.sensors.other;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Custom Rule Import, all static analysis are supported.
 */
public class CxxOtherSensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.other.reportPaths";
  public static final String RULES_KEY = "sonar.cxx.other.rules";

  private static final Logger LOG = Loggers.get(CxxOtherSensor.class);

  public static List<PropertyDefinition> properties() {
    var subcategory = "Other Analyser(s)";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Other Report(s)")
        .description(
          "Comma-separated paths (absolute or relative to the project base directory) to `*.xml` files with"
            + " `other` issues. Ant patterns are accepted for relative paths."
            + " In the SonarQube UI, enter one entry per field."
        )
        .category("CXX External Analyzers")
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(RULES_KEY)
        .name("Other Rule Definitions")
        .description("XML rule definitions for `other` code analyzers. Use a separate field for each tool.")
        .type(PropertyType.TEXT)
        .multiValues(true)
        .category("CXX External Analyzers")
        .subCategory(subcategory)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX other analyser report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  public void processReport(File report) {
    try {
      var parser = new StaxParser((SMHierarchicCursor rootCursor) -> {
        rootCursor.advance();

        SMInputCursor errorCursor = rootCursor.childElementCursor("error");
        while (errorCursor.getNext() != null) {
          String file = errorCursor.getAttrValue("file");
          String line = errorCursor.getAttrValue("line");
          String column = errorCursor.getAttrValue("column");
          String id = errorCursor.getAttrValue("id");
          String msg = errorCursor.getAttrValue("msg");

          var issue = new CxxReportIssue(id, file, line, column, msg);
          saveUniqueViolation(issue);
        }
      });

      parser.parse(report);
    } catch (XMLStreamException e) {
      throw new InvalidReportException("The 'other' report is invalid", e);
    }
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxOtherRepository.KEY;
  }

}
