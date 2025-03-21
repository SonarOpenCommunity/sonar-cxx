/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.sensors.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.utils.CxxReportLocation;

/**
 * This class is used as base for all sensors which import external reports, which contain issues. It hosts common logic
 * such as saving issues in SonarQube
 */
public abstract class CxxIssuesReportSensor extends CxxReportSensor {

  private static final Logger LOG = LoggerFactory.getLogger(CxxIssuesReportSensor.class);

  public static final String DEFAULT_UNKNOWN_RULE_KEY = "unknown";

  private final HashSet<CxxReportIssue> uniqueIssues = new HashSet<>();
  private int savedNewIssues = 0;

  private SonarServerWebApi webApi = new SonarServerWebApi();
  private final HashMap<String, Set<String>> knownRulesPerRepositoryKey = new HashMap<>();
  private final HashMap<String, String> deprecatedRuleIds = new HashMap<>();
  private final HashSet<String> mappedRuleIds = new HashSet<>();

  /**
   * {@inheritDoc}
   */
  protected CxxIssuesReportSensor() {
  }

  /**
   * Set WebApi access object.
   *
   * Can be mocked in unit tests.
   *
   * @param webApi WebApi object to use
   * @return return current object
   */
  public CxxIssuesReportSensor setWebApi(SonarServerWebApi webApi) {
    this.webApi = webApi;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void executeImpl() {
    if (webApi != null) {
      String url = context.config().get("sonar.host.url").orElse("http://localhost:9000");
      String authenticationToken = context.config().get("sonar.token")
        .or(() -> context.config().get("sonar.login")) // deprecated: can be removed in future
        .orElse(System.getenv("SONAR_TOKEN"));

      downloadRulesFromServer(url, authenticationToken);
    }
    List<File> reports = getReports(getReportPathsKey());
    for (var report : reports) {
      executeReport(report);
    }
  }

  private void downloadRulesFromServer(String url, String authenticationToken) {
    try {
      LOG.info("Downloading rules for '{}' from server '{}'", getRuleRepositoryKey(), url);

      var rules = webApi
        .setServerUrl(url)
        .setAuthenticationToken(authenticationToken)
        .getRules("cxx", getRuleRepositoryKey());

      // deactivate mapping if 'unknown' rule is not active
      var ruleMappingActive = true;
      if (context.activeRules().find(RuleKey.of(getRuleRepositoryKey(), DEFAULT_UNKNOWN_RULE_KEY)) == null) {
        LOG.info("Rule mapping to '{}:{}' is not active", getRuleRepositoryKey(), DEFAULT_UNKNOWN_RULE_KEY);
        ruleMappingActive = false;
      }

      var ruleKeys = new HashSet<String>();
      for (var rule : rules) {
        var ruleKey = rule.key().replace(getRuleRepositoryKey() + ":", "");
        ruleKeys.add(ruleKey);
        for (var deprecatedKey : rule.deprecatedKeys().deprecatedKey()) {
          if (deprecatedKey.startsWith(getRuleRepositoryKey())) {
            deprecatedKey = deprecatedKey.replace(getRuleRepositoryKey() + ":", "");
            deprecatedRuleIds.put(deprecatedKey, ruleKey);
            LOG.debug("Map deprecated rule '{}' to '{}' for '{}'",
              deprecatedKey, ruleKey, getRuleRepositoryKey());
          }
        }
      }
      if (ruleMappingActive) {
        knownRulesPerRepositoryKey.put(getRuleRepositoryKey(), ruleKeys);
      }
      LOG.debug("{} rules for '{}' were loaded from server", ruleKeys.size(), getRuleRepositoryKey());
    } catch (IOException e) {
      LOG.warn("Rules for '{}' could not be loaded from server", getRuleRepositoryKey(), e);
    }
  }

  private void saveIssue(String ruleId, CxxReportIssue issue) {
    ruleId = mapDeprecatedRuleId(ruleId);
    ruleId = mapUnknownRuleId(ruleId, issue);
    var newIssue = context.newIssue();
    if (addLocations(newIssue, ruleId, issue)) {
      addFlow(newIssue, issue);
      newIssue.save();
      savedNewIssues++;
    }
  }

  private String mapDeprecatedRuleId(String ruleId) {
    return deprecatedRuleIds.getOrDefault(ruleId, ruleId);
  }

  private String mapUnknownRuleId(String ruleId, CxxReportIssue issue) {
    var repository = knownRulesPerRepositoryKey.get(getRuleRepositoryKey());
    if (repository != null && !repository.contains(ruleId)) {
      if (mappedRuleIds.add(ruleId)) {
        LOG.info("Rule '{}' is unknown in '{}' and will be mapped to '{}'",
          ruleId,
          getRuleRepositoryKey(),
          DEFAULT_UNKNOWN_RULE_KEY);
      }
      issue.addMappedInfo();
      ruleId = DEFAULT_UNKNOWN_RULE_KEY;
    }
    return ruleId;
  }

  /**
   * Saves code violation only if it wasn't already saved
   *
   * Saves a code violation which is detected in the given file/line and has given ruleId and message. Saves it to the
   * given project and context. Project or file-level violations can be saved by passing null for the according
   * parameters ('file' = null for project level, 'line' = null for file-level)
   *
   * @param issue
   */
  public void saveUniqueViolation(CxxReportIssue issue) {
    if (uniqueIssues.add(issue)) {
      try {
        saveIssue(issue.getRuleId(), issue);
        if (issue.hasAliasRuleIds()) {
          // in case of alias rule ids save the issues also with these ids
          for (var aliasRuleId : issue.getAliasRuleIds()) {
            saveIssue(aliasRuleId, issue);
          }
        }
      } catch (RuntimeException e) {
        var msg = "Cannot save the issue '" + issue + "'";
        CxxUtils.validateRecovery(msg, e, context.config());
      }
    }
  }

  /**
   * @param report to read
   */
  protected void executeReport(File report) {
    try {
      LOG.info("Processing report '{}'", report);
      savedNewIssues = 0;
      processReport(report);
      LOG.info("Processing successful, saved new issues={}", savedNewIssues);
    } catch (ReportException e) {
      var msg = e.getMessage() + ", report='" + report + "'";
      CxxUtils.validateRecovery(msg, e, context.config());
    }
  }

  private TextRange getRange(CxxReportLocation location, InputFile inputFile) {
    var line = 1;
    var column = -1;
    try {
      if (location.getLine() != null) {
        // https://jira.sonarsource.com/browse/SONAR-6792
        line = Integer.max(1, Integer.min(Integer.parseInt(location.getLine()), inputFile.lines()));
        if (location.getColumn() != null) {
          column = Integer.max(0, Integer.parseInt(location.getColumn()));
        }
      }
    } catch (java.lang.NumberFormatException e) {
      CxxUtils.validateRecovery("Invalid issue range: " + e.getMessage(), e, context.config());
    }

    if (column < 0) {
      return inputFile.selectLine(line);
    } else {
      try {
        // since we do not have more information, we select only one character
        return inputFile.newRange(line, column, line, column + 1);
      } catch (IllegalArgumentException e) {
        // second try without column number: sometimes locations is behind last valid column
        return inputFile.selectLine(line);
      }
    }
  }

  @CheckForNull
  private NewIssueLocation createNewIssueLocation(NewIssue newIssue, CxxReportLocation location) {
    var inputFile = getInputFileIfInProject(location.getFile());
    if (inputFile != null) {
      TextRange range = getRange(location, inputFile);
      return newIssue.newLocation()
        .on(inputFile)
        .at(range)
        .message(location.getInfo());
    }
    return null;
  }

  private boolean addLocations(NewIssue newIssue, String ruleId, CxxReportIssue issue) {
    var first = true;
    for (var location : issue.getLocations()) {
      NewIssueLocation newLocation;
      if (location.getFile() != null && !location.getFile().isEmpty()) {
        newLocation = createNewIssueLocation(newIssue, location);
      } else {
        newLocation = newIssue.newLocation()
          .on(context.project())
          .message(location.getInfo());
      }

      if (newLocation != null) {
        if (first) {
          newIssue
            .forRule(RuleKey.of(getRuleRepositoryKey(), ruleId))
            .at(newLocation);
          first = false;
        } else {
          newIssue.addLocation(newLocation);
        }
      }
    }

    return !first;
  }

  private void addFlow(NewIssue newIssue, CxxReportIssue issue) {
    var newIssueFlow = new ArrayList<NewIssueLocation>();
    for (var location : issue.getFlow()) {
      var newIssueLocation = createNewIssueLocation(newIssue, location);
      if (newIssueLocation != null) {
        newIssueFlow.add(newIssueLocation);
      } else {
        LOG.debug("Failed to create new issue location from flow location {}", location);
        newIssueFlow.clear();
        break;
      }
    }

    if (!newIssueFlow.isEmpty()) {
      newIssue.addFlow(newIssueFlow);
    }
  }

  protected abstract void processReport(File report);

  protected abstract String getReportPathsKey();

  protected abstract String getRuleRepositoryKey();
}
