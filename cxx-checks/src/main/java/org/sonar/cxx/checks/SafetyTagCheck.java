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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * SafetyTagCheck - testifies whether the file name has suffix "_SAFETY" whenever the pattern
 * "<Safetykey>.*</Safetykey>" exists in the file.
 */
@Rule(
  key = "SafetyTag",
  name = "Risk mitigation implementation shall be defined in separate file",
  priority = Priority.BLOCKER,
  tags = {Tag.CONVENTION})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class SafetyTagCheck extends SquidCheck<Grammar> implements AstAndTokenVisitor {

  private static final String DEFAULT_REGULAR_EXPRESSION = "<Safetykey>.*</Safetykey>";
  private static final String DEFAULT_MESSAGE = "Source files implementing risk mitigations shall use "
    + "special name suffix";
  private static final String DEFAULT_NAME_SUFFIX = "_SAFETY";
  private Pattern pattern;
  /**
   * regularExpression
   */
  @RuleProperty(
    key = "regularExpression",
    description = "Comment regular expression rule",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  /**
   * message
   */
  @RuleProperty(
    key = "message",
    description = "The violation message",
    defaultValue = DEFAULT_MESSAGE + " '" + DEFAULT_NAME_SUFFIX + "'")
  public String message = DEFAULT_MESSAGE + " '" + DEFAULT_NAME_SUFFIX + "'";

  /**
   * suffix
   */
  @RuleProperty(
    key = "suffix",
    description = "The appropriate file name suffix",
    defaultValue = DEFAULT_NAME_SUFFIX)
  public String suffix = DEFAULT_NAME_SUFFIX;

  public String getRegularExpression() {
    return regularExpression;
  }

  public String getMessage() {
    return message;
  }

  public String getSuffix() {
    return suffix;
  }

  @Override
  public void init() {
    String regEx = getRegularExpression();
    Objects.requireNonNull(regEx, "getRegularExpression() should not return null");

    if (!regEx.isEmpty()) {
      try {
        pattern = Pattern.compile(regEx, Pattern.DOTALL);
      } catch (RuntimeException e) {
        throw new IllegalStateException("Unable to compile regular expression: " + regEx, e);
      }
    }
  }

  @Override
  public void visitToken(Token token) {
    if (pattern != null) {
      for (Trivia trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          String comment = trivia.getToken().getOriginalValue();
          Matcher regexMatcher = pattern.matcher(comment);
          if (regexMatcher.find() && !getContext().getFile().getName().contains(getSuffix())) {
            getContext().createLineViolation(this, getMessage() + " : " + regexMatcher.group(0), trivia.getToken());
          }
        }
      }
    }
  }

}
