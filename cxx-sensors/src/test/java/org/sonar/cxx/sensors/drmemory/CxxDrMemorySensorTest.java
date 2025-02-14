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
package org.sonar.cxx.sensors.drmemory;

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxDrMemorySensorTest {

  private DefaultFileSystem fs;

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV1() {
    var context = SensorContextTester.create(fs.baseDir());
    context.settings().setProperty(CxxDrMemorySensor.REPORT_PATH_KEY,
      "drmemory-reports/drmemory-result-SAMPLE-V1.txt");

    context.fileSystem().add(createTestInputFile("sources/utils/code_chunks.cpp", 3));

    var sensor = new CxxDrMemorySensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxDrMemorySensor().setWebApi(null);
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Dr. Memory report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxDrMemoryRuleRepository.KEY);
    softly.assertAll();
  }

}
