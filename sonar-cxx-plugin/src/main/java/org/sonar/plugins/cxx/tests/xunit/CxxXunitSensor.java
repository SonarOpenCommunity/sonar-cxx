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
package org.sonar.plugins.cxx.tests.xunit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.EmptyReportException;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.CxxPlugin;
import static org.sonar.plugins.cxx.coverage.CxxCoverageSensor.LOG;
import org.sonar.plugins.cxx.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {
  public static final Logger LOG = Loggers.get(CxxXunitSensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPath";
  public static final String XSLT_URL_KEY = "sonar.cxx.xunit.xsltURL";
  private static final double PERCENT_BASE = 100d;

  private String xsltURL = null;

  static Pattern classNameOnlyMatchingPattern = Pattern.compile("(?:\\w*::)*?(\\w+?)::\\w+?:\\d+$");
  static Pattern qualClassNameMatchingPattern = Pattern.compile("((?:\\w*::)*?(\\w+?))::\\w+?:\\d+$");

  /**
   * {@inheritDoc}
   */
  public CxxXunitSensor(Settings settings) {
    super(settings, null);
    xsltURL = settings.getString(XSLT_URL_KEY);
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("CxxXunitSensor");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {    
    String moduleKey = context.settings().getString("sonar.moduleKey");
    if (moduleKey != null) {
        LOG.debug("Runs unit test import sensor only at top level project skip : Module Key = '{}'", moduleKey);
        return;        
    }
    
    LOG.debug("Root module imports test metrics: Module Key = '{}'", context.module());    
    
    try {
      List<File> reports = getReports(settings, context.fileSystem().baseDir(), REPORT_PATH_KEY);
      if (!reports.isEmpty()) {
        XunitReportParser parserHandler = new XunitReportParser();
        StaxParser parser = new StaxParser(parserHandler, false);
        for (File report : reports) {
          LOG.info("Processing report '{}'", report);
          try {
            parser.parse(transformReport(report));
          } catch (EmptyReportException e) {
            LOG.warn("The report '{}' seems to be empty, ignoring.", report);
          }
        }
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
      throw new IllegalStateException(msg, e);
    }
  }

  private void simpleMode(final SensorContext context, List<TestCase> testcases)
    throws javax.xml.stream.XMLStreamException,
    java.io.IOException,
    javax.xml.transform.TransformerException {
        
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
      double testsPassed = testsCount - testsErrors - testsFailures;
      double successDensity = testsPassed * PERCENT_BASE / testsCount;

      try
      {
        context.<Integer>newMeasure()
           .forMetric(CoreMetrics.TESTS)
           .on(context.module())
           .withValue(testsCount)
           .save();
      } catch(Exception ex) {
        LOG.error("Cannot save measure Test : '{}', ignoring measure", ex.getMessage());
        if (!settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY)) {
          LOG.info("Recovery is disabled, failing analysis.");
          throw ex;
        }
      }       

      try
      {
       context.<Integer>newMeasure()
         .forMetric(CoreMetrics.TEST_ERRORS)
         .on(context.module())
         .withValue(testsErrors)
         .save();
      } catch(Exception ex) {
        LOG.error("Cannot save measure Test : '{}', ignoring measure", ex.getMessage());
        if (!settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY)) {
          LOG.info("Recovery is disabled, failing analysis.");
          throw ex;
        }
      } 
      
      try
      {
       context.<Integer>newMeasure()
         .forMetric(CoreMetrics.TEST_FAILURES)
         .on(context.module())
         .withValue(testsFailures)
         .save();
      } catch(Exception ex) {
        LOG.error("Cannot save measure Test : '{}', ignoring measure", ex.getMessage());
        if (!settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY)) {
          LOG.info("Recovery is disabled, failing analysis.");
          throw ex;
        }
      } 
      
      try
      {
       context.<Integer>newMeasure()
         .forMetric(CoreMetrics.SKIPPED_TESTS)
         .on(context.module())
         .withValue(testsSkipped)
         .save();
      } catch(Exception ex) {
        LOG.error("Cannot save measure Test : '{}', ignoring measure", ex.getMessage());
        if (!settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY)) {
          LOG.info("Recovery is disabled, failing analysis.");
          throw ex;
        }
      } 

      try
      {
       context.<Double>newMeasure()
         .forMetric(CoreMetrics.TEST_SUCCESS_DENSITY)
         .on(context.module())
         .withValue(ParsingUtils.scaleValue(successDensity))
         .save();
      } catch(Exception ex) {
        LOG.error("Cannot save measure Test : '{}', ignoring measure", ex.getMessage());
        if (!settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY)) {
          LOG.info("Recovery is disabled, failing analysis.");
          throw ex;
        }
      }       

      try
      {
        context.<Long>newMeasure()
         .forMetric(CoreMetrics.TEST_EXECUTION_TIME)
         .on(context.module())
         .withValue(testsTime)
         .save();
      } catch(Exception ex) {
        LOG.error("Cannot save measure Test : '{}', ignoring measure", ex.getMessage());
        if (!settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY)) {
          LOG.info("Recovery is disabled, failing analysis.");
          throw ex;
        }
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
      TransformerFactory factory = TransformerFactory.newInstance();
      Templates template = factory.newTemplates(xsl);
      Transformer xformer = template.newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new StreamSource(report);
      transformed = new File(report.getAbsolutePath() + ".after_xslt");
      Result result = new StreamResult(transformed);
      xformer.transform(source, result);
    } else {
      LOG.debug("Transformation skipped: no xslt given");
    }

    return transformed;
  }  
}
