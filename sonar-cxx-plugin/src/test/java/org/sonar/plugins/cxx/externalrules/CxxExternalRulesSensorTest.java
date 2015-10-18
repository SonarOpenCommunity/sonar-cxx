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

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

public class CxxExternalRulesSensorTest {

  private CxxExternalRulesSensor sensor;
  private SensorContext context;
  private Project project;
  private RulesProfile profile;
  private Settings settings;
  private DefaultFileSystem fs;
  private Issuable issuable;
  private ProjectReactor reactor;
  private ResourcePerspectives perspectives;

  @Before
  public void setUp() {    
    project = TestUtils.mockProject();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    fs = TestUtils.mockFileSystem();
    reactor = TestUtils.mockReactor();
    profile = mock(RulesProfile.class);
    context = mock(SensorContext.class);
    settings = new Settings();
  }

  @Test
  public void shouldReportCorrectViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-ok.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/code_chunks.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/utils.cpp");  
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(2)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportFileLevelViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-filelevelviolation.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/code_chunks.cpp");
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-projectlevelviolation.xml");
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionWhenReportEmpty() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-empty.xml");
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportNoViolationsIfNoReportFound() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/noreport.xml");
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowInCaseOfATrashyReport() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-invalid.xml");
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
  }

  @Test
  public void shouldReportOnlyOneViolationAndRemoveDuplicates() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-with-duplicates.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/code_chunks.cpp");
    sensor = new CxxExternalRulesSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }
}
