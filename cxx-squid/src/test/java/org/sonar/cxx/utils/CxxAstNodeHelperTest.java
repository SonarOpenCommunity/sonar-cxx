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
import org.sonar.cxx.parser.CxxKeyword;
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
    // Build: assignmentExpression → [logicalOrExpression("x"), "=",
    // initializerClause(→ expr)]
    var assignExpr = createNode(CxxGrammarImpl.assignmentExpression, "x");

    // LHS: identifier "x" with an associated symbol
    var lhsId = createIdentifierNode("x");
    var xSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(lhsId, xSymbol);

    // The LHS in the grammar is a logicalOrExpression that eventually holds the
    // identifier
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
    // Build: initDeclarator → [declarator(→declaratorId(→IDENTIFIER "x")),
    // initializer(→expr)]
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
  void testGetAssignedSymbolDirectLhsSymbol() {
    // Symbol set directly on the LHS node (logicalOrExpression), not on a descendant
    var assignExpr = createNode(CxxGrammarImpl.assignmentExpression, "x");

    var lhsExpr = createNode(CxxGrammarImpl.logicalOrExpression, "x");
    var xSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(lhsExpr, xSymbol);
    assignExpr.addChild(lhsExpr);
    assignExpr.addChild(createTokenNode("="));

    var rhsExpr = createNode(CxxGrammarImpl.initializerClause, "foo()");
    var funcCall = createNode(CxxGrammarImpl.postfixExpression, "foo");
    rhsExpr.addChild(funcCall);
    assignExpr.addChild(rhsExpr);

    assertThat(CxxAstNodeHelper.getAssignedSymbol(funcCall)).isEqualTo(xSymbol);

    AstNodeSymbolExtension.clear();
  }

  @Test
  void testGetAssignedSymbolDirectDeclaratorSymbol() {
    // Symbol set directly on the declarator node (not on declaratorId or identifier)
    var initDecl = createNode(CxxGrammarImpl.initDeclarator, "x");

    var declarator = createNode(CxxGrammarImpl.declarator, "x");
    var xSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(declarator, xSymbol);
    initDecl.addChild(declarator);

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
  void testGetAssignedSymbolDeclaratorIdSymbol() {
    // Symbol set on declaratorId node (not on the identifier descendant)
    var initDecl = createNode(CxxGrammarImpl.initDeclarator, "x");

    var declarator = createNode(CxxGrammarImpl.declarator, "x");
    var declaratorId = createNode(CxxGrammarImpl.declaratorId, "x");
    var xSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(declaratorId, xSymbol);
    declarator.addChild(declaratorId);
    initDecl.addChild(declarator);

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

  // -------------------------------------------------------------------------
  // isConstructorCall
  // -------------------------------------------------------------------------

  @Test
  void testIsConstructorCallNull() {
    assertThat(CxxAstNodeHelper.isConstructorCall(null)).isFalse();
  }

  @Test
  void testIsConstructorCallTrue() {
    var node = createNode(CxxGrammarImpl.newExpression, "new");
    assertThat(CxxAstNodeHelper.isConstructorCall(node)).isTrue();
  }

  @Test
  void testIsConstructorCallWrongType() {
    var node = createNode(CxxGrammarImpl.postfixExpression, "foo");
    assertThat(CxxAstNodeHelper.isConstructorCall(node)).isFalse();
  }

  // -------------------------------------------------------------------------
  // getFunctionCallArguments
  // -------------------------------------------------------------------------

  @Test
  void testGetFunctionCallArgumentsNull() {
    assertThat(CxxAstNodeHelper.getFunctionCallArguments(null)).isEmpty();
  }

  @Test
  void testGetFunctionCallArgumentsNotACall() {
    var node = createNode(CxxGrammarImpl.primaryExpression, "x");
    assertThat(CxxAstNodeHelper.getFunctionCallArguments(node)).isEmpty();
  }

  @Test
  void testGetFunctionCallArgumentsNoArgs() {
    // postfixExpression with "(" but no expressionList child
    var callNode = createNode(CxxGrammarImpl.postfixExpression, "foo");
    callNode.addChild(createTokenNode("("));
    callNode.addChild(createTokenNode(")"));
    assertThat(CxxAstNodeHelper.getFunctionCallArguments(callNode)).isEmpty();
  }

  @Test
  void testGetFunctionCallArgumentsWithArgs() {
    var callNode = createNode(CxxGrammarImpl.postfixExpression, "foo");
    callNode.addChild(createTokenNode("("));
    var exprList = createNode(CxxGrammarImpl.expressionList, "args");
    var arg1 = createNode(CxxGrammarImpl.initializerClause, "a");
    var arg2 = createNode(CxxGrammarImpl.initializerClause, "b");
    exprList.addChild(arg1);
    exprList.addChild(arg2);
    callNode.addChild(exprList);
    callNode.addChild(createTokenNode(")"));
    var args = CxxAstNodeHelper.getFunctionCallArguments(callNode);
    assertThat(args).hasSize(2);
  }

  // -------------------------------------------------------------------------
  // getFunctionCallName
  // -------------------------------------------------------------------------

  @Test
  void testGetFunctionCallNameNull() {
    assertThat(CxxAstNodeHelper.getFunctionCallName(null)).isNull();
  }

  @Test
  void testGetFunctionCallNameWrongType() {
    var node = createNode(CxxGrammarImpl.primaryExpression, "foo");
    assertThat(CxxAstNodeHelper.getFunctionCallName(node)).isNull();
  }

  @Test
  void testGetFunctionCallNameSimple() {
    // postfixExpression → primaryExpression → idExpression → IDENTIFIER("myFunc")
    var callNode = createNode(CxxGrammarImpl.postfixExpression, "myFunc");
    var primary = createNode(CxxGrammarImpl.primaryExpression, "myFunc");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "myFunc");
    idExpr.addChild(createIdentifierNode("myFunc"));
    primary.addChild(idExpr);
    callNode.addChild(primary);
    callNode.addChild(createTokenNode("("));
    callNode.addChild(createTokenNode(")"));
    assertThat(CxxAstNodeHelper.getFunctionCallName(callNode)).isEqualTo("myFunc");
  }

  @Test
  void testGetFunctionCallNameNoIdExpression() {
    // postfixExpression with no idExpression and no typeName — returns null
    var callNode = createNode(CxxGrammarImpl.postfixExpression, "x");
    callNode.addChild(createTokenNode("("));
    assertThat(CxxAstNodeHelper.getFunctionCallName(callNode)).isNull();
  }

  // -------------------------------------------------------------------------
  // getFunctionDefinitionName
  // -------------------------------------------------------------------------

  @Test
  void testGetFunctionDefinitionNameNull() {
    assertThat(CxxAstNodeHelper.getFunctionDefinitionName(null)).isNull();
  }

  @Test
  void testGetFunctionDefinitionNameWrongType() {
    var node = createNode(CxxGrammarImpl.postfixExpression, "x");
    assertThat(CxxAstNodeHelper.getFunctionDefinitionName(node)).isNull();
  }

  @Test
  void testGetFunctionDefinitionNameValid() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "compute");
    var decl = createNode(CxxGrammarImpl.declarator, "compute");
    var declId = createNode(CxxGrammarImpl.declaratorId, "compute");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "compute");
    idExpr.addChild(createIdentifierNode("compute"));
    declId.addChild(idExpr);
    decl.addChild(declId);
    funcDef.addChild(decl);
    assertThat(CxxAstNodeHelper.getFunctionDefinitionName(funcDef)).isEqualTo("compute");
  }

  @Test
  void testGetFunctionDefinitionNameNoDeclarator() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    // no declarator child
    assertThat(CxxAstNodeHelper.getFunctionDefinitionName(funcDef)).isNull();
  }

  // -------------------------------------------------------------------------
  // getFunctionDefinitionBody
  // -------------------------------------------------------------------------

  @Test
  void testGetFunctionDefinitionBodyNull() {
    assertThat(CxxAstNodeHelper.getFunctionDefinitionBody(null)).isNull();
  }

  @Test
  void testGetFunctionDefinitionBodyWrongType() {
    var node = createNode(CxxGrammarImpl.primaryExpression, "x");
    assertThat(CxxAstNodeHelper.getFunctionDefinitionBody(node)).isNull();
  }

  @Test
  void testGetFunctionDefinitionBodyValid() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    var funcBody = createNode(CxxGrammarImpl.functionBody, "body");
    funcDef.addChild(funcBody);
    assertThat(CxxAstNodeHelper.getFunctionDefinitionBody(funcDef)).isEqualTo(funcBody);
  }

  @Test
  void testGetFunctionDefinitionBodyNotPresent() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    // No functionBody child
    assertThat(CxxAstNodeHelper.getFunctionDefinitionBody(funcDef)).isNull();
  }

  // -------------------------------------------------------------------------
  // getFunctionDefinitionParameters
  // -------------------------------------------------------------------------

  @Test
  void testGetFunctionDefinitionParametersNull() {
    assertThat(CxxAstNodeHelper.getFunctionDefinitionParameters(null)).isEmpty();
  }

  @Test
  void testGetFunctionDefinitionParametersWrongType() {
    var node = createNode(CxxGrammarImpl.primaryExpression, "x");
    assertThat(CxxAstNodeHelper.getFunctionDefinitionParameters(node)).isEmpty();
  }

  @Test
  void testGetFunctionDefinitionParametersNone() {
    // functionDefinition with no declarator
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    assertThat(CxxAstNodeHelper.getFunctionDefinitionParameters(funcDef)).isEmpty();
  }

  @Test
  void testGetFunctionDefinitionParametersValid() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    var decl = createNode(CxxGrammarImpl.declarator, "f");
    var paramsQuals = createNode(CxxGrammarImpl.parametersAndQualifiers, "()");
    var param1 = createNode(CxxGrammarImpl.parameterDeclaration, "int x");
    var param2 = createNode(CxxGrammarImpl.parameterDeclaration, "int y");
    paramsQuals.addChild(param1);
    paramsQuals.addChild(param2);
    decl.addChild(paramsQuals);
    funcDef.addChild(decl);
    var params = CxxAstNodeHelper.getFunctionDefinitionParameters(funcDef);
    assertThat(params).containsExactly(param1, param2);
  }

  // -------------------------------------------------------------------------
  // getEnclosingFunction / getEnclosingClass
  // -------------------------------------------------------------------------

  @Test
  void testGetEnclosingFunctionNull() {
    assertThat(CxxAstNodeHelper.getEnclosingFunction(null)).isNull();
  }

  @Test
  void testGetEnclosingFunctionNotNested() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    // funcDef has no parent — itself is not an ancestor of itself
    assertThat(CxxAstNodeHelper.getEnclosingFunction(funcDef)).isNull();
  }

  @Test
  void testGetEnclosingFunctionNested() {
    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "f");
    var inner = createNode(CxxGrammarImpl.primaryExpression, "x");
    funcDef.addChild(inner);
    assertThat(CxxAstNodeHelper.getEnclosingFunction(inner)).isEqualTo(funcDef);
  }

  @Test
  void testGetEnclosingClassNull() {
    assertThat(CxxAstNodeHelper.getEnclosingClass(null)).isNull();
  }

  @Test
  void testGetEnclosingClassNotNested() {
    var cls = createNode(CxxGrammarImpl.classSpecifier, "MyClass");
    assertThat(CxxAstNodeHelper.getEnclosingClass(cls)).isNull();
  }

  @Test
  void testGetEnclosingClassNested() {
    var cls = createNode(CxxGrammarImpl.classSpecifier, "MyClass");
    var inner = createNode(CxxGrammarImpl.primaryExpression, "x");
    cls.addChild(inner);
    assertThat(CxxAstNodeHelper.getEnclosingClass(inner)).isEqualTo(cls);
  }

  // -------------------------------------------------------------------------
  // getIdentifierName
  // -------------------------------------------------------------------------

  @Test
  void testGetIdentifierNameNull() {
    assertThat(CxxAstNodeHelper.getIdentifierName(null)).isNull();
  }

  @Test
  void testGetIdentifierNameIsIdentifier() {
    var id = createIdentifierNode("myVar");
    assertThat(CxxAstNodeHelper.getIdentifierName(id)).isEqualTo("myVar");
  }

  @Test
  void testGetIdentifierNameHasDescendant() {
    var wrapper = createNode(CxxGrammarImpl.declaratorId, "myVar");
    wrapper.addChild(createIdentifierNode("myVar"));
    assertThat(CxxAstNodeHelper.getIdentifierName(wrapper)).isEqualTo("myVar");
  }

  @Test
  void testGetIdentifierNameNoIdentifier() {
    // A node with no IDENTIFIER token and no IDENTIFIER descendant
    var node = createNode(CxxGrammarImpl.primaryExpression, "x");
    // createNode uses TestTokenType, not IDENTIFIER
    assertThat(CxxAstNodeHelper.getIdentifierName(node)).isNull();
  }

  // -------------------------------------------------------------------------
  // isReturnStatement
  // -------------------------------------------------------------------------

  @Test
  void testIsReturnStatementNull() {
    assertThat(CxxAstNodeHelper.isReturnStatement(null)).isFalse();
  }

  @Test
  void testIsReturnStatementWrongType() {
    var node = createNode(CxxGrammarImpl.primaryExpression, "x");
    assertThat(CxxAstNodeHelper.isReturnStatement(node)).isFalse();
  }

  @Test
  void testIsReturnStatementTrue() {
    var jumpStmt = createNode(CxxGrammarImpl.jumpStatement, "return");
    var returnKw = createKeywordNode(CxxKeyword.RETURN);
    jumpStmt.addChild(returnKw);
    assertThat(CxxAstNodeHelper.isReturnStatement(jumpStmt)).isTrue();
  }

  @Test
  void testIsReturnStatementBreak() {
    var jumpStmt = createNode(CxxGrammarImpl.jumpStatement, "break");
    var breakKw = createKeywordNode(CxxKeyword.BREAK);
    jumpStmt.addChild(breakKw);
    assertThat(CxxAstNodeHelper.isReturnStatement(jumpStmt)).isFalse();
  }

  @Test
  void testIsReturnStatementNoChildren() {
    var jumpStmt = createNode(CxxGrammarImpl.jumpStatement, "return");
    assertThat(CxxAstNodeHelper.isReturnStatement(jumpStmt)).isFalse();
  }

  // -------------------------------------------------------------------------
  // getReturnExpression
  // -------------------------------------------------------------------------

  @Test
  void testGetReturnExpressionNull() {
    assertThat(CxxAstNodeHelper.getReturnExpression(null)).isNull();
  }

  @Test
  void testGetReturnExpressionNotReturn() {
    var jumpStmt = createNode(CxxGrammarImpl.jumpStatement, "break");
    var breakKw = createKeywordNode(CxxKeyword.BREAK);
    jumpStmt.addChild(breakKw);
    assertThat(CxxAstNodeHelper.getReturnExpression(jumpStmt)).isNull();
  }

  @Test
  void testGetReturnExpressionVoid() {
    // return; — no expression
    var jumpStmt = createNode(CxxGrammarImpl.jumpStatement, "return");
    jumpStmt.addChild(createKeywordNode(CxxKeyword.RETURN));
    assertThat(CxxAstNodeHelper.getReturnExpression(jumpStmt)).isNull();
  }

  @Test
  void testGetReturnExpressionWithValue() {
    var jumpStmt = createNode(CxxGrammarImpl.jumpStatement, "return");
    jumpStmt.addChild(createKeywordNode(CxxKeyword.RETURN));
    var expr = createNode(CxxGrammarImpl.exprOrBracedInitList, "42");
    jumpStmt.addChild(expr);
    assertThat(CxxAstNodeHelper.getReturnExpression(jumpStmt)).isEqualTo(expr);
  }

  // -------------------------------------------------------------------------
  // getMemberAccessName
  // -------------------------------------------------------------------------

  @Test
  void testGetMemberAccessNameNull() {
    assertThat(CxxAstNodeHelper.getMemberAccessName(null)).isNull();
  }

  @Test
  void testGetMemberAccessNameNotMemberAccess() {
    var node = createNode(CxxGrammarImpl.postfixExpression, "foo");
    // No "." or "->" child — not a member access
    assertThat(CxxAstNodeHelper.getMemberAccessName(node)).isNull();
  }

  @Test
  void testGetMemberAccessNameDot() {
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "obj");
    var qualifier = createNode(CxxGrammarImpl.primaryExpression, "obj");
    postfix.addChild(qualifier);
    postfix.addChild(createTokenNode("."));
    postfix.addChild(createIdentifierNode("method"));
    assertThat(CxxAstNodeHelper.getMemberAccessName(postfix)).isEqualTo("method");
  }

  @Test
  void testGetMemberAccessNameArrow() {
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "ptr");
    var qualifier = createNode(CxxGrammarImpl.primaryExpression, "ptr");
    postfix.addChild(qualifier);
    postfix.addChild(createTokenNode("->"));
    postfix.addChild(createIdentifierNode("field"));
    assertThat(CxxAstNodeHelper.getMemberAccessName(postfix)).isEqualTo("field");
  }

  // -------------------------------------------------------------------------
  // isInvocationOnVariable — acceptParentMemberAccess=true path
  // -------------------------------------------------------------------------

  @Test
  void testIsInvocationOnVariableChainedAccess() {
    // a.b.method() — method is invoked on b which is accessed via a.
    // With acceptParentMemberAccess=true, also check the outer qualifier 'a'.
    var outerObj = new SourceCodeSymbol("a", Symbol.Kind.VARIABLE, null);

    // Inner member access: a.b
    var innerQualifier = createNode(CxxGrammarImpl.primaryExpression, "a");
    var innerObjId = createIdentifierNode("a");
    AstNodeSymbolExtension.setSymbol(innerObjId, outerObj);
    innerQualifier.addChild(innerObjId);

    var innerPostfix = createNode(CxxGrammarImpl.postfixExpression, "a");
    innerPostfix.addChild(innerQualifier);
    innerPostfix.addChild(createTokenNode("."));
    innerPostfix.addChild(createIdentifierNode("b"));

    // Outer call: [a.b].method()
    var outerPostfix = createNode(CxxGrammarImpl.postfixExpression, "a");
    outerPostfix.addChild(innerPostfix);
    outerPostfix.addChild(createTokenNode("."));
    outerPostfix.addChild(createIdentifierNode("method"));
    outerPostfix.addChild(createTokenNode("("));
    outerPostfix.addChild(createTokenNode(")"));

    // With acceptParentMemberAccess=true, the chain should match 'a'
    assertThat(CxxAstNodeHelper.isInvocationOnVariable(outerPostfix, outerObj, true)).isTrue();

    AstNodeSymbolExtension.clear();
  }

  // -------------------------------------------------------------------------
  // getFunctionCallName — qualified name via qualifiedId (getIdentifierText)
  // -------------------------------------------------------------------------

  @Test
  void testGetFunctionCallNameQualified() {
    // postfixExpression → idExpression → qualifiedId → ["ns", "::", "foo"]
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "ns");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "ns");
    var qualId = createNode(CxxGrammarImpl.qualifiedId, "ns::foo");

    // Add three tokens to qualifiedId: "ns", "::", "foo"
    qualId.addChild(createIdentifierNode("ns"));
    qualId.addChild(createTokenNode("::"));
    qualId.addChild(createIdentifierNode("foo"));

    idExpr.addChild(qualId);
    postfix.addChild(idExpr);
    postfix.addChild(createTokenNode("("));
    postfix.addChild(createTokenNode(")"));

    assertThat(CxxAstNodeHelper.getFunctionCallName(postfix)).isEqualTo("ns::foo");
  }

  @Test
  void testGetFunctionCallNameViaTypeName() {
    // postfixExpression → typeName → className → IDENTIFIER("vector")
    // This is a functional-style type conversion: vector(1, 2, 3)
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "vector");
    var typeName = createNode(CxxGrammarImpl.typeName, "vector");
    var className = createNode(CxxGrammarImpl.className, "vector");
    className.addChild(createIdentifierNode("vector"));
    typeName.addChild(className);
    postfix.addChild(typeName);
    postfix.addChild(createTokenNode("("));
    postfix.addChild(createTokenNode(")"));
    assertThat(CxxAstNodeHelper.getFunctionCallName(postfix)).isEqualTo("vector");
  }

  @Test
  void testGetMemberAccessNameViaIdDescendant() {
    // postfixExpression → [qualifier, ".", wrapper(→IDENTIFIER("field"))]
    // Tests the path where the member node after "." isn't directly an IDENTIFIER
    // but has a descendant IDENTIFIER
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "obj");
    var qualifier = createNode(CxxGrammarImpl.primaryExpression, "obj");
    postfix.addChild(qualifier);
    postfix.addChild(createTokenNode("."));
    // Wrapper node containing an IDENTIFIER descendant
    var wrapper = createNode(CxxGrammarImpl.idExpression, "field");
    wrapper.addChild(createIdentifierNode("field"));
    postfix.addChild(wrapper);
    assertThat(CxxAstNodeHelper.getMemberAccessName(postfix)).isEqualTo("field");
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------
  private AstNode createKeywordNode(com.sonar.cxx.sslr.api.TokenType type) {
    var token = Token.builder()
        .setLine(1)
        .setColumn(0)
        .setValueAndOriginalValue(type.getValue())
        .setType(type)
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
