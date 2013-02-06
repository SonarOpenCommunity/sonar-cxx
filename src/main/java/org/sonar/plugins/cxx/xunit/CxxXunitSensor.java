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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPath";
  public static final String XSLT_URL_KEY = "sonar.cxx.xunit.xsltURL";
  private static final String DEFAULT_REPORT_PATH = "xunit-reports/xunit-result-*.xml";
  private final Settings settings;
  private int globalDuplicationCounter = 0;

  /**
   * {@inheritDoc}
   */
  public CxxXunitSensor(Settings settings) {
    super(settings);
    this.settings = settings;
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
          throws
          java.io.IOException,
          javax.xml.transform.TransformerException,
          javax.xml.stream.XMLStreamException {
    parseReport(project, context, transformReport(report));
  }

  @Override
  protected void handleNoReportsCase(SensorContext context) {
    context.saveMeasure(CoreMetrics.TESTS, 0.0);
  }

  File transformReport(File report)
          throws java.io.IOException, javax.xml.transform.TransformerException {
    File transformed = report;
    if (settings.getString(XSLT_URL_KEY) != null) {
      CxxUtils.LOG.debug("Transforming the report using xslt '{}'", settings.getString(XSLT_URL_KEY));
      InputStream inputStream = this.getClass().getResourceAsStream("/xsl/" + settings.getString(XSLT_URL_KEY));
      if (inputStream == null) {
        URL url = new URL(settings.getString(XSLT_URL_KEY));
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
      CxxUtils.LOG.debug("Transformation skipped: no xslt given");
    }

    return transformed;
  }

  private void parseReport(Project project, SensorContext context, File report)
          throws javax.xml.stream.XMLStreamException, IOException {
    CxxUtils.LOG.info("Parsing report '{}'", report);

    TestSuiteParser parserHandler = new TestSuiteParser();
    StaxParser parser = new StaxParser(parserHandler, false);
    parser.parse(report);

    for (TestSuite fileReport : parserHandler.getParsedReports()) {
      String fileKey = fileReport.getTestFileName();
      org.sonar.api.resources.File resource = getTestFileFile(project, context, fileKey);
      double testsCount = fileReport.getTests() - fileReport.getSkipped();            
      
      try {
        saveTestMetrics(context, resource, fileReport, testsCount);
      } catch (org.sonar.api.utils.SonarException ex) {                
        String dupFileKey = StringUtils.substringBeforeLast(fileKey, ".")
                + "_dup_" + Integer.toString(globalDuplicationCounter)
                + "." + StringUtils.substringAfterLast(fileKey, ".");

        org.sonar.api.resources.File unitTest = getTestFileFile(project, context, dupFileKey);
        globalDuplicationCounter += 1;
        saveTestMetrics(context, unitTest, fileReport, testsCount);
        
      }      
    }
  }

  private void saveTestMetrics(SensorContext context, org.sonar.api.resources.File resource, TestSuite fileReport, double testsCount) {
    context.saveMeasure(resource, CoreMetrics.SKIPPED_TESTS, (double) fileReport.getSkipped());
    context.saveMeasure(resource, CoreMetrics.TESTS, testsCount);
    context.saveMeasure(resource, CoreMetrics.TEST_ERRORS, (double) fileReport.getErrors());
    context.saveMeasure(resource, CoreMetrics.TEST_FAILURES, (double) fileReport.getFailures());
    context.saveMeasure(resource, CoreMetrics.TEST_EXECUTION_TIME, (double) fileReport.getTime());
    final double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
    if (testsCount > 0) {
      double percentage = passedTests * 100d / testsCount;
      context.saveMeasure(resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
    }
    context.saveMeasure(resource, new Measure(CoreMetrics.TEST_DATA, fileReport.getDetails()));     
  }

  private org.sonar.api.resources.File getTestFileFile(Project project, SensorContext context, String fileKey) {

    org.sonar.api.resources.File resource =
            org.sonar.api.resources.File.fromIOFile(new File(fileKey), project);

    if (context.getResource(resource) == null) {
      resource = new org.sonar.api.resources.File(fileKey);
      resource.setLanguage(CxxLanguage.INSTANCE);
      resource.setQualifier(Qualifiers.UNIT_TEST_FILE);
      context.saveSource(resource, "<This is a virtual file or duplicated file due to multiple test classes in file>");
    }

    return resource;
  }  
}
