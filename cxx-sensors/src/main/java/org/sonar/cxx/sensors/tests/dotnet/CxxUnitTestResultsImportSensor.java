/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
    String category = "CXX External Analyzers";
    String subcategory = "Unit Test";
    String NOTE
             = " Note that while measures such as the number of tests are displayed at project level,"
                 + " no drilldown is available.\n";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(UnitTestConfiguration.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("Visual Studio Test Reports Paths")
        .description(
          "Paths to Visual Studio Test Reports. Multiple paths may be comma-delimited, or included via wildcards."
            + NOTE
            + "Example: \"report.trx\", \"report1.trx,report2.trx\" or \"C:/report.trx\"")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(UnitTestConfiguration.XUNIT_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("xUnit Test Reports Paths")
        .description(
          "Paths to xUnit execution reports. Multiple paths may be comma-delimited, or included via wildcards."
            + NOTE
            + "Example: \"report.xml\", \"report1.xml,report2.xml\" or \"C:/report.xml\"")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(UnitTestConfiguration.NUNIT_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("NUnit Test Reports Paths")
        .description(
          "Paths to NUnit execution reports. Multiple paths may be comma-delimited, or included via wildcards."
            + NOTE
            + "Example: \"TestResult.xml\", \"TestResult1.xml,TestResult2.xml\" or \"C:/TestResult.xml\"")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX VSTest/xUnit/NUnit Test report import")
      .onlyWhenConfiguration(conf -> new UnitTestConfiguration(conf).hasUnitTestResultsProperty())
      .onlyOnLanguage("cxx");
  }

  @Override
  public void execute(SensorContext context) {
    this.context = context;
    analyze(new UnitTestResults(), new UnitTestConfiguration(context.config()));
  }

  public void analyze(UnitTestResults unitTestResults, UnitTestConfiguration unitTestConf) {
    UnitTestResults aggregatedResults = unitTestResultsAggregator.aggregate(wildcardPatternFileProvider,
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
