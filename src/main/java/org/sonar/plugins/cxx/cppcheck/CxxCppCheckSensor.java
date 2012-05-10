
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
package org.sonar.plugins.cxx.cppcheck;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxSensor;

/**
 * Sensor for CppCheck external tool.
 *
 * CppCheck is an equivalent to FindBug but for C++
 *
 * @author fbonin
 * @author vhardion
 * @todo enable include dirs (-I)
 * @todo allow configuration of path to analyze
 */
public class CxxCppCheckSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPath";
  private static final String DEFAULT_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  private RulesProfile profile;
  
  /**
   * {@inheritDoc}
   */
  public CxxCppCheckSensor(RuleFinder ruleFinder, Configuration conf,
                           RulesProfile profile) {
    super(ruleFinder, conf);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxCppCheckRuleRepository.KEY).isEmpty();
  }
  
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }
  
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }
  
  protected void processReport(final Project project, final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException
  {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance(); //results

        SMInputCursor errorCursor = rootCursor.childElementCursor("error"); //error
        while (errorCursor.getNext() != null) {
          String file = errorCursor.getAttrValue("file");
          String line = errorCursor.getAttrValue("line");
          String id = errorCursor.getAttrValue("id");
          String msg = errorCursor.getAttrValue("msg");
          
          saveViolation(project, context, CxxCppCheckRuleRepository.KEY,
                        file, Integer.parseInt(line), id, msg);
        }
      }
    });

    parser.parse(report);
  }
}
