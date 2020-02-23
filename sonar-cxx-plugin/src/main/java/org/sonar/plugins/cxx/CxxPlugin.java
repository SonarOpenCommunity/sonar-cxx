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
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.AggregateMeasureComputer;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.DensityMeasureComputer;
import org.sonar.cxx.postjobs.FinalReport;
import org.sonar.cxx.sensors.clangsa.CxxClangSARuleRepository;
import org.sonar.cxx.sensors.clangsa.CxxClangSASensor;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidyRuleRepository;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidySensor;
import org.sonar.cxx.sensors.compiler.gcc.CxxCompilerGccRuleRepository;
import org.sonar.cxx.sensors.compiler.gcc.CxxCompilerGccSensor;
import org.sonar.cxx.sensors.compiler.vc.CxxCompilerVcRuleRepository;
import org.sonar.cxx.sensors.compiler.vc.CxxCompilerVcSensor;
import org.sonar.cxx.sensors.coverage.CxxCoverageCache;
import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.cppcheck.CxxCppCheckRuleRepository;
import org.sonar.cxx.sensors.cppcheck.CxxCppCheckSensor;
import org.sonar.cxx.sensors.drmemory.CxxDrMemoryRuleRepository;
import org.sonar.cxx.sensors.drmemory.CxxDrMemorySensor;
import org.sonar.cxx.sensors.other.CxxOtherRepository;
import org.sonar.cxx.sensors.other.CxxOtherSensor;
import org.sonar.cxx.sensors.pclint.CxxPCLintRuleRepository;
import org.sonar.cxx.sensors.pclint.CxxPCLintSensor;
import org.sonar.cxx.sensors.rats.CxxRatsRuleRepository;
import org.sonar.cxx.sensors.rats.CxxRatsSensor;
import org.sonar.cxx.sensors.squid.CxxSquidSensor;
import org.sonar.cxx.sensors.tests.dotnet.CxxUnitTestResultsAggregator;
import org.sonar.cxx.sensors.tests.dotnet.CxxUnitTestResultsImportSensor;
import org.sonar.cxx.sensors.tests.dotnet.UnitTestConfiguration;
import org.sonar.cxx.sensors.tests.xunit.CxxXunitSensor;
import org.sonar.cxx.sensors.valgrind.CxxValgrindRuleRepository;
import org.sonar.cxx.sensors.valgrind.CxxValgrindSensor;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxRuleRepository;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxSensor;
import org.sonar.cxx.visitors.CxxFunctionComplexityVisitor;
import org.sonar.cxx.visitors.CxxFunctionSizeVisitor;

/**
 * {@inheritDoc}
 */
public final class CxxPlugin implements Plugin {

  public static final String SOURCE_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.sources";
  public static final String HEADER_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.headers";
  public static final String DEFINES_KEY = "sonar.cxx.defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "sonar.cxx.includeDirectories";
  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  public static final String FORCE_INCLUDE_FILES_KEY = "sonar.cxx.forceIncludes";
  public static final String C_FILES_PATTERNS_KEY = "sonar.cxx.cFilesPatterns";
  public static final String JSON_COMPILATION_DATABASE_KEY = "sonar.cxx.jsonCompilationDatabase";
  public static final String CPD_IGNORE_LITERALS_KEY = "sonar.cxx.cpd.ignoreLiterals";
  public static final String CPD_IGNORE_IDENTIFIERS_KEY = "sonar.cxx.cpd.ignoreIdentifiers";
  private static final String USE_ANT_STYLE_WILDCARDS
    = " Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.";
  private static final String EXTENDING_THE_CODE_ANALYSIS = " The used format is described <a href='"
    + "https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.";

