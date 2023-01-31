/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.TextScanner;
import org.sonar.cxx.utils.CxxReportIssue;

public class ClangTidyParser {

  private static final Logger LOG = Loggers.get(ClangTidyParser.class);

  private static final String REGEX = "((?>[a-zA-Z]:[\\\\/])??[^:]++):(\\d{1,5}):(\\d{1,5}): ([^:]++): (.+)";
  private static final Pattern PATTERN = Pattern.compile(REGEX);

  private final CxxClangTidySensor sensor;
  private Issue issue = null;

  public ClangTidyParser(CxxClangTidySensor sensor) {
    this.sensor = sensor;
  }

  public void parse(File report, String defaultEncoding) throws IOException {
    try (var scanner = new TextScanner(report, defaultEncoding)) {
      LOG.debug("Encoding='{}'", scanner.encoding());

      CxxReportIssue currentIssue = null;
      while (scanner.hasNextLine()) {
        if (!parseLine(scanner.nextLine())) {
          continue;
        }
        if ("note".equals(issue.level)) {
          if (currentIssue != null) {
            currentIssue.addFlowElement(issue.path, issue.line, issue.column, issue.info);
          }
        } else {
          if (currentIssue != null) {
            sensor.saveUniqueViolation(currentIssue);
          }
          currentIssue = new CxxReportIssue(issue.ruleId, issue.path, issue.line, issue.column, issue.info);
          for (var aliasRuleId : issue.aliasRuleIds) {
            currentIssue.addAliasRuleId(aliasRuleId);
          }
        }
      }
      if (currentIssue != null) {
        sensor.saveUniqueViolation(currentIssue);
      }
    }
  }

  private boolean parseLine(String data) {
    var matcher = PATTERN.matcher(data);
    issue = null;
    if (matcher.matches()) {
      issue = new Issue();
      // group: 1      2      3         4        5
      //      <path>:<line>:<column>: <level>: <info> [ruleIds]
      // sample:
      //      c:\a\file.cc:5:20: warning: txt txt [clang-diagnostic-writable-strings]
      var m = matcher.toMatchResult();
      issue.path = m.group(1);   // relative paths
      issue.line = m.group(2);   // 1...n
      issue.column = m.group(3); // 1...n
      issue.level = m.group(4);  // error, warning, note, ...
      issue.info = m.group(5);   // info [ruleIds]

      // Clang-Tidy column numbers are from 1...n and SQ is using 0...n
      try {
        issue.column = Integer.toString(Integer.parseInt(issue.column) - 1);
      } catch (java.lang.NumberFormatException e) {
        issue.column = "";
      }

      splitRuleIds(); // info [ruleId, aliasId, ...]
    }

    return issue != null;
  }

  private void splitRuleIds() {
    issue.ruleId = getDefaultRuleId();

    if (!issue.info.endsWith("]")) { // [...]
      return;
    }

    var end = issue.info.length() - 1;
    for (var start = issue.info.length() - 2; start >= 0; start--) {
      var c = issue.info.charAt(start);
      if (Character.isLetterOrDigit(c) || c == '-' || c == '.' || c == '_') {
        continue;
      } else if (c == ',') {
        var aliasId = issue.info.substring(start + 1, end);
        if (!"-warnings-as-errors".equals(aliasId)) {
          issue.aliasRuleIds.addFirst(aliasId);
        }
        end = start;
      } else {
        if (c == '[') {
          issue.ruleId = issue.info.substring(start + 1, end);
          issue.info = issue.info.substring(0, start - 1);
        } else {
          issue.aliasRuleIds.clear();
        }
        break;
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

    return map.getOrDefault(issue.level, "clang-diagnostic-unknown");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private static class Issue {

    private String path;
    private String line;
    private String column;
    private String level;
    private String ruleId;
    private LinkedList<String> aliasRuleIds = new LinkedList<>();
    private String info;
  }

}
