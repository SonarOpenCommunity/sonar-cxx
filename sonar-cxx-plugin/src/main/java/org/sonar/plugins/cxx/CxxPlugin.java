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
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

import org.sonar.plugins.cxx.compiler.CxxCompilerGccParser;
import org.sonar.plugins.cxx.compiler.CxxCompilerGccRuleRepository;
import org.sonar.plugins.cxx.compiler.CxxCompilerSensor;
import org.sonar.plugins.cxx.compiler.CxxCompilerVcParser;
import org.sonar.plugins.cxx.compiler.CxxCompilerVcRuleRepository;
import org.sonar.plugins.cxx.coverage.CxxCoverageSensor;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckRuleRepository;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckSensor;
import org.sonar.plugins.cxx.externalrules.CxxExternalRuleRepository;
import org.sonar.plugins.cxx.externalrules.CxxExternalRulesSensor;
import org.sonar.plugins.cxx.pclint.CxxPCLintRuleRepository;
import org.sonar.plugins.cxx.pclint.CxxPCLintSensor;
import org.sonar.plugins.cxx.rats.CxxRatsRuleRepository;
import org.sonar.plugins.cxx.rats.CxxRatsSensor;
import org.sonar.plugins.cxx.squid.CxxSquidSensor;
import org.sonar.plugins.cxx.valgrind.CxxValgrindRuleRepository;
import org.sonar.plugins.cxx.valgrind.CxxValgrindSensor;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxRuleRepository;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxSensor;
import org.sonar.plugins.cxx.tests.xunit.CxxXunitSensor;
import org.sonar.plugins.cxx.tests.dotnet.CxxUnitTestResultsProvider;
import org.sonar.plugins.cxx.tests.dotnet.CxxUnitTestResultsProvider.CxxUnitTestResultsAggregator;
import org.sonar.plugins.cxx.tests.dotnet.CxxUnitTestResultsProvider.CxxUnitTestResultsImportSensor;
import org.sonar.plugins.cxx.utils.CxxMetrics;

import org.sonar.plugins.cxx.coverage.CxxCoverageCache;
import org.sonar.plugins.cxx.drmemory.CxxDrMemoryRuleRepository;
import org.sonar.plugins.cxx.drmemory.CxxDrMemorySensor;

/**
 * {@inheritDoc}
 */
