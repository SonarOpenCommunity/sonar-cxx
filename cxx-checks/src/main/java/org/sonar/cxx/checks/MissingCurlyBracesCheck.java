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

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.SquidCheck;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import static org.sonar.cxx.checks.utils.CheckUtils.isIfStatement;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.tag.Tag;

@Rule(
  key = "MissingCurlyBraces",
  name = "if/else/for/while/do statements should always use curly braces",
  tags = {Tag.CONVENTION, Tag.PITFALL},
  priority = Priority.MAJOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class MissingCurlyBracesCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(
      CxxGrammarImpl.selectionStatement,
      CxxGrammarImpl.iterationStatement);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode statement = astNode.getFirstChild(CxxGrammarImpl.statement);
    if (!statement.getFirstChild().is(CxxGrammarImpl.compoundStatement)
      && !";".equals(statement.getToken().getValue())) {
      getContext().createLineViolation(this, "Missing curly brace.", astNode);
    }

    if (isIfStatement(astNode)) {
      AstNode elseClause = astNode.getFirstChild(CxxKeyword.ELSE);
      if (elseClause != null) {
        statement = elseClause.getNextSibling().getFirstChild();
        if (!statement.is(CxxGrammarImpl.compoundStatement)
          && !isIfStatement(statement)) {
          getContext().createLineViolation(this, "Missing curly brace.", elseClause);
        }
      }
    }
  }

}
