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

public class CollapsibleIfSelectTest {

  private final Parser<Grammar> p = MiniCParser.create();
  private final Grammar g = p.getGrammar();

  @Test
  public void test() {
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
    var select = node.select();
    return hasNoElseClause(select) && (hasIfStatementWithoutElse(select)
                                       || hasIfStatementWithoutElseInCompoundStatement(select));
  }

  private boolean hasNoElseClause(AstSelect select) {
    return select.children(MiniCGrammar.ELSE_CLAUSE).isEmpty();
  }

  private boolean hasIfStatementWithoutElseInCompoundStatement(AstSelect select) {
    select = select
      .children(MiniCGrammar.STATEMENT)
      .children(MiniCGrammar.COMPOUND_STATEMENT);
    return select.children().size() == 3
             && hasIfStatementWithoutElse(select);
  }

  private boolean hasIfStatementWithoutElse(AstSelect select) {
    select = select.children(MiniCGrammar.STATEMENT).children(MiniCGrammar.IF_STATEMENT);
    return select.isNotEmpty() && hasNoElseClause(select);
  }

}
