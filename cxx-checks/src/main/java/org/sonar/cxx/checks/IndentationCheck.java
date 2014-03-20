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
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;

@Rule(
  key = "IndentationCheck",
  description = "Well formed line itention improves readbility",
  priority = Priority.MAJOR)

public class IndentationCheck extends SquidCheck<Grammar> {
  
  private static final AstNodeType[] BLOCK_TYPES = new AstNodeType[] {
      CxxGrammarImpl.statementSeq,
      CxxGrammarImpl.switchBlockStatementGroups,
      CxxGrammarImpl.switchBlockStatementGroup,
      CxxGrammarImpl.namespaceBody,
      CxxGrammarImpl.declarationSeq,
      CxxGrammarImpl.classSpecifier,
      CxxGrammarImpl.enumSpecifier
  };

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[] {
      CxxGrammarImpl.statement,
      CxxGrammarImpl.simpleTypeSpecifier,
      CxxGrammarImpl.memberSpecification,
      CxxGrammarImpl.enumeratorDefinition,
  };

  private static final int DEFAULT_INDENTATION_LEVEL = 2;

  @RuleProperty(
    key = "indentationLevel",
    defaultValue = "" + DEFAULT_INDENTATION_LEVEL)
  public int indentationLevel = DEFAULT_INDENTATION_LEVEL;

  private int expectedLevel;
  private boolean isBlockAlreadyReported;
  private int lastCheckedLine;

  @Override
  public void init() {
    subscribeTo(BLOCK_TYPES);
    subscribeTo(CHECKED_TYPES);
  }

  @Override
  public void visitFile(AstNode node) {
    expectedLevel = 0;
    isBlockAlreadyReported = false;
    lastCheckedLine = 0;
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(BLOCK_TYPES)) {
      expectedLevel += indentationLevel;
      isBlockAlreadyReported = false;
    } else if (node.getToken().getColumn() != expectedLevel && !isExcluded(node)) {
      getContext().createLineViolation(this, "Make this line start at column " + (expectedLevel + 1) + ".", node);
      isBlockAlreadyReported = true;
    }
  }

  @Override
  public void leaveNode(AstNode node) {
    if (node.is(BLOCK_TYPES)) {
      expectedLevel -= indentationLevel;
      isBlockAlreadyReported = false;
    }

    Token lastToken = getLastToken(node);
    lastCheckedLine = lastToken.getLine();
  }

  private boolean isExcluded(AstNode node) {
    return isBlockAlreadyReported || !isLineFirstStatement(node);
  }

  private boolean isLineFirstStatement(AstNode node) {
    return lastCheckedLine != node.getTokenLine();
  }

  private static Token getLastToken(AstNode node) {
    AstNode lastNodeWithTokens = node;

    while (!lastNodeWithTokens.hasToken()) {
      lastNodeWithTokens = lastNodeWithTokens.getPreviousAstNode();
    }

    return lastNodeWithTokens.getLastToken();
  }

}
