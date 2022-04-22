/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.sensors.tests.xunit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxXunitSensorTest {

  private FileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
  }

  @Test
  void shouldReportNothingWhenNoReportFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxXunitSensor.REPORT_PATH_KEY, "notexistingpath");
    context.setSettings(settings);

    var sensor = new CxxXunitSensor();
    sensor.execute(context);

    assertThat(context.measures(context.project().key())).isEmpty();
  }

  @Test
  void shouldReadXunitReport() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxXunitSensor.REPORT_PATH_KEY, "xunit-reports/xunit-result-SAMPLE_with_fileName.xml");
    context.setSettings(settings);

    var sensor = new CxxXunitSensor();
    sensor.execute(context);

    assertThat(context.measures(context.project().key())).hasSize(5);
    assertThat(context.measures(context.project().key()))
      .extracting("metric.key", "value")
      .containsOnly(
        tuple(CoreMetrics.TESTS_KEY, 3),
        tuple(CoreMetrics.SKIPPED_TESTS_KEY, 0),
        tuple(CoreMetrics.TEST_FAILURES_KEY, 0),
        tuple(CoreMetrics.TEST_ERRORS_KEY, 0),
        tuple(CoreMetrics.TEST_EXECUTION_TIME_KEY, 0L));
  }

  @Test
  void shouldThrowWhenGivenInvalidTime() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxXunitSensor.REPORT_PATH_KEY, "xunit-reports/invalid-time-xunit-report.xml");
    context.setSettings(settings);
    var sensor = new CxxXunitSensor();

    IllegalStateException thrown = catchThrowableOfType(() -> {
      sensor.execute(context);
    }, IllegalStateException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxXunitSensor();
    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("CXX xUnit Test report import");
  }

}
