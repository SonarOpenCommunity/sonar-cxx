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
package org.sonar.plugins.c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.cxx.sensors.clangsa.CxxClangSARuleRepository;
import org.sonar.cxx.sensors.clangsa.CxxClangSASensor;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidyRuleRepository;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidySensor;
import org.sonar.cxx.sensors.compiler.CxxCompilerGccParser;
import org.sonar.cxx.sensors.compiler.CxxCompilerGccRuleRepository;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;
import org.sonar.cxx.sensors.compiler.CxxCompilerVcParser;
import org.sonar.cxx.sensors.compiler.CxxCompilerVcRuleRepository;
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
import org.sonar.cxx.sensors.squid.CustomCxxRulesDefinition;
import org.sonar.cxx.sensors.squid.CxxSquidSensor;
import org.sonar.cxx.sensors.tests.xunit.CxxXunitSensor;
import org.sonar.cxx.sensors.utils.CxxMetrics;
import org.sonar.cxx.sensors.valgrind.CxxValgrindRuleRepository;
import org.sonar.cxx.sensors.valgrind.CxxValgrindSensor;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxRuleRepository;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxSensor;

/**
 * {@inheritDoc}
 */
public final class CPlugin implements Plugin {

  private static final String USE_ANT_STYLE_WILDCARDS_1 = " Use <a href='"
    + "https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.";
  private static final String USE_ANT_STYLE_WILDCARDS_2 = " If neccessary, <a href='"
    + "https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service.";
  private static final String EXTENDING_THE_CODE_ANALYSIS = " The used format is described <a href='"
    + "https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>here</a>.";
  public static final String LANG_PROP_PREFIX = "sonar.c.";
  public static final String SOURCE_FILE_SUFFIXES_KEY = LANG_PROP_PREFIX + "suffixes.sources";
  public static final String HEADER_FILE_SUFFIXES_KEY = LANG_PROP_PREFIX + "suffixes.headers";
  public static final String DEFINES_KEY = LANG_PROP_PREFIX + "defines";
  public static final String INCLUDE_DIRECTORIES_KEY = LANG_PROP_PREFIX + "includeDirectories";
  public static final String ERROR_RECOVERY_KEY = LANG_PROP_PREFIX + "errorRecoveryEnabled";
  public static final String FORCE_INCLUDE_FILES_KEY = LANG_PROP_PREFIX + "forceIncludes";
  public static final String C_FILES_PATTERNS_KEY = LANG_PROP_PREFIX + "cFilesPatterns";
  public static final String MISSING_INCLUDE_WARN = LANG_PROP_PREFIX + "missingIncludeWarnings";
  public static final String JSON_COMPILATION_DATABASE_KEY = LANG_PROP_PREFIX + "jsonCompilationDatabase";
  public static final String CPD_IGNORE_LITERALS_KEY = LANG_PROP_PREFIX + "cpd.ignoreLiterals";
  public static final String CPD_IGNORE_IDENTIFIERS_KEY = LANG_PROP_PREFIX + "cpd.ignoreIdentifiers";

