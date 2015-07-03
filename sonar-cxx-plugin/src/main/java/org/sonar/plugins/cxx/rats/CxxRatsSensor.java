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
package org.sonar.plugins.cxx.rats;

import java.io.File;
import java.util.List;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.api.batch.bootstrap.ProjectReactor;

/**
 * {@inheritDoc}
 */
public final class CxxRatsSensor extends CxxReportSensor {
  private static final String MISSING_RATS_TYPE = "fixed size global buffer";
  public static final String REPORT_PATH_KEY = "sonar.cxx.rats.reportPath";
  private RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxRatsSensor(ResourcePerspectives perspectives, Settings conf, FileSystem fs, RulesProfile profile, ProjectReactor reactor) {
    super(perspectives, conf, fs, reactor, CxxMetrics.RATS);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxRatsRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws org.jdom.JDOMException, java.io.IOException
  {
    CxxUtils.LOG.info("Parsing 'RATS' format");
    
    try
    {
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
            saveUniqueViolation(project, context, CxxRatsRuleRepository.KEY,
                fileName, line, type, message);
          }
        }
      }
    } catch (org.jdom.input.JDOMParseException e) {
      // when RATS fails the XML file might be incomplete
      CxxUtils.LOG.error("Ignore incomplete XML output from RATS '{}'", e.toString());
    }
  }

  private String getVulnerabilityType(Element child) {
    if (child != null) {
      return child.getTextTrim();
    }
    return MISSING_RATS_TYPE;
  }
}
