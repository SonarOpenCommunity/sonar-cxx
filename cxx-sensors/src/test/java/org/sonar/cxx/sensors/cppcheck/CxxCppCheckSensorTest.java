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
package org.sonar.cxx.sensors.cppcheck;

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxCppCheckSensorTest {

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
  }

  @Test
  void shouldReportCorrectViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY, "cppcheck-reports/cppcheck-result-*.xml");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("sources/utils/code_chunks.cpp", 3));
    context.fileSystem().add(createTestInputFile("sources/utils/utils.cpp", 3));

    var sensor = new CxxCppCheckSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(7);
  }

  @Test
  void shouldReportProjectLevelViolationsV2() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY,
      "cppcheck-reports/cppcheck-result-projectlevelviolation-V2.xml");
    context.setSettings(settings);

    var sensor = new CxxCppCheckSensor().setWebApi(null);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.allIssues()).hasSize(3);

    // assert that all all issues were filed on on the module
    String moduleKey = context.project().key();
    for (var issue : context.allIssues()) {
      softly.assertThat(issue.primaryLocation().inputComponent().key()).isEqualTo(moduleKey);
    }
    softly.assertAll();
  }

  @Test
  void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV1() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY, "cppcheck-reports/cppcheck-result-SAMPLE-V1.xml");
    context.setSettings(settings);

    var sensor = new CxxCppCheckSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV2() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");
    context.setSettings(settings);

    var sensor = new CxxCppCheckSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenRecoveryIsDisabled() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
    settings.setProperty(CxxCppCheckSensor.REPORT_PATH_KEY, "cppcheck-reports/cppcheck-result-empty.xml");
    context.setSettings(settings);

    var sensor = new CxxCppCheckSensor().setWebApi(null);
    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      sensor.execute(context);
    });
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxCppCheckSensor().setWebApi(null);
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Cppcheck report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxCppCheckRuleRepository.KEY);
    softly.assertAll();
  }

}
