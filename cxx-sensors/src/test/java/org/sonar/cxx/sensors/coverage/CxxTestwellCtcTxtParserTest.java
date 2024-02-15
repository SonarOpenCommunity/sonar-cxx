/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.coverage.ctc.CxxCoverageTestwellCtcTxtSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxTestwellCtcTxtParserTest {

  private DefaultFileSystem fs;
  private SensorContextTester context;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
  }

  @Test
  void shouldReportCoveredLines() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_small_v8.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "HGBuildNumberLookup.cpp")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.lineHits("ProjectKey:HGBuildNumberLookup.cpp", 42)).isEqualTo(10);
    softly.assertAll();

  }

  @Test
  void shouldReportCoveredConditionsOne() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_small_v8.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "HGBuildNumberLookup.cpp")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.coveredConditions("ProjectKey:HGBuildNumberLookup.cpp", 50)).isEqualTo(1);
    softly.assertAll();

  }

  @Test
  void shouldReportCoveredConditionsTwo() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_small_v8.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "HGBuildNumberLookup.cpp")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.coveredConditions("ProjectKey:HGBuildNumberLookup.cpp", 56)).isEqualTo(2);
    softly.assertAll();

  }

  @Test
  void shouldConsumeLargeReportCoveredLines() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_big.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "test-wildmatch.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "credential-store.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.lineHits("ProjectKey:test-wildmatch.c", 3)).isEqualTo(209);
    softly.assertAll();

  }

  @Test
  void shouldConsumeLargeReportCoveredConditions() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_big.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "test-wildmatch.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "credential-store.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.coveredConditions("ProjectKey:test-wildmatch.c", 6)).isEqualTo(2);
    softly.assertAll();

  }

  @Test
  void shouldConsumeLargeReportConditions() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_big.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "test-wildmatch.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "credential-store.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.conditions("ProjectKey:credential-store.c", 78)).isEqualTo(8);
    softly.assertAll();

  }

  @Test
  void shouldConsumeEmptyReport() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageTestwellCtcTxtSensor.REPORT_PATH_KEY,
                         "coverage-reports/TestwellCTC/report_empty.txt");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "test-wildmatch.c")
      .setLanguage("cxx").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                         + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                       + "\n\n\n\n\n\n\n").build());

    var sensor = new CxxCoverageTestwellCtcTxtSensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.lineHits("ProjectKey:test-wildmatch.c", 3)).isNull();
    softly.assertAll();

  }

}
