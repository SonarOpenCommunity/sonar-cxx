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
package org.sonar.cxx.sensors.tests.xunit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxXunitSensor.class);
  public static final String REPORT_PATH_KEY = "xunit.reportPath";
  public static final String KEY = "Xunit";
  public static final String XSLT_URL_KEY = "xunit.xsltURL";
  private static final double PERCENT_BASE = 100D;

  private String xsltURL;

  /**
   * CxxXunitSensor
   * @param language for C or C++
   */
  public CxxXunitSensor(CxxLanguage language) {
    super(language);
    if (language.getStringOption(XSLT_URL_KEY).isPresent()) {
      xsltURL = language.getStringOption(XSLT_URL_KEY).orElse("xunit-report.xslt");
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " XunitSensor")
      //.onlyOnLanguage(this.language.getKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {
    String moduleKey = context.config().get("sonar.moduleKey").orElse(null);
    if (moduleKey != null) {
      LOG.debug("Runs unit test import sensor only at top level project skip : Module Key = '{}'", moduleKey);
      return;
    }

    LOG.debug("Root module imports test metrics: Module Key = '{}'", context.module());

    try {
      List<File> reports = getReports(context.config(), context.fileSystem().baseDir(), getReportPathKey());
      if (!reports.isEmpty()) {
        XunitReportParser parserHandler = parseReport(reports);
        List<TestCase> testcases = parserHandler.getTestCases();

        LOG.info("Parsing 'xUnit' format");
        simpleMode(context, testcases);
      } else {
        LOG.debug("No reports found, nothing to process");
      }
    } catch (IOException | TransformerException | XMLStreamException e) {
      String msg = new StringBuilder()
        .append("Cannot feed the data into SonarQube, details: '")
        .append(e)
        .append("'")
        .toString();
      LOG.error(msg);
      CxxUtils.validateRecovery(e, this.language);
    }
  }

  /**
   * @param reports
   * @return
   * @throws XMLStreamException
   * @throws IOException
   * @throws TransformerException
   */
  private XunitReportParser parseReport(List<File> reports)
    throws XMLStreamException, IOException, TransformerException {
    XunitReportParser parserHandler = new XunitReportParser();
    StaxParser parser = new StaxParser(parserHandler, false);
    for (File report : reports) {
      LOG.info("Processing report '{}'", report);
      try {
        parser.parse(transformReport(report));
      } catch (EmptyReportException e) {
        LOG.warn("The report '{}' seems to be empty, ignoring.", report);
        if (LOG.isDebugEnabled()) {
          LOG.debug("{}", e);
        }
      }
    }
    return parserHandler;
  }

  private void simpleMode(final SensorContext context, List<TestCase> testcases) {

    int testsCount = 0;
    int testsSkipped = 0;
    int testsErrors = 0;
    int testsFailures = 0;
    long testsTime = 0;
    for (TestCase tc : testcases) {
      if (tc.isSkipped()) {
        testsSkipped++;
      } else if (tc.isFailure()) {
        testsFailures++;
      } else if (tc.isError()) {
        testsErrors++;
      }
      testsCount++;
      testsTime += tc.getTime();
    }
    testsCount -= testsSkipped;

    if (testsCount > 0) {
      double testsPassed = (double) testsCount - testsErrors - testsFailures;
      double successDensity = testsPassed * PERCENT_BASE / testsCount;

      try {
        context.<Integer>newMeasure()
          .forMetric(CoreMetrics.TESTS)
          .on(context.module())
          .withValue(testsCount)
          .save();
      } catch (IllegalArgumentException ex) {
        LOG.error("Cannot save measure TESTS : '{}', ignoring measure", ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }

      try {
        context.<Integer>newMeasure()
          .forMetric(CoreMetrics.TEST_ERRORS)
          .on(context.module())
          .withValue(testsErrors)
          .save();
      } catch (IllegalArgumentException ex) {
        LOG.error("Cannot save measure TEST_ERRORS : '{}', ignoring measure", ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }

      try {
        context.<Integer>newMeasure()
          .forMetric(CoreMetrics.TEST_FAILURES)
          .on(context.module())
          .withValue(testsFailures)
          .save();
      } catch (IllegalArgumentException ex) {
        LOG.error("Cannot save measure TEST_FAILURES : '{}', ignoring measure", ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }

      try {
        context.<Integer>newMeasure()
          .forMetric(CoreMetrics.SKIPPED_TESTS)
          .on(context.module())
          .withValue(testsSkipped)
          .save();
      } catch (IllegalArgumentException ex) {
        LOG.error("Cannot save measure SKIPPED_TESTS : '{}', ignoring measure", ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }

      try {
        context.<Double>newMeasure()
          .forMetric(CoreMetrics.TEST_SUCCESS_DENSITY)
          .on(context.module())
          .withValue(ParsingUtils.scaleValue(successDensity))
          .save();
      } catch (IllegalArgumentException ex) {
        LOG.error("Cannot save measure TEST_SUCCESS_DENSITY : '{}', ignoring measure", ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }

      try {
        context.<Long>newMeasure()
          .forMetric(CoreMetrics.TEST_EXECUTION_TIME)
          .on(context.module())
          .withValue(testsTime)
          .save();
      } catch (IllegalArgumentException ex) {
        LOG.error("Cannot save measure TEST_EXECUTION_TIME : '{}', ignoring measure", ex.getMessage());
        CxxUtils.validateRecovery(ex, this.language);
      }
    } else {
      LOG.debug("The reports contain no testcases");
    }
  }

  File transformReport(File report)
    throws java.io.IOException, javax.xml.transform.TransformerException {
    File transformed = report;
    if (xsltURL != null && report.length() > 0) {
      LOG.debug("Transforming the report using xslt '{}'", xsltURL);
      InputStream inputStream = this.getClass().getResourceAsStream("/xsl/" + xsltURL);
      if (inputStream == null) {
        LOG.debug("Transforming: try to access external XSLT via URL");
        URL url = new URL(xsltURL);
        inputStream = url.openStream();
      }

      Source xsl = new StreamSource(inputStream);
      transformed = new File(report.getAbsolutePath() + ".after_xslt");
      CxxUtils.transformFile(xsl, report, transformed);
    } else {
      LOG.debug("Transformation skipped: no xslt given");
    }

    return transformed;
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }

  @Override
  protected Optional<CxxMetricsFactory.Key> getMetricKey() {
    return Optional.empty();
  }
}
