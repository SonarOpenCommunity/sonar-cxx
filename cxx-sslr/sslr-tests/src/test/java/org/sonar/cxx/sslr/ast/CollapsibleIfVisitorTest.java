/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.ast;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import com.sonar.cxx.sslr.test.minic.MiniCParser;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CollapsibleIfVisitorTest {

  private final Parser<Grammar> p = MiniCParser.create();
  private final Grammar g = p.getGrammar();

  @Test
  void test() {
    var fileNode = p.parse(new File("src/test/resources/queries/collapsible_if.mc"));
    var ifStatements = fileNode.getDescendants(MiniCGrammar.IF_STATEMENT);

    Set<Integer> violations = new HashSet<>();
    for (var node : ifStatements) {
      if (visit(node)) {
        violations.add(node.getTokenLine());
      }
    }
    assertThat(violations).containsOnly(7, 16);
  }

  private boolean visit(AstNode node) {
    return !hasElseClause(node) && hasCollapsibleIfStatement(node);
  }

  private boolean hasElseClause(AstNode node) {
    return node.hasDirectChildren(MiniCGrammar.ELSE_CLAUSE);
  }

  private boolean hasCollapsibleIfStatement(AstNode node) {
    var statementNode = node.getFirstChild(MiniCGrammar.STATEMENT).getFirstChild();
    return isIfStatementWithoutElse(statementNode) || isIfStatementWithoutElseInCompoundStatement(statementNode);
  }

  private boolean isIfStatementWithoutElse(AstNode node) {
    return node.is(MiniCGrammar.IF_STATEMENT) && !hasElseClause(node);
  }

  private boolean isIfStatementWithoutElseInCompoundStatement(AstNode node) {
    if (!node.is(MiniCGrammar.COMPOUND_STATEMENT) || node.getNumberOfChildren() != 3) {
      return false;
    }
    var statementNode = node.getFirstChild(MiniCGrammar.STATEMENT);
    if (statementNode == null) {
      // Null check was initially forgotten, did not led to a NPE because the unit test did not cover that case yet!
      return false;
    }
    return isIfStatementWithoutElse(statementNode.getFirstChild());
  }

}
