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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxGrammarImpl;
import com.sonar.sslr.api.Grammar;


import javax.annotation.Nullable;

@Rule(
  key = "CollapsibleIfCandidate",
  description = "Merge this if statement with the enclosing one.",
  priority = Priority.MAJOR)

public class CollapsibleIfCandidateCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.ifStatement);
  }

  @Override
  public void visitNode(AstNode node) {
    if (!hasElseClause(node)) {
      AstNode enclosingIfStatement = getEnclosingIfStatement(node);
      if (enclosingIfStatement != null && !hasElseClause(enclosingIfStatement) && hasSingleTrueStatement(enclosingIfStatement)) {
        getContext().createLineViolation(this, "Merge this if statement with the enclosing one.", node);
      }
    }
  }

  private static boolean hasElseClause(AstNode node) {
    return node.hasDirectChildren(CxxKeyword.ELSE);
  }

  @Nullable
  private static AstNode getEnclosingIfStatement(AstNode node) {
    AstNode grandParent = node.getParent().getParent();
    if (grandParent.is(CxxGrammarImpl.ifStatement)) {
      return grandParent;    
    } else if (!grandParent.is(CxxGrammarImpl.statementSeq)) {
      return null;
    }

    AstNode statement = grandParent.getFirstAncestor(CxxGrammarImpl.compoundStatement).getParent();
    if (!statement.is(CxxGrammarImpl.statement)) {
      return null;
    }

    AstNode enclosingStatement = statement.getParent();
    return enclosingStatement.is(CxxGrammarImpl.ifStatement) ? enclosingStatement : null;
  }

  private static boolean hasSingleTrueStatement(AstNode node) {
    AstNode statement = node.getFirstChild(CxxGrammarImpl.statement);
    return statement.hasDirectChildren(CxxGrammarImpl.compoundStatement) ?
        statement.getFirstChild(CxxGrammarImpl.compoundStatement).getFirstChild(CxxGrammarImpl.statementSeq).getChildren().size() == 1 : true;
  }

}
