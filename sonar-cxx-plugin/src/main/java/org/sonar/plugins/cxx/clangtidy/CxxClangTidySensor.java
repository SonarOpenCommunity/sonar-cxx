/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.plugins.cxx.clangtidy;

import java.io.File;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;

/**
 * Sensor for clang-tidy
 */
public class CxxClangTidySensor extends CxxReportSensor {
  public static final Logger LOG = Loggers.get(CxxClangTidySensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.clangtidy.reportPath";

  private class Issue {
    String path;
    String line;
    String level;
    String message;
    String check;
  }
  
  // Extended issue information disabled for now as this has some formatting issues in the UI (not very readable and ugly).
  private boolean extendedIssueInformation = false;

  /**
   * {@inheritDoc}
   */
  public CxxClangTidySensor(Settings settings) {
    super(settings, CxxMetrics.CLANGTIDY);
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(CxxLanguage.KEY).name("CxxClangTidySensor");
  }

  @Override
  protected void processReport(final SensorContext context, File report) {
    LOG.debug("Parsing clang-tidy report");

    try (Scanner scanner = new Scanner(report, "UTF-8")) {
      // <path>:<line>:<column>: <level>: <message> [<checkname>]
      Pattern p = Pattern.compile("([^:]+):([0-9]+):([0-9]+): ([^:]+): ([^]]+) \\[([^]]+)\\]");
      Issue currentIssue = null;

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        Matcher matcher = p.matcher(line);
        if (matcher.matches()) {
          if (currentIssue != null) {
            saveUniqueViolation(context, CxxClangTidyRuleRepository.KEY, currentIssue.path, currentIssue.line, currentIssue.check,
                currentIssue.message);
          }
          MatchResult m = matcher.toMatchResult();
          currentIssue = new Issue();
          currentIssue.path = m.group(1);
          currentIssue.line = m.group(2);
          currentIssue.level = m.group(4);
          currentIssue.message = m.group(5);
          currentIssue.check = m.group(6);
        } else if (extendedIssueInformation && currentIssue != null) {
          currentIssue.message += "\n" + line;
        }
      }
      if (currentIssue != null) {
        saveUniqueViolation(context, CxxClangTidyRuleRepository.KEY, currentIssue.path, currentIssue.line, currentIssue.check,
            currentIssue.message);
      }
    } catch (final Exception e) {
      LOG.error("Failed to parse clang-tidy report: {}", e);
    }
  }
}
