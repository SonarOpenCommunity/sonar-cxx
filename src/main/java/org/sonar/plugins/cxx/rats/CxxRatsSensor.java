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
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.Configuration;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.cxx.CxxSensor;


public final class CxxRatsSensor extends CxxSensor {
  private static Logger logger = LoggerFactory.getLogger(CxxRatsSensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.rats.reportPath";
  private static final String DEFAULT_REPORT_PATH = "rats-reports/rats-result-*.xml";
  
  private RuleFinder ruleFinder;
  private Configuration conf;
  
  public CxxRatsSensor(RuleFinder ruleFinder, Configuration conf) {
    this.ruleFinder = ruleFinder;
    this.conf = conf;
  }
  
  public void analyse(Project project, SensorContext context) {
    File[] reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
    for (File report : reports) {
      analyseXmlReport(report, project, context);
    }
  }
  

  void analyseXmlReport(File xmlReport, Project project, SensorContext context) {
    logger.info("parsing rats report '{}'", xmlReport);
    
    final SAXBuilder builder = new SAXBuilder(false);
    try {
      final Document doc = builder.build(xmlReport);
      final Element root = doc.getRootElement();

      @SuppressWarnings("unchecked")
      final List<Element> vulnerabilities = root.getChildren("vulnerability");
      
      for (Element element : vulnerabilities) {
        analyseVulnerabilities(element, project, context);
      }
    } catch (JDOMException ex) {
      logger.warn("Cannot analyse " + xmlReport.getPath());
    } catch (IOException ex) {
      logger.warn("Cannot analyse " + xmlReport.getPath());
    }
  }

  /*
   * <vulnerability> <severity>High</severity> <type>fixed size global buffer</type> <message> Extra care should be taken to ensure that
   * character arrays that are allocated on the stack are used safely. They are prime targets for buffer overflow attacks. </message> <file>
   * <name>ModuloCalculo/src/CompactaAReal.cpp</name> <line>40</line> <line>58</line> </file> </vulnerability>
   */
  void analyseVulnerabilities(Element vulnerability, Project project, SensorContext context) {
    final String type = vulnerability.getChild("type").getTextTrim();
    final String message = vulnerability.getChild("message").getTextTrim();
    
    @SuppressWarnings("unchecked")
    final List<Element> files = vulnerability.getChildren("file");

    for (Element file : files) {
      final String filename = file.getChild("name").getTextTrim();
      
      @SuppressWarnings("unchecked")
      final List<Element> lines = file.getChildren("line");

      for (Element line : lines) {
        final int lineNumber = Integer.parseInt(line.getTextTrim());
        final org.sonar.api.resources.File ressource =
          org.sonar.api.resources.File.fromIOFile(new File(filename), project);
        final Rule rule = ruleFinder.findByKey(CxxRatsRuleRepository.REPOSITORY_KEY, type);
        final Violation violation = Violation.create(rule, ressource);

        violation.setMessage(message);
        violation.setLineId(lineNumber);
        context.saveViolation(violation);
      }
    }
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
