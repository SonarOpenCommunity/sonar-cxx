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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.batch.bootstrap.ProjectReactor;

import org.junit.Before;
import org.junit.Test;

public class CxxCppCheckSensorTest {

  private CxxCppCheckSensor sensor;
  private SensorContext context;
  private Project project;
  private RulesProfile profile;
  private Settings settings;
  private FileSystem fs;
  private ProjectReactor reactor;
  private Issuable issuable;
  private ResourcePerspectives perspectives;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    fs = TestUtils.mockFileSystem();
    issuable = TestUtils.mockIssuable();
    reactor = TestUtils.mockReactor();
    perspectives = TestUtils.mockPerspectives(issuable);
    profile = mock(RulesProfile.class);
    settings = new Settings();
    sensor = new CxxCppCheckSensor(perspectives, settings, fs, profile, reactor);
    context = mock(SensorContext.class);
    File resourceMock = mock(File.class);
    when(context.getResource((File) anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectViolations() {
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY,
      "cppcheck-reports/cppcheck-result-*.xml");    
    sensor.analyse(project, context);
    verify(issuable, times(9)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportProjectLevelViolationsV1() {
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY,
      "cppcheck-reports/cppcheck-result-projectlevelviolation-V1.xml");
    sensor = new CxxCppCheckSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(3)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportProjectLevelViolationsV2() {
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY,
      "cppcheck-reports/cppcheck-result-projectlevelviolation-V2.xml");
    sensor = new CxxCppCheckSensor(perspectives, settings, fs, profile, reactor);
    sensor.analyse(project, context);
    verify(issuable, times(3)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV1() {
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY,
      "cppcheck-reports/cppcheck-result-SAMPLE-V1.xml");
    sensor = new CxxCppCheckSensor(perspectives, settings, fs, profile, reactor);
    when(context.getResource((File) anyObject())).thenReturn(null);
    sensor.analyse(project, context);
    verify(issuable, times(0)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV2() {
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY,
      "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");
    sensor = new CxxCppCheckSensor(perspectives, settings, fs, profile, reactor);
    when(context.getResource((File) anyObject())).thenReturn(null);
    sensor.analyse(project, context);
    verify(issuable, times(0)).addIssue(any(Issue.class));
  }
}