@Properties({
  /**
   * General
   */
  @Property(
    category = "(1) General",
    key = CxxPlugin.SOURCE_FILE_SUFFIXES_KEY,
    defaultValue = CxxLanguage.DEFAULT_SOURCE_SUFFIXES,
    name = "Source files suffixes",
    description = "Comma-separated list of suffixes for source files to analyze. Leave empty to use the default.",
    project = true,
    module = true
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.HEADER_FILE_SUFFIXES_KEY,
    defaultValue = CxxLanguage.DEFAULT_HEADER_SUFFIXES,
    name = "Header files suffixes",
    description = "Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.",
    project = true,
    module = true
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.INCLUDE_DIRECTORIES_KEY,
    name = "Include directories",
    description = "Comma-separated list of directories to search the included files in. May be defined either relative to projects root or absolute.",
    project = true,
    module = true
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.FORCE_INCLUDE_FILES_KEY,
    name = "Force includes",
    description = "Comma-separated list of files which should to be included implicitly at the beginning of each source file.",
    project = true,
    module = true
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.DEFINES_KEY,
    name = "Default macros",
    description = "Additional macro definitions (one per line) to use when analysing the source code. Use to provide macros which cannot be resolved by other means. Use the 'force includes' setting to inject more complex, multi-line macros.",
    project = true,
    module = true,
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.C_FILES_PATTERNS_KEY,
    defaultValue = CxxLanguage.DEFAULT_C_FILES,
    name = "C source files patterns",
    description = "Comma-separated list of wildcard patterns used to detect C files. When a file matches any of the patterns, it is parsed in C-compatibility mode.",
    project = true,
    module = true
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.ERROR_RECOVERY_KEY,
    defaultValue = "False",
    name = "Parse error recovery",
    description = "Enables/disables the parse error recovery (experimental).",
    project = true,
    module = true,
    type = PropertyType.BOOLEAN
  ),
  @Property(
    category = "(1) General",
    key = CxxPlugin.MISSING_INCLUDE_WARN,
    defaultValue = "True",
    name = "Missing include warnings",
    description = "Enables/disables the warnings when included files could not be found.",
    project = true,
    module = true,
    type = PropertyType.BOOLEAN
  ),

  /**
   * Code Analysis
   */
  @Property(
    category = "(2) Code analysis",
    key = CxxCppCheckSensor.REPORT_PATH_KEY,
    name = "Cppcheck report(s)",
    description = "Path to a <a href='http://cppcheck.sourceforge.net/'>Cppcheck</a> analysis XML report, relative to projects root. Both XML formats (version 1 and version 2) are supported. If neccessary, <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxCppCheckRuleRepository.CUSTOM_RULES_KEY,
    name = "Cppcheck custom rules",
    description = "XML definitions of custom Cppcheck rules, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    project = true,
    module = true,
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxValgrindSensor.REPORT_PATH_KEY,
    name = "Valgrind report(s)",
    description = "Path to <a href='http://valgrind.org/'>Valgrind</a> report(s), relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxValgrindRuleRepository.CUSTOM_RULES_KEY,
    name = "Valgrind custom rules",
    description = "XML definitions of custom Valgrind rules, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    project = true,
    module = true,
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxDrMemorySensor.REPORT_PATH_KEY,
    name = "Dr Memory report(s)",
    description = "Path to <a href='http://drmemory.org/'>Dr. Memory</a> reports(s), relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxPCLintSensor.REPORT_PATH_KEY,
    name = "PC-lint report(s)",
    description = "Path to <a href='http://www.gimpel.com/html/pcl.htm'>PC-lint</a> reports(s), relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxPCLintRuleRepository.CUSTOM_RULES_KEY,
    name = "PC-lint custom rules",
    description = "XML definitions of custom PC-lint rules, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxRatsSensor.REPORT_PATH_KEY,
    name = "RATS report(s)",
    description = "Path to <a href='https://code.google.com/p/rough-auditing-tool-for-security/'>RATS<a/> reports(s), relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxRatsRuleRepository.CUSTOM_RULES_KEY,
    name = "RATS custom rules",
    description = "XML definitions of custom RATS rules, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxVeraxxSensor.REPORT_PATH_KEY,
    name = "Vera++ report(s)",
    description = "Path to <a href='https://bitbucket.org/verateam'>Vera++</a> reports(s), relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxVeraxxRuleRepository.CUSTOM_RULES_KEY,
    name = "Vera++ custom rules",
    description = "XML definitions of custom Vera++ rules, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxExternalRulesSensor.REPORT_PATH_KEY,
    name = "External checkers report(s)",
    description = "Path to a code analysis report, which is generated by some unsupported code analyser, relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a> for details.",
    project = true,
    module = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxExternalRuleRepository.RULES_KEY,
    name = "External rules",
    description = "Rule sets for 'external' code analysers. Use one value per rule set. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>this page</a> for details.",
    type = PropertyType.TEXT,
    multiValues = true
  ),
  @Property(
    category = "(2) Code analysis",
    key = CxxExternalRuleRepository.SQALES_KEY,
    name = "External SQALE characteristics",
    description = "SQALE characteristics for 'external' code analysers. Use one value per rule set. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>this page</a> for details.",
    type = PropertyType.TEXT,
    multiValues = true
  ),

  /**
   * Testing & Coverage
   */
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxCoverageSensor.REPORT_PATH_KEY,
    name = "Unit test coverage report(s)",
    description = "Path to a report containing unit test coverage data, relative to projects root. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-code-coverage-metrics'>here</a> for supported formats. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxCoverageSensor.IT_REPORT_PATH_KEY,
    name = "Integration test coverage report(s)",
    description = "Path to a report containing integration test coverage data, relative to projects root. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-code-coverage-metrics'>here</a> for supported formats. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxCoverageSensor.OVERALL_REPORT_PATH_KEY,
    name = "Overall test coverage report(s)",
    description = "Path to a report containing overall test coverage data (i.e. test coverage gained by all tests of all kinds), relative to projects root. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-code-coverage-metrics'>here</a> for supported formats. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY,
    name = "Assign zero line coverage to source files without coverage report(s)",
    description = "If 'True', assign zero line coverage to source files without coverage report(s),which results in a more realistic overall Technical Debt value.",
    defaultValue = "True",
    project = true,
    module = true,
    type = PropertyType.BOOLEAN
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxXunitSensor.REPORT_PATH_KEY,
    name = "Unit test execution report(s)",
    description = "Path to unit test execution report(s), relative to projects root. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-test-execution-metrics'>here</a> for supported formats. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxXunitSensor.XSLT_URL_KEY,
    name = "XSLT transformer",
    description = "By default, the unit test execution reports are expected to be in the JUnitReport format. To import a report in an other format, set this property to an URL to a XSLT stylesheet which is able to perform the according transformation.",
    project = true,
    module = true
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxXunitSensor.PROVIDE_DETAILS_KEY,
    name = "Provide test execution details",
    description = "If 'True', tries to assign testcases in report to test resources in SonarQube, thus making the drillown to details possible",
    defaultValue = "False",
    project = true,
    module = true,
    type = PropertyType.BOOLEAN
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxUnitTestResultsProvider.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY,
    name = "Visual Studio Test Reports Paths",
    description = "Example: \"report.trx\", \"report1.trx,report2.trx\" or \"C:/report.trx\"",
    project = true,
    module = true
  ),
  @Property(
    category = "(3) Testing & Coverage",
    key = CxxUnitTestResultsProvider.NUNIT_TEST_RESULTS_PROPERTY_KEY,
    name = "Nunit Test Reports Paths",
    description = "Example: \"nunit.xml\", \"nunit1.xml,nunit2.xml\" or \"C:/nunit.xml\"",
    project = true,
    module = true
  ),
  /**
   * Compiler Warnings
   */
  @Property(
    category = "(4) Compiler warnings",
    key = CxxCompilerSensor.REPORT_PATH_KEY,
    name = "Compiler report(s)",
    description = "Path to compilers output (i.e. file(s) containg compiler warnings), relative to projects root. Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.",
    project = true,
    module = true
  ),
  @Property(
    category = "(4) Compiler warnings",
    key = CxxCompilerSensor.PARSER_KEY_DEF,
    defaultValue = CxxCompilerSensor.DEFAULT_PARSER_DEF,
    name = "Format",
    type = PropertyType.SINGLE_SELECT_LIST,
    options = {CxxCompilerVcParser.KEY, CxxCompilerGccParser.KEY},
    description = "The format of the warnings file. Currently supported are Visual C++ and GCC.",
    project = true,
    module = true
  ),
  @Property(
    category = "(4) Compiler warnings",
    key = CxxCompilerSensor.REPORT_CHARSET_DEF,
    defaultValue = CxxCompilerSensor.DEFAULT_CHARSET_DEF,
    name = "Encoding",
    description = "The encoding to use when reading the compiler report. Leave empty to use parser's default.",
    project = true,
    module = true
  ),
  @Property(
    category = "(4) Compiler warnings",
    key = CxxCompilerSensor.REPORT_REGEX_DEF,
    name = "Custom matcher",
    description = "Regular expression to identify the four groups of the compiler warning message: file, line, ID, message. For advanced usages. Leave empty to use parser's default. See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Compilers'>this page</a> for details regarding the different regular expression that can be use per compiler.",
    project = true,
    module = true
  ),
  @Property(
    category = "(4) Compiler warnings",
    key = CxxCompilerVcRuleRepository.CUSTOM_RULES_KEY,
    name = "Custom rules for Visual C++ warnings",
    description = "XML definitions of custom rules for Visual C++ warnings, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    type = PropertyType.TEXT
  ),
  @Property(
    category = "(4) Compiler warnings",
    key = CxxCompilerGccRuleRepository.CUSTOM_RULES_KEY,
    name = "Custom rules for GCC warnings",
    description = "XML definitions of custom rules for GCC's warnings, which are'nt builtin into the plugin. The used format is described <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.",
    type = PropertyType.TEXT
  )

})
public final class CxxPlugin extends SonarPlugin {

