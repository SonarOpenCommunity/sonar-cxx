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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * {@inheritDoc}
 */
public class CxxCompilerVcParser implements CompilerParser {

  private static final Logger LOG = Loggers.get(CxxCompilerVcParser.class);
  public static final String KEY_VC = "Visual C++";
  // search for single line with compiler warning message VS2008 - order for groups: 1 = file, 2 = line, 3 = ID, 4=message
  public static final String DEFAULT_REGEX_DEF = "^(.*)\\((\\d+)\\)\\x20:\\x20warning\\x20(C\\d+):(.*)$";
  // sample regex for VS2012/2013: "^.*>(?<filename>.*)\\((?<line>\\d+)\\):\\x20warning\\x20(?<id>C\\d+):(?<message>.*)$";
  // get value with e.g. scanner.match().group("filename");
  public static final String DEFAULT_CHARSET_DEF = "UTF-8"; // use "UTF-16" for VS2010 build log or TFS Team build log file

  private static final Pattern jobNumberPrefixPattern = Pattern.compile("^\\d+>(.*)$");

  /**
   * {@inheritDoc}
   */
  @Override
  public String key() {
    return KEY_VC;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String rulesRepositoryKey() {
    return CxxCompilerVcRuleRepository.KEY;
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
  public void processReport(final SensorContext context, File report, String charset,
    String reportRegEx, List<Warning> warnings) throws java.io.FileNotFoundException {
    LOG.info("Parsing 'Visual C++' format ({})", charset);

    try (Scanner scanner = new Scanner(report, charset)) {
      Pattern p = Pattern.compile(reportRegEx, Pattern.MULTILINE);
      LOG.info("Using pattern : '{}'", p);
      MatchResult matchres;
      while (scanner.findWithinHorizon(p, 0) != null) {
        matchres = scanner.match();
        String filename = removeMPPrefix(matchres.group(1).trim());
        String line = matchres.group(2);
        String id = matchres.group(3);
        String msg = matchres.group(4);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Scanner-matches file='{}' line='{}' id='{}' msg={}", filename, line, id, msg);
        }
        warnings.add(new Warning(filename, line, id, msg));
      }
    }
  }

  private static String removeMPPrefix(String fpath) {
    // /MP (Build with Multiple Processes) will create a line prefix with the job number eg. '   42>'
    Matcher m = jobNumberPrefixPattern.matcher(fpath);
    if (m.matches()) {
      return m.group(1);
    }
    return fpath;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
