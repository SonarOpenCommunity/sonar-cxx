/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.tests.dotnet;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.tests.dotnet.CxxUnitTestResultsProvider.CxxUnitTestResultsAggregator;
import org.sonar.plugins.cxx.tests.dotnet.CxxUnitTestResultsProvider.CxxUnitTestResultsImportSensor;
import org.sonar.plugins.dotnet.tests.UnitTestResults;
import org.sonar.plugins.dotnet.tests.UnitTestResultsImportSensor;
import org.sonar.plugins.dotnet.tests.WildcardPatternFileProvider;

import com.google.common.collect.ImmutableList;

public class CxxUnitTestResultsProviderTest {


  private Project project;
  private SensorContext context;
  private CxxUnitTestResultsAggregator resultsAggregator;
  private CxxUnitTestResultsImportSensor sensor;

  @Before
  public void setUp() {
    new DefaultFileSystem(null);
    project = TestUtils.mockProject();
    context = mock(SensorContext.class);
  }

  @Test
  public void should_execute_on_project() {

    resultsAggregator = mock(CxxUnitTestResultsAggregator.class);

    when(resultsAggregator.hasUnitTestResultsProperty()).thenReturn(true);
    assertThat(new UnitTestResultsImportSensor(resultsAggregator).shouldExecuteOnProject(project)).isTrue();

    when(resultsAggregator.hasUnitTestResultsProperty()).thenReturn(false);
    assertThat(new UnitTestResultsImportSensor(resultsAggregator).shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void should_not_analyze_on_reactor_project() {
    when(project.isRoot()).thenReturn(false);
    when(project.getModules()).thenReturn(ImmutableList.of(mock(Project.class)));

    resultsAggregator = mock(CxxUnitTestResultsAggregator.class);
    sensor = new CxxUnitTestResultsImportSensor(resultsAggregator);
    sensor.analyse(project, context);

    verify(context, Mockito.never()).saveMeasure(Mockito.any(Metric.class), Mockito.anyDouble());
  }

  @Test
  public void should_analyze_on_multi_module_modules() {
    when(project.isRoot()).thenReturn(true);

    resultsAggregator = mock(CxxUnitTestResultsAggregator.class);

    UnitTestResults results = mock(UnitTestResults.class);
    when(results.tests()).thenReturn(1.0);
    when(results.passedPercentage()).thenCallRealMethod();
    when(results.skipped()).thenReturn(0.0);
    when(results.failures()).thenReturn(1.0);
    when(results.errors()).thenReturn(0.0);

    when(resultsAggregator.aggregate(Mockito.any(WildcardPatternFileProvider.class), Mockito.any(UnitTestResults.class))).thenReturn(results);
    sensor = new CxxUnitTestResultsImportSensor(resultsAggregator);
    sensor.analyse(project, context);

    verify(context, Mockito.atLeastOnce()).saveMeasure(Mockito.any(Metric.class), Mockito.anyDouble());
  }
}
