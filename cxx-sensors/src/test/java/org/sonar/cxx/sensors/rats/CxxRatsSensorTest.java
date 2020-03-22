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
package org.sonar.cxx.sensors.rats;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxRatsSensorTest {

  private CxxRatsSensor sensor;
  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxRatsSensor.REPORT_PATH_KEY, "rats-reports/rats-result-*.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp").setLanguage(
      "cpp").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "report.c").setLanguage("cpp").initMetadata(
      "asd\nasdas\nasda\n").build());

    sensor = new CxxRatsSensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(5);
  }

  @Test
  public void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    sensor = new CxxRatsSensor();
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo(CxxLanguage.NAME + " RatsSensor");
    softly.assertThat(descriptor.languages()).containsOnly(CxxLanguage.KEY);
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxRatsRuleRepository.KEY);
    softly.assertAll();
  }

}
