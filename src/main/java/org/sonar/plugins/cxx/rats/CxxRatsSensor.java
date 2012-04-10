/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.rats;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.cxx.utils.CxxSensor;

/**
 * {@inheritDoc}
 */
public final class CxxRatsSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.rats.reportPath";
  private static final String DEFAULT_REPORT_PATH = "rats-reports/rats-result-*.xml";

  /**
   * {@inheritDoc}
   */
  public CxxRatsSensor(RuleFinder ruleFinder, Configuration conf) {
    super(ruleFinder, conf);
  }

  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }
  
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }
  
  protected void parseReport(Project project, SensorContext context, File report)
    throws org.jdom.JDOMException, java.io.IOException
  {
    SAXBuilder builder = new SAXBuilder(false);
    Element root = builder.build(report).getRootElement();

    List<Element> vulnerabilities = root.getChildren("vulnerability");
    for (Element vulnerability : vulnerabilities) {
      String type = vulnerability.getChild("type").getTextTrim();
      String message = vulnerability.getChild("message").getTextTrim();

      List<Element> files = vulnerability.getChildren("file");

      for (Element file : files) {
        String fileName = file.getChild("name").getTextTrim();

        List<Element> lines = file.getChildren("line");
        for (Element lineElem : lines) {
          int line = Integer.parseInt(lineElem.getTextTrim());
          saveViolation(project, context, CxxRatsRuleRepository.KEY,
                        fileName, line, type, message);
        }
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