  private static List<PropertyDefinition> generalProperties() {
    String subcateg = "(1) General";
    return new ArrayList<>(Arrays.asList(
      PropertyDefinition.builder(SOURCE_FILE_SUFFIXES_KEY).multiValues(true)
        .defaultValue(CLanguage.DEFAULT_SOURCE_SUFFIXES)
        .name("Source files suffixes")
        .description("Comma-separated list of suffixes for source files to analyze. Leave empty to use the default.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(1)
        .build(),
      PropertyDefinition.builder(HEADER_FILE_SUFFIXES_KEY).multiValues(true)
        .defaultValue(CLanguage.DEFAULT_HEADER_SUFFIXES)
        .name("Header files suffixes")
        .description("Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(2)
        .build(),
      PropertyDefinition.builder(INCLUDE_DIRECTORIES_KEY)
        .name("Include directories")
        .description("Comma-separated list of directories to search the included files in. May be defined either relative"
          + " to projects root or absolute.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(3)
        .build(),
      PropertyDefinition.builder(FORCE_INCLUDE_FILES_KEY)
        .subCategory(subcateg)
        .name("Force includes")
        .description("Comma-separated list of files which should to be included implicitly "
          + "at the beginning of each source file.")
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(4)
        .build(),
      PropertyDefinition.builder(DEFINES_KEY)
        .name("Default macros")
        .description("Additional macro definitions (one per line) to use when analysing the source code. Use to provide "
          + "macros which cannot be resolved by other means."
          + " Use the 'force includes' setting to inject more complex, multi-line macros.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.TEXT)
        .index(5)
        .build(),
      PropertyDefinition.builder(C_FILES_PATTERNS_KEY)
        .defaultValue(CLanguage.DEFAULT_C_FILES)
        .name("C source files patterns")
        .description("Comma-separated list of wildcard patterns used to detect C files. When a file matches any of the "
          + "patterns, it is parsed in C-compatibility mode.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(6)
        .build(),
      PropertyDefinition.builder(CPlugin.ERROR_RECOVERY_KEY)
        .defaultValue("True")
        .name("Parse error recovery")
        .description("Defines mode for error handling of report files and parsing errors. `False' (strict) breaks after"
          + " an error or 'True' (tolerant) continues.  See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/"
          + "wiki/Supported-configuration-properties#sonarcxxerrorrecoveryenabled'>sonar.cxx.errorRecoveryEnabled</a>"
          + "for a complete description.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.BOOLEAN)
        .index(7)
        .build(),
      PropertyDefinition.builder(CPlugin.MISSING_INCLUDE_WARN)
        .defaultValue("True")
        .name("Missing include warnings")
        .description("Enables/disables the warnings when included files could not be found.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.BOOLEAN)
        .index(8)
        .build(),
      PropertyDefinition.builder(CPlugin.JSON_COMPILATION_DATABASE_KEY)
        .subCategory(subcateg)
        .name("JSON Compilation Database")
        .description("JSON Compilation Database file to use as specification for what defines"
          + "and includes should be used for source files.")
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(9)
        .build()
    ));
  }

  private static List<PropertyDefinition> codeAnalysisProperties() {
    String subcateg = "(2) Code analysis";
    return new ArrayList<>(Arrays.asList(PropertyDefinition.builder(LANG_PROP_PREFIX
      + CxxCppCheckSensor.REPORT_PATH_KEY)
      .name("Cppcheck report(s)")
      .description("Path to a <a href='http://cppcheck.sourceforge.net/'>Cppcheck</a> analysis XML report, "
        + "relative to projects root. Both XML formats (version 1 and version 2) are supported."
        + USE_ANT_STYLE_WILDCARDS_2
      )
      .subCategory(subcateg)
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .index(1)
      .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCppCheckRuleRepository.CUSTOM_RULES_KEY)
        .name("Cppcheck custom rules")
        .description("XML definitions of custom Cppcheck rules, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(2)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxValgrindSensor.REPORT_PATH_KEY)
        .name("Valgrind report(s)")
        .description("Path to <a href='http://valgrind.org/'>Valgrind</a> report(s), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(3)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxValgrindRuleRepository.CUSTOM_RULES_KEY)
        .name("Valgrind custom rules")
        .description("XML definitions of custom Valgrind rules, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(4)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxDrMemorySensor.REPORT_PATH_KEY)
        .name("Dr Memory report(s)")
        .description("Path to <a href='http://drmemory.org/'>Dr. Memory</a> reports(s), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(5)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxPCLintSensor.REPORT_PATH_KEY)
        .name("PC-lint report(s)")
        .description("Path to <a href='http://www.gimpel.com/html/pcl.htm'>PC-lint</a> reports(s), relative to "
          + "projects root." + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(5)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxPCLintRuleRepository.CUSTOM_RULES_KEY)
        .name("PC-lint custom rules")
        .description("XML definitions of custom PC-lint rules, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(6)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxRatsSensor.REPORT_PATH_KEY)
        .name("RATS report(s)")
        .description("Path to <a href='https://code.google.com/p/rough-auditing-tool-for-security/'>RATS<a/> reports(s),"
          + "relative to projects root." + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(7)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxRatsRuleRepository.CUSTOM_RULES_KEY)
        .name("RATS custom rules")
        .description("XML definitions of custom RATS rules, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(8)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxVeraxxSensor.REPORT_PATH_KEY)
        .name("Vera++ report(s)")
        .description("Path to <a href='https://bitbucket.org/verateam'>Vera++</a> reports(s), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(9)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxVeraxxRuleRepository.CUSTOM_RULES_KEY)
        .name("Vera++ custom rules")
        .description("XML definitions of custom Vera++ rules, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(10)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxOtherSensor.REPORT_PATH_KEY)
        .name("External checkers report(s)")
        .description("Path to a code analysis report, which is generated by some unsupported code analyser, "
          + "relative to projects root." + USE_ANT_STYLE_WILDCARDS_1
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>"
          + "here</a> for details.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(11)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxOtherRepository.RULES_KEY)
        .name("External rules")
        .description("Rule sets for 'external' code analysers. Use one value per rule set."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Extending-the-code-analysis'>"
          + "this page</a> for details.")
        .type(PropertyType.TEXT)
        .multiValues(true)
        .subCategory(subcateg)
        .index(12)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxClangTidySensor.REPORT_PATH_KEY)
        .name("Clang-Tidy analyzer report(s)")
        .description("Path to Clang-Tidy reports, relative to projects root."
          + USE_ANT_STYLE_WILDCARDS_2)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(13)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxClangTidySensor.REPORT_CHARSET_DEF)
        .defaultValue(CxxClangTidySensor.DEFAULT_CHARSET_DEF)
        .name("Encoding")
        .description("The encoding to use when reading the clang-tidy report. Leave empty to use parser's default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(14)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxClangTidyRuleRepository.CUSTOM_RULES_KEY)
        .name("Clang-Tidy custom rules")
        .description("XML definitions of custom Clang-Tidy rules, which aren't builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(15)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxClangSASensor.REPORT_PATH_KEY)
        .name("Clang Static analyzer analyzer report(s)")
        .description("Path to Clang Static Analyzer reports, relative to projects root."
          + USE_ANT_STYLE_WILDCARDS_2)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(16)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxClangSARuleRepository.CUSTOM_RULES_KEY)
        .name("Clang-SA custom rules")
        .description("NO DESC")
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(17)
        .build()
    ));
  }

  private static List<PropertyDefinition> compilerWarningsProperties() {
    String subcateg = "(4) Compiler warnings";
    return new ArrayList<>(Arrays.asList(
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCompilerSensor.REPORT_PATH_KEY)
        .name("Compiler report(s)")
        .description("Path to compilers output (i.e. file(s) containg compiler warnings), relative to projects root."
          + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(1)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCompilerSensor.PARSER_KEY_DEF)
        .defaultValue(CxxCompilerSensor.DEFAULT_PARSER_DEF)
        .name("Format")
        .type(PropertyType.SINGLE_SELECT_LIST)
        .options(CxxCompilerVcParser.KEY_VC, CxxCompilerGccParser.KEY_GCC)
        .description("The format of the warnings file. Currently supported are Visual C++ and GCC.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(2)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCompilerSensor.REPORT_CHARSET_DEF)
        .defaultValue(CxxCompilerSensor.DEFAULT_CHARSET_DEF)
        .name("Encoding")
        .description("The encoding to use when reading the compiler report. Leave empty to use parser's default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(3)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCompilerSensor.REPORT_REGEX_DEF)
        .name("Custom matcher")
        .description("Regular expression to identify the four groups of the compiler warning message: file, line, ID, "
          + "message. For advanced usages. Leave empty to use parser's default. See <a href='"
          + "https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Compilers'>this page</a> for details regarding the "
          + "different regular expression that can be use per compiler.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(4)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCompilerVcRuleRepository.CUSTOM_RULES_KEY)
        .name("Custom rules for Visual C++ warnings")
        .description("XML definitions of custom rules for Visual C++ warnings, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(5)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCompilerGccRuleRepository.CUSTOM_RULES_KEY)
        .name("Custom rules for GCC warnings")
        .description("XML definitions of custom rules for GCC's warnings, which are'nt builtin into the plugin."
          + EXTENDING_THE_CODE_ANALYSIS)
        .type(PropertyType.TEXT)
        .subCategory(subcateg)
        .index(6)
        .build()
    ));
  }

  private static List<PropertyDefinition> testingAndCoverageProperties() {
    String subcateg = "(3) Testing & Coverage";
    return new ArrayList<>(Arrays.asList(
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxCoverageSensor.REPORT_PATH_KEY)
        .name("Unit test coverage report(s)")
        .description("Path to a report containing unit test coverage data, relative to projects root."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-code-coverage-metrics'>"
          + "here</a> for supported formats." + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(1)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxXunitSensor.REPORT_PATH_KEY)
        .name("Unit test execution report(s)")
        .description("Path to unit test execution report(s), relative to projects root."
          + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-test-execution-metrics'>"
          + "here</a> for supported formats." + USE_ANT_STYLE_WILDCARDS_1)
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(6)
        .build(),
      PropertyDefinition.builder(LANG_PROP_PREFIX + CxxXunitSensor.XSLT_URL_KEY)
        .name("XSLT transformer")
        .description("By default, the unit test execution reports are expected to be in the JUnitReport format."
          + " To import a report in an other format, set this property to an URL to a XSLT stylesheet which is able"
          + " to perform the according transformation.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .index(7)
        .build()
    ));
  }

  private static List<PropertyDefinition> duplicationsProperties() {
    String subcateg = "(5) Duplications";
    return new ArrayList<>(Arrays.asList(
      PropertyDefinition.builder(CPlugin.CPD_IGNORE_LITERALS_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .name("Ignores literal value differences when evaluating a duplicate block")
        .description("Ignores literal (numbers, characters and strings) value differences when evaluating a duplicate "
          + "block. This means that e.g. foo=42; and foo=43; will be seen as equivalent. Default is 'False'.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.BOOLEAN)
        .index(1)
        .build(),
      PropertyDefinition.builder(CPlugin.CPD_IGNORE_IDENTIFIERS_KEY)
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
    l.add(CLanguage.class);
    l.add(CDefaultProfile.class);
    l.add(CRuleRepository.class);

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
    l.add(CxxCoverageAggregator.class);

    // metrics
    l.add(CxxMetricsImp.class);

    // issue sensors
    l.add(CxxSquidSensorImpl.class);
    l.add(CxxRatsSensorImpl.class);
    l.add(CxxCppCheckSensorImpl.class);
    l.add(CxxPCLintSensorImpl.class);
    l.add(CxxDrMemorySensorImpl.class);
    l.add(CxxCompilerSensorImpl.class);
    l.add(CxxVeraxxSensorImpl.class);
    l.add(CxxValgrindSensorImpl.class);
    l.add(CxxClangTidySensorImpl.class);
    l.add(CxxClangSASensorImpl.class);
    l.add(CxxExternalRulesSensorImpl.class);

    // test sensors
    l.add(CxxXunitSensorImpl.class);
    l.add(CxxCoverageSensorImpl.class);

    // rule provides
    l.add(CxxRatsRuleRepositoryImpl.class);
    l.add(CxxCppCheckRuleRepositoryImpl.class);
    l.add(CxxPCLintRuleRepositoryImpl.class);
    l.add(CxxDrMemoryRuleRepositoryImpl.class);
    l.add(CxxCompilerVcRuleRepositoryImpl.class);
    l.add(CxxCompilerGccRuleRepositoryImpl.class);
    l.add(CxxVeraxxRuleRepositoryImpl.class);
    l.add(CxxValgrindRuleRepositoryImpl.class);
    l.add(CxxExternalRuleRepositoryImpl.class);
    l.add(CxxClangTidyRuleRepositoryImpl.class);
    l.add(CxxClangSARuleRepositoryImpl.class);

    return l;
  }

  public static class CxxMetricsImp extends CxxMetrics {

    public CxxMetricsImp(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxRatsRuleRepositoryImpl extends CxxRatsRuleRepository {

    public CxxRatsRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxCppCheckRuleRepositoryImpl extends CxxCppCheckRuleRepository {

    public CxxCppCheckRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxPCLintRuleRepositoryImpl extends CxxPCLintRuleRepository {

    public CxxPCLintRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxDrMemoryRuleRepositoryImpl extends CxxDrMemoryRuleRepository {

    public CxxDrMemoryRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxCompilerVcRuleRepositoryImpl extends CxxCompilerVcRuleRepository {

    public CxxCompilerVcRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxCompilerGccRuleRepositoryImpl extends CxxCompilerGccRuleRepository {

    public CxxCompilerGccRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxVeraxxRuleRepositoryImpl extends CxxVeraxxRuleRepository {

    public CxxVeraxxRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxValgrindRuleRepositoryImpl extends CxxValgrindRuleRepository {

    public CxxValgrindRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxExternalRuleRepositoryImpl extends CxxOtherRepository {

    public CxxExternalRuleRepositoryImpl(RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxClangTidyRuleRepositoryImpl extends CxxClangTidyRuleRepository {

    public CxxClangTidyRuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxClangSARuleRepositoryImpl extends CxxClangSARuleRepository {

    public CxxClangSARuleRepositoryImpl(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
      Configuration settings) {
      super(fileSystem, xmlRuleLoader, new CLanguage(settings));
    }
  }

  public static class CxxSquidSensorImpl extends CxxSquidSensor {

    public CxxSquidSensorImpl(Configuration settings,
      FileLinesContextFactory fileLinesContextFactory,
      CheckFactory checkFactory) {
      super(new CLanguage(settings), fileLinesContextFactory, checkFactory);
    }

    public CxxSquidSensorImpl(Configuration settings,
      FileLinesContextFactory fileLinesContextFactory,
      CheckFactory checkFactory,
      @Nullable CustomCxxRulesDefinition[] customRulesDefinition) {
      super(new CLanguage(settings), fileLinesContextFactory, checkFactory, customRulesDefinition);
    }
  }

  public static class CxxRatsSensorImpl extends CxxRatsSensor {

    public CxxRatsSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxXunitSensorImpl extends CxxXunitSensor {

    public CxxXunitSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxCoverageSensorImpl extends CxxCoverageSensor {

    public CxxCoverageSensorImpl(Configuration settings, CxxCoverageAggregator cache, SensorContext context) {
      super(cache, new CLanguage(settings), context);
    }
  }

  public static class CxxCppCheckSensorImpl extends CxxCppCheckSensor {

    public CxxCppCheckSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxPCLintSensorImpl extends CxxPCLintSensor {

    public CxxPCLintSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxDrMemorySensorImpl extends CxxDrMemorySensor {

    public CxxDrMemorySensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxCompilerSensorImpl extends CxxCompilerSensor {

    public CxxCompilerSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxVeraxxSensorImpl extends CxxVeraxxSensor {

    public CxxVeraxxSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxValgrindSensorImpl extends CxxValgrindSensor {

    public CxxValgrindSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxClangTidySensorImpl extends CxxClangTidySensor {

    public CxxClangTidySensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxClangSASensorImpl extends CxxClangSASensor {

    public CxxClangSASensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
  }

  public static class CxxExternalRulesSensorImpl extends CxxOtherSensor {

    public CxxExternalRulesSensorImpl(Configuration settings) {
      super(new CLanguage(settings));
    }
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
