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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    String reportEncoding = context.config().get(REPORT_ENCODING_DEF).orElse(DEFAULT_ENCODING_DEF);

    try ( var scanner = new TextScanner(report, reportEncoding)) {
      LOG.debug("Encoding='{}'", scanner.encoding());

      // sample:
      // c:\a\file.cc:5:20: warning: ... conversion from string literal to 'char *' [clang-diagnostic-writable-strings]
      CxxReportIssue currentIssue = null;
      while (scanner.hasNextLine()) {
        var issue = Issue.create(scanner.nextLine());
        if (issue != null) {
          if ("note".equals(issue.level)) {
            if (currentIssue != null) {
              currentIssue.addFlowElement(issue.path, issue.line, issue.column, issue.info);
            }
          } else {
            if (currentIssue != null) {
              saveUniqueViolation(currentIssue);
            }
            currentIssue = new CxxReportIssue(issue.ruleId, issue.path, issue.line, issue.column, issue.info);
            for (var aliasRuleId : issue.aliasRuleIds) {
              currentIssue.addAliasRuleId(aliasRuleId);
            }
          }
        }
      }
      if (currentIssue != null) {
        saveUniqueViolation(currentIssue);
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

  private static class Issue {

    private static final String REGEX = "(.+|[a-zA-Z]:\\\\.+):([0-9]+):([0-9]+): ([^:]+): (.+)";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private String path;
    private String line;
    private String column;
    private String level;
    private String ruleId;
    private LinkedList<String> aliasRuleIds = new LinkedList<>();
    private String info;

    static Issue create(String data) {
      var matcher = PATTERN.matcher(data);
      if (matcher.matches()) {
        var issue = new Issue();
        // group: 1      2      3         4        5
        //      <path>:<line>:<column>: <level>: <info>
        var m = matcher.toMatchResult();
        issue.path = m.group(1); // relative paths
        issue.line = m.group(2);
        issue.column = m.group(3);
        issue.level = m.group(4); // error, warning, note, ...
        issue.info = m.group(5); // info [ruleIds]

        issue.splitRuleId();
        return issue;
      }
      return null;
    }

    void splitRuleId() {
      ruleId = getDefaultRuleId();

      if (info.endsWith("]")) { // [ruleIds]
        var end = info.length() - 1;
        for (var start = info.length() - 2; start >= 0; start--) {
          var c = info.charAt(start);
          if (!(Character.isLetterOrDigit(c) || c == '-' || c == '.' || c == '_')) {
            if (c == ',') {
              var aliasId = info.substring(start + 1, end);
              if (!"warnings-as-errors".equals(aliasId)) {
                aliasRuleIds.addFirst(aliasId);
              }
              end = start;
              continue;
            } else if (c == '[') {
              ruleId = info.substring(start + 1, end);
              info = info.substring(0, start - 1);
            }
            break;
          }
        }
      }
    }

    String getDefaultRuleId() {
      Map<String, String> map = Map.of(
        "note", "",
        "warning", "clang-diagnostic-warning",
        "error", "clang-diagnostic-error",
        "fatal error", "clang-diagnostic-error"
      );

      return map.getOrDefault(level, "clang-diagnostic-unknown");
    }
  }

}
