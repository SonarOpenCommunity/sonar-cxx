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
package org.sonar.plugins.cxx.veraxx;

import java.io.File;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.cxx.utils.EmptyReportException;

/**
 * {@inheritDoc}
 */
public class CxxVeraxxSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.vera.reportPath";
  private RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxVeraxxSensor(ResourcePerspectives perspectives, Settings conf, FileSystem fs, RulesProfile profile, ProjectReactor reactor) {
    super(perspectives, conf, fs, reactor, CxxMetrics.VERAXX);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxVeraxxRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    CxxUtils.LOG.info("Parsing 'Vera++' format");
    
    try {
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
        /**
         * {@inheritDoc}
         */
        public void stream(SMHierarchicCursor rootCursor) throws javax.xml.stream.XMLStreamException {
          try{
            rootCursor.advance();
          } catch(com.ctc.wstx.exc.WstxEOFException eofExc){
            throw new EmptyReportException();
          }

          SMInputCursor fileCursor = rootCursor.childElementCursor("file");
          while (fileCursor.getNext() != null) {
            String name = fileCursor.getAttrValue("name");

            CxxUtils.LOG.info("Vera++ processes file = " + name);
            SMInputCursor errorCursor = fileCursor.childElementCursor("error");
            while (errorCursor.getNext() != null) {
              if (!"error".equals(name)) {
                String line = errorCursor.getAttrValue("line");
                String message = errorCursor.getAttrValue("message");
                String source = errorCursor.getAttrValue("source");

                saveUniqueViolation(project, context, CxxVeraxxRuleRepository.KEY,
                                    name, line, source, message);
              } else {
                CxxUtils.LOG.debug("Error in file '{}', with message '{}'",
                    name + "(" + errorCursor.getAttrValue("line") + ")",
                    errorCursor.getAttrValue("message"));
              }
            }
          }
        }
      });

      parser.parse(report);
    } catch (com.ctc.wstx.exc.WstxUnexpectedCharException e) {
      CxxUtils.LOG.error("Ignore XML error from Veraxx '{}'", e.toString());
    }
  }
}
