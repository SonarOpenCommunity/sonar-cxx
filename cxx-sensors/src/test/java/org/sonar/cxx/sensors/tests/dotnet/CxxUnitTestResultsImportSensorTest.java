/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.cppcheck.CxxCppCheckSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxUnitTestResultsImportSensorTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private CxxLanguage language;

  @Before
  public void setUp() {
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxCppCheckSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxCppCheckSensor.REPORT_PATH_KEY);
    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.TRUE));
  }

  @Test
  public void coverage() {
    CxxUnitTestResultsAggregator unitTestResultsAggregator = mock(CxxUnitTestResultsAggregator.class);
    new CxxUnitTestResultsImportSensor(unitTestResultsAggregator, ProjectDefinition.create(), language)
      .describe(new DefaultSensorDescriptor());
    SensorContext sensorContext = mock(SensorContext.class);
    new CxxUnitTestResultsImportSensor(unitTestResultsAggregator, ProjectDefinition.create(), language)
      .execute(sensorContext);
    verifyZeroInteractions(sensorContext);
    when(unitTestResultsAggregator.hasUnitTestResultsProperty()).thenReturn(true);
    ProjectDefinition sub = ProjectDefinition.create();
    ProjectDefinition.create().addSubProject(sub);
    new CxxUnitTestResultsImportSensor(unitTestResultsAggregator, sub, language)
      .execute(sensorContext);
    verifyZeroInteractions(sensorContext);
  }

  @Test
  public void analyze() throws Exception {
    UnitTestResults results = mock(UnitTestResults.class);
    when(results.tests()).thenReturn(42);
    when(results.passedPercentage()).thenReturn(84d);
    when(results.skipped()).thenReturn(1);
    when(results.failures()).thenReturn(2);
    when(results.errors()).thenReturn(3);
    when(results.executionTime()).thenReturn(321L);

    CxxUnitTestResultsAggregator unitTestResultsAggregator = mock(CxxUnitTestResultsAggregator.class);
    SensorContextTester context = SensorContextTester.create(temp.newFolder());

    when(unitTestResultsAggregator.aggregate(Mockito.any(WildcardPatternFileProvider.class), Mockito.any(UnitTestResults.class)))
      .thenReturn(results);

    new CxxUnitTestResultsImportSensor(unitTestResultsAggregator, ProjectDefinition.create(), language)
      .analyze(context, results);

    verify(unitTestResultsAggregator).aggregate(Mockito.any(WildcardPatternFileProvider.class), Mockito.eq(results));

    assertThat(context.measures("projectKey"))
      .extracting("metric.key", "value")
      .containsOnly(
        tuple(CoreMetrics.TESTS_KEY, 42),
        tuple(CoreMetrics.SKIPPED_TESTS_KEY, 1),
        tuple(CoreMetrics.TEST_FAILURES_KEY, 2),
        tuple(CoreMetrics.TEST_ERRORS_KEY, 3),
        tuple(CoreMetrics.TEST_EXECUTION_TIME_KEY, 321L));
  }

  @Test
  public void should_not_save_metrics_with_empty_results() throws Exception {
    SensorContextTester context = SensorContextTester.create(temp.newFolder());

    CxxUnitTestResultsAggregator unitTestResultsAggregator = mock(CxxUnitTestResultsAggregator.class);
    UnitTestResults results = mock(UnitTestResults.class);
    when(results.tests()).thenReturn(0);
    when(results.skipped()).thenReturn(1);
    when(results.failures()).thenReturn(2);
    when(results.errors()).thenReturn(3);
    when(results.executionTime()).thenReturn(null);
    when(unitTestResultsAggregator.aggregate(Mockito.any(WildcardPatternFileProvider.class), Mockito.any(UnitTestResults.class))).thenReturn(results);

    new CxxUnitTestResultsImportSensor(unitTestResultsAggregator, ProjectDefinition.create(), language)
      .analyze(context, results);

    verify(unitTestResultsAggregator).aggregate(Mockito.any(WildcardPatternFileProvider.class), Mockito.eq(results));

    assertThat(context.measures("projectKey"))
      .extracting("metric.key", "value")
      .containsOnly(
        tuple(CoreMetrics.TESTS_KEY, 0),
        tuple(CoreMetrics.SKIPPED_TESTS_KEY, 1),
        tuple(CoreMetrics.TEST_FAILURES_KEY, 2),
        tuple(CoreMetrics.TEST_ERRORS_KEY, 3));
  }

}
