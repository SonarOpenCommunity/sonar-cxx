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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.cxx.TestUtils;

public class CxxCoverageSensorTest {
  private CxxCoverageSensor sensor;
  private SensorContext context;
  private Project project;
  private ModuleFileSystem fs;
  private ProjectReactor reactor;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    fs = TestUtils.mockFileSystem();
    reactor = TestUtils.mockReactor();
    context = mock(SensorContext.class);
    File resourceMock = mock(File.class);
    when(context.getResource((File) anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectCoverageOnUnitTestCoverage() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    
    sensor = new CxxCoverageSensor(settings, fs, reactor);

    sensor.analyse(project, context);
    verify(context, times(33)).saveMeasure((File) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    
    sensor = new CxxCoverageSensor(settings, fs, reactor);

    sensor.analyse(project, context);
    verify(context, times(99)).saveMeasure((File) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportNoCoverageSaved() {
    sensor = new CxxCoverageSensor(new Settings(), fs, reactor);
    when(context.getResource((File) anyObject())).thenReturn(null);
    sensor.analyse(project, context);
    verify(context, times(0)).saveMeasure((File) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");
    sensor = new CxxCoverageSensor(settings, fs, reactor);

    sensor.analyse(project, context);
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesEmpty() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-cobertura-empty.xml");
    sensor = new CxxCoverageSensor(settings, fs, reactor);

    sensor.analyse(project, context);

    verify(context, times(0)).saveMeasure((File) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesInvalid() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-invalid.xml");
    sensor = new CxxCoverageSensor(settings, fs, reactor);

    sensor.analyse(project, context);

    verify(context, times(0)).saveMeasure((File) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportCoverageWhenVisualStudioCase() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio.xml");
    sensor = new CxxCoverageSensor(settings, fs, reactor);

    sensor.analyse(project, context);

    verify(context, times(0)).saveMeasure((File) anyObject(), any(Measure.class));
  }
}

