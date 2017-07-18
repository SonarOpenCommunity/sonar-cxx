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
  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  
  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

//  @Test
  public void shouldReportCorrectCoverage() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    String coverageReport = "coverage-reports/bullseye/coverage-result-bullseye.xml";
    when(language.getStringArrayOption(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(new String [] { coverageReport });
    when(language.getStringArrayOption(CxxCoverageSensor.IT_REPORT_PATH_KEY)).thenReturn(new String [] { coverageReport });
    when(language.getStringArrayOption(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY)).thenReturn(new String [] { coverageReport });
    when(language.getStringArrayOption(CxxCoverageSensor.REPORT_PATHS_KEY)).thenReturn(new String [] { coverageReport });

    when(language.getStringOption(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(coverageReport);
    when(language.getStringOption(CxxCoverageSensor.IT_REPORT_PATH_KEY)).thenReturn(coverageReport);
    when(language.getStringOption(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY)).thenReturn(coverageReport);
    when(language.getStringOption(CxxCoverageSensor.REPORT_PATHS_KEY)).thenReturn(coverageReport);

    when(language.hasKey(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.IT_REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.REPORT_PATHS_KEY)).thenReturn(true);

    SensorContextTester context = SensorContextTester.create(fs.baseDir());
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
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.hasKey(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(true);
    String coverageReport;
    if (TestUtils.isWindows()) {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-win.xml";
    } else {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-linux.xml";
    }
    when(language.getStringArrayOption(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(new String [] { coverageReport });
    when(language.getStringOption(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(coverageReport);

    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    if (TestUtils.isWindows()) {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/test/test.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/test2/test2.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/main.c")).setLanguage("cpp"));
    } else {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/anotherincludeattop.h").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/test/test.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/test2/test2.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/main.c").setLanguage("cpp"));
    }

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);
    if (TestUtils.isWindows()) {
    assertThat(context.lineHits("ProjectKey:C:/randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h", CoverageType.UNIT, 4)).isEqualTo(1);
    } else {
      assertThat(context.lineHits("ProjectKey:/c/test/anotherincludeattop.h", CoverageType.UNIT, 4)).isEqualTo(1);      
    }

//    verify(context, times(28)).newCoverage();
  }

//  @Test
  public void shouldCorrectlyHandleDriveLettersWithoutSlash() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.hasKey(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(true);
    String coverageReport;
    if (TestUtils.isWindows()) {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash-win.xml";
    } else {
      coverageReport = "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash-linux.xml";
    }
    when(language.getStringArrayOption(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(new String [] { coverageReport });
    when(language.getStringOption(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(coverageReport);

    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    if (TestUtils.isWindows()) {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/main.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/test.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas2/test2.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", CxxUtils.normalizePath("C:/anotherincludeattop.h")).setLanguage("cpp"));
    } else {
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/main.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test/test.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/test2/test2.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "/c/anotherincludeattop.h").setLanguage("cpp"));
    }

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);

    if (TestUtils.isWindows()) {
      assertThat(context.lineHits("C:/randomfoldernamethatihopeknowmachinehas/test.c", CoverageType.UNIT, 4)).isEqualTo(1);
    } else {
      assertThat(context.lineHits("/c/test/test.c", CoverageType.UNIT, 4)).isEqualTo(1);
    }    
//    verify(context, times(28)).newCoverage();
  }
}
