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

import java.io.File;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.CxxLanguage;

public class CxxUnitTestResultsImportSensor implements Sensor {

  private final WildcardPatternFileProvider wildcardPatternFileProvider
    = new WildcardPatternFileProvider(new File("."), File.separator);
  private final CxxUnitTestResultsAggregator unitTestResultsAggregator;
  protected final CxxLanguage language;

  public CxxUnitTestResultsImportSensor(CxxUnitTestResultsAggregator unitTestResultsAggregator,
    CxxLanguage language) {
    this.unitTestResultsAggregator = unitTestResultsAggregator;
    this.language = language;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    String name = String.format("%s Unit Test Results Import", this.language.getName());
    descriptor.name(name);
    descriptor.global();
    descriptor.onlyWhenConfiguration(conf -> new UnitTestConfiguration(language, conf).hasUnitTestResultsProperty());
    descriptor.onlyOnLanguage(this.language.getKey());
  }

  @Override
  public void execute(SensorContext context) {
    analyze(context, new UnitTestResults(), new UnitTestConfiguration(language, context.config()));
  }

  void analyze(SensorContext context, UnitTestResults unitTestResults, UnitTestConfiguration unitTestConf) {
    UnitTestResults aggregatedResults = unitTestResultsAggregator.aggregate(wildcardPatternFileProvider,
        unitTestResults, unitTestConf);

    context.<Integer>newMeasure()
      .forMetric(CoreMetrics.TESTS)
      .on(context.module())
      .withValue(aggregatedResults.tests())
      .save();
    context.<Integer>newMeasure()
      .forMetric(CoreMetrics.TEST_ERRORS)
      .on(context.module())
      .withValue(aggregatedResults.errors())
      .save();
    context.<Integer>newMeasure()
      .forMetric(CoreMetrics.TEST_FAILURES)
      .on(context.module())
      .withValue(aggregatedResults.failures())
      .save();
    context.<Integer>newMeasure()
      .forMetric(CoreMetrics.SKIPPED_TESTS)
      .on(context.module())
      .withValue(aggregatedResults.skipped())
      .save();

    Long executionTime = aggregatedResults.executionTime();
    if (executionTime != null) {
      context.<Long>newMeasure()
        .forMetric(CoreMetrics.TEST_EXECUTION_TIME)
        .on(context.module())
        .withValue(executionTime)
        .save();
    }
  }

}
