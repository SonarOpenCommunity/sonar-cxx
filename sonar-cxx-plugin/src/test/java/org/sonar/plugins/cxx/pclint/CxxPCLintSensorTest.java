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

import java.util.ArrayList;

import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;

public class CxxPCLintSensorTest {
  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-SAMPLE.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "FileZip.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "FileZip.h").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "ZipManager.cpp").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(16);
  }

  @Test
  public void shouldReportCorrectMisra2004Violations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA2004-SAMPLE1.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "test.c").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(29);
  }

  @Test
  public void shouldReportCorrectMisra2004PcLint9Violations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA2004-SAMPLE2.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "test.c").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportCorrectMisraCppViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRACPP.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "test.c").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(2);
    ArrayList<Issue> issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("M5-0-19");
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("M18-4-1");
  }

  @Test
  public void shouldNotSaveIssuesWhenMisra2004DescIsWrong() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/incorrect-pclint-MISRA2004-desc.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "test.c").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldNotSaveAnythingWhenMisra2004RuleDoNotExist() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/incorrect-pclint-MISRA2004-rule-do-not-exist.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "test.c").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldNotRemapMisra1998Rules() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-MISRA1998-SAMPLE.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "test.c").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-projectlevelviolation.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldThrowExceptionInvalidChar() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxPCLintSensor.REPORT_PATH_KEY, "pclint-reports/pclint-result-invalid-char.xml");
    CxxPCLintSensor sensor = new CxxPCLintSensor(settings);
    sensor.execute(context);
  }
}
