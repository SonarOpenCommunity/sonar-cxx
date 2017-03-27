/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import org.sonar.cxx.sensors.tests.xunit.CxxXunitSensor;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxXunitSensorTest {

  private FileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldReportNothingWhenNoReportFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxXunitSensor.REPORT_PATH_KEY)).thenReturn(new String[] { "notexistingpath" });    
    
    CxxXunitSensor sensor = new CxxXunitSensor(language);

    sensor.execute(context);

    assertThat(context.measures(context.module().key())).hasSize(0);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowWhenGivenInvalidTime() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxXunitSensor.REPORT_PATH_KEY))
            .thenReturn(new String[] { "xunit-reports/invalid-time-xunit-report.xml" });
    when(language.IsRecoveryEnabled())
            .thenReturn(false);
    
    CxxXunitSensor sensor = new CxxXunitSensor(language);

    sensor.execute(context);
  }

  @Test(expected = java.net.MalformedURLException.class)
  public void transformReport_shouldThrowWhenGivenNotExistingStyleSheet()
    throws java.io.IOException, javax.xml.transform.TransformerException {
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringOption(CxxXunitSensor.XSLT_URL_KEY)).thenReturn("whatever");    
    
    CxxXunitSensor sensor = new CxxXunitSensor(language);

    sensor.transformReport(cppunitReport());
  }

  @Test
  public void transformReport_shouldTransformCppunitReport()
    throws java.io.IOException, javax.xml.transform.TransformerException {
    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringOption(CxxXunitSensor.XSLT_URL_KEY)).thenReturn("cppunit-1.x-to-junit-1.0.xsl");    
    
    CxxXunitSensor sensor = new CxxXunitSensor(language);
    File reportBefore = cppunitReport();

    File reportAfter = sensor.transformReport(reportBefore);

    assert (reportAfter != reportBefore);
  }

  File cppunitReport() {
    return new File(new File(fs.baseDir(), "xunit-reports"), "cppunit-report.xml");
  }
}
