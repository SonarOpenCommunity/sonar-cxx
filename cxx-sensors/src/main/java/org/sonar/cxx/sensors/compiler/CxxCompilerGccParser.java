/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * {@inheritDoc}
 */
public class CxxCompilerGccParser implements CompilerParser {

  private static final Logger LOG = Loggers.get(CxxCompilerGccParser.class);
  public static final String KEY_GCC = "GCC";
  // search for single line with compiler warning message - order for groups: 1 = file, 2 = line, 3 = message, 4=id
  public static final String DEFAULT_REGEX_DEF = "^(.*):([0-9]+):[0-9]+:\\x20warning:\\x20(.*)\\x20\\[(.*)\\]$";
  // ToDo: as long as java 7 API is not used the support of named groups for regular expression is not possible
  // sample regex: "^.*[\\\\,/](?<filename>.*)\\((?<line>[0-9]+)\\)\\x20:\\x20warning\\x20(?<id>C\\d\\d\\d\\d):(?<message>.*)$";
  // get value with e.g. scanner.match().group("filename");
  public static final String DEFAULT_CHARSET_DEF = "UTF-8";

  /**
   * {@inheritDoc}
   */
  @Override
  public String key() {
    return KEY_GCC;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String rulesRepositoryKey() {
    return CxxCompilerGccRuleRepository.KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String defaultRegexp() {
    return DEFAULT_REGEX_DEF;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String defaultCharset() {
    return DEFAULT_CHARSET_DEF;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report, String charset, String reportRegEx, List<Warning> warnings) throws java.io.FileNotFoundException {
    LOG.info("Parsing '{}' format", KEY_GCC);

    Scanner scanner = new Scanner(report, charset);
    Pattern p = Pattern.compile(reportRegEx, Pattern.MULTILINE);
    LOG.debug("Using pattern : '{}'", p);
    MatchResult matchres;
    while (scanner.findWithinHorizon(p, 0) != null) {
      matchres = scanner.match();
      String filename = matchres.group(1).trim();
      String line = matchres.group(2);
      String msg = matchres.group(3);
      String id = matchres.group(4).replaceAll("=$", "");
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scanner-matches file='{}' line='{}' id='{}' msg={}", filename, line, id, msg);
      }
      warnings.add(new Warning(filename, line, id, msg));
    }
    scanner.close();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
