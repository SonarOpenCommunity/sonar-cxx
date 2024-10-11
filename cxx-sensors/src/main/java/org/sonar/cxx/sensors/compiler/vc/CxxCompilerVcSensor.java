/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
  public static final String REPORT_ENCODING_DEF = "sonar.cxx.vc.encoding";
  public static final String DEFAULT_ENCODING_DEF = StandardCharsets.UTF_8.name();
  public static final String DEFAULT_REGEX_DEF = """
    (?>[^>]*+>)?(?<file>(?>[^\\\\]{1,260}\\\\)*[^\\\\]{1,260})\\((?<line>\\d{1,5})\\)\\x20?:\
    \\x20warning\\x20(?<id>C\\d{4,5}):\\x20?(?<message>.*)""";

  public static List<PropertyDefinition> properties() {
    var category = "CXX External Analyzers";
    var subcategory = "Visual C++";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Visual C++ Compiler Report(s)")
        .description("""
                     Comma-separated paths (absolute or relative to the project base directory) to `*.log` files with \
                     `Visual Studio` warnings. Ant patterns are accepted for relative paths.""")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(REPORT_ENCODING_DEF)
        .defaultValue(DEFAULT_ENCODING_DEF)
        .name("VC Report Encoding")
        .description(
          "Defines the encoding to be used to read the files from `sonar.cxx.vc.reportPaths` (default is `UTF-8`)."
        )
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(REPORT_REGEX_DEF)
        .name("VC Regular Expression")
        .description("""
                     Java regular expressions to parse the `Visual Studio` warnings. You can use the named-capturing \
                     groups `<file>`, `<line>`, `<column>`, `<id>` and `<message>`.""")
        .category(category)
        .subCategory(subcategory)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Visual C++ compiler report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepositories(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  protected String getCompilerKey() {
    return KEY;
  }

  @Override
  protected String getEncoding() {
    return context.config().get(REPORT_ENCODING_DEF).orElse(DEFAULT_ENCODING_DEF);
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
