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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.annotations.RuleTemplate;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * UseCorrectTypeCheck
 *
 */
@Rule(
  key = "UseCorrectType",
  name = "C++ type(s) shall be used",
  tags = {Tag.CONVENTION},
  priority = Priority.MINOR)
@RuleTemplate
@NoSqale
public class UseCorrectTypeCheck extends SquidCheck<Grammar> {

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[]{
    CxxGrammarImpl.typeName,
    CxxGrammarImpl.condition
  };

  private static final String DEFAULT_REGULAR_EXPRESSION = "WORD|BOOL|BYTE|FLOAT|NULL";
  private static final String DEFAULT_MESSAGE = "Use C++ types whenever possible";
  private Pattern pattern;
  private final Map<String, Integer> firstOccurrence = new HashMap<>();
  private final Map<String, Integer> literalsOccurrences = new HashMap<>();
  /**
   * regularExpression
   */
  @RuleProperty(
    key = "regularExpression",
    description = "Type regular expression rule",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  /**
   * message
   */
  @RuleProperty(
    key = "message",
    description = "The violation message",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  public String getRegularExpression() {
    return regularExpression;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public void init() {
    subscribeTo(CHECKED_TYPES);
    if (null != regularExpression && !regularExpression.isEmpty()) {
      try {
        pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
      } catch (RuntimeException e) {
        throw new IllegalStateException("Unable to compile regular expression: " + regularExpression, e);
      }
    }
  }

  @Override
  public void visitFile(AstNode node) {
    firstOccurrence.clear();
    literalsOccurrences.clear();
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CHECKED_TYPES) && pattern.matcher(node.getTokenOriginalValue()).find()) {
      visitOccurence(node.getTokenOriginalValue(), node.getTokenLine());
    }
  }

  @Override
  public void leaveFile(AstNode node) {
    for (Map.Entry<String, Integer> literalOccurences : literalsOccurrences.entrySet()) {
      Integer occurences = literalOccurences.getValue();
      String literal = literalOccurences.getKey();
      getContext().createLineViolation(this, "Use the correct type instead of "
        + literal + " (" + occurences + " times).", firstOccurrence.get(literal));
    }
  }

  private void visitOccurence(String literal, int line) {
    if (!firstOccurrence.containsKey(literal)) {
      firstOccurrence.put(literal, line);
      literalsOccurrences.put(literal, 1);
    } else {
      int occurences = literalsOccurrences.get(literal);
      literalsOccurrences.put(literal, occurences + 1);
    }
  }

}
