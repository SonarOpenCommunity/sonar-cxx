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
import com.sonar.sslr.api.*;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;

import java.io.File;
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
      CxxGrammarImpl.declarationSeq,
      CxxGrammarImpl.classSpecifier,
      CxxGrammarImpl.enumeratorList,
      CxxGrammarImpl.iterationStatement,
      CxxGrammarImpl.ifStatement
  };

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[] {
      CxxGrammarImpl.statement,
      CxxGrammarImpl.emptyStatement,
      CxxGrammarImpl.declaration,
      CxxGrammarImpl.memberDeclaration,
      CxxGrammarImpl.enumeratorDefinition,
      CxxGrammarImpl.switchLabelStatement
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

  private static final boolean DEFAULT_INDENT_NAMESPACE = true;

  @RuleProperty(
    key = "indentNamespace",
    defaultValue = "" + DEFAULT_INDENT_NAMESPACE)
  boolean indentNamespace = DEFAULT_INDENT_NAMESPACE;

  private static final boolean DEFAULT_INDENT_LINKAGE_SPEC = false;

  @RuleProperty(
    key = "indentLinkageSpecification",
    defaultValue = "" + DEFAULT_INDENT_LINKAGE_SPEC)
  boolean indentLinkageSpecification = DEFAULT_INDENT_LINKAGE_SPEC;

  private static final boolean DEFAULT_INDENT_SWITCH_CASE = true;

  @RuleProperty(
    key = "indentSwitchCase",
    defaultValue = "" + DEFAULT_INDENT_SWITCH_CASE)
  boolean indentSwitchCase = DEFAULT_INDENT_SWITCH_CASE;

  private int expectedLevel;
  private boolean isBlockAlreadyReported;

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
  }

  @Override
  public void leaveFile(AstNode node) {
    fileLines = null;
  }

  private List<String> fileLines = null;
  private int getTabColumn(Token token)
  {
    if (fileLines == null) {
      try {
        fileLines = Files.readLines(getContext().getFile(), charset);
      } catch (IOException e) {
        throw new SonarException(e);
      }
    }

    int line = token.getLine() - 1;
    int column = token.getColumn();
    if (fileLines != null && line < fileLines.size()) {
      final String prefix = fileLines.get(line);
      for (int i = 0; i < prefix.length() && i < token.getColumn(); i++) {
        if (prefix.charAt(i) == '\t') {
          column += tabWidth - 1;
        }
      }
    }
    return column;
  }

  private Stack<Integer> blockLevels = new Stack<Integer>();

  /** Check if the node is a conditional block.
   * i.e. verify if the node is a statementSeq (inside a compoundStatement inside a statement), which is
   * inside an if or iteration statement.
   */
  private boolean isConditionalBlock(AstNode node) {
    if (node.is(CxxGrammarImpl.statementSeq)) {
      node = node.getParent() //compoundStatement
                 .getParent() //statement
                 .getParent();
      return node.is(CxxGrammarImpl.ifStatement) || node.is(CxxGrammarImpl.iterationStatement);
    }
    return false;
  }

  private boolean isNamespaceBody(AstNode node) {
    return node.is(CxxGrammarImpl.declarationSeq) && node.getParent().is(CxxGrammarImpl.namespaceBody);
  }

  private boolean isLinkageSpecificationBlock(AstNode node) {
    return node.is(CxxGrammarImpl.declarationSeq) && node.getParent().is(CxxGrammarImpl.linkageSpecification);
  }

  private void verifyIndent(AstNode node, Token token) {
    int expectedNodeLevel = token.getType() == CxxTokenType.PREPROCESSOR ? 0 : getExpectedNodeLevel(node);
    if (token.getColumn() != expectedNodeLevel && !isExcluded(node, token) && getTabColumn(token) != expectedNodeLevel) {
      getContext().createLineViolation(this, "Make this line start at column " + (expectedNodeLevel + 1) + ".", token);
      isBlockAlreadyReported = true;
    }
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CxxGrammarImpl.ifStatement) || node.is(CxxGrammarImpl.iterationStatement)) {
      blockLevels.push(expectedLevel);
      if (!node.getParent().getParent().is(CxxGrammarImpl.ifStatement) || isLineFirstStatement(node))
        //do not indent if this condition block is a direct child of another one, and not on his own line: e.g. "else if ()"
        expectedLevel += indentationLevel;
      isBlockAlreadyReported = false;
    }
    else if (node.is(BLOCK_TYPES)) {
      blockLevels.push(expectedLevel);
      if (!isConditionalBlock(node) && //do not further indent conditional block, the if/for/... statement already incremented the indentation
          (indentNamespace || !isNamespaceBody(node)) && //do not indent inside namespace
          (indentLinkageSpecification || !isLinkageSpecificationBlock(node)) && //do not indent inside linkage specification block
          (indentSwitchCase || !node.is(CxxGrammarImpl.switchBlockStatementGroups))) { //do not indent 'case' and 'default' inside switch statement
        expectedLevel += indentationLevel;
        isBlockAlreadyReported = false;
      }

      AstNode firstChild = node.getFirstChild(CHECKED_TYPES);
      if (firstChild != null) {
        AstNode prevNode = firstChild.getPreviousAstNode();
        if (prevNode != null && firstChild.getToken().getLine() == prevNode.getToken().getLine()) {
          expectedLevel = getTabColumn(getFirstTokenOnLine(firstChild));
        }
      }
    }
    else {
      for(Trivia trivia : node.getToken().getTrivia())
        for(Token token : trivia.getTokens()) {
          verifyIndent(node, token);
        }
      verifyIndent(node, node.getToken());
    }
  }

  /** Get the expected indent level for the specified.
   * Base indent is expectedLevel, but some special construct get specific indentation rules.
   * @param node The current node.
   * @return The indent level.
   */
  public int getExpectedNodeLevel(AstNode node)
  {
     if (node.is(CxxGrammarImpl.statement) && node.getFirstChild().is(CxxGrammarImpl.labeledStatement)) {
       //Label should be at the beginning of the line
       return 0;
     }
     if (node.is(CxxGrammarImpl.statement) && node.getFirstChild().is(CxxGrammarImpl.compoundStatement) &&
           (node.getParent().is(CxxGrammarImpl.iterationStatement) || node.getParent().is(CxxGrammarImpl.ifStatement))) {
       //Compound statements inside condition/loops should be at the same indent level as the if/else/loop keyword
       return expectedLevel - indentationLevel;
     }
     if (node.is(CxxGrammarImpl.switchLabelStatement)) {
       //Switch label statements are visited after we enter the switchBlockStatementGroup, so indent has already been incremented
       return expectedLevel - indentationLevel;
     }
     return expectedLevel;
  }

  @Override
  public void leaveNode(AstNode node) {
    if (node.is(BLOCK_TYPES)) {
      expectedLevel = blockLevels.pop();
      isBlockAlreadyReported = false;
    }
  }

  private boolean isExcluded(AstNode node, Token token) {
    return isBlockAlreadyReported || !isLineFirstToken(node, token);
  }

  private boolean isLineFirstToken(AstNode node, Token token) {
    for(Trivia trivia : node.getToken().getTrivia())
      for(Token t : trivia.getTokens())
        if (t.getLine() == token.getLine() && t.getColumn() < token.getColumn()) {
          return false;
        }

    AstNode prev = node.getPreviousAstNode();
    return prev == null || getLastToken(prev).getLine() != token.getLine();
  }

  private boolean isLineFirstStatement(AstNode node) {
    AstNode prev = node.getPreviousAstNode();
    return prev == null || getLastToken(prev).getLine() != node.getTokenLine();
  }

  private static Token getFirstTokenOnLine(AstNode node) {
    Token firstTokenOnLine = node.getToken();
    for(Trivia trivia : firstTokenOnLine.getTrivia())
      for(Token token : trivia.getTokens())
        if (token.getLine() == firstTokenOnLine.getLine() && token.getColumn() < firstTokenOnLine.getColumn()) {
          firstTokenOnLine = token;
        }
    return firstTokenOnLine;
  }

  private static Token getLastToken(AstNode node) {
    AstNode lastNodeWithTokens = node;

    while (!lastNodeWithTokens.hasToken()) {
      lastNodeWithTokens = lastNodeWithTokens.getPreviousAstNode();
    }

    return lastNodeWithTokens.getLastToken();
  }

}
