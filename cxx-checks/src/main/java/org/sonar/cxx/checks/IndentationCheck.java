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

import com.google.common.io.Files;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.sonar.api.utils.SonarException;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;

@Rule(
  key = "IndentationCheck",
  description = "Well formed line itention improves readbility",
  priority = Priority.MAJOR)

public class IndentationCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {
  
  private static final AstNodeType[] BLOCK_TYPES = new AstNodeType[] {
      CxxGrammarImpl.statementSeq,
      CxxGrammarImpl.switchBlockStatementGroups,
      CxxGrammarImpl.switchBlockStatementGroup,
      CxxGrammarImpl.namespaceBody,
      CxxGrammarImpl.declarationSeq,
      CxxGrammarImpl.classSpecifier,
      CxxGrammarImpl.enumeratorList
  };

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[] {
      CxxGrammarImpl.statement,
      CxxGrammarImpl.emptyStatement,
      CxxGrammarImpl.emptyDeclaration,
      CxxGrammarImpl.memberDeclaration,
      CxxGrammarImpl.enumeratorDefinition,
  };

  private static final int DEFAULT_INDENTATION_LEVEL = 2;

  @RuleProperty(
    key = "indentationLevel",
    defaultValue = "" + DEFAULT_INDENTATION_LEVEL)
  public int indentationLevel = DEFAULT_INDENTATION_LEVEL;

  private static final int DEFAULT_TAB_WIDTH = 8;

  @RuleProperty(
    key = "tabWidth",
    defaultValue = "" + DEFAULT_TAB_WIDTH)
  public int tabWidth = DEFAULT_TAB_WIDTH;

  private int expectedLevel;
  private boolean isBlockAlreadyReported;
  private int lastCheckedLine;

  private Charset charset;

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

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

  private List<String> fileLines = null;
  private int getTabColumn(AstNode node)
  {
      if (fileLines == null) {
          try {
              fileLines = Files.readLines(getContext().getFile(), charset);
          } catch (IOException e) {
              throw new SonarException(e);
          }
      }

      int line = node.getToken().getLine() - 1;
      int column = node.getToken().getColumn();
      if (fileLines != null && line < fileLines.size()) {
          final String prefix = fileLines.get(line);
          for (int i = 0; i < prefix.length() && i < column; i++) {
              if (prefix.charAt(i) == '\t') {
                  column += tabWidth - 1;
              }
          }
      }
      return column;
  }

  private Stack<Integer> blockLevels = new Stack<Integer>();

  @Override
  public void visitNode(AstNode node) {
    if (node.is(BLOCK_TYPES)) {
      blockLevels.push(expectedLevel);
      expectedLevel += indentationLevel;
      isBlockAlreadyReported = false;

      AstNode firstChild = node.getFirstChild(CHECKED_TYPES);
      if (firstChild != null) {
        AstNode prevNode = firstChild.getPreviousAstNode();
        if (prevNode != null && firstChild.getToken().getLine() == prevNode.getToken().getLine()) {
          expectedLevel = getTabColumn(firstChild);
        }
      }
    } else if (node.getToken().getColumn() != expectedLevel && !isExcluded(node) && getTabColumn(node) != expectedLevel) {
      getContext().createLineViolation(this, "Make this line start at column " + (expectedLevel + 1) + ".", node);
      isBlockAlreadyReported = true;
    }
  }

  @Override
  public void leaveNode(AstNode node) {
    if (node.is(BLOCK_TYPES)) {
      expectedLevel = blockLevels.pop();
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
