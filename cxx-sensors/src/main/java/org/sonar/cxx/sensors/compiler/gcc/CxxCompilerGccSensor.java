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
package org.sonar.cxx.sensors.compiler.gcc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;

public class CxxCompilerGccSensor extends CxxCompilerSensor {

  public static final String KEY = "GCC";
  public static final String REPORT_PATH_KEY = "sonar.cxx.gcc.reportPath";
  public static final String REPORT_REGEX_DEF = "sonar.cxx.gcc.regex";
  public static final String REPORT_CHARSET_DEF = "sonar.cxx.gcc.charset";
  public static final String DEFAULT_CHARSET_DEF = StandardCharsets.UTF_8.name();
  /**
   * Default id used for gcc warnings not associated with any activation switch.
   */
  public static final String DEFAULT_ID = "default";
  public static final String DEFAULT_REGEX_DEF
                               = "(?<file>.*):(?<line>[0-9]+):[0-9]+:\\x20warning:\\x20(?<message>.*?)(\\x20\\[(?<id>.*)\\])?\\s*$";

  public static List<PropertyDefinition> properties() {
    String subcateg = "GCC Compiler Warnings";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("GCC Compiler Report(s)")
        .description("Path to compilers output (i.e. file(s) containg compiler warnings), relative to projects root."
                       + USE_ANT_STYLE_WILDCARDS)
        .category("CXX External Analyzers")
        .subCategory("Compiler")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(REPORT_CHARSET_DEF)
        .defaultValue(CxxCompilerGccSensor.DEFAULT_CHARSET_DEF)
        .name("GCC Report Encoding")
        .description("The encoding to use when reading the compiler report. Leave empty to use parser's default UTF-8.")
        .category("CXX External Analyzers")
        .subCategory("Compiler")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(REPORT_REGEX_DEF)
        .name("GCC Regular Expression")
        .description("Regular expression to identify the four named groups of the compiler warning message:"
                       + " <file>, <line>, <id>, <message>. Leave empty to use parser's default."
                       + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Compilers'>"
                       + "this page</a> for details regarding the different regular expression that can be use per compiler.")
        .category("CXX External Analyzers")
        .subCategory("Compiler")
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX GCC compiler report import")
      .onlyOnLanguage("c++")
      .createIssuesForRuleRepositories(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected String getCompilerKey() {
    return KEY;
  }

  @Override
  protected String getCharset(final SensorContext context) {
    return getContextStringProperty(context, REPORT_CHARSET_DEF, DEFAULT_CHARSET_DEF);
  }

  @Override
  protected String getRegex(final SensorContext context) {
    return getContextStringProperty(context, REPORT_REGEX_DEF, DEFAULT_REGEX_DEF);
  }

  @Override
  protected String alignId(String id) {
    /* Some gcc warnings are not associated to any activation switch and don't have a matching id.
	 * In these cases a default id is used.
     */
    if (id == null || "".equals(id)) {
      id = DEFAULT_ID;
    }
    return id.replaceAll("=$", "");
  }

  @Override
  protected String getReportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxCompilerGccRuleRepository.KEY;
  }

}
