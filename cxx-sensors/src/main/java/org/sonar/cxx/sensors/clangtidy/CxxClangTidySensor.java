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
package org.sonar.cxx.sensors.clangtidy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxMetrics;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Sensor for clang-tidy
 */
public class CxxClangTidySensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.clangtidy.reportPath";
  public static final String REPORT_CHARSET_DEF = "sonar.cxx.clangtidy.charset";
  public static final String DEFAULT_CHARSET_DEF = StandardCharsets.UTF_8.name();
  private static final Logger LOG = Loggers.get(CxxClangTidySensor.class);

  private static final String REGEX
                                = "(.+|[a-zA-Z]:\\\\.+):([0-9]+):([0-9]+): ([^:]+): (.+)";
  private static final Pattern PATTERN = Pattern.compile(REGEX);

  /**
   * CxxClangTidySensor for clang-tidy Sensor
   *
   * @param settings sensor configuration
   */
  public CxxClangTidySensor(Configuration settings) {
    super(settings, REPORT_PATH_KEY, CxxClangTidyRuleRepository.KEY);
  }

  public static List<PropertyDefinition> properties() {
    String subcateg = "Clang-Tidy";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Clang-Tidy analyzer report(s)")
        .description("Path to Clang-Tidy reports, relative to projects root. If neccessary, "
                       + "<a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(REPORT_CHARSET_DEF)
        .defaultValue(DEFAULT_CHARSET_DEF)
        .name("Encoding")
        .description("The encoding to use when reading the clang-tidy report."
                       + " Leave empty to use parser's default UTF-8.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " ClangTidySensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected void processReport(final SensorContext context, File report) {
    final String reportCharset = getContextStringProperty(context,
                                                          REPORT_CHARSET_DEF, DEFAULT_CHARSET_DEF);
    LOG.debug("Parsing 'clang-tidy' report, CharSet= '{}'", reportCharset);

    try (Scanner scanner = new Scanner(report, reportCharset)) {
      // sample:
      // E:\Development\SonarQube\cxx\sonar-cxx\sonar-cxx-plugin\src\test\resources\org\sonar\plugins\cxx\
      //   reports-project\clang-tidy-reports\..\..\cpd.cc:76:20:
      //   warning: ISO C++11 does not allow conversion from string literal to 'char *'
      //   [clang-diagnostic-writable-strings]
      CxxReportIssue issue = null;
      while (scanner.hasNextLine()) {
        String nextLine = scanner.nextLine();
        final Matcher matcher = PATTERN.matcher(nextLine);
        if (matcher.matches()) {
          // group: 1      2      3         4        5
          //      <path>:<line>:<column>: <level>: <txt>
          MatchResult m = matcher.toMatchResult();
          String path = m.group(1); // relative paths
          String line = m.group(2);
          //String column = m.group(3);
          String level = m.group(4); // error, warning, note, ...
          String txt = m.group(5); // info( [ruleId])?
          String info = null;
          String ruleId = null;

          if (txt.endsWith("]")) { // [ruleId]
            for (int i = txt.length() - 2; i >= 0; i--) {
              char c = txt.charAt(i);
              if (c == '[') {
                info = txt.substring(0, i - 1);
                ruleId = txt.substring(i + 1, txt.length() - 1);
                break;
              }
              if (!(Character.isLetterOrDigit(c) || c == '-' || c == '.')) {
                break;
              }
            }
          }
          if (info == null) {
            info = txt;
          }

          if (ruleId != null) {
            if (issue != null) {
              saveUniqueViolation(context, issue);
            }
            issue = new CxxReportIssue(ruleId, path, line, info);
          } else if ((issue != null) && "note".equals(level)) {
            issue.addFlowElement(path, line, info);
          }
        }
      }
      if (issue != null) {
        saveUniqueViolation(context, issue);
      }
    } catch (final java.io.FileNotFoundException
                     | java.lang.IllegalArgumentException
                     | java.lang.IllegalStateException
                     | java.util.InputMismatchException e) {
      LOG.error("Failed to parse clang-tidy report: {}", e);
    }
  }

  @Override
  protected String getMetricKey() {
    return CxxMetrics.CLANG_TIDY_SENSOR_ISSUES_KEY;
  }

}
