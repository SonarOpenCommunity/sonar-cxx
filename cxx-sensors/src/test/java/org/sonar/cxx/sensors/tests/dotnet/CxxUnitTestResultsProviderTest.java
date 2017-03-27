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
package org.sonar.cxx.sensors.tests.dotnet;

import static org.mockito.Mockito.mock;

import static org.fest.assertions.Assertions.assertThat;
import org.junit.Before;

import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.tests.dotnet.CxxUnitTestResultsProvider.CxxUnitTestResultsAggregator;
import org.sonar.cxx.sensors.tests.dotnet.CxxUnitTestResultsProvider.CxxUnitTestResultsImportSensor;
import org.sonar.plugins.dotnet.tests.UnitTestResults;
import org.sonar.plugins.dotnet.tests.WildcardPatternFileProvider;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxUnitTestResultsProviderTest {

  private CxxUnitTestResultsAggregator resultsAggregator;
  private FileSystem fs;
  
  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }
  
  // @Test @todo reactor
  public void should_not_analyze_on_reactor_project() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    ProjectDefinition projectDef = mock(ProjectDefinition.class);

    //when(project.isRoot()).thenReturn(false);
    //when(project.getModules()).thenReturn(new ArrayList<>(Collections.singletonList(mock(Project.class))));

    resultsAggregator = mock(CxxUnitTestResultsAggregator.class);
    CxxLanguage language = TestUtils.mockCxxLanguage();    
    CxxUnitTestResultsImportSensor sensor = new CxxUnitTestResultsImportSensor(resultsAggregator, projectDef, language);
    sensor.execute(context);

    assertThat(context.measures(context.module().key())).hasSize(0);
  }

  // @Test @todo reactor
  public void should_analyze_on_multi_module_modules() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    ProjectDefinition projectDef = mock(ProjectDefinition.class);
    
    resultsAggregator = mock(CxxUnitTestResultsAggregator.class);

    UnitTestResults results = mock(UnitTestResults.class);
    when(results.tests()).thenReturn(1);
    when(results.passedPercentage()).thenCallRealMethod();
    when(results.skipped()).thenReturn(0);
    when(results.failures()).thenReturn(1);
    when(results.errors()).thenReturn(0);

    when(resultsAggregator.aggregate(Mockito.any(WildcardPatternFileProvider.class), Mockito.any(UnitTestResults.class))).thenReturn(results);
    
    CxxLanguage language = TestUtils.mockCxxLanguage();    
    CxxUnitTestResultsImportSensor sensor = new CxxUnitTestResultsImportSensor(resultsAggregator, projectDef, language);
    sensor.execute(context);

    assertThat(context.measures(context.module().key())).isNotEmpty();
  }
}
