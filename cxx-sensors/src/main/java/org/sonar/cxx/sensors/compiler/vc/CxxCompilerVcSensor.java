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
package org.sonar.cxx.sensors.compiler.vc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;

public class CxxCompilerVcSensor extends CxxCompilerSensor {

  public static final String KEY = "Visual C++";
  public static final String REPORT_PATH_KEY = "sonar.cxx.vc.reportPaths";
  public static final String REPORT_REGEX_DEF = "sonar.cxx.vc.regex";
  public static final String REPORT_CHARSET_DEF = "sonar.cxx.vc.charset";
  public static final String DEFAULT_CHARSET_DEF = StandardCharsets.UTF_8.name();
  public static final String DEFAULT_REGEX_DEF
                               = "(.*>)?(?<file>.*)\\((?<line>\\d+)\\)\\x20:\\x20warning\\x20(?<id>C\\d+):(?<message>.*)";

  public static List<PropertyDefinition> properties() {
    String category = "CXX External Analyzers";
    String subcategory = "Compiler";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("VC Compiler Report(s)")
        .description("Path to compilers output (i.e. file(s) containg compiler warnings), relative to projects root."
                       + USE_ANT_STYLE_WILDCARDS)
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(REPORT_CHARSET_DEF)
        .defaultValue(DEFAULT_CHARSET_DEF)
        .name("VC Report Encoding")
        .description("The encoding to use when reading the compiler report. Leave empty to use parser's default UTF-8.")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(REPORT_REGEX_DEF)
        .name("VC Regular Expression")
        .description("Regular expression to identify the four named groups of the compiler warning message:"
                       + " &lt;file&gt;, &lt;line&gt;, &lt;column&gt;, &lt;id&gt;, &lt;message&gt;. Leave empty to use parser's default."
                     + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Compilers'>"
                       + "this page</a> for details regarding the different regular expression that can be use per compiler.")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Visual Studio compiler report import")
      .onlyOnLanguage("cxx")
      .createIssuesForRuleRepositories(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  protected String getCompilerKey() {
    return KEY;
  }

  @Override
  protected String getCharset() {
    return context.config().get(REPORT_CHARSET_DEF).orElse(DEFAULT_CHARSET_DEF);
  }

  @Override
  protected String getRegex() {
    return context.config().get(REPORT_REGEX_DEF).orElse(DEFAULT_REGEX_DEF);
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxCompilerVcRuleRepository.KEY;
  }

}
