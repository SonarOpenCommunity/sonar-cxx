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

import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;

import java.util.Map;

@Rule(
  key = "StringLiteralDuplicated",
  priority = Priority.MINOR)

public class StringLiteralDuplicatedCheck extends SquidCheck<Grammar> {

  private static final int MINIMAL_LITERAL_LENGTH = 7;

  private final Map<String, Integer> firstOccurrence = Maps.newHashMap();
  private final Map<String, Integer> literalsOccurrences = Maps.newHashMap();

  @RuleProperty(
      key = "minimalLiteralLength",
      defaultValue = "" + MINIMAL_LITERAL_LENGTH)
    public int minimalLiteralLength = MINIMAL_LITERAL_LENGTH;

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.LITERAL);
  }

  @Override
  public void visitFile(AstNode node) {
    firstOccurrence.clear();
    literalsOccurrences.clear();
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CxxGrammarImpl.LITERAL)) {
      visitOccurence(node.getTokenOriginalValue(), node.getTokenLine());
    }
  }

  @Override
  public void leaveFile(AstNode node) {
    for (Map.Entry<String, Integer> literalOccurences : literalsOccurrences.entrySet()) {
      Integer occurences = literalOccurences.getValue();

      if (occurences > 1) {
        String literal = literalOccurences.getKey();

        getContext().createLineViolation(this, "Define a constant instead of duplicating this literal " + literal + " " + occurences 
            + " times.", firstOccurrence.get(literal));
      }
    }
  }

  private void visitOccurence(String literal, int line) {
    if (literal.length() >= minimalLiteralLength) {
      if (!firstOccurrence.containsKey(literal)) {
        firstOccurrence.put(literal, line);
        literalsOccurrences.put(literal, 1);
      } else {
        int occurences = literalsOccurrences.get(literal);
        literalsOccurrences.put(literal, occurences + 1);
      }
    }
  }

}
