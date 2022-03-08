/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.checks.regex;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.regex.Pattern;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.checks.utils.CheckUtils;
import org.sonar.cxx.squidbridge.annotations.NoSqale;
import org.sonar.cxx.squidbridge.annotations.RuleTemplate;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

/**
 * LineRegularExpressionCheck
 *
 */
@Rule(
  key = "LineRegularExpression",
  name = "Track lines matching a regular expression",
  priority = Priority.MAJOR)
@RuleTemplate
@NoSqale
public class LineRegularExpressionCheck extends SquidCheck<Grammar> {

  private static final String DEFAULT_MATCH_FILE_PATTERN = "";
  private static final boolean DEFAULT_INVERT_FILE_PATTERN = false;
  private static final String DEFAULT_REGULAR_EXPRESSION = "";
  private static final boolean DEFAULT_INVERT_REGULAR_EXPRESSION = false;
  private static final String DEFAULT_MESSAGE = "The regular expression matches this line";

  /**
   * matchFilePattern
   */
  @RuleProperty(
    key = "matchFilePattern",
    description = "Ant-style matching patterns for path",
    defaultValue = DEFAULT_MATCH_FILE_PATTERN)
  public String matchFilePattern = DEFAULT_MATCH_FILE_PATTERN;

  /**
   * invertFilePattern
   */
  @RuleProperty(
    key = "invertFilePattern",
    description = "Invert file pattern comparison",
    defaultValue = "" + DEFAULT_INVERT_FILE_PATTERN)
  public boolean invertFilePattern = DEFAULT_INVERT_FILE_PATTERN;

  /**
   * regularExpression
   */
  @RuleProperty(
    key = "regularExpression",
    description = "The regular expression",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  /**
   * invertRegularExpression
   */
  @RuleProperty(
    key = "invertRegularExpression",
    description = "Invert regular expression comparison",
    defaultValue = "" + DEFAULT_INVERT_REGULAR_EXPRESSION)
  public boolean invertRegularExpression = DEFAULT_INVERT_REGULAR_EXPRESSION;

  /**
   * message
   */
  @RuleProperty(
    key = "message",
    description = "The violation message",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;
  private Pattern pattern = null;

  private static boolean compare(boolean invert, boolean condition) {
    return invert ? !condition : condition;
  }

  @Override
  public void init() {
    pattern = CheckUtils.compileUserRegexp(regularExpression);
  }

  @Override
  public void visitFile(AstNode fileNode) {
    if (compare(invertFilePattern, matchFile())) {
      var nr = 0;
      for (var line : getContext().getInputFileLines()) {
        var matcher = pattern.matcher(line);
        ++nr;
        if (compare(invertRegularExpression, matcher.find())) {
          getContext().createLineViolation(this, message, nr);
        }
      }
    }
  }

  private boolean matchFile() {
    if (!matchFilePattern.isEmpty()) {
      var filePattern = WildcardPattern.create(matchFilePattern);
      String path = PathUtils.sanitize(getContext().getInputFile().file().getPath());
      return path != null ? filePattern.match(path) : false;
    }
    return true;
  }

}