  static final String SOURCE_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.sources";
  public static final String HEADER_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.headers";
  public static final String DEFINES_KEY = "sonar.cxx.defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "sonar.cxx.includeDirectories";
  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  public static final String FORCE_INCLUDE_FILES_KEY = "sonar.cxx.forceIncludes";
  public static final String C_FILES_PATTERNS_KEY = "sonar.cxx.cFilesPatterns";
  public static final String MISSING_INCLUDE_WARN = "sonar.cxx.missingIncludeWarnings";

  /**
   * {@inheritDoc}
   */
  @Override
  public List getExtensions() {
    List<Object> l = new ArrayList<>();
    l.add(CxxProjectBuilder.class);
    l.add(CxxLanguage.class);
    l.add(CxxMetrics.class);
    l.add(CxxSquidSensor.class);
    l.add(CxxCpdMapping.class);
    l.add(CxxRatsRuleRepository.class);
    l.add(CxxRatsSensor.class);
    l.add(CxxXunitSensor.class);
    l.add(CxxCoverageSensor.class);
    l.add(CxxCoverageAggregator.class);
    l.add(CxxCppCheckRuleRepository.class);
    l.add(CxxCppCheckSensor.class);
    l.add(CxxPCLintRuleRepository.class);
    l.add(CxxPCLintSensor.class);
    l.add(CxxDrMemoryRuleRepository.class);
    l.add(CxxDrMemorySensor.class);
    l.add(CxxCompilerVcRuleRepository.class);
    l.add(CxxCompilerGccRuleRepository.class);
    l.add(CxxCompilerSensor.class);
    l.add(CxxVeraxxRuleRepository.class);
    l.add(CxxVeraxxSensor.class);
    l.add(CxxValgrindRuleRepository.class);
    l.add(CxxValgrindSensor.class);
    l.add(CxxDefaultProfile.class);
    l.add(CxxCommonRulesEngine.class);
    l.add(CxxCommonRulesDecorator.class);
    l.add(CxxExternalRulesSensor.class);
    l.add(CxxExternalRuleRepository.class);
    l.add(CxxRuleRepository.class);
    l.add(CxxUnitTestResultsAggregator.class);
    l.add(CxxUnitTestResultsImportSensor.class);
    l.add(CxxSourceCodeColorizer.class);

    return l;
  }

  public static class CxxCoverageAggregator extends CxxCoverageCache {

    public CxxCoverageAggregator() {
      super();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
