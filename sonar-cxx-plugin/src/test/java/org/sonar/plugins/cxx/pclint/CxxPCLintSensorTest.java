/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.cxx.pclint;

import org.sonar.api.batch.SensorContext; //@todo deprecated
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.component.ResourcePerspectives; //@todo deprecated
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project; //@todo deprecated
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CxxPCLintSensorTest {

  private SensorContext context; //@todo deprecated
  private Project project; //@todo deprecated
  private RulesProfile profile;
  private ResourcePerspectives perspectives; //@todo deprecated
  private Issuable issuable;
  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    fs = TestUtils.mockFileSystem();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    profile = mock(RulesProfile.class);
    context = mock(SensorContext.class);
  }

  @Test
  public void shouldReportCorrectViolations() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-SAMPLE.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "FileZip.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "FileZip.h");
    TestUtils.addInputFile(fs, perspectives, issuable, "ZipManager.cpp");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(16)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportCorrectMisra2004Violations() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA2004-SAMPLE1.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "test.c");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(29)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportCorrectMisra2004PcLint9Violations() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA2004-SAMPLE2.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "test.c");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionWhenMisra2004DescIsWrong() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/incorrect-pclint-MISRA2004-desc.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "test.c");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionWhenMisra2004RuleDoNotExist() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/incorrect-pclint-MISRA2004-rule-do-not-exist.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "test.c");
    CxxPCLintSensor sensor = new CxxPCLintSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
  }

  @Test
  public void shouldNotRemapMisra1998Rules() {
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA1998-SAMPLE.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "test.c");
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
