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
import java.text.ParseException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
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
    File[] reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
    for (File report : reports) {
      parseReport(project, report, context);
    }
  }

  private void parseReport(final Project project, File xmlFile, final SensorContext context) {
    try {
      logger.info("parsing vera++ report '{}'", xmlFile);
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

        public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
          try {
            rootCursor.advance();
            collectFile(project, rootCursor.childElementCursor("file"), context);
          } catch (ParseException e) {
            throw new XMLStreamException(e);
          }
        }
      });
      parser.parse(xmlFile);
    } catch (XMLStreamException e) {
      throw new XmlParserException(e);
    }
  }

  private void collectFile(Project project, SMInputCursor file, SensorContext context) throws ParseException, XMLStreamException {
    while (file.getNext() != null) {
      // logger.info("collectError nodename = {} {}", error.getPrefixedName(), error.getAttrCount());
      String fileName = file.getAttrValue("name");
      if ( !StringUtils.isEmpty(fileName)) {
        SMInputCursor error = file.childElementCursor("error");
        while (error.getNext() != null) {

          String line = error.getAttrValue("line");
          String severity = error.getAttrValue("severity");
          String message = error.getAttrValue("message");
          String source = error.getAttrValue("source");

          if ( !StringUtils.isEmpty(source)) {

            if (StringUtils.isEmpty(line)) {
              line = "0";
            }

            org.sonar.api.resources.File ressource =
              org.sonar.api.resources.File.fromIOFile(new File(fileName), project);
            if (ressource != null && fileExist(context, ressource)) {
              Rule rule = ruleFinder.findByKey(CxxVeraxxRuleRepository.REPOSITORY_KEY, source);
              if (rule != null) {
                Object t[] = { source, message, line, ressource.getKey() };
                logger.debug("error source={} message={} found at line {} from ressource {}", t);

                Violation violation = Violation.create(rule, ressource);
                violation.setMessage(message);
                violation.setLineId(Integer.parseInt(line));
                context.saveViolation(violation);
              } else {
                Object t[] = { source, message, line, fileName };
                logger.warn("No rule for error source={} message={} found at line {} from file {}", t);
              }
            } else {
              Object t[] = { source, message, line, fileName };
              logger.warn("error id={} msg={} found at line {} from file {} has no ressource associated", t);
            }
          } else {
            logger.warn("error no source for error");
          }
        }
      } else {
        logger.warn("error no name in file node");
      }
    }
  }

  private boolean fileExist(SensorContext context, org.sonar.api.resources.File file) {
    return context.getResource(file) != null;
  }
}
