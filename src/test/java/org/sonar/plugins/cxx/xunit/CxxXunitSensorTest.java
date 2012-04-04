/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx.xunit;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.any;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.cxx.TestUtils;

public class CxxXunitSensorTest {
  private CxxXunitSensor sensor;
  private SensorContext context;
  private Project project;

  @Before
  public void setUp() {
    Configuration config = mock(Configuration.class);
    project = TestUtils.mockProject();
    sensor = new CxxXunitSensor(config);
    context = mock(SensorContext.class);
  }

  @Test
  public void shouldReportCorrectViolations() {
    sensor.analyse(project, context);

    verify(context, times(1)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.TESTS), anyDouble());
    verify(context, times(1)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.SKIPPED_TESTS), anyDouble());
    verify(context, times(1)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.TEST_ERRORS), anyDouble());
    verify(context, times(1)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.TEST_FAILURES), anyDouble());
    verify(context, times(1)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.TEST_SUCCESS_DENSITY), anyDouble());
    verify(context, times(1)).saveMeasure((Resource) anyObject(), any(Measure.class));
  }

  @Test(expected=java.net.MalformedURLException.class)
  public void transformReport_shouldThrowWhenGivenNotExistingStyleSheet()
    throws java.io.IOException, javax.xml.transform.TransformerException 
  {
    Configuration config = mock(Configuration.class);
    when(config.getString(CxxXunitSensor.XSLT_URL_KEY)).thenReturn("whatever");
    sensor = new CxxXunitSensor(config);
    
    sensor.transformReport(project, cppunitReportList(), context);
  }
  
  @Test
  public void transformReport_shouldTransformCppunitReport()
    throws java.io.IOException, javax.xml.transform.TransformerException 
  {
    Configuration config = mock(Configuration.class);
    when(config.getString(CxxXunitSensor.XSLT_URL_KEY)).thenReturn("cppunit-1.x-to-junit-1.0.xsl");
    sensor = new CxxXunitSensor(config);
    List<File> reports = cppunitReportList();
    File reportBefore = reports.get(0);
    
    sensor.transformReport(project, reports, context);
    
    assert(reports.get(0) != reportBefore);
  }
  
  List<File> cppunitReportList() {
    List<File> reports = new ArrayList<File>();
    File reportBefore = new File(new File(project.getFileSystem().getBasedir().getPath(), "xunit-reports"),
                                 "cppunit-report.xml");
    reports.add(reportBefore);
    return reports;
  }
}
