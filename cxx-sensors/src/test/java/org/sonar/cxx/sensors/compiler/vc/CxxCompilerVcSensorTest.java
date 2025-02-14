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
package org.sonar.cxx.sensors.compiler.vc;

import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxCompilerVcSensorTest {

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  void sensorDescriptorVc() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxCompilerVcSensor();
    sensor.describe(descriptor);
    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Visual C++ compiler report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories())
      .containsOnly(CxxCompilerVcRuleRepository.KEY);
    softly.assertAll();
  }

  @Test
  void shouldReportACorrectVcViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCompilerVcSensor.REPORT_PATH_KEY,
      "compiler-reports/BuildLog.htm");
    settings.setProperty(CxxCompilerVcSensor.REPORT_ENCODING_DEF, StandardCharsets.UTF_16.name());
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("zipmanager.cpp", 3));

    var sensor = new CxxCompilerVcSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(9);
  }

  @Test
  void shouldReportBCorrectVcViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCompilerVcSensor.REPORT_PATH_KEY, "compiler-reports/VC-report.vclog");
    settings.setProperty(CxxCompilerVcSensor.REPORT_ENCODING_DEF, StandardCharsets.UTF_8.name());
    settings.setProperty(CxxCompilerVcSensor.REPORT_REGEX_DEF,
      "[^>]*+>(?<file>.*)\\((?<line>\\d{1,5})\\):\\x20warning\\x20(?<id>C\\d{4,5}):(?<message>.*)");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("Server/source/zip/zipmanager.cpp", 3));

    var sensor = new CxxCompilerVcSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(9);
  }

}
