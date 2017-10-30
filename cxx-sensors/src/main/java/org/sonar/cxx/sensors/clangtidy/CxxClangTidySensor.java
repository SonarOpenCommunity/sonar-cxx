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
package org.sonar.cxx.sensors.clangtidy;

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
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * Sensor for clang-tidy
 */
public class CxxClangTidySensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxClangTidySensor.class);
  public static final String KEY = "Clang-Tidy";
  public static final String REPORT_PATH_KEY = "clangtidy.reportPath";
  public static final String REPORT_CHARSET_DEF = "clangtidy.charset";
  public static final String DEFAULT_CHARSET_DEF = "UTF-8";

  /**
   * {@inheritDoc}
   */
  public CxxClangTidySensor(CxxLanguage language, Settings settings) {
    super(language, settings);
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(this.language.getKey()).name(language.getName() + " ClangTidySensor");
  }

  /**
   * Get string property from configuration. If the string is not set or empty,
   * return the default value.
   *
   * @param name Name of the property
   * @param def Default value
   * @return Value of the property if set and not empty, else default value.
   */
  public String getParserStringProperty(String name, String def) {
    String s = this.settings.getString(name);
    if (s == null || s.isEmpty()) {
      return def;
    }
    return s;
  }

  @Override
  protected void processReport(final SensorContext context, File report) {
    final String reportCharset = getParserStringProperty(this.language.getPluginProperty(REPORT_CHARSET_DEF), "UTF-8");
    LOG.debug("Parsing 'clang-tidy' report, CharSet= '{}'", reportCharset);

    try (Scanner scanner = new Scanner(report, reportCharset)) {
      // E:\Development\SonarQube\cxx\sonar-cxx\sonar-cxx-plugin\src\test\resources\org\sonar\plugins\cxx\reports-project\clang-tidy-reports\..\..\cpd.cc:76:20: warning: ISO C++11 does not allow conversion from string literal to 'char *' [clang-diagnostic-writable-strings]
      // <path>:<line>:<column>: <level>: <message> [<checkname>]
      // relative paths
      final String regex = "(.+|[a-zA-Z]:\\\\.+):([0-9]+):([0-9]+): ([^:]+): ([^]]+) \\[([^]]+)\\]";
      final Pattern pattern = Pattern.compile(regex);

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        final Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          MatchResult m = matcher.toMatchResult();
          String path = m.group(1);
          String lineId = m.group(2);
          String message = m.group(5);
          String check = m.group(6);
          saveUniqueViolation(context,
            CxxClangTidyRuleRepository.KEY,
            path,
            lineId,
            check,
            message);
        }
      }
    } catch (final java.io.FileNotFoundException
      | java.lang.IllegalArgumentException
      | java.lang.IllegalStateException
      | java.util.InputMismatchException e) {
      LOG.error("Failed to parse clang-tidy report: {}", e);
    }
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }
}
