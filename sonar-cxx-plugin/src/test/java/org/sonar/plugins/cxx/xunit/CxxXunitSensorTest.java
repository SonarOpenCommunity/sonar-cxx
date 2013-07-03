/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.xunit;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CxxXunitSensorTest {
  private CxxXunitSensor sensor;
  private SensorContext context;
  private Project project;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    sensor = new CxxXunitSensor(new Settings(), TestUtils.mockCxxLanguage());
    context = mock(SensorContext.class);
  }

  @Test
  public void shouldReportCorrectViolations() {
    sensor.analyse(project, context);

    verify(context, times(3)).saveMeasure((org.sonar.api.resources.File) anyObject(),
        eq(CoreMetrics.TESTS), anyDouble());
    verify(context, times(3)).saveMeasure((org.sonar.api.resources.File) anyObject(),
        eq(CoreMetrics.SKIPPED_TESTS), anyDouble());
    verify(context, times(3)).saveMeasure((org.sonar.api.resources.File) anyObject(),
        eq(CoreMetrics.TEST_ERRORS), anyDouble());
    verify(context, times(3)).saveMeasure((org.sonar.api.resources.File) anyObject(),
        eq(CoreMetrics.TEST_FAILURES), anyDouble());
    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(),
        eq(CoreMetrics.TEST_SUCCESS_DENSITY), anyDouble());
    verify(context, times(3)).saveMeasure((org.sonar.api.resources.File) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportZeroTestWhenNoReportFound() {
    Settings config = new Settings();
    config.setProperty(CxxXunitSensor.REPORT_PATH_KEY, "notexistingpath");

    sensor = new CxxXunitSensor(config, TestUtils.mockCxxLanguage());

    sensor.analyse(project, context);

    verify(context, times(1)).saveMeasure(eq(CoreMetrics.TESTS), eq(0.0));
  }

  @Test(expected = org.sonar.api.utils.SonarException.class)
  public void shouldThrowWhenGivenInvalidTime() {
    Settings config = new Settings();
    config.setProperty(CxxXunitSensor.REPORT_PATH_KEY, "xunit-reports/invalid-time-xunit-report.xml");
    sensor = new CxxXunitSensor(config, TestUtils.mockCxxLanguage());

    sensor.analyse(project, context);
  }

  @Test(expected = java.net.MalformedURLException.class)
  public void transformReport_shouldThrowWhenGivenNotExistingStyleSheet()
      throws java.io.IOException, javax.xml.transform.TransformerException
  {
    Settings config = new Settings();
    config.setProperty(CxxXunitSensor.XSLT_URL_KEY, "whatever");

    sensor = new CxxXunitSensor(config, TestUtils.mockCxxLanguage());

    sensor.transformReport(cppunitReport());
  }

  @Test
  public void transformReport_shouldTransformCppunitReport()
      throws java.io.IOException, javax.xml.transform.TransformerException
  {
    Settings config = new Settings();
    config.setProperty(CxxXunitSensor.XSLT_URL_KEY, "cppunit-1.x-to-junit-1.0.xsl");

    sensor = new CxxXunitSensor(config, TestUtils.mockCxxLanguage());
    File reportBefore = cppunitReport();

    File reportAfter = sensor.transformReport(reportBefore);

    assert (reportAfter != reportBefore);
  }

  File cppunitReport() {
    return new File(new File(project.getFileSystem().getBasedir().getPath(), "xunit-reports"),
        "cppunit-report.xml");
  }
}
