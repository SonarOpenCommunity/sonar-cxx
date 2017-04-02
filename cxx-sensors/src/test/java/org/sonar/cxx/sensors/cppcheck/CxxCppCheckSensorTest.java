/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.cxx.sensors.cppcheck;

import org.sonar.cxx.sensors.cppcheck.CxxCppCheckSensor;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.pclint.CxxPCLintSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCppCheckSensorTest {

  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject"));
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxCppCheckSensor.REPORT_PATH_KEY))
            .thenReturn(new String [] { fs.baseDir().getAbsolutePath() + "/cppcheck-reports/cppcheck-result-*.xml" });    
    when(language.IsRecoveryEnabled()).thenReturn(true);    
        
    CxxCppCheckSensor sensor = new CxxCppCheckSensor(language);
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(9);
  }

  @Test
  public void shouldReportProjectLevelViolationsV1() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxCppCheckSensor.REPORT_PATH_KEY)).thenReturn(new String [] { "cppcheck-reports/cppcheck-result-projectlevelviolation-V1.xml" });    
    when(language.IsRecoveryEnabled()).thenReturn(true);
    
    CxxCppCheckSensor sensor = new CxxCppCheckSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(3);
  }

  @Test
  public void shouldReportProjectLevelViolationsV2() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxCppCheckSensor.REPORT_PATH_KEY)).thenReturn(new String [] { "cppcheck-reports/cppcheck-result-projectlevelviolation-V2.xml" });    
    when(language.IsRecoveryEnabled()).thenReturn(true);
    
    CxxCppCheckSensor sensor = new CxxCppCheckSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(3);
  }

  @Test
  public void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV1() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxCppCheckSensor.REPORT_PATH_KEY)).thenReturn(new String [] { "cppcheck-reports/cppcheck-result-SAMPLE-V1.xml" });    
    when(language.IsRecoveryEnabled()).thenReturn(true);
    
    CxxCppCheckSensor sensor = new CxxCppCheckSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV2() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxCppCheckSensor.REPORT_PATH_KEY)).thenReturn(new String [] { "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml" });    
    when(language.IsRecoveryEnabled()).thenReturn(true);
    
    CxxCppCheckSensor sensor = new CxxCppCheckSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }
  
  @Test(expected=IllegalStateException.class)
  public void shouldThrowExceptionWhenRecoveryIsDisabled() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxCppCheckSensor.REPORT_PATH_KEY)).thenReturn(new String [] { "cppcheck-reports/cppcheck-result-empty.xml" });    
    when(language.IsRecoveryEnabled()).thenReturn(false);
    
    CxxCppCheckSensor sensor = new CxxCppCheckSensor(language);
    sensor.execute(context);
  }  
}
