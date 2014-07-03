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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CxxExternalRulesSensorTest {

  private CxxExternalRulesSensor sensor;
  private SensorContext context;
  private Project project;
  private RuleFinder ruleFinder;
  private RulesProfile profile;
  private Settings settings;
  private ModuleFileSystem fs;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    ruleFinder = TestUtils.mockRuleFinder();
    fs = TestUtils.mockFileSystem();
    profile = mock(RulesProfile.class);
    context = mock(SensorContext.class);
    settings = new Settings();
    File resourceMock = mock(File.class);
    when(context.getResource((File) anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-ok.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
    verify(context, times(2)).saveViolation(any(Violation.class));
  }

  @Test
  public void shouldReportFileLevelViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-filelevelviolation.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
    verify(context, times(1)).saveViolation(any(Violation.class));
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-projectlevelviolation.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
    verify(context, times(1)).saveViolation(any(Violation.class));
  }

  @Test(expected = SonarException.class)
  public void shouldThrowExceptionWhenReportEmpty() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-empty.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test
  public void shouldReportNoViolationsIfNoReportFound() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/noreport.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test(expected = SonarException.class)
  public void shouldThrowInCaseOfATrashyReport() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-invalid.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
  }

  @Test
  public void shouldReportOnlyOneViolationAndRemoveDuplicates() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-with-duplicates.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, fs, profile);
    sensor.analyse(project, context);
    verify(context, times(1)).saveViolation(any(Violation.class));
  }
}
