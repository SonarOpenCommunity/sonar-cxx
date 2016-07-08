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
package org.sonar.plugins.cxx.coverage;

import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.cxx.CxxPlugin.CxxCoverageAggregator;

public class CxxCoverageSensorTest {

  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    Settings settings = new Settings();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    sensor = new CxxCoverageSensor(settings, new CxxCoverageAggregator());
    sensor.execute(context);
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isEqualTo(1);
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 3)).isEqualTo(4);
    assertThat(context.lineHits("myProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 2)).isEqualTo(0);
    assertThat(context.lineHits("myProjectKey:sources/application/main.cpp", CoverageType.UNIT, 8)).isEqualTo(8);
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.IT, 1)).isEqualTo(1);
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.IT, 3)).isEqualTo(4);
    assertThat(context.lineHits("myProjectKey:sources/utils/utils.cpp", CoverageType.IT, 2)).isEqualTo(0);
    assertThat(context.lineHits("myProjectKey:sources/application/main.cpp", CoverageType.IT, 8)).isEqualTo(8);
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.OVERALL, 1)).isEqualTo(1);
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.OVERALL, 3)).isEqualTo(4);
    assertThat(context.lineHits("myProjectKey:sources/utils/utils.cpp", CoverageType.OVERALL, 2)).isEqualTo(0);
    assertThat(context.lineHits("myProjectKey:sources/application/main.cpp", CoverageType.OVERALL, 8)).isEqualTo(8);    
  }

  // @Test @todo
  public void shouldReportNoCoverageSaved() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    sensor = new CxxCoverageSensor(new Settings(), new CxxCoverageAggregator());
    //when(context.getResource((InputFile) anyObject())).thenReturn(null);
    sensor.execute(context);
    verify(context, times(0)).newCoverage();
  }

  @Test
  public void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");
    sensor = new CxxCoverageSensor(settings, new CxxCoverageAggregator());
    sensor.execute(context);
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesEmpty() {
    Settings settings = new Settings();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));    
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-cobertura-empty.xml");
    sensor = new CxxCoverageSensor(settings, new CxxCoverageAggregator());
    sensor.execute(context);
    assertThat(context.lineHits("myProjectKey:sources/application/main.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("myProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isNull();
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesInvalid() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/application/main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/utils.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-invalid.xml");
    sensor = new CxxCoverageSensor(settings, new CxxCoverageAggregator());
    sensor.execute(context);
    assertThat(context.lineHits("myProjectKey:sources/application/main.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("myProjectKey:sources/utils/utils.cpp", CoverageType.UNIT, 1)).isNull();
    assertThat(context.lineHits("myProjectKey:sources/utils/code_chunks.cpp", CoverageType.UNIT, 1)).isNull();
  }

  @Test
  public void shouldReportCoverageWhenVisualStudioCase() {
    Settings settings = new Settings();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio.xml");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "project1/source1.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "project2/source1.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "project2/source2.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

    sensor = new CxxCoverageSensor(settings, new CxxCoverageCache());
    sensor.execute(context);
    assertThat(context.lineHits("myProjectKey:project1/source1.cpp", CoverageType.UNIT, 4)).isEqualTo(0);
    assertThat(context.lineHits("myProjectKey:project2/source1.cpp", CoverageType.UNIT, 4)).isEqualTo(1);
    assertThat(context.lineHits("myProjectKey:project2/source2.cpp", CoverageType.UNIT, 4)).isEqualTo(1);
  }
}
