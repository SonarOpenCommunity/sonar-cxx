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
package org.sonar.plugins.cxx.externalrules;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import javax.xml.stream.XMLStreamException;

import java.io.File;

/**
 * Custom Rule Import, all static analysis are supported.
 * 
 * @author jorge costa
 */
public class CxxExternalRulesSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.other.reportPath";
  private static final String DEFAULT_REPORT_PATH = "externalrules-reports/externalrules-result-*.xml";
  private final RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxExternalRulesSensor(RuleFinder ruleFinder, Settings conf, ModuleFileSystem fs, RulesProfile profile) {
    super(ruleFinder, conf, fs, CxxMetrics.EXTERNAL);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxExternalRuleRepository.KEY).isEmpty();
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
  protected void processReport(final Project project, final SensorContext context, File report) throws javax.xml.stream.XMLStreamException {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

      /**
       * {@inheritDoc}
       */
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();

        SMInputCursor errorCursor = rootCursor.childElementCursor("error");
        while (errorCursor.getNext() != null) {
          String file = errorCursor.getAttrValue("file");
          String line = errorCursor.getAttrValue("line");
          String id = errorCursor.getAttrValue("id");
          String msg = errorCursor.getAttrValue("msg");

          saveUniqueViolation(project, context, CxxExternalRuleRepository.KEY, file, line, id, msg);
        }
      }
    });

    parser.parse(report);
  }
}
