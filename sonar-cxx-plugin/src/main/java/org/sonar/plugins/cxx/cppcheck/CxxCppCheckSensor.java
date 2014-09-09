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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.cxx.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * Sensor for cppcheck (static code analyzer).
 *
 * @author fbonin
 * @author vhardion
 */
public class CxxCppCheckSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPath";
  private static final String DEFAULT_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  
  private final RulesProfile profile;
  private final List<CppcheckParser> parsers = new LinkedList<CppcheckParser>();
  
  /**
   * {@inheritDoc}
   */
  public CxxCppCheckSensor(RuleFinder ruleFinder, Settings conf, ModuleFileSystem fs,
      RulesProfile profile) {
    super(ruleFinder, conf, fs, CxxMetrics.CPPCHECK);
    this.profile = profile;
    parsers.add(new CppcheckParserV2(this));
    parsers.add(new CppcheckParserV1(this));
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
    throws javax.xml.stream.XMLStreamException {
    boolean parsed = false;
    
    for (CppcheckParser parser : parsers) {
      try {
        parser.processReport(project, context, report);
        CxxUtils.LOG.info("Added report '{}' (parsed by: {})", report, parser);
        parsed = true;
        break;
      } catch (XMLStreamException e) {
        CxxUtils.LOG.trace("Report {} cannot be parsed by {}", report, parser);
      }
    }

    if (!parsed) {
      CxxUtils.LOG.error("Report {} cannot be parsed", report);
    }
  }
}
