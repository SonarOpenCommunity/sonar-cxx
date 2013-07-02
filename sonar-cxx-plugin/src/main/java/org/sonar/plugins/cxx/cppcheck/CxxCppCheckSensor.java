/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.cppcheck;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.cxx.utils.EmptyReportException;

import javax.xml.stream.XMLStreamException;

import java.io.File;

/**
 * Sensor for cppcheck (static code analyzer).
 *
 * @author fbonin
 * @author vhardion
 */
public class CxxCppCheckSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPath";
  private static final String DEFAULT_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  private RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxCppCheckSensor(RuleFinder ruleFinder, Settings conf,
      RulesProfile profile) {
    super(ruleFinder, conf);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxCppCheckRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        try{
          rootCursor.advance(); // results
        }
        catch(com.ctc.wstx.exc.WstxEOFException eofExc){
          throw new EmptyReportException();
        }

        SMInputCursor errorCursor = rootCursor.childElementCursor("error"); // error
        while (errorCursor.getNext() != null) {
          String file = errorCursor.getAttrValue("file");
          String line = errorCursor.getAttrValue("line");
          String id = errorCursor.getAttrValue("id");
          String msg = errorCursor.getAttrValue("msg");

          if (isInputValid(file, line, id, msg)) {
            saveViolation(project, context, CxxCppCheckRuleRepository.KEY,
                file, Integer.parseInt(line), id, msg);
          } else {
            CxxUtils.LOG.warn("CppCheck warning: {}", msg);
          }
        }
      }

      private boolean isInputValid(String file, String line, String id, String msg) {
        return !StringUtils.isEmpty(file) && !StringUtils.isEmpty(line)
          && !StringUtils.isEmpty(id) && !StringUtils.isEmpty(msg);
      }
    });

    parser.parse(report);
  }
}
