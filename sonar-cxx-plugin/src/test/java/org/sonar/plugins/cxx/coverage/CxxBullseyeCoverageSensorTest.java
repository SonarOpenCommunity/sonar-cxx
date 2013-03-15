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
package org.sonar.plugins.cxx.coverage;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CxxBullseyeCoverageSensorTest {
  private CxxCoverageSensor sensor;
  private SensorContext context;
  private Project project;

  @Before
  public void setUp() {
    project = TestUtils.mockVsProject();
    Settings bulls = new Settings();
    bulls.appendProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/ut-coverage-same-drive.xml");
    bulls.appendProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY, "coverage-reports/it-coverage-another-drive.xml");
    bulls.appendProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY, "coverage-reports/overall-coverage-same-drive.xml");
    
    sensor = new CxxCoverageSensor(bulls);
    context = mock(SensorContext.class);
    File cxxfile = org.sonar.api.resources.File.fromIOFile(
        new java.io.File("E:/Development/sonar-cxx/sonar-cxx/sonar-cxx-plugin/target/test-classes/org/sonar/plugins/cxx/SampleProjectWindows/sample/sampleclass.h"), project);
    File cxxfile1 = org.sonar.api.resources.File.fromIOFile(
        new java.io.File("E:/Development/sonar-cxx/sonar-cxx/sonar-cxx-plugin/target/test-classes/org/sonar/plugins/cxx/SampleProjectWindows/sample/sampleclass.cpp"), project);
    
    
    File resourceMock = mock(File.class);
    when(context.getResource(null)).thenReturn(null);
    when(context.getResource(cxxfile)).thenReturn(resourceMock);
    when(context.getResource(cxxfile1)).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectCoverage() {
    ArgumentCaptor<File> argument = ArgumentCaptor.forClass(File.class);
    
    sensor.analyse(project, context);
    verify(context, times(18)).saveMeasure(argument.capture(), any(Measure.class));
    
    List<File> capturedFiles = argument.getAllValues();
    assertThat(capturedFiles.get(0).getLongName(), is("sampleclass.h"));
    assertThat(capturedFiles.get(3).getLongName(), is("sampleclass.cpp"));
    assertThat(capturedFiles.get(6).getLongName(), is("sampleclass.cpp"));
    assertThat(capturedFiles.get(9).getLongName(), is("sampleclass.h"));
    assertThat(capturedFiles.get(12).getLongName(), is("sampleclass.h"));
    assertThat(capturedFiles.get(15).getLongName(), is("sampleclass.cpp"));
    
  }
}
