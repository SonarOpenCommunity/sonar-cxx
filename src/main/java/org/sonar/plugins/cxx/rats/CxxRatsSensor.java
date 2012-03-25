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
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
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
    try {
      File[] reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                  REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
      for (File report : reports) {
        parseReport(project, context, report);
      }
    } catch (Exception e) {
      String msg = new StringBuilder()
        .append("Cannot feed the rats data into sonar, details: '")
        .append(e)
        .append("'")
        .toString();
      throw new SonarException(msg, e);
    }
  }


  void parseReport(Project project, SensorContext context, File report)
    throws org.jdom.JDOMException, java.io.IOException
  {
    logger.info("parsing rats report '{}'", report);

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
          processError(project, context, fileName, line, type, message);
        }
      }
    }
  }

  void processError(Project project, SensorContext context,
                    String file, int line, String ruleId, String msg) {
    RuleQuery ruleQuery = RuleQuery.create()
      .withRepositoryKey(CxxRatsRuleRepository.KEY)
      .withKey(ruleId);
    Rule rule = ruleFinder.find(ruleQuery);
    if (rule != null) {
      org.sonar.api.resources.File resource =
        org.sonar.api.resources.File.fromIOFile(new File(file), project);
      Violation violation = Violation.create(rule, resource).setLineId(line).setMessage(msg);
      context.saveViolation(violation);
    }
    else{
      logger.warn("Cannot find the rule {}-{}, skipping violation", CxxRatsRuleRepository.KEY, ruleId);
    }
  }


  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
