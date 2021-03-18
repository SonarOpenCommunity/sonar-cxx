/*
 * Sonar C++ Plugin (Community)
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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.TextScanner;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Sensor for clang-tidy
 */
public class CxxClangTidySensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.clangtidy.reportPaths";
  public static final String REPORT_ENCODING_DEF = "sonar.cxx.clangtidy.encoding";
  public static final String DEFAULT_ENCODING_DEF = StandardCharsets.UTF_8.name();
  private static final Logger LOG = Loggers.get(CxxClangTidySensor.class);

  private static final String REGEX
                                = "(.+|[a-zA-Z]:\\\\.+):([0-9]+):([0-9]+): ([^:]+): (.+)";
  private static final Pattern PATTERN = Pattern.compile(REGEX);

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

  /**
   * Extract the rule id from info
   *
   * @param info text (with rule id)
   * @param defaultRuleId rule id to use if info has no or invalid rule id
   * @return sting array: ruleId, info
   */
  protected static String[] splitRuleId(String info, String defaultRuleId) {
    String ruleId = defaultRuleId;

    if (info.endsWith("]")) { // [ruleId]
      for (var i = info.length() - 2; i >= 0; i--) {
        char c = info.charAt(i);
        if (!(Character.isLetterOrDigit(c) || c == '-' || c == '.' || c == '_')) {
          if (c == '[') {
            ruleId = info.substring(i + 1, info.length() - 1);
            info = info.substring(0, i - 1);
          }
          break;
        }
      }
    }
    return new String[]{ruleId, info};
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Clang-Tidy report import")
      .onlyOnLanguages("cxx","cpp", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  protected void processReport(File report)  {
    String reportEncoding = context.config().get(REPORT_ENCODING_DEF).orElse(DEFAULT_ENCODING_DEF);
    LOG.debug("Encoding='{}'", reportEncoding);

    try ( var scanner = new TextScanner(report, reportEncoding)) {
      // sample:
      // c:\a\file.cc:5:20: warning: ... conversion from string literal to 'char *' [clang-diagnostic-writable-strings]
      CxxReportIssue currentIssue = null;
      while (scanner.hasNextLine()) {
        String nextLine = scanner.nextLine();
        final Matcher matcher = PATTERN.matcher(nextLine);
        if (matcher.matches()) {
          // group: 1      2      3         4        5
          //      <path>:<line>:<column>: <level>: <info>
          MatchResult m = matcher.toMatchResult();
          String path = m.group(1); // relative paths
          String line = m.group(2);
          String column = m.group(3);
          String level = m.group(4); // error, warning, note, ...
          String info = m.group(5); // info [ruleId]

          CxxReportIssue newIssue = null;
          Boolean saveIssue = true;

          switch (level) {
            case "note":
              saveIssue = false;
              if (currentIssue != null) {
                currentIssue.addFlowElement(path, line, column, info);
              }
              break;
            case "warning": {
              String [] rule = splitRuleId(info, "clang-diagnostic-warning");
              newIssue = new CxxReportIssue(rule[0], path, line, column, rule[1]);
            }
              break;
            case "error":
            case "fatal error": {
              String [] rule = splitRuleId(info, "clang-diagnostic-error");
              newIssue = new CxxReportIssue(rule[0], path, line, column, rule[1]);
            }
              break;
            default: {
              String [] rule = splitRuleId(info, "clang-diagnostic-unknown");
              newIssue = new CxxReportIssue(rule[0], path, line, column, rule[1]);
            }
              break;
          }
          if (saveIssue) {
            if (currentIssue != null) {
              saveUniqueViolation(currentIssue);
              currentIssue = null;
            }
            currentIssue = newIssue;
            newIssue = null;
          }
        }
      }
      if (currentIssue != null) {
        saveUniqueViolation(currentIssue);
        currentIssue = null;
      }
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
