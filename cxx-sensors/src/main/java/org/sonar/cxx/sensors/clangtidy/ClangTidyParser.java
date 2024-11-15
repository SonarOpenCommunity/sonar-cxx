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
package org.sonar.cxx.sensors.clangtidy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.TextScanner;
import org.sonar.cxx.utils.CxxReportIssue;

public class ClangTidyParser {

  private static final Logger LOG = LoggerFactory.getLogger(ClangTidyParser.class);

  private static final String LINE_REGEX = "((?>[a-zA-Z]:[\\\\/])??[^:]++):(\\d{1,5}):(\\d{1,5}): ([^:]++): (.+)";
  private static final Pattern LINE_PATTERN = Pattern.compile(LINE_REGEX);

  private static final Map<String, String> map = Map.of(
    "note", "",
    "warning", "clang-diagnostic-warning",
    "error", "clang-diagnostic-error",
    "fatal error", "clang-diagnostic-error"
  );

  private final CxxIssuesReportSensor sensor;

  public ClangTidyParser(CxxIssuesReportSensor sensor) {
    this.sensor = sensor;
  }

  public void parse(File report, String defaultEncoding) throws IOException {
    try (var scanner = new TextScanner(report, defaultEncoding)) {
      LOG.debug("Encoding='{}'", scanner.encoding());

      CxxReportIssue issue = null;
      while (scanner.hasNextLine()) {
        LineData data = parseLine(scanner.nextLine());
        if (data == null) {
          continue;
        }
        if ("note".equals(data.level)) {
          if (issue != null) {
            issue.addFlowElement(data.path, data.line, data.column, data.info);
          }
        } else {
          if (issue != null) {
            sensor.saveUniqueViolation(issue);
          }
          issue = new CxxReportIssue(data.ruleId, data.path, data.line, data.column, data.info);
          for (var aliasRuleId : data.aliasRuleIds) {
            issue.addAliasRuleId(aliasRuleId);
          }
        }
      }
      if (issue != null) {
        sensor.saveUniqueViolation(issue);
      }
    }
  }

  private static LineData parseLine(String line) {
    var lineMatcher = LINE_PATTERN.matcher(line);
    if (lineMatcher.matches()) {
      var data = new LineData();
      // group: 1      2      3         4        5
      //      <path>:<line>:<column>: <level>: <info> [ruleIds]
      // sample:
      //      c:\a\file.cc:5:20: warning: txt txt [clang-diagnostic-writable-strings]
      var lineMatchResult = lineMatcher.toMatchResult();
      data.path = lineMatchResult.group(1);   // relative paths
      data.line = lineMatchResult.group(2);   // 1...n
      data.column = lineMatchResult.group(3); // 1...n
      data.level = lineMatchResult.group(4);  // error, warning, note, ...
      data.info = lineMatchResult.group(5);   // info [ruleIds]

      adjustColumn(data);
      parseRuleIds(data);

      return data;
    }

    return null;
  }

  private static void adjustColumn(LineData data) {
    try {
      // Clang-Tidy column numbers are from 1...n and SQ is using 0...n
      data.column = Integer.toString(Integer.parseInt(data.column) - 1);
    } catch (java.lang.NumberFormatException e) {
      data.column = "";
    }
  }

  private static void parseRuleIds(LineData data) {
    // info [ruleId, aliasId, ...]
    if (data.info.endsWith("]")) {
      int pos = data.info.lastIndexOf('[');
      if (pos != -1) {
        for (var ruleId : data.info.substring(pos + 1, data.info.length() - 1).trim().split("\\s*+,\\s*+")) {
          if (data.ruleId == null) {
            data.ruleId = ruleId;
          } else {
            if (!"-warnings-as-errors".equals(ruleId)) {
              data.aliasRuleIds.add(ruleId);
            }
          }
        }
        data.info = data.info.substring(0, pos - 1);
      }
    }

    if (data.ruleId != null) {
      // map Clang warning (-W<warning>) to Clang-Tidy warning (clang-diagnostic-<warning>)
      if (data.ruleId.startsWith("-W")) {
        data.ruleId = "clang-diagnostic-" + data.ruleId.substring(2);
      }
    } else {
      data.ruleId = getDefaultRuleId(data.level);
    }
  }

  private static String getDefaultRuleId(String level) {
    return map.getOrDefault(level, "clang-diagnostic-unknown");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private static class LineData {

    private String path;
    private String line;
    private String column;
    private String level;
    private String ruleId;
    private ArrayList<String> aliasRuleIds = new ArrayList<>();
    private String info;
  }

}
