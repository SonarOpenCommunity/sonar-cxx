/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.coverage.bullseye.CxxCoverageBullseyeSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxBullseyeCoverageSensorTest {

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
  }

  @Test
  void shouldReportCorrectCoverage() {
    var context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
      CxxCoverageBullseyeSensor.REPORT_PATH_KEY,
      "coverage-reports/bullseye/coverage-result-bullseye.xml"
    );
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("main.cpp", 32));
    context.fileSystem().add(createTestInputFile("source_1.cpp", 52));
    context.fileSystem().add(createTestInputFile("src/testclass.h", 32));
    context.fileSystem().add(createTestInputFile("src/testclass.cpp", 52));
    context.fileSystem().add(createTestInputFile("testclass.h", 32));
    context.fileSystem().add(createTestInputFile("testclass.cpp", 62));

    var sensor = new CxxCoverageBullseyeSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:main.cpp", 7)).isEqualTo(1);

    var zeroHitLines = new int[]{5, 10, 15, 17, 28, 32, 35, 40, 41, 44};
    for (var line : zeroHitLines) {
      assertThat(context.lineHits("ProjectKey:source_1.cpp", line)).isZero();
    }

    var oneHitlinesA = new int[]{7, 12, 17, 30};
    for (var line : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:testclass.cpp", line)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", line)).isEqualTo(1);
    }
    var fullCoveredTwoCondition = new int[]{34, 43, 46};
    for (var line : fullCoveredTwoCondition) {
      assertThat(context.conditions("ProjectKey:testclass.cpp", line)).isEqualTo(2);
      assertThat(context.conditions("ProjectKey:src/testclass.cpp", line)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", line)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", line)).isEqualTo(2);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", line)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", line)).isEqualTo(1);
    }

    assertThat(context.conditions("ProjectKey:testclass.cpp", 42)).isEqualTo(4);
    assertThat(context.conditions("ProjectKey:src/testclass.cpp", 42)).isEqualTo(4);
    assertThat(context.coveredConditions("ProjectKey:testclass.cpp", 42)).isEqualTo(4);
    assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", 42)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:testclass.cpp", 42)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:src/testclass.cpp", 42)).isEqualTo(1);

    assertThat(context.conditions("ProjectKey:testclass.cpp", 19)).isEqualTo(2);
    assertThat(context.coveredConditions("ProjectKey:testclass.cpp", 19)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:testclass.cpp", 19)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:src/testclass.cpp", 19)).isEqualTo(1);

    assertThat(context.conditions("ProjectKey:testclass.cpp", 37)).isEqualTo(6);
    assertThat(context.conditions("ProjectKey:src/testclass.cpp", 37)).isEqualTo(6);
    assertThat(context.coveredConditions("ProjectKey:testclass.cpp", 37)).isEqualTo(6);
    assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", 37)).isEqualTo(6);
    assertThat(context.lineHits("ProjectKey:testclass.cpp", 37)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:src/testclass.cpp", 37)).isEqualTo(1);
  }

  @Test
  void shouldParseTopLevelFiles() {
    // read top level folder name from report file
    var context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
      CxxCoverageBullseyeSensor.REPORT_PATH_KEY,
      "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-win.xml"
    );
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h", 32));
    context.fileSystem().add(createTestInputFile("randomfoldernamethatihopeknowmachinehas/test/test.c", 32));
    context.fileSystem().add(createTestInputFile("randomfoldernamethatihopeknowmachinehas/test2/test2.c", 32));
    context.fileSystem().add(createTestInputFile("randomfoldernamethatihopeknowmachinehas/main.c", 32));

    var sensor = new CxxCoverageBullseyeSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/test/test.c", 4))
      .isEqualTo(1);
    assertThat(context.conditions("ProjectKey:randomfoldernamethatihopeknowmachinehas/test/test.c", 7))
      .isEqualTo(2);
  }

  @Test
  void shouldReportAllProbes() {
    var context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
      CxxCoverageBullseyeSensor.REPORT_PATH_KEY,
      "coverage-reports/bullseye/bullseye-coverage-Linux-V8.9.60.xml"
    );
    context.setSettings(settings);

    var fileList = new String[]{
      "covfile/import/cereal/archives/json.hpp",
      "covfile/import/jpeg-compressor/src/jpgd.cpp",
      "covfile/src/main/vr_io/src/ModalityLUTJson.cpp",
      "covfile/src/test/vr_core/Image_unittest.cpp",
      "covfile/import/cereal/external/base64.hpp",
      "covfile/import/cereal/details/traits.hpp",
      "covfile/import/jpeg-compressor/src/jpge.cpp",
      "covfile/src/main/vr_io/src/RenderingParamJson.cpp",
      "covfile/import/cereal/external/rapidjson/prettywriter.h",
      "covfile/import/cereal/external/rapidjson/document.h",
      "covfile/src/main/main/main.cpp",
      "covfile/import/cereal/details/static_object.hpp",
      "covfile/src/main/vr_core/src/VR.cpp"
    };

    for (var filepath : fileList) {
      context.fileSystem().add(createTestInputFile(filepath, 4032));
    }
    var sensor = new CxxCoverageBullseyeSensor();
    sensor.execute(context);

    var coveredCondition = new int[]{496, 524};

    for (var line : coveredCondition) {
      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", line))
        .isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", line))
        .isEqualTo(2);
    }

    assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 530))
      .isEqualTo(6);
    assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 530))
      .isEqualTo(5);

    assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 483))
      .isEqualTo(6);
    assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 483))
      .isEqualTo(3);

    assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 552))
      .isEqualTo(2);
    assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 552))
      .isEqualTo(1);

    assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 495))
      .isEqualTo(2);
    assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 495))
      .isEqualTo(1);

    assertThat(context.lineHits("ProjectKey:covfile/import/cereal/archives/json.hpp", 474))
      .isZero();
    assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 474))
      .isEqualTo(1);
    assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 474))
      .isZero();
    assertThat(context.lineHits("ProjectKey:covfile/import/cereal/archives/json.hpp", 475))
      .isEqualTo(1);
    assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 475))
      .isEqualTo(1);
    assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", 475))
      .isEqualTo(1);

    assertThat(context.lineHits("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", 39))
      .isEqualTo(1);
    assertThat(context.conditions("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", 39))
      .isEqualTo(2);
    assertThat(context.coveredConditions("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", 39))
      .isEqualTo(1);
  }

  @Test
  void shouldIgnoreBlocks() {
    // report contains a block tag => ignore
    var context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
      CxxCoverageBullseyeSensor.REPORT_PATH_KEY,
      "coverage-reports/bullseye/bullseye-coverage-Windows-V8.20.2.xml"
    );
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("root/folder/test.cpp", 7));

    var sensor = new CxxCoverageBullseyeSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:root/folder/test.cpp", 3)).isEqualTo(1);
  }

}
