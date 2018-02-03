/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCoberturaSensorTest {

  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private final Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private CxxLanguage language;
  private final MapSettings settings = new MapSettings();
  @org.junit.Rule
  public LogTester logTester = new LogTester();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY))
      .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
  }

  @Test
  public void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");

    context.setSettings(settings);
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
  }

  @Test
  public void shouldReportCorrectCoverageSQ62() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
//    context.setSonarQubeVersion(SQ_6_2);

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/coverage-result-cobertura.xml");

    context.setSettings(settings);
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
  }

  @Test
  public void shouldReportNoCoverageSaved() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/cobertura/specific-cases/does-not-exist.xml");
    context.setSettings(settings);

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);

    List<String> log = logTester.logs();
    assertThat(log.contains("Scanner found '0' report files")).isTrue();
  }

  @Test
  public void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");

    sensor.execute(context);
    assertThat(linesOfCodeByFile.isEmpty()).isTrue();
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesEmpty() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    settings.setProperty(sensor.getReportPathKey(), "coverage-reports/cobertura/specific-cases/coverage-result-cobertura-empty.xml");

    context.setSettings(settings);
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isNull();
  }

  @Test
  public void shouldReportNoCoverageWhenFilesInvalid() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/cobertura/specific-cases/coverage-result-invalid.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isNull();
  }

  @Test
  public void shouldReportCoverageWhenVisualStudioCase() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "project2/source1.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "project2/source2.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);

    int[] oneHitlinesA = new int[]{4, 5, 6, 8, 13, 15, 16, 25};
    int[] zeroHitlinesA = new int[]{9, 10, 22, 23};
    for (int zeroHitline : zeroHitlinesA) {
      assertThat(context.lineHits("ProjectKey:project2/source1.cpp", zeroHitline)).isEqualTo(0);
    }
    for (int oneHitline : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:project2/source1.cpp", oneHitline)).isEqualTo(1);
    }

    int[] oneHitlinesB = new int[]{4, 5, 6, 8, 9, 10, 13, 21, 25};
    int[] zeroHitlinesB = new int[]{15, 16, 22, 23};
    for (int zeroHitline : zeroHitlinesB) {
      assertThat(context.lineHits("ProjectKey:project2/source2.cpp", zeroHitline)).isEqualTo(0);
    }
    for (int oneHitline : oneHitlinesB) {
      assertThat(context.lineHits("ProjectKey:project2/source2.cpp", oneHitline)).isEqualTo(1);
    }

  }
}