  private static List<PropertyDefinition> generalProperties() {
    String subcateg = "(1) General";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(SOURCE_FILE_SUFFIXES_KEY)
        .multiValues(true)
        .defaultValue(CxxLanguage.DEFAULT_SOURCE_SUFFIXES)
        .name("Source files suffixes")
        .description("Comma-separated list of suffixes for source files to analyze. Leave empty to use the default.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(1)
        .build(),
      PropertyDefinition.builder(HEADER_FILE_SUFFIXES_KEY)
        .multiValues(true)
        .defaultValue(CxxLanguage.DEFAULT_HEADER_SUFFIXES)
        .name("Header files suffixes")
        .description("Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(2)
        .build(),
      PropertyDefinition.builder(INCLUDE_DIRECTORIES_KEY)
        .multiValues(true)
        .name("Include directories")
        .description("Comma-separated list of directories to search the included files in. "
          + "May be defined either relative to projects root or absolute.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(3)
        .build(),
      PropertyDefinition.builder(FORCE_INCLUDE_FILES_KEY)
        .multiValues(true)
        .subCategory(subcateg)
        .name("Force includes")
        .description("Comma-separated list of files which should to be included implicitly at the "
          + "beginning of each source file.")
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(4)
        .build(),
      PropertyDefinition.builder(DEFINES_KEY)
        .name("Default macros")
        .description("Additional macro definitions (one per line) to use when analysing the source code. Use to provide"
          + "macros which cannot be resolved by other means."
          + " Use the 'force includes' setting to inject more complex, multi-line macros.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.TEXT)
        .index(5)
        .build(),
      PropertyDefinition.builder(C_FILES_PATTERNS_KEY)
        .defaultValue(CxxLanguage.DEFAULT_C_FILES)
        .multiValues(true)
        .name("C source files patterns")
        .description("Comma-separated list of wildcard patterns used to detect C files. When a file matches any of the"
          + "patterns, it is parsed in C-compatibility mode.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(6)
        .build(),
      PropertyDefinition.builder(CxxPlugin.ERROR_RECOVERY_KEY)
        .defaultValue(Boolean.TRUE.toString())
        .name("Parse error recovery")
        .description("Defines mode for error handling of report files and parsing errors. `False' (strict) breaks after"
          + " an error or 'True' (tolerant=default) continues. See <a href='https://github.com/SonarOpenCommunity/"
          + "sonar-cxx/wiki/Supported-configuration-properties#sonarcxxerrorrecoveryenabled'>"
          + "sonar.cxx.errorRecoveryEnabled</a> for a complete description.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.BOOLEAN)
        .index(7)
        .build(),
      PropertyDefinition.builder(CxxSquidSensor.REPORT_PATH_KEY)
        .name("Path(s) to MSBuild log(s)")
        .description("Extract includes, defines and compiler options from the build log. This works only"
          + " if the produced log during compilation adds enough information (MSBuild verbosity set to"
          + " detailed or diagnostic)."
          + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(8)
        .build(),
      PropertyDefinition.builder(CxxSquidSensor.REPORT_CHARSET_DEF)
        .defaultValue(CxxSquidSensor.DEFAULT_CHARSET_DEF)
        .name("MSBuild log encoding")
        .description("The encoding to use when reading a MSBuild log. Leave empty to use default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(9)
        .build(),
      PropertyDefinition.builder(CxxPlugin.JSON_COMPILATION_DATABASE_KEY)
        .subCategory(subcateg)
        .name("JSON Compilation Database")
        .description("JSON Compilation Database file to use as specification for what defines and includes should be "
          + "used for source files.")
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(10)
        .build()
    ));
  }

  private static List<PropertyDefinition> codeAnalysisProperties() {
    String subcateg = "(2) Code analysis";
    return Collections.unmodifiableList(Arrays.asList(PropertyDefinition.builder(
      CxxCppCheckSensor.REPORT_PATH_KEY)
      .name("Cppcheck report(s)")
      .description("Path to a <a href='http://cppcheck.sourceforge.net/'>Cppcheck</a> analysis XML report, relative to"
        + " projects root. Both XML formats (version 1 and version 2) are supported. If neccessary, <a href='https://"
        + "ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service."
      )
      .subCategory(subcateg)
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .multiValues(true)
      .index(1)
      .build(),
      PropertyDefinition.builder(CxxValgrindSensor.REPORT_PATH_KEY)
        .name("Valgrind report(s)")
        .description("Path to <a href='http://valgrind.org/'>Valgrind</a> report(s), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(2)
        .build(),
      PropertyDefinition.builder(CxxDrMemorySensor.REPORT_PATH_KEY)
        .name("Dr Memory report(s)")
        .description("Path to <a href='http://drmemory.org/'>Dr. Memory</a> reports(s), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(3)
        .build(),
      PropertyDefinition.builder(CxxPCLintSensor.REPORT_PATH_KEY)
        .name("PC-lint report(s)")
        .description("Path to <a href='http://www.gimpel.com/html/pcl.htm'>PC-lint</a> reports(s), relative to projects"
          + "  root." + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(4)
        .build(),
      PropertyDefinition.builder(CxxRatsSensor.REPORT_PATH_KEY)
        .name("RATS report(s)")
        .description("Path to <a href='https://code.google.com/p/rough-auditing-tool-for-security/'>RATS<a/>"
          + " reports(s), relative to projects root." + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(5)
        .build(),
      PropertyDefinition.builder(CxxVeraxxSensor.REPORT_PATH_KEY)
        .name("Vera++ report(s)")
        .description("Path to <a href='https://bitbucket.org/verateam'>Vera++</a> reports(s),"
          + " relative to projects root." + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(6)
        .build(),
      PropertyDefinition.builder(CxxVeraxxRuleRepository.CUSTOM_RULES_KEY)
        .name("Vera++ custom rules")
        .description("XML definitions of custom Vera++ rules, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(7)
        .build(),
      PropertyDefinition.builder(CxxOtherSensor.REPORT_PATH_KEY)
        .name("External checkers report(s)")
        .description("Path to a code analysis report, which is generated by some unsupported code analyser, relative to"
          + "projects root." + USE_ANT_STYLE_WILDCARDS + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx"
          + "/wiki/Extending-the-code-analysis'>here</a> for details.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(8)
        .build(),
      PropertyDefinition.builder(CxxOtherRepository.RULES_KEY)
        .name("External rules")
        .description("Rule sets for 'external' code analysers. Use one value per rule set. See <a href='https:"
          + "//github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>this page</a> for details.")
        .type(PropertyType.TEXT)
        .multiValues(true)
        .subCategory(subcateg)
        .index(9)
        .build(),
      PropertyDefinition.builder(CxxClangTidySensor.REPORT_PATH_KEY)
        .name("Clang-Tidy analyzer report(s)")
        .description("Path to Clang-Tidy reports, relative to projects root. If neccessary, "
          + "<a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(10)
        .build(),
      PropertyDefinition.builder(CxxClangTidySensor.REPORT_CHARSET_DEF)
        .defaultValue(CxxClangTidySensor.DEFAULT_CHARSET_DEF)
        .name("Encoding")
        .description("The encoding to use when reading the clang-tidy report."
          + " Leave empty to use parser's default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(11)
        .build(),
      PropertyDefinition.builder(CxxClangSASensor.REPORT_PATH_KEY)
        .name("Clang Static analyzer analyzer report(s)")
        .description("Path to Clang Static Analyzer reports, relative to projects root. If neccessary, "
          + "<a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(12)
        .build(),
      PropertyDefinition.builder(CxxFunctionComplexityVisitor.FUNCTION_COMPLEXITY_THRESHOLD_KEY)
        .defaultValue("10")
        .name("Cyclomatic complexity threshold")
        .description("Cyclomatic complexity threshold used to classify a function as complex")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.INTEGER)
        .index(13)
        .build(),
      PropertyDefinition.builder(CxxFunctionSizeVisitor.FUNCTION_SIZE_THRESHOLD_KEY)
        .defaultValue("20")
        .name("Function size threshold")
        .description("Function size threshold to consider a function to be too big")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.INTEGER)
        .index(14)
        .build()
    ));
  }

  private static List<PropertyDefinition> compilerWarningsProperties() {
    String subcateg = "(4) Compiler warnings";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(CxxCompilerVcSensor.REPORT_PATH_KEY)
        .name("VC Compiler Report(s)")
        .description("Path to compilers output (i.e. file(s) containg compiler warnings), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(1)
        .build(),
      PropertyDefinition.builder(CxxCompilerVcSensor.REPORT_CHARSET_DEF)
        .defaultValue(CxxCompilerVcSensor.DEFAULT_CHARSET_DEF)
        .name("VC Report Encoding")
        .description("The encoding to use when reading the compiler report. Leave empty to use parser's default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(2)
        .build(),
      PropertyDefinition.builder(CxxCompilerVcSensor.REPORT_REGEX_DEF)
        .name("VC Regular Expression")
        .description("Regular expression to identify the four named groups of the compiler warning message:"
          + " &lt;file&gt;, &lt;line&gt;, &lt;id&gt;, &lt;message&gt;. Leave empty to use parser's default."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Compilers'>"
          + "this page</a> for details regarding the different regular expression that can be use per compiler.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(3)
        .build(),
      PropertyDefinition.builder(CxxCompilerGccSensor.REPORT_PATH_KEY)
        .name("GCC Compiler Report(s)")
        .description("Path to compilers output (i.e. file(s) containg compiler warnings), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(4)
        .build(),
      PropertyDefinition.builder(CxxCompilerGccSensor.REPORT_CHARSET_DEF)
        .defaultValue(CxxCompilerVcSensor.DEFAULT_CHARSET_DEF)
        .name("GCC Report Encoding")
        .description("The encoding to use when reading the compiler report. Leave empty to use parser's default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(5)
        .build(),
      PropertyDefinition.builder(CxxCompilerGccSensor.REPORT_REGEX_DEF)
        .name("GCC Regular Expression")
        .description("Regular expression to identify the four named groups of the compiler warning message:"
          + " <file>, <line>, <id>, <message>. Leave empty to use parser's default."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Compilers'>"
          + "this page</a> for details regarding the different regular expression that can be use per compiler.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(6)
        .build()
    ));
  }

  private static List<PropertyDefinition> testingAndCoverageProperties() {
    String subcateg = "(3) Testing & Coverage";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(CxxCoverageSensor.REPORT_PATH_KEY)
        .name("Unit test coverage report(s)")
        .description("List of paths to reports containing unit test coverage data, relative to projects root."
          + " The values are separated by commas."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-code-coverage-metrics'>"
          + "here</a> for supported formats.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(1)
        .build(),
      PropertyDefinition.builder(CxxXunitSensor.REPORT_PATH_KEY)
        .name("Unit test execution report(s)")
        .description("Path to unit test execution report(s), relative to projects root."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-test-execution-metrics'>"
          + "here</a> for supported formats." + USE_ANT_STYLE_WILDCARDS)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .multiValues(true)
        .index(2)
        .build(),
      PropertyDefinition.builder(CxxXunitSensor.XSLT_URL_KEY)
        .name("XSLT transformer")
        .description("By default, the unit test execution reports are expected to be in the JUnitReport format."
          + " To import a report in an other format, set this property to an URL to a XSLT stylesheet which is "
          + "able to perform the according transformation.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(3)
        .build(),
      PropertyDefinition.builder(UnitTestConfiguration.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("Visual Studio Test Reports Paths")
        .description("Example: \"report.trx\", \"report1.trx,report2.trx\" or \"C:/report.trx\"")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(4)
        .build(),
      PropertyDefinition.builder(UnitTestConfiguration.XUNIT_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("xUnit (MS) Test Reports Paths")
        .description("Example: \"report.xml\", \"report1.xml,report2.xml\" or \"C:/report.xml\"")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(5)
        .build(),
      PropertyDefinition.builder(UnitTestConfiguration.NUNIT_TEST_RESULTS_PROPERTY_KEY)
        .multiValues(true)
        .name("NUnit Test Reports Paths")
        .description("Example: \"TestResult.xml\", \"TestResult1.xml,TestResult2.xml\" or \"C:/TestResult.xml\"")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(6)
        .build()
    ));
  }

  private static List<PropertyDefinition> duplicationsProperties() {
    String subcateg = "(5) Duplications";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(CxxPlugin.CPD_IGNORE_LITERALS_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .name("Ignores literal value differences when evaluating a duplicate block")
        .description("Ignores literal (numbers, characters and strings) value differences when evaluating a duplicate "
          + "block. This means that e.g. foo=42; and foo=43; will be seen as equivalent. Default is 'False'.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.BOOLEAN)
        .index(1)
        .build(),
      PropertyDefinition.builder(CxxPlugin.CPD_IGNORE_IDENTIFIERS_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .name("Ignores identifier value differences when evaluating a duplicate block")
        .description("Ignores identifier value differences when evaluating a duplicate block e.g. variable names, "
          + "methods names, and so forth. Default is 'False'.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.BOOLEAN)
        .index(2)
        .build()
    ));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void define(Context context) {
    List<Object> l = new ArrayList<>();

    // plugin elements
    l.add(CxxLanguage.class);
    l.add(CxxDefaultProfile.class);
    l.add(CxxRuleRepository.class);

    // reusable elements
    l.addAll(getSensorsImpl());

    // properties elements
    l.addAll(generalProperties());
    l.addAll(codeAnalysisProperties());
    l.addAll(testingAndCoverageProperties());
    l.addAll(compilerWarningsProperties());
    l.addAll(duplicationsProperties());

    context.addExtensions(l);
  }

  public List<Object> getSensorsImpl() {
    List<Object> l = new ArrayList<>();

    // utility classes
    l.add(CxxCoverageCache.class);
    l.add(CxxUnitTestResultsAggregator.class);

    // metrics
    l.add(CxxMetricDefinition.class);
    // ComputeEngine: propagate metrics through all levels (FILE -> MODULE -> PROJECT)
    l.add(AggregateMeasureComputer.class);
    // ComputeEngine: calculate new metrics from existing ones
    l.add(DensityMeasureComputer.class);

    // issue sensors
    l.add(CxxSquidSensor.class);
    l.add(CxxRatsSensor.class);
    l.add(CxxCppCheckSensor.class);
    l.add(CxxPCLintSensor.class);
    l.add(CxxDrMemorySensor.class);
    l.add(CxxCompilerGccSensor.class);
    l.add(CxxCompilerVcSensor.class);
    l.add(CxxVeraxxSensor.class);
    l.add(CxxValgrindSensor.class);
    l.add(CxxClangTidySensor.class);
    l.add(CxxClangSASensor.class);
    l.add(CxxOtherSensor.class);

    // test sensors
    l.add(CxxXunitSensor.class);
    l.add(CxxUnitTestResultsImportSensor.class);
    l.add(CxxCoverageSensor.class);

    // rule provides
    l.add(CxxRatsRuleRepository.class);
    l.add(CxxCppCheckRuleRepository.class);
    l.add(CxxPCLintRuleRepository.class);
    l.add(CxxDrMemoryRuleRepository.class);
    l.add(CxxCompilerVcRuleRepository.class);
    l.add(CxxCompilerGccRuleRepository.class);
    l.add(CxxVeraxxRuleRepository.class);
    l.add(CxxValgrindRuleRepository.class);
    l.add(CxxOtherRepository.class);
    l.add(CxxClangTidyRuleRepository.class);
    l.add(CxxClangSARuleRepository.class);

    // post jobs
    l.add(FinalReport.class);

    return l;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
