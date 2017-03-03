/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.plugins.cxx.rats;

import java.io.File;
import java.util.List;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public final class CxxRatsSensor extends CxxReportSensor {
  public static final Logger LOG = Loggers.get(CxxRatsSensor.class);
  private static final String MISSING_RATS_TYPE = "fixed size global buffer";
  public static final String REPORT_PATH_KEY = "sonar.cxx.rats.reportPath";
  
  /**
   * {@inheritDoc}
   */
  public CxxRatsSensor(Settings settings) {
    super(settings, CxxMetrics.RATS);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(CxxLanguage.KEY).name("CxxRatsSensor");
  }
  
  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws org.jdom.JDOMException, java.io.IOException {
    LOG.debug("Parsing 'RATS' format");
    
    try {
      SAXBuilder builder = new SAXBuilder(false);
      Element root = builder.build(report).getRootElement();
      @SuppressWarnings("unchecked")
      List<Element> vulnerabilities = root.getChildren("vulnerability");
      for (Element vulnerability : vulnerabilities) {
        String type = getVulnerabilityType(vulnerability.getChild("type"));
        String message = vulnerability.getChild("message").getTextTrim();

        @SuppressWarnings("unchecked")
        List<Element> files = vulnerability.getChildren("file");

        for (Element file : files) {
          String fileName = file.getChild("name").getTextTrim();

          @SuppressWarnings("unchecked")
          List<Element> lines = file.getChildren("line");
          for (Element lineElem : lines) {
            String line = lineElem.getTextTrim();
            saveUniqueViolation(context, CxxRatsRuleRepository.KEY,
              fileName, line, type, message);
          }
        }
      }
    } catch (org.jdom.input.JDOMParseException e) {
      // when RATS fails the XML file might be incomplete
      LOG.error("Ignore incomplete XML output from RATS '{}'", CxxUtils.getStackTrace(e));
    }
  }

  private String getVulnerabilityType(Element child) {
    if (child != null) {
      return child.getTextTrim();
    }
    return MISSING_RATS_TYPE;
  }
}
