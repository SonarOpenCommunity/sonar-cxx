/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.sensors.clangtidy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.InvalidReportException;

/**
 * Sensor for clang-tidy
 */
public class CxxClangTidySensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.clangtidy.reportPaths";
  public static final String REPORT_ENCODING_DEF = "sonar.cxx.clangtidy.encoding";
  public static final String DEFAULT_ENCODING_DEF = StandardCharsets.UTF_8.name();

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Clang-Tidy Report(s)")
        .description(
          "Comma-separated paths (absolute or relative to the project base directory) to `*.txt` files with"
            + " `Clang-Tidy` issues. Ant patterns are accepted for relative paths."
        )
        .category("CXX External Analyzers")
        .subCategory("Clang-Tidy")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(REPORT_ENCODING_DEF)
        .defaultValue(DEFAULT_ENCODING_DEF)
        .name("Clang-Tidy Report Encoding")
        .description("Defines the encoding to be used to read the files from `sonar.cxx.clangtidy.reportPaths`"
                       + " (default is `UTF-8`).")
        .category("CXX External Analyzers")
        .subCategory("Clang-Tidy")
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Clang-Tidy report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  protected void processReport(File report) {
    var parser = new ClangTidyParser(this);
    try {
      String defaultEncoding = context.config().get(REPORT_ENCODING_DEF).orElse(DEFAULT_ENCODING_DEF);
      parser.parse(report, defaultEncoding);
    } catch (final java.io.IOException
                     | java.lang.IllegalArgumentException
                     | java.lang.IllegalStateException
                     | java.util.InputMismatchException e) {
      throw new InvalidReportException("The 'Clang-Tidy' report is invalid", e);
    }
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxClangTidyRuleRepository.KEY;
  }

}
