/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.scanner.sensor.ProjectSensor;

public class CxxUnitTestResultsImportSensor implements ProjectSensor {

  private final WildcardPatternFileProvider wildcardPatternFileProvider
                                              = new WildcardPatternFileProvider(new File("."), File.separator);
  private final CxxUnitTestResultsAggregator unitTestResultsAggregator;
  private SensorContext context;

  public CxxUnitTestResultsImportSensor(CxxUnitTestResultsAggregator unitTestResultsAggregator) {
    this.unitTestResultsAggregator = unitTestResultsAggregator;
  }

  public static List<PropertyDefinition> properties() {
    var category = "CXX External Analyzers";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(UnitTestConfiguration.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("VSTest Report(s)")
        .description(
          "Paths to VSTest reports. Multiple paths may be comma-delimited, or included via wildcards."
            + " Note that while measures such as the number of tests are displayed at project level, no drilldown"
            + " is available."
            + " In the SonarQube UI, enter one entry per field."
        )
        .category(category)
        .subCategory("Visual C++")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(UnitTestConfiguration.NUNIT_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("NUnit Report(s)")
        .description(
          "Paths to NUnit execution reports. Multiple paths may be comma-delimited, or included via wildcards."
            + " Note that while measures such as the number of tests are displayed at project level, no drilldown"
            + " is available."
            + " In the SonarQube UI, enter one entry per field."
        )
        .category(category)
        .subCategory("NUnit")
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX VSTest/NUnit Test report import")
      .onlyWhenConfiguration(conf -> new UnitTestConfiguration(conf).hasUnitTestResultsProperty())
      .onlyOnLanguages("cxx", "cpp", "c++", "c");
  }

  @Override
  public void execute(SensorContext context) {
    this.context = context;
    analyze(new UnitTestResults(), new UnitTestConfiguration(context.config()));
  }

  public void analyze(UnitTestResults unitTestResults, UnitTestConfiguration unitTestConf) {
    var aggregatedResults = unitTestResultsAggregator.aggregate(wildcardPatternFileProvider,
                                                            unitTestResults, unitTestConf);
    if (aggregatedResults != null) {
      saveMetric(CoreMetrics.TESTS, aggregatedResults.tests());
      saveMetric(CoreMetrics.TEST_ERRORS, aggregatedResults.errors());
      saveMetric(CoreMetrics.TEST_FAILURES, aggregatedResults.failures());
      saveMetric(CoreMetrics.SKIPPED_TESTS, aggregatedResults.skipped());
      Long executionTime = aggregatedResults.executionTime();
      if (executionTime != null) {
        saveMetric(CoreMetrics.TEST_EXECUTION_TIME, executionTime);
      }
    }
  }

  private <T extends Serializable> void saveMetric(Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(context.project())
      .save();
  }

}
