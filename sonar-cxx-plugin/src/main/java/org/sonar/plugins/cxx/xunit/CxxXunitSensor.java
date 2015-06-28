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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException; //@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import org.sonar.api.utils.StaxParser;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.cxx.utils.EmptyReportException;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.api.batch.bootstrap.ProjectReactor;


/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPath";
  public static final String XSLT_URL_KEY = "sonar.cxx.xunit.xsltURL";
  public static final String PROVIDE_DETAILS_KEY = "sonar.cxx.xunit.provideDetails";

  private static final String DEFAULT_REPORT_PATH = "xunit-reports/xunit-result-*.xml";
  private static final double PERCENT_BASE = 100d;

  private String xsltURL = null;
  private Map<String, String> classDeclTable = new TreeMap<String, String>();
  private Map<String, String> classImplTable = new TreeMap<String, String>();
  private ResourceFinder resourceFinder = null;
  private int tcTotal = 0;
  private int tcSkipped = 0;

  static Pattern classNameOnlyMatchingPattern = Pattern.compile("(?:\\w*::)*?(\\w+?)::\\w+?:\\d+$");
  static Pattern qualClassNameMatchingPattern = Pattern.compile("((?:\\w*::)*?(\\w+?))::\\w+?:\\d+$");

  /**
   * {@inheritDoc}
   */
  public CxxXunitSensor(Settings conf, FileSystem fs, ProjectReactor reactor) {
    super(conf, fs, reactor);
    xsltURL = conf.getString(XSLT_URL_KEY);
    this.resourceFinder = new DefaultResourceFinder();
  }

  void injectResourceFinder(ResourceFinder finder) {
    this.resourceFinder = finder;
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
  public boolean shouldExecuteOnProject(Project project) {
    boolean providedetails = conf.getBoolean(PROVIDE_DETAILS_KEY);
    
    if (!providedetails) {
      return !project.isModule();      
    }
    
    return !project.getFileSystem().mainFiles(CxxLanguage.KEY).isEmpty();
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
        XunitReportParser parserHandler = new XunitReportParser();
        StaxParser parser = new StaxParser(parserHandler, false);
        for (File report : reports) {
          CxxUtils.LOG.info("Processing report '{}'", report);
          try {
            parser.parse(transformReport(report));
          } catch (EmptyReportException e) {
            CxxUtils.LOG.warn("The report '{}' seems to be empty, ignoring.", report);
          }
        }
        List<TestCase> testcases = parserHandler.getTestCases();

        CxxUtils.LOG.info("Parsing 'xUnit' format");
        boolean providedetails = conf.getBoolean(PROVIDE_DETAILS_KEY);
        if (providedetails) {
          detailledMode(project, context, testcases);
        } else {
          simpleMode(project, context, testcases);
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
      CxxUtils.LOG.error(msg);
      throw new SonarException(msg, e); //@todo SonarException has been deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
    }
  }

  private void simpleMode(final Project project, final SensorContext context, List<TestCase> testcases)
    throws javax.xml.stream.XMLStreamException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    CxxUtils.LOG.info("Processing in 'simple mode' i.e. with provideDetails=false.");

    double testsCount = 0.0;
    double testsSkipped = 0.0;
    double testsErrors = 0.0;
    double testsFailures = 0.0;
    double testsTime = 0.0;
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

  private void detailledMode(final Project project, final SensorContext context, List<TestCase> testcases)
    throws
    javax.xml.stream.XMLStreamException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    CxxUtils.LOG.info("Processing in 'detailled mode' i.e. with provideDetails=true");

    String sonarTests = conf.getString("sonar.tests");
    if (sonarTests == null || "".equals(sonarTests)){
      CxxUtils.LOG.error("The property 'sonar.tests' is unset. Please set it to proceed");
      return;
    }

    Collection<TestResource> locatedResources = lookupResources(project, context, testcases);

    for (TestResource resource : locatedResources) {
      saveTestMetrics(context, resource);
    }
    CxxUtils.LOG.info("Summary: testcases processed = {}, skipped = {}", tcTotal, tcSkipped);
    if (tcSkipped > 0){
      CxxUtils.LOG.warn("Some testcases had to be skipped, check the relevant parts of your setup "
                        + "(sonar.tests, sonar.test.exclusions, sonar.test.inclusions)");
    }
  }


  private Collection<TestResource> lookupResources(Project project, SensorContext context, List<TestCase> testcases) {
    Map<String, TestResource> resources = new HashMap<String, TestResource>();

    for (TestCase tc : testcases) {
      tcTotal++;

      CxxUtils.LOG.debug("Trying the resource for the testcase '{}' ...", tc.getFullname());
      org.sonar.api.resources.File sonarResource = lookupResource(project, context, tc);
      if (sonarResource != null) {
        CxxUtils.LOG.debug("... found! The resource is '{}'", sonarResource);

        TestResource resource = resources.get(sonarResource.getKey());
        if (resource == null) {
          resource = new TestResource(sonarResource);
          resources.put(resource.getKey(), resource);
        }

        resource.addTestCase(tc);
      } else {
        tcSkipped++;
        CxxUtils.LOG.warn("... no resource found, the testcase '{}' has to be skipped",
                          tc.getFullname());
      }
    }

    return resources.values();
  }

  File transformReport(File report)
      throws java.io.IOException, javax.xml.transform.TransformerException
  {
    File transformed = report;
    if (xsltURL != null && report.length() > 0) {
      CxxUtils.LOG.debug("Transforming the report using xslt '{}'", xsltURL);
      InputStream inputStream = this.getClass().getResourceAsStream("/xsl/" + xsltURL);
      if (inputStream == null) {
        CxxUtils.LOG.debug("Transforming: try to access external XSLT via URL");
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

  private void saveTestMetrics(SensorContext context, TestResource resource) {
    org.sonar.api.resources.File testfile = resource.getSonarResource();
    double testsRun = resource.getTests() - resource.getSkipped();

    context.saveMeasure(testfile, CoreMetrics.SKIPPED_TESTS, (double) resource.getSkipped());
    context.saveMeasure(testfile, CoreMetrics.TESTS, testsRun);
    context.saveMeasure(testfile, CoreMetrics.TEST_ERRORS, (double) resource.getErrors());
    context.saveMeasure(testfile, CoreMetrics.TEST_FAILURES, (double) resource.getFailures());
    context.saveMeasure(testfile, CoreMetrics.TEST_EXECUTION_TIME, (double) resource.getTime());


    if (testsRun > 0) {
      double testsPassed = testsRun - resource.getErrors() - resource.getFailures();
      double successDensity = testsPassed * PERCENT_BASE / testsRun;
      context.saveMeasure(testfile, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(successDensity));
    }
    context.saveMeasure(testfile, new Measure(CoreMetrics.TEST_DATA, resource.getDetails()));
  }

  private org.sonar.api.resources.File lookupResource(Project project, SensorContext context, TestCase tc) {
    // The lookup of the test resource for a test case is performed as follows:
    // 1. If the testcase carries a filepath just perform the lookup using this data.
    //    As we assume this as the absolute knowledge, we dont want to fallback to other
    //    methods, we want to FAIL.
    // 2. Use the classname to search in the lookupTable (this is the AST-based lookup)
    //    and redo the lookup in Sonar with the gained value.

    org.sonar.api.resources.File sonarResource = null;
    String filepath = tc.getFilename();
    if (filepath != null){
      CxxUtils.LOG.debug("Performing the 'filename'-based lookup using the value '{}'", filepath);
      return lookupInSonar(filepath, context, project);
    }

    if(classDeclTable.isEmpty() && classImplTable.isEmpty()){
      buildLookupTables();
    }

    String classname = tc.getClassname();
    if (classname != null){
      filepath = lookupFilePath(classname);
      CxxUtils.LOG.debug("Performing AST-based lookup, determined file path: '{}'", filepath);
      sonarResource = lookupInSonar(filepath, context, project);
    } else {
      CxxUtils.LOG.debug("Skipping the AST-based lookup: no classname provided");
    }

    return sonarResource;
  }

  private org.sonar.api.resources.File lookupInSonar(String filepath, SensorContext context, Project project) {
    return resourceFinder.findInSonar(new File(filepath), context, this.fs, project);
  }

  String lookupFilePath(String key) {
    String path = classImplTable.get(key);
    if(path == null){
      path = classDeclTable.get(key);
    }

    return path != null ? path : key;
  }

  void buildLookupTables() {
    FilePredicates predicates = fs.predicates();
    Iterable<File> files = fs.files(predicates.and(
        predicates.hasType(org.sonar.api.batch.fs.InputFile.Type.TEST),
        predicates.hasLanguage(CxxLanguage.KEY)));

    CxxConfiguration cxxConf = new CxxConfiguration(fs.encoding());
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = conf.getStringLines(CxxPlugin.DEFINES_KEY);
    if (lines.length > 0) {
      cxxConf.setDefines(Arrays.asList(lines));
    }
    cxxConf.setIncludeDirectories(conf.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));
    cxxConf.setMissingIncludeWarningsEnabled(conf.getBoolean(CxxPlugin.MISSING_INCLUDE_WARN));

    for (File file : files) {
      @SuppressWarnings("unchecked")
      SourceFile source = CxxAstScanner.scanSingleFileConfig(file, cxxConf);
      if (source.hasChildren()) {
        for (SourceCode child : source.getChildren()) {
          if (child instanceof SourceClass) {
            classDeclTable.put(child.getName(), file.getPath());
          } else if (child instanceof SourceFunction) {
            String clsNameOnly = matchClassNameOnly(child.getKey());
            if (clsNameOnly != null) {
              classImplTable.put(clsNameOnly, file.getPath());
            }
            String qualClsName = matchQualClassName(child.getKey());
            if (qualClsName != null && !qualClsName.isEmpty() && !qualClsName.equals(clsNameOnly)) {
              classImplTable.put(qualClsName, file.getPath());
            }
          }
        }
      }
    }
  }

  String matchClassNameOnly(String fullQualFunctionName) {
    Matcher matcher = classNameOnlyMatchingPattern.matcher(fullQualFunctionName);
    String clsname = null;
    if (matcher.matches()) {
      clsname = matcher.group(1);
    }
    return clsname;
  }

  String matchQualClassName(String fullQualFunctionName) {
    Matcher matcher = qualClassNameMatchingPattern.matcher(fullQualFunctionName);
    String clsname = null;
    if (matcher.matches()) {
      clsname = matcher.group(1);
    }
    return clsname;
  }
}
