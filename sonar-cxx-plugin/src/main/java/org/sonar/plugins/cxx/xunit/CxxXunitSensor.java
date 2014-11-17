/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.xunit;

import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.EmptyReportException;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.TreeMap;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.squid.api.SourceClass;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourceFunction;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPath";
  public static final String XSLT_URL_KEY = "sonar.cxx.xunit.xsltURL";
  public static final String PROVIDE_DETAILS_KEY = "sonar.cxx.xunit.provideDetails";

  private static final String DEFAULT_REPORT_PATH = "xunit-reports/xunit-result-*.xml";
  private String xsltURL = null;
  private CxxLanguage lang = null;
  private Map<String, String> classDeclTable = new TreeMap<String, String>();
  private Map<String, String> classImplTable = new TreeMap<String, String>();
  static Pattern classNameMatchingPattern = Pattern.compile("(?:\\w*::)*?(\\w+?)::\\w+?:\\d+$");
  private final static double PERCENT_BASE = 100d;

  /**
   * {@inheritDoc}
   */
  public CxxXunitSensor(Settings conf, ModuleFileSystem fs, CxxLanguage cxxLang) {
    super(conf, fs);
    this.lang = cxxLang;
    xsltURL = conf.getString(XSLT_URL_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @DependsUpon
  public Class<?> dependsUponCoverageSensors() {
    return CoverageExtension.class;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    try{
      List<File> reports = getReports(conf, fs.baseDir().getPath(),
                                      REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
      if (!reports.isEmpty()) {
        boolean providedetails = conf.getBoolean(PROVIDE_DETAILS_KEY);
        if (providedetails) {
          detailledMode(project, context, reports);
        } else {
          simpleMode(project, context, reports);
        }
      }
      else{
        CxxUtils.LOG.debug("No reports found, nothing to process");
      }
    } catch (Exception e) {
      String msg = new StringBuilder()
        .append("Cannot feed the data into SonarQube, details: '")
        .append(e)
        .append("'")
        .toString();
      throw new SonarException(msg, e);
    }
  }

  private void simpleMode(final Project project, final SensorContext context, List<File> reports)
    throws javax.xml.stream.XMLStreamException {

    CxxUtils.LOG.info("Processing in 'simple mode' i.e. with provideDetails=false.");

    XunitReportParser parserHandler = new XunitReportParser();
    StaxParser parser = new StaxParser(parserHandler, false);
    for (File report : reports) {
      CxxUtils.LOG.info("Parsing report '{}'", report);
      try{
        parser.parse(report);
      } catch(EmptyReportException e){
        CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
      }
    }

    double testsCount = 0.0;
    double testsSkipped = 0.0;
    double testsErrors = 0.0;
    double testsFailures = 0.0;
    double testsTime = 0.0;
    for (TestSuite ts : parserHandler.getTestSuites()) {
      testsCount += ts.getTests() - ts.getSkipped();
      testsSkipped += ts.getSkipped();
      testsErrors += ts.getErrors();
      testsFailures += ts.getFailures();
      testsTime += ts.getTime();
    }

    if (testsCount > 0) {
      double testsPassed = testsCount - testsErrors - testsFailures;
      double successDensity = testsPassed * PERCENT_BASE / testsCount;
      context.saveMeasure(project, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(successDensity));

      context.saveMeasure(project, CoreMetrics.TESTS, testsCount);
      context.saveMeasure(project, CoreMetrics.SKIPPED_TESTS, testsSkipped);
      context.saveMeasure(project, CoreMetrics.TEST_ERRORS, testsErrors);
      context.saveMeasure(project, CoreMetrics.TEST_FAILURES, testsFailures);
      context.saveMeasure(project, CoreMetrics.TEST_EXECUTION_TIME, testsTime);
    }
    else{
      CxxUtils.LOG.debug("The reports contain no testcases");
    }
  }

  private void detailledMode(final Project project, final SensorContext context, List<File> reports)
    throws
    javax.xml.stream.XMLStreamException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    CxxUtils.LOG.info("Processing in 'detailled mode' i.e. with provideDetails=true");

    buildLookupTables(project);

    for (File report : reports) {
      CxxUtils.LOG.info("Processing report '{}'", report);
      try{
        //int prevViolationsCount = violationsCount;
        processReport(project, context, report);
        //CxxUtils.LOG.info("{} processed = {}", metric == null ? "Issues" : metric.getName(),
        //                  violationsCount - prevViolationsCount);
      } catch(EmptyReportException e){
        CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
      }
    }
  }


  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws
      java.io.IOException,
      javax.xml.transform.TransformerException,
      javax.xml.stream.XMLStreamException
  {
    parseReport(project, context, transformReport(report));
  }

  File transformReport(File report)
      throws java.io.IOException, javax.xml.transform.TransformerException
  {
    File transformed = report;
    if (xsltURL != null) {
      CxxUtils.LOG.debug("Transforming the report using xslt '{}'", xsltURL);
      InputStream inputStream = this.getClass().getResourceAsStream("/xsl/" + xsltURL);
      if (inputStream == null) {
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
      CxxUtils.LOG.debug("Transformation skipped: no xslt given");
    }

    return transformed;
  }

  private void parseReport(Project project, SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException, IOException {

    XunitReportParser parserHandler = new XunitReportParser();
    StaxParser parser = new StaxParser(parserHandler, false);
    parser.parse(report);

    for (TestSuite tsuite : parserHandler.getTestSuites()) {
      String fileKey = tsuite.getKey();
      try {
        org.sonar.api.resources.File resource = getTestFile(project, context, fileKey);
        saveTestMetrics(context, resource, tsuite);
      } catch (org.sonar.api.utils.SonarException ex) {
        CxxUtils.LOG.warn("Cannot save test metrics for '{}', details: {}", fileKey, ex);
      }
    }
  }

  private void saveTestMetrics(SensorContext context, org.sonar.api.resources.File resource, TestSuite fileReport) {
    double testsRun = fileReport.getTests() - fileReport.getSkipped();

    context.saveMeasure(resource, CoreMetrics.SKIPPED_TESTS, (double) fileReport.getSkipped());
    context.saveMeasure(resource, CoreMetrics.TESTS, testsRun);
    context.saveMeasure(resource, CoreMetrics.TEST_ERRORS, (double) fileReport.getErrors());
    context.saveMeasure(resource, CoreMetrics.TEST_FAILURES, (double) fileReport.getFailures());
    context.saveMeasure(resource, CoreMetrics.TEST_EXECUTION_TIME, (double) fileReport.getTime());


    if (testsRun > 0) {
      double testsPassed = testsRun - fileReport.getErrors() - fileReport.getFailures();
      double successDensity = testsPassed * PERCENT_BASE / testsRun;
      context.saveMeasure(resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(successDensity));
    }
    context.saveMeasure(resource, new Measure(CoreMetrics.TEST_DATA, fileReport.getDetails()));
  }

  private org.sonar.api.resources.File getTestFile(Project project, SensorContext context, String fileKey) {
    org.sonar.api.resources.File resource = fromIOFile(fileKey, project);

    if (context.getResource(resource) == null) {
      String filePath = lookupFilePath(fileKey);
      resource = fromIOFile(filePath, project);
      if (context.getResource(resource) == null) {
        CxxUtils.LOG.debug("Cannot find the source file for test '{}', creating a dummy one", fileKey);
        resource = createVirtualFile(context, fileKey);
      }
    } else {
      CxxUtils.LOG.debug("Assigning the test '{}' to resource '{}'", fileKey, resource.getKey());
    }

    return resource;
  }


  org.sonar.api.resources.File fromIOFile(String filepath, Project project){
    File file = new File(filepath);
    org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(file, project);
    if (resource == null) {
      // support SQ<4.2
      resource = org.sonar.api.resources.File.fromIOFile(file, fs.testDirs());
    }
    return resource;
  }


  private org.sonar.api.resources.File createVirtualFile(SensorContext context, String fileKey) {
    org.sonar.api.resources.File file = new org.sonar.api.resources.File(fileKey);
    file.setLanguage(this.lang);
    file.setQualifier(Qualifiers.UNIT_TEST_FILE);
    context.saveSource(file, "<The sources could not be found. Consult the log file for details>");
    return file;
  }

  String lookupFilePath(String key) {
    String path = classImplTable.get(key);
    if(path == null){
      path = classDeclTable.get(key);
    }

    return path != null ? path : key;
  }

  void buildLookupTables(Project project) {
    List<File> files = fs.files(CxxLanguage.testQuery);

    CxxConfiguration cxxConf = new CxxConfiguration(fs.sourceCharset());
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = conf.getStringLines(CxxPlugin.DEFINES_KEY);
    if(lines.length > 0){
      cxxConf.setDefines(Arrays.asList(lines));
    }
    cxxConf.setIncludeDirectories(conf.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));

    for (File file : files) {
      @SuppressWarnings("unchecked")
      SourceFile source = CxxAstScanner.scanSingleFileConfig(file, cxxConf);
      if(source.hasChildren()) {
        for (SourceCode child : source.getChildren()) {
          if (child instanceof SourceClass) {
            classDeclTable.put(child.getName(), file.getPath());
          }
          else if(child instanceof SourceFunction){
            String clsName = matchClassName(child.getKey());
            if(clsName != null){
              classImplTable.put(clsName, file.getPath());
            }
          }
        }
      }
    }

    filterMapUsingKeyList(classImplTable, classDeclTable.keySet());
  }

  private Map<String, String> filterMapUsingKeyList(Map<String, String> map, Collection keys){
    return map;
  }

  String matchClassName(String fullQualFunctionName){
    Matcher matcher = classNameMatchingPattern.matcher(fullQualFunctionName);
    String clsname = null;
    if(matcher.matches()){
      clsname = matcher.group(1);
    }
    return clsname;
  }
}
