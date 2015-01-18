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

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.SquidCheck;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.AstNode;


@Rule(
  key = "BooleanEqualityComparison",
  priority = Priority.MINOR)

public class BooleanEqualityComparisonCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.equalityExpression);
  }

  @Override
  public void visitNode(AstNode node) {
    if (hasBooleanLiteralOperand(node)) {
      getContext().createLineViolation(
          this,
          "Remove the unnecessary boolean comparison to simplify this expression.",
          node);
    }
  }

  private static boolean hasBooleanLiteralOperand(AstNode node) {
    return node.select()
//        .children(CxxGrammarImpl.idExpression)
        .children(CxxGrammarImpl.LITERAL)
        .children(CxxGrammarImpl.BOOL)
        .descendants(CxxKeyword.TRUE, CxxKeyword.FALSE)
        .isNotEmpty();
  }

}

