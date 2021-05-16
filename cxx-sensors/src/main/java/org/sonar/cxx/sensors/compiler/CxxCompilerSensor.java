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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.TextScanner;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * base class for compiler issues
 */
public abstract class CxxCompilerSensor extends CxxIssuesReportSensor {

  private static final Logger LOG = Loggers.get(CxxCompilerSensor.class);
  private final Set<String> notExistingGroupName = new HashSet<>();

  @Override
  protected void processReport(File report) {

    final String reportEncoding = getEncoding();
    final String reportRegEx = getRegex();

    if (reportRegEx.isEmpty()) {
      LOG.error("processReport terminated because of empty custom regular expression");
      return;
    }

    if (!reportRegEx.contains("(?<")) {
      LOG.error("processReport terminated because regular expression contains no named-capturing group");
      return;
    }

    try ( var scanner = new TextScanner(report, reportEncoding)) {
      var pattern = Pattern.compile(reportRegEx);
      LOG.debug("Processing '{}' report '{}', Encoding='{}', Pattern='{}'",
                getCompilerKey(), report, scanner.encoding(), pattern);

      while (scanner.hasNextLine()) {
        var matcher = pattern.matcher(scanner.nextLine());
        if (matcher.find()) {
          String filename = alignFilename(getSubSequence(matcher, "file"));
          String line = alignLine(getSubSequence(matcher, "line"));
          String column = alignColumn(getSubSequence(matcher, "column"));
          String id = alignId(getSubSequence(matcher, "id"));
          String msg = alignMessage(getSubSequence(matcher, "message"));
          if (isInputValid(filename, line, column, id, msg)) {
            var issue = new CxxReportIssue(id, filename, line, column, msg);
            saveUniqueViolation(issue);
          } else {
            LOG.debug("Invalid compiler warning: '{}''{}', skipping", id, msg);
          }
        }
      }
    } catch (java.io.IOException | java.lang.IllegalArgumentException | java.lang.IllegalStateException e) {
      throw new InvalidReportException("The compiler report is invalid", e);
    }
  }

  /**
   * Unique string to identify the compiler
   *
   * @return
   */
  protected abstract String getCompilerKey();

  /**
   * Encoding of the report
   *
   * @return
   */
  protected abstract String getEncoding();

  /**
   * Regular expression to parse the report
   *
   * @return
   */
  protected abstract String getRegex();

  /**
   * Derived classes can overload this method
   *
   * A valid issue must have an id and, if it has a line number, a filename.
   *
   * @param filename
   * @param line
   * @param column is optional
   * @param id
   * @param msg
   * @return true, if valid
   */
  protected boolean isInputValid(@Nullable String filename,
                                 @Nullable String line, @Nullable String column,
                                 @Nullable String id, String msg) {
    if ((id == null) || id.isEmpty()) {
      return false;
    }
    if ((line != null) && !line.isEmpty() && ((filename == null) || filename.isEmpty())) {
      return false;
    }
    return true;
  }

  /**
   * Derived classes can overload this method to align filename
   *
   * @param filename
   * @return
   */
  @CheckForNull
  protected String alignFilename(@Nullable String filename) {
    return filename;
  }

  /**
   * Derived classes can overload this method to align line number
   *
   * @param line
   * @return
   */
  @CheckForNull
  protected String alignLine(@Nullable String line) {
    return line;
  }

  /**
   * Derived classes can overload this method to align column number
   *
   * @param column
   * @return
   */
  @CheckForNull
  protected String alignColumn(@Nullable String column) {
    return column;
  }

  /**
   * Derived classes can overload this method to align message id
   *
   * @param id
   * @return
   */
  @CheckForNull
  protected String alignId(@Nullable String id) {
    return id;
  }

  /**
   * Derived classes can overload this method to align message
   *
   * @param message
   * @return
   */
  protected String alignMessage(String message) {
    return message;
  }

  /**
   * Returns the input subsequence captured by the given named-capturing group.
   */
  @CheckForNull
  private String getSubSequence(Matcher matcher, String groupName) {
    try {
      if (!notExistingGroupName.contains(groupName)) {
        return matcher.group(groupName);
      }
    } catch (IllegalArgumentException e) {
      notExistingGroupName.add(groupName);
      LOG.debug("named-capturing group '{}' is not used in regex.", groupName);
    }
    return null;
  }

}
