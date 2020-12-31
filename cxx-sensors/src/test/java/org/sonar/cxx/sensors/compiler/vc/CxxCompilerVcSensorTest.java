/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCompilerVcSensorTest {

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void sensorDescriptorVc() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxCompilerVcSensor();
    sensor.describe(descriptor);
    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Visual Studio compiler report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c");
    softly.assertThat(descriptor.ruleRepositories())
      .containsOnly(CxxCompilerVcRuleRepository.KEY);
    softly.assertAll();
  }

  @Test
  public void shouldReportACorrectVcViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCompilerVcSensor.REPORT_PATH_KEY,
                         "compiler-reports/BuildLog.htm");
    settings.setProperty(CxxCompilerVcSensor.REPORT_CHARSET_DEF, StandardCharsets.UTF_16.name());
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "zipmanager.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    var sensor = new CxxCompilerVcSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(9);
  }

  @Test
  public void shouldReportBCorrectVcViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCompilerVcSensor.REPORT_PATH_KEY, "compiler-reports/VC-report.vclog");
    settings.setProperty(CxxCompilerVcSensor.REPORT_CHARSET_DEF, StandardCharsets.UTF_8.name());
    settings.setProperty(CxxCompilerVcSensor.REPORT_REGEX_DEF,
                         ".*>(?<file>.*)\\((?<line>\\d+)\\):\\x20warning\\x20(?<id>C\\d+):(?<message>.*)");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "Server/source/zip/zipmanager.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    var sensor = new CxxCompilerVcSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(9);
  }

}
