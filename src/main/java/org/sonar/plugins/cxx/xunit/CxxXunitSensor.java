/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.xunit;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractCoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxSensor;

/**
 * Copied from sonar-surefire-plugin with modifications: JavaFile replaced by C++ getUnitTestResource use Project to locate C++ File
 * getReports use FileSetManager for smarter report select using new now plugin configuration use Fileset (ex ** /TEST*.xml)
 *
 */

public class CxxXunitSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPath";
  public static final String XSLT_URL_KEY = "sonar.cxx.xunit.xsltURL";
  private static final String DEFAULT_REPORT_PATH = "xunit-reports/xunit-result-*.xml";
  private static Logger logger = LoggerFactory.getLogger(CxxXunitSensor.class);

  private String xsltURL = null;
  private Configuration conf = null;

  public CxxXunitSensor(Configuration conf) {
    this.conf = conf;
    xsltURL = conf.getString(XSLT_URL_KEY);
  }

  @DependsUpon
  public Class<?> dependsUponCoverageSensors() {
    return AbstractCoverageExtension.class;
  }

  public void analyse(Project project, SensorContext context) {
    File[] reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                REPORT_PATH_KEY, DEFAULT_REPORT_PATH);

    if (reports.length == 0) {
      insertZeroWhenNoReports(project, context);
    } else {
      transformReport(project, reports, context);
      parseReport(project, reports, context);
    }
  }

  private void insertZeroWhenNoReports(Project pom, SensorContext context) {
    if ( !StringUtils.equalsIgnoreCase("pom", pom.getPackaging())) {
      context.saveMeasure(CoreMetrics.TESTS, 0.0);
    }
  }

  private void transformReport(Project project, File[] reports, SensorContext context) {

    try {
      if (this.xsltURL != null) {
        URL url = new URL(this.xsltURL);
        logger.debug("xslt used :" + this.xsltURL);
        Source xsl = new StreamSource(url.openStream());
        for (int i = 0; i < reports.length; i++) {
          Source source = new StreamSource(reports[i]);

          File fileOutPut = new File(reports[i].getAbsolutePath() + ".after_xslt");

          Result result = new StreamResult(fileOutPut);

          TransformerFactory factory = TransformerFactory.newInstance();
          Templates template = factory.newTemplates(xsl);

          Transformer xformer = template.newTransformer();
          xformer.setOutputProperty(OutputKeys.INDENT, "yes");
          xformer.transform(source, result);
          reports[i] = fileOutPut;
        }
      } else {
        logger.debug("No xslt.");
      }
    } catch (MalformedURLException e) {
      throw new XmlParserException("Url doesn't existe", e);
    } catch (FileNotFoundException e) {
      throw new XmlParserException("Url doesn't existe", e);
    } catch (UnknownHostException e) {
      throw new XmlParserException("UnknownHostException : " + this.xsltURL, e);
    } catch (Exception e) {
      throw new XmlParserException("Can not parse xunit reports", e);
    }
  }

  private void parseReport(Project project, File[] reports, SensorContext context) {
    Set<TestSuiteReport> analyzedReports = new HashSet<TestSuiteReport>();
    for (File report : reports) {
      try {
        logger.info("parsing xunit report '{}'", report);
        TestSuiteParser parserHandler = new TestSuiteParser();
        StaxParser parser = new StaxParser(parserHandler, false);
        parser.parse(report);

        for (TestSuiteReport fileReport : parserHandler.getParsedReports()) {
          if ( !fileReport.isValid() || analyzedReports.contains(fileReport)) {
            continue;
          }
          if (fileReport.getTests() > 0) {
            double testsCount = fileReport.getTests() - fileReport.getSkipped();
            saveClassMeasure(project, context, fileReport, CoreMetrics.SKIPPED_TESTS, fileReport.getSkipped());
            saveClassMeasure(project, context, fileReport, CoreMetrics.TESTS, testsCount);
            saveClassMeasure(project, context, fileReport, CoreMetrics.TEST_ERRORS, fileReport.getErrors());
            saveClassMeasure(project, context, fileReport, CoreMetrics.TEST_FAILURES, fileReport.getFailures());
            saveClassMeasure(project, context, fileReport, CoreMetrics.TEST_EXECUTION_TIME, fileReport.getTimeMS());
            double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
            if (testsCount > 0) {
              double percentage = passedTests * 100d / testsCount;
              saveClassMeasure(project, context, fileReport, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
            }
            saveTestsDetails(project, context, fileReport);
            analyzedReports.add(fileReport);
          }
        }
      } catch (Exception e) {
        logger.warn("Can't parse, skipped.");
      }
    }
  }

  private void saveTestsDetails(Project project, SensorContext context, TestSuiteReport fileReport) throws TransformerException {
    StringBuilder testCaseDetails = new StringBuilder(256);
    testCaseDetails.append("<tests-details>");
    List<TestCaseDetails> details = fileReport.getDetails();
    for (TestCaseDetails detail : details) {
      testCaseDetails.append("<testcase status=\"").append(detail.getStatus()).append("\" time=\"").append(detail.getTimeMS())
          .append("\" name=\"").append(detail.getName()).append("\"");
      boolean isError = detail.getStatus().equals(TestCaseDetails.STATUS_ERROR);
      if (isError || detail.getStatus().equals(TestCaseDetails.STATUS_FAILURE)) {
        testCaseDetails.append(">").append(isError ? "<error message=\"" : "<failure message=\"")
            .append(StringEscapeUtils.escapeXml(detail.getErrorMessage())).append("\">").append("<![CDATA[")
            .append(StringEscapeUtils.escapeXml(detail.getStackTrace())).append("]]>").append(isError ? "</error>" : "</failure>")
            .append("</testcase>");
      } else {
        testCaseDetails.append("/>");
      }
    }
    testCaseDetails.append("</tests-details>");
    context.saveMeasure(getUnitTestResource(project, fileReport), new Measure(CoreMetrics.TEST_DATA, testCaseDetails.toString()));
  }

  private void saveClassMeasure(Project project, SensorContext context, TestSuiteReport fileReport, Metric metric, double value) {
    if ( !Double.isNaN(value)) {
      context.saveMeasure(getUnitTestResource(project, fileReport), metric, value);
    }
  }

  private Resource<?> getUnitTestResource(Project project, TestSuiteReport fileReport) {
    logger.debug("Unit Test Resource key = {}", fileReport.getClassKey());
    return org.sonar.api.resources.File.fromIOFile(new File(fileReport.getClassKey()),
                                                   project);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
