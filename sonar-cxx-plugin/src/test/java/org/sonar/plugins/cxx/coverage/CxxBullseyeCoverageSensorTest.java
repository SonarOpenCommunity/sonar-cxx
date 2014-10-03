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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CxxBullseyeCoverageSensorTest {
  private CxxCoverageSensor sensor;
  private SensorContext context;
  private Project project;
  private ModuleFileSystem fs;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    fs = TestUtils.mockFileSystem();
    context = mock(SensorContext.class);
    File resourceMock = mock(File.class);
    when(context.getResource((File) anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectCoverage() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/coverage-result-bullseye.xml");
    settings.setProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY, "coverage-reports/bullseye/coverage-result-bullseye.xml");
    settings.setProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY, "coverage-reports/bullseye/coverage-result-bullseye.xml");
    sensor = new CxxCoverageSensor(settings, fs);
    
    sensor.analyse(project, context);
    verify(context, times(90)).saveMeasure((File) anyObject(), any(Measure.class));
  }
  
  @Test
  public void shoulParseTopLevelFiles() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node.xml");
    sensor = new CxxCoverageSensor(settings, fs);
    
    sensor.analyse(project, context);
    verify(context, times(94)).saveMeasure((File) anyObject(), any(Measure.class));
  }
  
    @Test
  public void shoulCorrectlyHandleDriveLettersWithoutSlash() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash.xml");
    sensor = new CxxCoverageSensor(settings, fs);
    
    sensor.analyse(project, context);
    verify(context, times(94)).saveMeasure((File) anyObject(), any(Measure.class));
  }

}
