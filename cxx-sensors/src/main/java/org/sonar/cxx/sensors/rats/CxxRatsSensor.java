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
package org.sonar.cxx.sensors.rats;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * {@inheritDoc}
 */
public class CxxRatsSensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.rats.reportPath";

  private static final Logger LOG = Loggers.get(CxxRatsSensor.class);
  private static final String MISSING_RATS_TYPE = "fixed size global buffer";

  /**
   * CxxRatsSensor for RATS Sensor
   */
  public CxxRatsSensor() {
  }

  private static String getVulnerabilityType(@Nullable Element child) {
    if (child != null) {
      return child.getTextTrim();
    }
    return MISSING_RATS_TYPE;
  }

  public static List<PropertyDefinition> properties() {
    String subcateg = "RATS";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("RATS report(s)")
        .description("Path to <a href='https://code.google.com/p/rough-auditing-tool-for-security/'>RATS<a/>"
                       + " reports(s), relative to projects root." + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(CxxLanguage.NAME + " RatsSensor")
      .onlyOnLanguage(CxxLanguage.KEY)
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws org.jdom2.JDOMException, java.io.IOException {
    LOG.debug("Parsing 'RATS' format");

    try {
      SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
      builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      Element root = builder.build(report).getRootElement();
      List<Element> vulnerabilities = root.getChildren("vulnerability");
      for (Element vulnerability : vulnerabilities) {
        String type = getVulnerabilityType(vulnerability.getChild("type"));
        String message = vulnerability.getChild("message").getTextTrim();

        List<Element> files = vulnerability.getChildren("file");

        for (Element file : files) {
          String fileName = file.getChild("name").getTextTrim();

          List<Element> lines = file.getChildren("line");
          for (Element lineElem : lines) {
            String line = lineElem.getTextTrim();

            CxxReportIssue issue = new CxxReportIssue(type, fileName, line, message);
            saveUniqueViolation(context, issue);
          }
        }
      }
    } catch (org.jdom2.input.JDOMParseException e) {
      // when RATS fails the XML file might be incomplete
      LOG.error("Ignore incomplete XML output from RATS '{}'", CxxUtils.getStackTrace(e));
    }
  }

  @Override
  protected String getReportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxRatsRuleRepository.KEY;
  }

}
