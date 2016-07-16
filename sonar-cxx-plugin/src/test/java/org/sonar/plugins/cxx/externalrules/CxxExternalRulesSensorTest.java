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
package org.sonar.plugins.cxx.externalrules;

import java.io.File;
import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxExternalRulesSensorTest {

  private CxxExternalRulesSensor sensor;
  private Settings settings;
  private DefaultFileSystem fs;

  @Before
  public void setUp() {    
    fs = TestUtils.mockFileSystem();
    settings = new Settings();
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-ok.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(2);
  }

  @Test
  public void shouldReportFileLevelViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-filelevelviolation.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY,
                         "externalrules-reports/externalrules-result-projectlevelviolation.xml");
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
        assertThat(context.allIssues()).hasSize(1);
  }

  @Test(expected = IllegalStateException.class)  
  public void shouldThrowExceptionWhenReportEmpty() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-empty.xml");
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldReportNoViolationsIfNoReportFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/noreport.xml");
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowInCaseOfATrashyReport() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-invalid.xml");
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
  }

  @Test
  public void shouldReportOnlyOneViolationAndRemoveDuplicates() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-with-duplicates.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    sensor = new CxxExternalRulesSensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }
}
