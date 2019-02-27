/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.checks.utils.CheckUtils;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.annotations.RuleTemplate;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * LineRegularExpressionCheck
 *
 */
@Rule(
  key = "LineRegularExpression",
  name = "Line RegEx rule",
  priority = Priority.MAJOR)
@RuleTemplate
@NoSqale
public class LineRegularExpressionCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

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
  private Charset charset = StandardCharsets.UTF_8;
  private Pattern pattern = null;

  private static boolean compare(boolean invert, boolean condition) {
    return invert ? !condition : condition;
  }

  @Override
  public void init() {
    pattern = CheckUtils.compileUserRegexp(regularExpression);
  }

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode fileNode) {
    if (compare(invertFilePattern, matchFile())) {
      // use onMalformedInput(CodingErrorAction.REPLACE) / onUnmappableCharacter(CodingErrorAction.REPLACE)
      try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(getContext().getFile()), charset))) {
        String line;
        int nr = 0;

        while ((line = br.readLine()) != null) {
          Matcher matcher = pattern.matcher(line);
          ++nr;
          if (compare(invertRegularExpression, matcher.find())) {
            getContext().createLineViolation(this, message, nr);
          }
        }
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private boolean matchFile() {
    if (!matchFilePattern.isEmpty()) {
      WildcardPattern filePattern = WildcardPattern.create(matchFilePattern);
      String path = PathUtils.sanitize(getContext().getFile().getPath());
      return filePattern.match(path);
    }
    return true;
  }

}
