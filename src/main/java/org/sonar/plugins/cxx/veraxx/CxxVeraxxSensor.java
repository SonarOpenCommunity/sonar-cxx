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
package org.sonar.plugins.cxx.veraxx;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.CxxSensor;


public class CxxVeraxxSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.vera.reportPath";
  private static final String DEFAULT_REPORT_PATH = "vera++-reports/vera++-result-*.xml";
  private static Logger logger = LoggerFactory.getLogger(CxxVeraxxSensor.class);
  
  private RuleFinder ruleFinder = null;
  private Configuration conf = null;

  
  public CxxVeraxxSensor(RuleFinder ruleFinder, Configuration conf) {
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
        .append("Cannot feed the vera++-data into sonar, details: '")
        .append(e)
        .append("'")
        .toString();
      throw new SonarException(msg, e);
    }
  }

  
  private void parseReport(final Project project, final SensorContext context, File report)
    throws XMLStreamException 
  {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();
        
        SMInputCursor fileCursor = rootCursor.childElementCursor("file");
        while (fileCursor.getNext() != null) {
          String name = fileCursor.getAttrValue("name");
          
          SMInputCursor errorCursor = fileCursor.childElementCursor("error");
          while (errorCursor.getNext() != null) {
            int line = Integer.parseInt(errorCursor.getAttrValue("line"));
            String message = errorCursor.getAttrValue("message");
            String source = errorCursor.getAttrValue("source");
            
            processError(project, context, name, line, source, message);
          }
        }
      }
    });
    
    parser.parse(report);
  }

  
  void processError(Project project, SensorContext context,
                    String file, int line, String ruleId, String msg) {
    RuleQuery ruleQuery = RuleQuery.create()
      .withRepositoryKey(CxxVeraxxRuleRepository.KEY)
      .withConfigKey(ruleId);
    Rule rule = ruleFinder.find(ruleQuery);
    if (rule != null) {
      org.sonar.api.resources.File resource =
        org.sonar.api.resources.File.fromIOFile(new File(file), project);
      Violation violation = Violation.create(rule, resource).setLineId(line).setMessage(msg);
      context.saveViolation(violation);
    }
    else{
      logger.warn("Cannot find the rule {}-{}, skipping violation", CxxVeraxxRuleRepository.KEY, ruleId);
    }
  }
}
