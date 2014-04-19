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
package org.sonar.plugins.cxx;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.cxx.coverage.CxxCoverageSensor;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckRuleRepository;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckSensor;
import org.sonar.plugins.cxx.externalrules.CxxExternalRuleRepository;
import org.sonar.plugins.cxx.externalrules.CxxExternalRulesSensor;
import org.sonar.plugins.cxx.pclint.CxxPCLintRuleRepository;
import org.sonar.plugins.cxx.pclint.CxxPCLintSensor;
import org.sonar.plugins.cxx.compiler.CxxCompilerVcRuleRepository;
import org.sonar.plugins.cxx.compiler.CxxCompilerGccRuleRepository;
import org.sonar.plugins.cxx.compiler.CxxCompilerVcParser;
import org.sonar.plugins.cxx.compiler.CxxCompilerGccParser;
import org.sonar.plugins.cxx.compiler.CxxCompilerSensor;
import org.sonar.plugins.cxx.rats.CxxRatsRuleRepository;
import org.sonar.plugins.cxx.rats.CxxRatsSensor;
import org.sonar.plugins.cxx.squid.CxxSquidSensor;
import org.sonar.plugins.cxx.valgrind.CxxValgrindRuleRepository;
import org.sonar.plugins.cxx.valgrind.CxxValgrindSensor;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxRuleRepository;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxSensor;
import org.sonar.plugins.cxx.xunit.CxxXunitSensor;

import java.util.ArrayList;
import java.util.List;

@Properties({
  @Property(
    key = CxxPlugin.INCLUDE_DIRECTORIES_KEY,
    defaultValue = "",
    name = "Directories to search included files in",
    description = "The include directories may be defined either relative to projects' root or absolute.",
    global = true,
    project = true),
  @Property(
    key = CxxPlugin.DEFINES_KEY,
    defaultValue = "",
    name = "Default macro definitions",
    description = "Macro definition to use while analysing the source. Use to provide macros which cannot be resolved by other means.",
    type = PropertyType.TEXT,
    global = true,
    project = true),
  @Property(
    key = CxxPlugin.SOURCE_FILE_SUFFIXES_KEY,
    defaultValue = CxxLanguage.DEFAULT_SOURCE_SUFFIXES,
    name = "Source files suffixes",
    description = "Comma-separated list of suffixes for source files to analyze. Leave empty to use the default.",
    global = true,
    project = true),
  @Property(
    key = CxxPlugin.HEADER_FILE_SUFFIXES_KEY,
    defaultValue = CxxLanguage.DEFAULT_HEADER_SUFFIXES,
    name = "Header files suffixes",
    description = "Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.",
    global = true,
    project = true),
  @Property(
    key = CxxCppCheckSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to cppcheck report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxPCLintSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to pclint report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxCompilerSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to C++ compiler report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxCompilerSensor.PARSER_KEY_DEF,
    defaultValue = CxxCompilerSensor.DEFAULT_PARSER_DEF,
    type = PropertyType.SINGLE_SELECT_LIST,
    options = { CxxCompilerVcParser.KEY, CxxCompilerGccParser.KEY },
    name = "Compiler parser to use",
    description = "The kind of compiler parser to use.",
    global = false,
    project = true),
  @Property(
    key = CxxCompilerSensor.REPORT_REGEX_DEF,
    defaultValue = "",
    name = "RegEx to identify the 4 groups of the compiler warning message",
    description = "Java regular expression with 4 groups for file, line, message ID, message. Leave empty to use the parser's default.",
    global = false,
    project = true),
  @Property(
    key = CxxCompilerSensor.REPORT_CHARSET_DEF,
    defaultValue = "",
    name = "Encoding of the compiler report",
    description = "The encoding to use when reading the compiler report. Leave empty to use the parser's default.",
    global = false,
    project = true),
  @Property(
    key = CxxCoverageSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to unit test coverage report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxCoverageSensor.IT_REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to integration test coverage report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxCoverageSensor.OVERALL_REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to overall test coverage report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxRatsSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to rats report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxValgrindSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to valgrind report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxVeraxxSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to vera++ report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxXunitSensor.REPORT_PATH_KEY,
    defaultValue = "",
    name = "Path to unit test execution report(s)",
    description = "Relative to projects' root. Ant patterns are accepted",
    global = false,
    project = true),
  @Property(
    key = CxxXunitSensor.XSLT_URL_KEY,
    defaultValue = "",
    name = "URL of the xslt transformer",
    description = "TODO",
    global = false,
    project = true),
  @Property(
    key = CxxPlugin.ERROR_RECOVERY_KEY,
    defaultValue = "true",
    name = "Parse error recovery control",
    description = "Enables/disables the parse error recovery. For development purposes.",
    type = PropertyType.BOOLEAN,
    global = false,
    project = true)
    })
public final class CxxPlugin extends SonarPlugin {
  static final String SOURCE_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.sources";
  static final String HEADER_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.headers";
  public static final String DEFINES_KEY = "sonar.cxx.defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "sonar.cxx.include_directories";
  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  
  /**
   * {@inheritDoc}
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> l = new ArrayList<Class<? extends Extension>>();
    l.add(CxxLanguage.class);
    l.add(CxxSourceImporter.class);
    l.add(CxxColorizer.class);
    l.add(CxxSquidSensor.class);
    l.add(CxxCpdMapping.class);
    l.add(CxxRatsRuleRepository.class);
    l.add(CxxRatsSensor.class);
    l.add(CxxXunitSensor.class);
    l.add(CxxCoverageSensor.class);
    l.add(CxxCppCheckRuleRepository.class);
    l.add(CxxCppCheckSensor.class);
    l.add(CxxPCLintRuleRepository.class);
    l.add(CxxPCLintSensor.class);
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
    l.add(CxxRuleRepositoryProvider.class);

    return l;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
