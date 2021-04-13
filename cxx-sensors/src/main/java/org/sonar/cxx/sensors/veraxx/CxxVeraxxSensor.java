/*
 * Sonar C++ Plugin (Community)
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
package org.sonar.cxx.sensors.veraxx;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * {@inheritDoc}
 */
public class CxxVeraxxSensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.vera.reportPaths";
  private static final Logger LOG = Loggers.get(CxxVeraxxSensor.class);

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Vera++ Report(s)")
        .description(
          "Comma-separated paths (absolute or relative to the project base directory) to `*.xml` files with"
            + " `Vera++` issues. Ant patterns are accepted for relative paths."
        )
        .category("CXX External Analyzers")
        .subCategory("Vera++")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Vera++ report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  protected void processReport(File report) {
    try {
      var parser = new StaxParser((SMHierarchicCursor rootCursor) -> {
        try {
          rootCursor.advance();
        } catch (com.ctc.wstx.exc.WstxEOFException e) {
          throw new EmptyReportException("The 'Vera++' report is empty", e);
        }

        SMInputCursor fileCursor = rootCursor.childElementCursor("file");
        while (fileCursor.getNext() != null) {
          String name = fileCursor.getAttrValue("name");

          SMInputCursor errorCursor = fileCursor.childElementCursor("error");
          while (errorCursor.getNext() != null) {
            if (!"error".equals(name)) {
              String line = errorCursor.getAttrValue("line");
              String message = errorCursor.getAttrValue("message");
              String source = errorCursor.getAttrValue("source");

              var issue = new CxxReportIssue(source, name, line, null, message);
              saveUniqueViolation(issue);
            } else {
              LOG.debug("Error in file '{}', with message '{}'",
                        name + "(" + errorCursor.getAttrValue("line") + ")",
                        errorCursor.getAttrValue("message"));
            }
          }
        }
      });

      parser.parse(report);
    } catch (XMLStreamException e) {
      throw new InvalidReportException("The 'Vera++' report is invalid", e);
    }
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxVeraxxRuleRepository.KEY;
  }

}
