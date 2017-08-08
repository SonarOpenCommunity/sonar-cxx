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
package org.sonar.cxx.sensors.coverage;

import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.coverage.CxxCoverageCache;
import static org.fest.assertions.Assertions.assertThat;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxBullseyeCoverageSensorTest {
  private static final Logger LOG = Loggers.get(CxxBullseyeCoverageSensorTest.class);
  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private CxxLanguage language;
  
  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
//    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY))
//    .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATHS_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.IT_REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.OVERALL_REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY);

    //    when(language.hasKey(CxxCoverageSensor.REPORT_PATHS_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.IT_REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY)).thenReturn(true);
  }

//  @Test
  public void shouldReportCorrectCoverage() {
    String coverageReport = "coverage-reports/bullseye/coverage-result-bullseye.xml";
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    Settings settings = new Settings();
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), coverageReport);
//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY), coverageReport);
//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY), coverageReport);
//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), coverageReport);

    context.setSettings(settings);
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "source_1.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "src/testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "src/testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);

    assertThat(context.lineHits("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 7)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:main.cpp", CoverageType.UNIT, 7)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:testclass.cpp", CoverageType.UNIT, 7)).isEqualTo(1);
  }

//  @Test
  public void shouldParseTopLevelFiles() {
    String coverageReport;
    if (TestUtils.isWindows()) {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-win.xml";
    } else {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-linux.xml";
    }
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    Settings settings = new Settings();
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), coverageReport);
//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), coverageReport);
    context.setSettings(settings);


    if (TestUtils.isWindows()) {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/randomfoldernamethatihopeknowmachinehas/test/test.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/randomfoldernamethatihopeknowmachinehas/test2/test2.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/randomfoldernamethatihopeknowmachinehas/main.c")).setLanguage("cpp"));
    } else {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/anotherincludeattop.h").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/test/test.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/test2/test2.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/main.c").setLanguage("cpp"));
    }

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);

  for (int i =1 ; i< 10; i++) {
  LOG.debug("lineHit for line '{} is '{}'", i , context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h", CoverageType.UNIT, i));
}
    
    if (TestUtils.isWindows()) {
      assertThat(context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h", CoverageType.UNIT, 4)).isEqualTo(1);
    } else {
      assertThat(context.lineHits("ProjectKey:/c/test/anotherincludeattop.h", CoverageType.UNIT, 4)).isEqualTo(1);      
    }

//    verify(context, times(28)).newCoverage();
  }

//  @Test
  public void shouldCorrectlyHandleDriveLettersWithoutSlash() {
    String coverageReport;
    if (TestUtils.isWindows()) {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash-win.xml";
    } else {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash-linux.xml";
    }
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    Settings settings = new Settings();
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), coverageReport);
//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), coverageReport);
    context.setSettings(settings);

    if (TestUtils.isWindows()) {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/main.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/randomfoldernamethatihopeknowmachinehas/test.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/randomfoldernamethatihopeknowmachinehas2/test2.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("c:/anotherincludeattop.h")).setLanguage("cpp"));
    } else {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/main.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/test.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test2/test2.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/anotherincludeattop.h").setLanguage("cpp"));
    }

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);

    for (int i =1 ; i< 10; i++) {
      LOG.debug("lineHit for line '{} is '{}'", i , context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/test.c", CoverageType.UNIT, i));
    }

    if (TestUtils.isWindows()) {
      assertThat(context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/test.c", CoverageType.UNIT, 4)).isEqualTo(1);
    } else {
      assertThat(context.lineHits("ProjectKey:/c/test/test.c", CoverageType.UNIT, 4)).isEqualTo(1);
    }    
//    verify(context, times(28)).newCoverage();
  }
}

