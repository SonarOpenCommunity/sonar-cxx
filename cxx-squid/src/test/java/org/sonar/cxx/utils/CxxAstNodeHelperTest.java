/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.utils;

import static org.assertj.core.api.Assertions.*;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.SourceCodeSymbol;
import org.sonar.cxx.squidbridge.api.Symbol;

class CxxAstNodeHelperTest {

  @Test
  void testGetAssignedSymbolNull() {
    assertThat(CxxAstNodeHelper.getAssignedSymbol(null)).isNull();
  }

  @Test
  void testGetAssignedSymbolNoAssignmentContext() {
    var node = createNode(CxxGrammarImpl.postfixExpression, "foo");
    assertThat(CxxAstNodeHelper.getAssignedSymbol(node)).isNull();
  }

  @Test
  void testGetAssignedSymbolFromAssignmentExpression() {
    // Build: assignmentExpression → [logicalOrExpression("x"), "=", initializerClause(→ expr)]
    var assignExpr = createNode(CxxGrammarImpl.assignmentExpression, "x");

    // LHS: identifier "x" with an associated symbol
    var lhsId = createIdentifierNode("x");
    var xSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(lhsId, xSymbol);

    // The LHS in the grammar is a logicalOrExpression that eventually holds the identifier
    var lhsExpr = createNode(CxxGrammarImpl.logicalOrExpression, "x");
    lhsExpr.addChild(lhsId);
    assignExpr.addChild(lhsExpr);

    // Assignment operator
    var op = createTokenNode("=");
    assignExpr.addChild(op);

    // RHS: the expression we'll look up from
    var rhsExpr = createNode(CxxGrammarImpl.initializerClause, "foo()");
    var funcCall = createNode(CxxGrammarImpl.postfixExpression, "foo");
    rhsExpr.addChild(funcCall);
    assignExpr.addChild(rhsExpr);

    // Now look up the assigned symbol from the RHS expression
    assertThat(CxxAstNodeHelper.getAssignedSymbol(funcCall)).isEqualTo(xSymbol);

    // Cleanup
    AstNodeSymbolExtension.clear();
  }

  @Test
  void testGetAssignedSymbolFromDeclaration() {
    // Build: initDeclarator → [declarator(→declaratorId(→IDENTIFIER "x")), initializer(→expr)]
    var initDecl = createNode(CxxGrammarImpl.initDeclarator, "x");

    var declarator = createNode(CxxGrammarImpl.declarator, "x");
    var declaratorId = createNode(CxxGrammarImpl.declaratorId, "x");
    var identifier = createIdentifierNode("x");
    var xSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(identifier, xSymbol);

    declaratorId.addChild(identifier);
    declarator.addChild(declaratorId);
    initDecl.addChild(declarator);

    // Initializer with expression
    var initializer = createNode(CxxGrammarImpl.initializer, "=");
    var initClause = createNode(CxxGrammarImpl.initializerClause, "foo()");
    var funcCall = createNode(CxxGrammarImpl.postfixExpression, "foo");
    initClause.addChild(funcCall);
    initializer.addChild(initClause);
    initDecl.addChild(initializer);

    assertThat(CxxAstNodeHelper.getAssignedSymbol(funcCall)).isEqualTo(xSymbol);

    AstNodeSymbolExtension.clear();
  }

  @Test
  void testIsInvocationOnVariableNull() {
    assertThat(CxxAstNodeHelper.isInvocationOnVariable(null, null, false)).isFalse();

    var node = createNode(CxxGrammarImpl.postfixExpression, "foo");
    assertThat(CxxAstNodeHelper.isInvocationOnVariable(node, null, false)).isFalse();
  }

  @Test
  void testIsInvocationOnVariableNonMemberCall() {
    // Simple function call foo() is not a member access
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "foo");
    var primary = createNode(CxxGrammarImpl.primaryExpression, "foo");
    postfix.addChild(primary);
    var paren = createTokenNode("(");
    postfix.addChild(paren);
    var closeParen = createTokenNode(")");
    postfix.addChild(closeParen);

    var symbol = new SourceCodeSymbol("obj", Symbol.Kind.VARIABLE, null);
    assertThat(CxxAstNodeHelper.isInvocationOnVariable(postfix, symbol, false)).isFalse();
  }

  @Test
  void testIsInvocationOnVariableMatch() {
    // obj.method() where obj is the target variable
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "obj");

    // Qualifier: primaryExpression "obj"
    var qualifier = createNode(CxxGrammarImpl.primaryExpression, "obj");
    var objId = createIdentifierNode("obj");
    var objSymbol = new SourceCodeSymbol("obj", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(objId, objSymbol);
    qualifier.addChild(objId);
    postfix.addChild(qualifier);

    // Dot operator
    var dot = createTokenNode(".");
    postfix.addChild(dot);

    // Member name
    var memberId = createIdentifierNode("method");
    postfix.addChild(memberId);

    // Open paren (marks this as a function call)
    var paren = createTokenNode("(");
    postfix.addChild(paren);
    var closeParen = createTokenNode(")");
    postfix.addChild(closeParen);

    assertThat(CxxAstNodeHelper.isInvocationOnVariable(postfix, objSymbol, false)).isTrue();

    // Different symbol should not match
    var otherSymbol = new SourceCodeSymbol("other", Symbol.Kind.VARIABLE, null);
    assertThat(CxxAstNodeHelper.isInvocationOnVariable(postfix, otherSymbol, false)).isFalse();

    AstNodeSymbolExtension.clear();
  }

  @Test
  void testIsInvocationOnVariableNoMatch() {
    // obj.method() where we check for a different variable
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "obj");

    var qualifier = createNode(CxxGrammarImpl.primaryExpression, "obj");
    postfix.addChild(qualifier);

    var dot = createTokenNode(".");
    postfix.addChild(dot);

    var memberId = createIdentifierNode("method");
    postfix.addChild(memberId);

    var paren = createTokenNode("(");
    postfix.addChild(paren);

    var symbol = new SourceCodeSymbol("obj", Symbol.Kind.VARIABLE, null);
    // No symbol set on the qualifier node, so it can't match
    assertThat(CxxAstNodeHelper.isInvocationOnVariable(postfix, symbol, false)).isFalse();
  }

  private AstNode createNode(AstNodeType type, String value) {
    var token = Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue(value)
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(type, value, token);
  }

  private AstNode createIdentifierNode(String name) {
    var token = Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue(name)
      .setType(GenericTokenType.IDENTIFIER)
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(token);
  }

  private AstNode createTokenNode(String value) {
    var token = Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue(value)
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(token);
  }

  private static class TestTokenType implements com.sonar.cxx.sslr.api.TokenType {
    @Override
    public String getName() {
      return "TEST";
    }

    @Override
    public String getValue() {
      return "test";
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }
  }
}
