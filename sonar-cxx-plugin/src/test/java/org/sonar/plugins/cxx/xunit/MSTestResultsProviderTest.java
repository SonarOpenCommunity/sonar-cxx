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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.xunit.MSTestResultsProvider.MSTestResultsAggregator;
import org.sonar.plugins.cxx.xunit.MSTestResultsProvider.MSTestResultsImportSensor;
import org.sonar.plugins.dotnet.tests.UnitTestResults;
import org.sonar.plugins.dotnet.tests.UnitTestResultsImportSensor;

import com.google.common.collect.ImmutableList;

public class MSTestResultsProviderTest {
  private Project project;
  private DefaultFileSystem fs;
  private Settings config;
  private SensorContext context;  
  private MSTestResultsAggregator resultsAggregator;
  private MSTestResultsImportSensor sensor;

  @Before
  public void setUp() {
    fs = new DefaultFileSystem();
    project = TestUtils.mockProject();
    context = mock(SensorContext.class);
    File resourceMock = mock(File.class);
    when(context.getResource((File) anyObject())).thenReturn(resourceMock);
    }

  @Test
  public void should_execute_on_project() {

    resultsAggregator = mock(MSTestResultsAggregator.class);

    when(resultsAggregator.hasUnitTestResultsProperty()).thenReturn(true);
    assertThat(new UnitTestResultsImportSensor(resultsAggregator).shouldExecuteOnProject(project)).isTrue();

    when(resultsAggregator.hasUnitTestResultsProperty()).thenReturn(false);
    assertThat(new UnitTestResultsImportSensor(resultsAggregator).shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void should_not_analyze_on_reactor_project() {
    when(project.isRoot()).thenReturn(true);
    when(project.getModules()).thenReturn(ImmutableList.of(mock(Project.class)));

    resultsAggregator = mock(MSTestResultsAggregator.class);
    sensor = new MSTestResultsImportSensor(resultsAggregator);
    sensor.analyse(project, context);

    verify(context, Mockito.never()).saveMeasure(Mockito.any(Metric.class), Mockito.anyDouble());
  }

  @Test
  public void should_analyze_on_multi_module_modules() {
    when(project.isRoot()).thenReturn(false);

    resultsAggregator = mock(MSTestResultsAggregator.class);
    
    UnitTestResults results = mock(UnitTestResults.class);
    when(results.tests()).thenReturn(1.0);
    when(results.passedPercentage()).thenCallRealMethod();
    when(results.skipped()).thenReturn(0.0);
    when(results.failed()).thenReturn(1.0);
    when(results.errors()).thenReturn(0.0);
    
    when(resultsAggregator.aggregate(Mockito.any(UnitTestResults.class))).thenReturn(results);
    sensor = new MSTestResultsImportSensor(resultsAggregator);
    sensor.analyse(project, context);

    verify(context, Mockito.atLeastOnce()).saveMeasure(Mockito.any(Metric.class), Mockito.anyDouble());
  }

//  @Test
//  public void testMSTestResult() {
//    config = new Settings();
//    config.setProperty(MSTestResultsProvider.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY, "./vs-test-report/VS-Results.xml");
//
//    resultsAggregator = new MSTestResultsAggregator(config);
//    sensor = new MSTestResultsImportSensor(resultsAggregator);
//    sensor.analyse(project, context);
//    
//    verify(context, times(0)).saveMeasure(eq(CoreMetrics.TESTS), any(Double.class));
//    
//    verify(resultsAggregator).aggregate(results);
//    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.TESTS), eq(1.0));
//    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.TEST_ERRORS), eq(1.0));
//
//    when(resultsAggregator.hasUnitTestResultsProperty()).thenReturn(true);
//    assertThat(new sensor(unitTestResultsAggregator).shouldExecuteOnProject(project)).isTrue();
//
//    when(resultsAggregator.hasUnitTestResultsProperty()).thenReturn(false);
//    assertThat(new sensor(unitTestResultsAggregator).shouldExecuteOnProject(project)).isFalse();
//  }

}

