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
import org.sonar.api.batch.fs.InputFile;

/**
 * {@inheritDoc}
 */
public class CxxXunitSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.xunit.reportPath";
  public static final String XSLT_URL_KEY = "sonar.cxx.xunit.xsltURL";
  public static final String PROVIDE_DETAILS_KEY = "sonar.cxx.xunit.provideDetails";
  private static final double PERCENT_BASE = 100d;

  private String xsltURL = null;
  private final Map<String, String> classDeclTable = new TreeMap<>();
  private final Map<String, String> classImplTable = new TreeMap<>();
  private int tcTotal = 0;
  private int tcSkipped = 0;

  static Pattern classNameOnlyMatchingPattern = Pattern.compile("(?:\\w*::)*?(\\w+?)::\\w+?:\\d+$");
  static Pattern qualClassNameMatchingPattern = Pattern.compile("((?:\\w*::)*?(\\w+?))::\\w+?:\\d+$");

  /**
   * {@inheritDoc}
   */
  public CxxXunitSensor(Settings settings, FileSystem fs) {
    super(settings, fs);
    xsltURL = settings.getString(XSLT_URL_KEY);
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
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
    if (settings.hasKey(reportPathKey())) {
      if (!settings.getBoolean(PROVIDE_DETAILS_KEY)) {
        return !project.isModule();
      } else {
        return fs.hasFiles(fs.predicates().hasLanguage(CxxLanguage.KEY));
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    try {
      List<File> reports = getReports(settings, fs.baseDir(), REPORT_PATH_KEY);
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
        boolean providedetails = settings.getBoolean(PROVIDE_DETAILS_KEY);
        if (providedetails) {
          detailledMode(project, context, testcases);
        } else {
          simpleMode(project, context, testcases);
        }
      } else {
        CxxUtils.LOG.debug("No reports found, nothing to process");
      }
    } catch (Exception e) {
      String msg = new StringBuilder()
        .append("Cannot feed the data into SonarQube, details: '")
        .append(e)
        .append("'")
        .toString();
      CxxUtils.LOG.error(msg);
      throw new IllegalStateException(msg, e);
    }
  }

  private void simpleMode(final Project project, final SensorContext context, List<TestCase> testcases)
    throws javax.xml.stream.XMLStreamException,
    java.io.IOException,
    javax.xml.transform.TransformerException {
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

    try
    {
      if (testsCount > 0) {
        double testsPassed = testsCount - testsErrors - testsFailures;
        double successDensity = testsPassed * PERCENT_BASE / testsCount;
        context.saveMeasure(project, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(successDensity));

        context.saveMeasure(project, CoreMetrics.TESTS, testsCount);
        context.saveMeasure(project, CoreMetrics.SKIPPED_TESTS, testsSkipped);
        context.saveMeasure(project, CoreMetrics.TEST_ERRORS, testsErrors);
        context.saveMeasure(project, CoreMetrics.TEST_FAILURES, testsFailures);
        context.saveMeasure(project, CoreMetrics.TEST_EXECUTION_TIME, testsTime);
      } else {
        CxxUtils.LOG.debug("The reports contain no testcases");
      }      
    } catch(Exception ex) {
      CxxUtils.LOG.error("Failed to save measures : ", ex.getMessage());
    }

  }

  private void detailledMode(final Project project, final SensorContext context, List<TestCase> testcases)
    throws
    javax.xml.stream.XMLStreamException,
    java.io.IOException,
    javax.xml.transform.TransformerException {
    CxxUtils.LOG.info("Processing in 'detailled mode' i.e. with provideDetails=true");

    String sonarTests = settings.getString("sonar.tests");
    if (sonarTests == null || "".equals(sonarTests)) {
      CxxUtils.LOG.error("The property 'sonar.tests' is unset. Please set it to proceed");
      return;
    }

    Collection<TestFile> testFiles = lookupTestFiles(project, context, testcases);

    for (TestFile testFile : testFiles) {
      saveTestMetrics(context, testFile);
    }
    CxxUtils.LOG.info("Summary: testcases processed = {}, skipped = {}", tcTotal, tcSkipped);
    if (tcSkipped > 0) {
      CxxUtils.LOG.warn("Some testcases had to be skipped, check the relevant parts of your setup "
        + "(sonar.tests, sonar.test.exclusions, sonar.test.inclusions)");
    }
  }

  private Collection<TestFile> lookupTestFiles(Project project, SensorContext context, List<TestCase> testcases) {
    HashMap<String, TestFile> testFileMap = new HashMap<>();

    for (TestCase tc : testcases) {
      tcTotal++;

      if (CxxUtils.LOG.isDebugEnabled()) {
        CxxUtils.LOG.debug("Trying the input file for the testcase '{}' ...", tc.getFullname());
      }
      InputFile inputFile = lookupFile(project, context, tc);
      if (inputFile != null) {
        CxxUtils.LOG.debug("... found! The input file is '{}'", inputFile);

        TestFile testFile = testFileMap.get(inputFile.absolutePath());
        if (testFile == null) {
          testFile = new TestFile(inputFile);
          testFileMap.put(testFile.getKey(), testFile);
        }

        testFile.addTestCase(tc);
      } else {
        tcSkipped++;
        CxxUtils.LOG.warn("... no input file found, the testcase '{}' has to be skipped",
          tc.getFullname());
      }
    }

    return testFileMap.values();
  }

  File transformReport(File report)
    throws java.io.IOException, javax.xml.transform.TransformerException {
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

  private void saveTestMetrics(SensorContext context, TestFile testFile) {
    InputFile inputFile = testFile.getInputFile();
    double testsRun = testFile.getTests() - testFile.getSkipped();

    context.saveMeasure(inputFile, CoreMetrics.SKIPPED_TESTS, (double) testFile.getSkipped());
    context.saveMeasure(inputFile, CoreMetrics.TESTS, testsRun);
    context.saveMeasure(inputFile, CoreMetrics.TEST_ERRORS, (double) testFile.getErrors());
    context.saveMeasure(inputFile, CoreMetrics.TEST_FAILURES, (double) testFile.getFailures());
    context.saveMeasure(inputFile, CoreMetrics.TEST_EXECUTION_TIME, (double) testFile.getTime());

    if (testsRun > 0) {
      double testsPassed = testsRun - testFile.getErrors() - testFile.getFailures();
      double successDensity = testsPassed * PERCENT_BASE / testsRun;
      context.saveMeasure(inputFile, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(successDensity));
    }
    context.saveMeasure(inputFile, new Measure(CoreMetrics.TEST_DATA, testFile.getDetails()));
  }

  private InputFile lookupFile(Project project, SensorContext context, TestCase tc) {
    // The lookup of the test input files for a test case is performed as follows:
    // 1. If the testcase carries a filepath just perform the lookup using this data.
    //    As we assume this as the absolute knowledge, we dont want to fallback to other
    //    methods, we want to FAIL.
    // 2. Use the classname to search in the lookupTable (this is the AST-based lookup)
    //    and redo the lookup in Sonar with the gained value.

    InputFile inputFile = null;
    String filepath = tc.getFilename();
    if (filepath != null) {
      CxxUtils.LOG.debug("Performing the 'filename'-based lookup using the value '{}'", filepath);
      return lookupInSonar(filepath);
    }

    if (classDeclTable.isEmpty() && classImplTable.isEmpty()) {
      buildLookupTables();
    }

    String classname = tc.getClassname();
    if (classname != null) {
      filepath = lookupFilePath(classname);
      CxxUtils.LOG.debug("Performing AST-based lookup, determined file path: '{}'", filepath);
      inputFile = lookupInSonar(filepath);
    } else {
      CxxUtils.LOG.debug("Skipping the AST-based lookup: no classname provided");
    }

    return inputFile;
  }

  private InputFile lookupInSonar(String filepath) {
    return fs.inputFile(fs.predicates().is(new File(filepath)));
  }

  String lookupFilePath(String key) {
    String path = classImplTable.get(key);
    if (path == null) {
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
    String[] lines = settings.getStringLines(CxxPlugin.DEFINES_KEY);
    if (lines.length > 0) {
      cxxConf.setDefines(Arrays.asList(lines));
    }
    cxxConf.setIncludeDirectories(settings.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));
    cxxConf.setMissingIncludeWarningsEnabled(settings.getBoolean(CxxPlugin.MISSING_INCLUDE_WARN));

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
