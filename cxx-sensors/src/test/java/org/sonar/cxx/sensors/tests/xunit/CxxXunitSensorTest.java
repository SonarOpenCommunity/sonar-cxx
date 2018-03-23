/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import java.io.File;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxXunitSensorTest {

  private FileSystem fs;
  private CxxLanguage language;
  private MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxXunitSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxXunitSensor.REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxXunitSensor.XSLT_URL_KEY)).thenReturn("sonar.cxx." + CxxXunitSensor.XSLT_URL_KEY);
    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.FALSE));
  }

  @Test
  public void shouldReportNothingWhenNoReportFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxXunitSensor.REPORT_PATH_KEY), "notexistingpath");
    context.setSettings(settings);

    CxxXunitSensor sensor = new CxxXunitSensor(language);

    sensor.execute(context);

    assertThat(context.measures(context.module().key())).hasSize(0);
  }

  @Test
  public void shouldReadXunitReport() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxXunitSensor.REPORT_PATH_KEY), "xunit-reports/xunit-result-SAMPLE_with_fileName.xml");
    context.setSettings(settings);

    CxxXunitSensor sensor = new CxxXunitSensor(language);

    sensor.execute(context);

    assertThat(context.measures(context.module().key())).hasSize(6);
    assertThat(context.measures(context.module().key()))
      .extracting("metric.key", "value")
      .containsOnly(
        tuple(CoreMetrics.TESTS_KEY, 3),
        tuple(CoreMetrics.SKIPPED_TESTS_KEY, 0),
        tuple(CoreMetrics.TEST_FAILURES_KEY, 0),
        tuple(CoreMetrics.TEST_ERRORS_KEY, 0),
        tuple(CoreMetrics.TEST_SUCCESS_DENSITY_KEY, 100.0),
        tuple(CoreMetrics.TEST_EXECUTION_TIME_KEY, 0L));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowWhenGivenInvalidTime() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxXunitSensor.REPORT_PATH_KEY), "xunit-reports/invalid-time-xunit-report.xml");
    context.setSettings(settings);

    CxxXunitSensor sensor = new CxxXunitSensor(language);

    sensor.execute(context);
  }

  @Test(expected = java.net.MalformedURLException.class)
  public void transformReport_shouldThrowWhenGivenNotExistingStyleSheet()
    throws java.io.IOException, javax.xml.transform.TransformerException {

    when(language.getStringOption(CxxXunitSensor.XSLT_URL_KEY)).thenReturn(Optional.of("whatever"));

    CxxXunitSensor sensor = new CxxXunitSensor(language);
    sensor.transformReport(cppunitReport());
  }

  @Test
  public void transformReport_shouldTransformCppunitReport()
    throws java.io.IOException, javax.xml.transform.TransformerException {

    when(language.getStringOption(CxxXunitSensor.XSLT_URL_KEY)).thenReturn(Optional.of("cppunit-1.x-to-junit-1.0.xsl"));

    CxxXunitSensor sensor = new CxxXunitSensor(language);
    File reportBefore = cppunitReport();

    File reportAfter = sensor.transformReport(reportBefore);
    assertThat(reportAfter).isNotSameAs(reportBefore);
  }

  File cppunitReport() {
    return new File(new File(fs.baseDir(), "xunit-reports"), "cppunit-report.xml");
  }
  
  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    CxxXunitSensor sensor = new CxxXunitSensor(language);
    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo(language.getName() + " XunitSensor");
  }
}
