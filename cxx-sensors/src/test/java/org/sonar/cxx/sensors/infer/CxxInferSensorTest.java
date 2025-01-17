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
package org.sonar.cxx.sensors.infer;

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

class CxxInferSensorTest {

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
    settings.setProperty(CxxInferSensor.REPORT_PATH_KEY, "infer-reports/infer-result-sample.json");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("lib/tokenize.cpp", 3));
    context.fileSystem().add(createTestInputFile("cli/cppcheckexecutor.cpp", 3));
    context.fileSystem().add(createTestInputFile("externals/tinyxml/tinyxml2.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/ctu.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/checkunusedvar.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/cppcheck.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/checkio.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/checkother.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/exprengine.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/checkstl.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/astutils.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/clangimport.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/templatesimplifier.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/checkclass.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/symboldatabase.cpp", 3));
    context.fileSystem().add(createTestInputFile("lib/valueflow.cpp", 3));

    var sensor = new CxxInferSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(34);
  }

  @Test
  void shouldIgnoreAViolationWhenTheResourceCouldntBeFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxInferSensor.REPORT_PATH_KEY, "infer-reports/infer-result-sample.json");
    context.setSettings(settings);

    var sensor = new CxxInferSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenRecoveryIsDisabled() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
    settings.setProperty(CxxInferSensor.REPORT_PATH_KEY, "infer-reports/infer-result-empty.json");
    context.setSettings(settings);
    var sensor = new CxxInferSensor().setWebApi(null);

    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      sensor.execute(context);
    });
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxInferSensor().setWebApi(null);
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Infer report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxInferRuleRepository.KEY);
    softly.assertAll();
  }

}
