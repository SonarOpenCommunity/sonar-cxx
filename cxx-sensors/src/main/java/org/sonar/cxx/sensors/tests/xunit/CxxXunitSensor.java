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
package org.sonar.cxx.sensors.tests.xunit;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPaths";
  private static final Logger LOG = Loggers.get(CxxXunitSensor.class);

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("xUnit Report(s)")
        .description(
          "Comma-separated list of paths to the various directories containing the *.xml xUnit report files."
            + " Each path may be absolute or relative to the project base directory. Ant patterns are accepted for"
            + " relative paths. Note that while measures such as the number of tests are displayed at project level,"
            + " no drilldown is available."
        )
        .category("CXX External Analyzers")
        .subCategory("xUnit")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX xUnit Test report import")
      //.onlyOnLanguages("cxx","cpp", "c++", "c")
      .onlyWhenConfiguration(conf -> conf.hasKey(REPORT_PATH_KEY));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void executeImpl() {
    try {
      List<File> reports = getReports(REPORT_PATH_KEY);
      if (!reports.isEmpty()) {
        XunitReportParser parserHandler = parseReport(reports);
        save(parserHandler.getTestFiles());
      } else {
        LOG.debug("No xUnit reports found, nothing to process");
      }
    } catch (XMLStreamException e) {
      CxxUtils.validateRecovery("Invalid xUnit report", e, context.config());
    }
  }

  /**
   * @param reports
   * @return
   * @throws XMLStreamException
   * @throws IOException
   */
  private XunitReportParser parseReport(List<File> reports) throws XMLStreamException {
    var parserHandler = new XunitReportParser(context.fileSystem().baseDir().getPath());
    var parser = new StaxParser(parserHandler, false);
    for (var report : reports) {
      LOG.info("Processing xUnit report '{}'", report);
      try {
        parser.parse(report);
      } catch (EmptyReportException e) {
        LOG.warn("The xUnit report '{}' seems to be empty, ignoring.", report);
      }
    }
    return parserHandler;
  }

  private void save(Collection<TestFile> testfiles) {

    var testsCount = 0;
    var testsSkipped = 0;
    var testsErrors = 0;
    var testsFailures = 0;
    long testsTime = 0;
    for (var tf : testfiles) {
      if (!tf.getFilename().isEmpty()) {
        InputFile inputFile = getInputFileIfInProject(tf.getFilename());
        if (inputFile != null) {
          if (inputFile.language() != null && inputFile.type() == Type.TEST) {
            LOG.debug("Saving xUnit data for '{}': tests={} | errors:{} | failure:{} | skipped:{} | time:{}",
                      tf.getFilename(), tf.getTests(), tf.getErrors(), tf.getFailures(), tf.getSkipped(),
                      tf.getExecutionTime());
            saveMetric(inputFile, CoreMetrics.TESTS, tf.getTests());
            saveMetric(inputFile, CoreMetrics.TEST_ERRORS, tf.getErrors());
            saveMetric(inputFile, CoreMetrics.TEST_FAILURES, tf.getFailures());
            saveMetric(inputFile, CoreMetrics.SKIPPED_TESTS, tf.getSkipped());
            saveMetric(inputFile, CoreMetrics.TEST_EXECUTION_TIME, tf.getExecutionTime());
          }
        }
      }
      testsTime += tf.getExecutionTime();
      testsCount += tf.getTests();
      testsFailures += tf.getFailures();
      testsErrors += tf.getErrors();
      testsSkipped += tf.getSkipped();
    }

    if (testsCount > 0) {
      LOG.debug("Saving xUnit report data: tests={}, errors={}, failure={}, skipped={}, time={}",
                testsCount, testsErrors, testsFailures, testsSkipped, testsTime);
      saveMetric(CoreMetrics.TESTS, testsCount);
      saveMetric(CoreMetrics.TEST_ERRORS, testsErrors);
      saveMetric(CoreMetrics.TEST_FAILURES, testsFailures);
      saveMetric(CoreMetrics.SKIPPED_TESTS, testsSkipped);
      saveMetric(CoreMetrics.TEST_EXECUTION_TIME, testsTime);
    }
  }

  private <T extends Serializable> void saveMetric(Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(context.project())
      .save();
  }

  private <T extends Serializable> void saveMetric(InputFile file, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(file)
      .save();
  }

}
