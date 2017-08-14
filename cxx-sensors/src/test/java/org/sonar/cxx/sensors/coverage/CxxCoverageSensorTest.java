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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.Version;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCoverageSensorTest {

  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private Settings settings;
  private static final Version SQ_6_2 = Version.create(6, 2);
  private CxxLanguage language;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings = new Settings();
    language = TestUtils.mockCxxLanguage();
//  when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY))
//  .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATHS_KEY);
  when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY))
  .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
  when(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY))
  .thenReturn("sonar.cxx." + CxxCoverageSensor.IT_REPORT_PATH_KEY);
  when(language.getPluginProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY))
  .thenReturn("sonar.cxx." + CxxCoverageSensor.OVERALL_REPORT_PATH_KEY);
  when(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY))
  .thenReturn("sonar.cxx." + CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY);

 }

  @Test
  public void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(sensor.getITReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(sensor.getOverallReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");

    context.setSettings(settings);
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));

    sensor.execute(context, linesOfCodeByFile);

    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", CoverageType.UNIT, 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.IT, 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.IT, 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", CoverageType.IT, 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", CoverageType.IT, 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.OVERALL, 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.OVERALL, 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", CoverageType.OVERALL, 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", CoverageType.OVERALL, 8)).isEqualTo(8);
  }

  @Test
  public void shouldReportCorrectCoverageSQ62() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    context.setSonarQubeVersion(SQ_6_2);
    
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(sensor.getITReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(sensor.getOverallReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");

    context.setSettings(settings);
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));

    sensor.execute(context, linesOfCodeByFile);

    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", CoverageType.UNIT, 8)).isEqualTo(8);
  }

  // @Test @todo
  public void shouldReportNoCoverageSaved() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    CxxLanguage language = TestUtils.mockCxxLanguage();    
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);
    verify(context, times(0)).newCoverage();
  }

  @Test
  public void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    CxxLanguage language = TestUtils.mockCxxLanguage();
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");
    context.setSettings(settings);
    sensor.execute(context, linesOfCodeByFile);
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesEmpty() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    CxxLanguage language = TestUtils.mockCxxLanguage();
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/specific-cases/coverage-result-cobertura-empty.xml");

    context.setSettings(settings);
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));    

    sensor.execute(context, linesOfCodeByFile);

    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isNull();
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesInvalid() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/cobertura/specific-cases/coverage-result-invalid.xml");
    context.setSettings(settings);
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);

    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isNull();
  }

  @Test
  public void shouldReportCoverageWhenVisualStudioCase() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio.xml");
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio.xml");
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY), "true");
    context.setSettings(settings);

    context.fileSystem().add(new DefaultInputFile("ProjectKey", "project2/source1.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "project2/source2.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);

    
    int[] oneHitlinesA = new int[] {4, 5, 6, 8, 13, 15, 16, 25};
    int[] zeroHitlinesA = new int[] {9, 10, 22,23};
    for (int zeroHitline : zeroHitlinesA) {
      assertThat(context.lineHits("ProjectKey:project2/source1.cpp", CoverageType.UNIT, zeroHitline)).isEqualTo(0);
    }
    for (int oneHitline : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:project2/source1.cpp", CoverageType.UNIT, oneHitline)).isEqualTo(1);
    }

    int[] oneHitlinesB = new int[] {4, 5, 6, 8, 9, 10, 13, 21, 25};
    int[] zeroHitlinesB = new int[] {15, 16, 22, 23};
    for (int zeroHitline : zeroHitlinesB) {
      assertThat(context.lineHits("ProjectKey:project2/source2.cpp", CoverageType.UNIT, zeroHitline)).isEqualTo(0);
    }
    for (int oneHitline : oneHitlinesB) {
      assertThat(context.lineHits("ProjectKey:project2/source2.cpp", CoverageType.UNIT, oneHitline)).isEqualTo(1);
    }

  }
}

