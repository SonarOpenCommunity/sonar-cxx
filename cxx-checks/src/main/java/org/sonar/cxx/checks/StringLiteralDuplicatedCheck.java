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
import com.sonar.sslr.api.Grammar;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "StringLiteralDuplicated",
  name = "String literals should not be duplicated",
  priority = Priority.MINOR,
  tags = {Tag.BAD_PRACTICE})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class StringLiteralDuplicatedCheck extends SquidCheck<Grammar> {

  private static final int MINIMAL_LITERAL_LENGTH = 7;
  private static final String[] ALLOWED_LITERAL_NAMES = {"nullptr"};

  private final Map<String, Integer> firstOccurrence = new HashMap<>();
  private final Map<String, Integer> literalsOccurrences = new HashMap<>();

  @RuleProperty(
    key = "minimalLiteralLength",
    description = "The minimal literal length",
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
    if (!node.getToken().isGeneratedCode()) {
      visitOccurence(node.getTokenOriginalValue(), node.getTokenLine());
    }
  }

  @Override
  public void leaveFile(AstNode node) {
    for (Map.Entry<String, Integer> literalOccurences : literalsOccurrences.entrySet()) {
      Integer occurences = literalOccurences.getValue();

      if (occurences > 1) {
        String literal = literalOccurences.getKey();

        getContext().createLineViolation(this, "Define a constant instead of duplicating this literal "
          + literal + " " + occurences
          + " times.", firstOccurrence.get(literal));
      }
    }
  }

  private void visitOccurence(String literal, int line) {
    if (literal.length() >= minimalLiteralLength) {
      if (!firstOccurrence.containsKey(literal)) {
        if (!Arrays.asList(ALLOWED_LITERAL_NAMES).contains(literal)) {
          firstOccurrence.put(literal, line);
          literalsOccurrences.put(literal, 1);
        }
      } else {
        int occurences = literalsOccurrences.get(literal);
        literalsOccurrences.put(literal, occurences + 1);
      }
    }
  }

}
