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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;

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
  public void shouldFindTheSourcesOfTheTestfiles() {
    Settings config = new Settings();
    config.setProperty(CxxXunitSensor.REPORT_PATH_KEY, "xunit-report.xml");
    sensor = new CxxXunitSensor(config, TestUtils.mockCxxLanguage());
    
    List<File> sourceDirs = new ArrayList<File>();
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/xunit/project_finding_sources");
    sourceDirs.add(baseDir);
    
    List<File> testDirs = new ArrayList<File>();
    testDirs.add(new File(baseDir, "tests1"));
    testDirs.add(new File(baseDir, "tests2"));
    
    Project project = TestUtils.mockProject(baseDir, sourceDirs, testDirs);
    sensor.lookupTestFiles(project);
    
    // case 1:
    // the testcase file resides: directly under the test directory
    // the testcase file contains: only one class
    // the report mentions: the class name
    assertEquals(sensor.getFilePath("TestClass1"), new File(baseDir, "tests1/Test1.cc").getPath());
    
    // case 2:
    // the testcase file resides: in a subdirectory
    // the testcase file contains: a couple of classes
    // the report mentions: the class name
    assertEquals(sensor.getFilePath("TestClass2"), new File(baseDir, "tests1/subdir/Test2.cc").getPath());

    // case 3:
    // the testcase file resides: in second directory
    // the testcase file contains: the class in a namespace
    // the report mentions: the class name
    assertEquals(sensor.getFilePath("TestClass3"), new File(baseDir, "tests2/Test3.cc").getPath());
    
    // case 4:
    // the testcase file resides: somewhere
    // the testcase file contains: the class is implemented via a header and impl. file
    // the report mentions: the class name

    // TODO: the case if the implementation of the mentioned tests is scattered
    //       across many files is inherently difficult to handle. The "class declarated in
    //       header and implemented in *.cc-file" is just a special case of it.
    
    assertEquals(sensor.getFilePath("TestClass4"), new File(baseDir, "tests2/Test4.hh").getPath());
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
