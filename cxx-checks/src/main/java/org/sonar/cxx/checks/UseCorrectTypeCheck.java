/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.checks;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;

import java.util.Map;
import java.util.regex.Pattern;

@Rule(
  key = "UseCorrectType",
  priority = Priority.MINOR,
  cardinality = Cardinality.MULTIPLE)

public class UseCorrectTypeCheck extends SquidCheck<Grammar> {
    
  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[] {
    CxxGrammarImpl.typeName, 
    CxxGrammarImpl.condition
  }; 
  
  private static final String DEFAULT_REGULAR_EXPRESSION = "WORD|BOOL|BYTE|FLOAT|NULL";
  private static final String DEFAULT_MESSAGE = "Use C++ types whenever possible";
  
  @RuleProperty(
    key = "regularExpression",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  @RuleProperty(
    key = "message",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;
    
  public String getRegularExpression() {
    return regularExpression;
  }
    
  public String getMessage() {
    return message;
  }
  
  private Pattern pattern = null;
  private final Map<String, Integer> firstOccurrence = Maps.newHashMap();
  private final Map<String, Integer> literalsOccurrences = Maps.newHashMap();

  @Override
  public void init() {
    subscribeTo(CHECKED_TYPES);
    if (!Strings.isNullOrEmpty(regularExpression)) {
      try {
        pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
      } catch (RuntimeException e) {
        throw new SonarException("Unable to compile regular expression: " + regularExpression, e);
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
    if (node.is(CHECKED_TYPES)) {
      if (pattern.matcher(node.getTokenOriginalValue()).find()) {
        visitOccurence(node.getTokenOriginalValue(), node.getTokenLine());
      }
    }
  }

  @Override
  public void leaveFile(AstNode node) {
    for (Map.Entry<String, Integer> literalOccurences : literalsOccurrences.entrySet()) {
      Integer occurences = literalOccurences.getValue();
      String literal = literalOccurences.getKey();
      getContext().createLineViolation(this, "Use the correct type instead of " + literal + " (" + occurences + " times).", firstOccurrence.get(literal));
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
