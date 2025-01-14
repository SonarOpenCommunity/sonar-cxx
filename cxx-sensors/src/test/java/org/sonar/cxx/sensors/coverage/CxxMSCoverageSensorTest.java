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

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.cxx.sensors.coverage.vs.CxxCoverageVisualStudioSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxMSCoverageSensorTest {

  @RegisterExtension
  private final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private DefaultFileSystem fs;
  private SensorContextTester context;
  private final MapSettings settings = new MapSettings();
  private final String metaData = "asd\nasdas\nasda" + "\n".repeat(30);

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  void shouldReportCorrectCoverage() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageVisualStudioSensor.REPORT_PATH_KEY, "coverage-reports/MSCoverage/MSCoverage.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/motorcontroller/motorcontroller.cpp")
      .setLanguage("cxx")
      .initMetadata(metaData)
      .build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/rootfinder/rootfinder.cpp")
      .setLanguage("cxx")
      .initMetadata(metaData)
      .build());

    var sensor = new CxxCoverageVisualStudioSensor();
    sensor.execute(context);

    var oneHitlinesA = new int[]{12, 14, 16, 19, 20, 21, 23, 25, 26, 27, 28};
    for (var oneHitline : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:source/rootfinder/rootfinder.cpp", oneHitline)).isEqualTo(1);
    }

    var oneHitlinesB = new int[]{9, 10, 11, 14, 15, 16, 19, 20, 21, 24, 25, 26, 29, 30, 31};
    for (var oneHitline : oneHitlinesB) {
      assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", oneHitline)).isEqualTo(1);
    }
  }

  @Test
  void shouldReportCoverageWhenVisualStudioCase() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageVisualStudioSensor.REPORT_PATH_KEY,
      "coverage-reports/MSCoverage/coverage-result-visual-studio.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "project2/source1.cpp")
      .setLanguage("cxx").initMetadata(metaData)
      .build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "project2/source2.cpp")
      .setLanguage("cxx").initMetadata(metaData)
      .build());

    var sensor = new CxxCoverageVisualStudioSensor();
    sensor.execute(context);

    var oneHitlinesA = new int[]{4, 5, 6, 8, 13, 15, 16, 25};
    var zeroHitlinesA = new int[]{9, 10, 22, 23};
    for (var zeroHitline : zeroHitlinesA) {
      assertThat(context.lineHits("ProjectKey:project2/source1.cpp", zeroHitline)).isZero();
    }
    for (var oneHitline : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:project2/source1.cpp", oneHitline)).isEqualTo(1);
    }

    var oneHitlinesB = new int[]{4, 5, 6, 8, 9, 10, 13, 21, 25};
    var zeroHitlinesB = new int[]{15, 16, 22, 23};
    for (var zeroHitline : zeroHitlinesB) {
      assertThat(context.lineHits("ProjectKey:project2/source2.cpp", zeroHitline)).isZero();
    }
    for (var oneHitline : oneHitlinesB) {
      assertThat(context.lineHits("ProjectKey:project2/source2.cpp", oneHitline)).isEqualTo(1);
    }

  }

  @Test
  void shouldReadFaultyReportAndNotCrash() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageVisualStudioSensor.REPORT_PATH_KEY, "coverage-reports/MSCoverage/faulty.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/motorcontroller/motorcontroller.cpp")
      .setLanguage("cxx")
      .initMetadata(metaData)
      .build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/rootfinder/rootfinder.cpp")
      .setLanguage("cxx")
      .initMetadata(metaData)
      .build());

    var sensor = new CxxCoverageVisualStudioSensor();
    logTester.clear();
    sensor.execute(context);

    var log = logTester.logs();
    assertThat(log).hasSize(2);
    assertThat(log.get(0)).contains("faulty.xml");
    assertThat(log.get(1)).contains("skipping");
  }

  @Test
  void shouldConsumeEmptyReport() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageVisualStudioSensor.REPORT_PATH_KEY, "coverage-reports/MSCoverage/empty-report.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/motorcontroller/motorcontroller.cpp")
      .setLanguage("cxx")
      .initMetadata(metaData)
      .build());

    var sensor = new CxxCoverageVisualStudioSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", 1)).isNull();
  }

  @Test
  void sensorDescriptor() {
    context = SensorContextTester.create(fs.baseDir());
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxCoverageVisualStudioSensor();
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Visual Studio XML coverage report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertAll();
  }

}
