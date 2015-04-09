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
package org.sonar.plugins.cxx.pclint;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException; //@todo SonarException has been deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import org.sonar.plugins.cxx.TestUtils;

public class CxxPCLintSensorTest {
  private SensorContext context;
  private Project project;
  private RulesProfile profile;
  private ResourcePerspectives perspectives;
  private Issuable issuable;
  private FileSystem fs;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    fs = TestUtils.mockFileSystem();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    profile = mock(RulesProfile.class);
    context = mock(SensorContext.class);
    File resourceMock = mock(File.class);
    when(context.getResource((File) anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectViolations() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-SAMPLE.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(15)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportCorrectMisra2004Violations() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA2004-SAMPLE.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(29)).addIssue(any(Issue.class));
  }

  @Test(expected=SonarException.class) //@todo SonarException has been deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
  public void shouldThrowExceptionWhenMisra2004DescIsWrong() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/incorrect-pclint-MISRA2004-desc.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
  }

  @Test(expected=SonarException.class) //@todo SonarException has been deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
  public void shouldThrowExceptionWhenMisra2004RuleDoNotExist() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/incorrect-pclint-MISRA2004-rule-do-not-exist.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
  }

  @Test
  public void shouldNotRemapMisra1998Rules() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA1998-SAMPLE.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-projectlevelviolation.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldThrowExceptionInvalidChar() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-invalid-char.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
  }
}
