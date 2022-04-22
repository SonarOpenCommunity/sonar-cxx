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
package com.sonar.cxx.sslr.api;

import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import static com.sonar.cxx.sslr.test.minic.MiniCParser.parseString;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AstNodeTest {

  @Test
  void testAddChild() {
    var expr = new AstNode(new NodeType(), "expr", null);
    var stat = new AstNode(new NodeType(), "stat", null);
    var assign = new AstNode(new NodeType(), "assign", null);
    expr.addChild(stat);
    expr.addChild(assign);

    assertThat(expr.getChildren()).contains(stat, assign);
  }

  @Test
  void testAddNullChild() {
    var expr = new AstNode(new NodeType(), "expr", null);
    expr.addChild(null);

    assertThat(expr.hasChildren()).isFalse();
  }

  @Test
  void testAddChildWhichMustBeSkippedFromAst() {
    var expr = new AstNode(new NodeType(), "expr", null);
    var all = new AstNode(new NodeType(true), "all", null);
    var stat = new AstNode(new NodeType(), "stat", null);
    all.addChild(stat);
    expr.addChild(all);

    var many = new AstNode(new NodeType(true), "many", null);
    var print = new AstNode(new NodeType(), "print", null);
    many.addChild(print);
    expr.addChild(many);

    assertThat(expr.getChildren()).contains(stat, print);
  }

  @Test
  void testAddMatcherChildWithoutChildren() {
    var expr = new AstNode(new NodeType(), "expr", null);
    var all = new AstNode(new NodeType(true), "all", null);
    expr.addChild(all);

    assertThat(expr.getChildren()).isEmpty();
  }

  @Test
  void testHasChildren() {
    var expr = new AstNode(new NodeType(), "expr", null);
    assertThat(expr.hasChildren()).isFalse();
  }

  @Test
  void testGetChild() {
    var parent = new AstNode(new NodeType(), "parent", null);
    var child1 = new AstNode(new NodeType(), "child1", null);
    var child2 = new AstNode(new NodeType(), "child2", null);
    parent.addChild(child1);
    parent.addChild(child2);

    assertThat(parent.getChild(0)).isSameAs(child1);
    assertThat(parent.getChild(1)).isSameAs(child2);
  }

  @Test
  void testGetLastToken() {
    var lastToken = mock(Token.class);
    when(lastToken.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(lastToken.getValue()).thenReturn("LAST_TOKEN");
    var parent = new AstNode(new NodeType(), "parent", lastToken);
    var child1 = new AstNode(new NodeType(), "child1", null);
    var child2 = new AstNode(new NodeType(), "child2", lastToken);
    parent.addChild(child1);
    parent.addChild(child2);

    assertThat(parent.getLastToken()).isSameAs(lastToken);
    assertThat(child2.getLastToken()).isSameAs(lastToken);
  }

  @Test
  void testGetTokens() {
    var child1Token = mock(Token.class);
    when(child1Token.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(child1Token.getValue()).thenReturn("CHILD 1");
    var child2Token = mock(Token.class);
    when(child2Token.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(child2Token.getValue()).thenReturn("CHILD 2");
    var parent = new AstNode(new NodeType(), "parent", null);
    var child1 = new AstNode(new NodeType(), "child1", child1Token);
    var child2 = new AstNode(new NodeType(), "child2", child2Token);
    parent.addChild(child1);
    parent.addChild(child2);

    assertThat(parent.getTokens()).hasSize(2);
    assertThat(parent.getTokens().get(0)).isSameAs(child1Token);
    assertThat(parent.getTokens().get(1)).isSameAs(child2Token);
  }

  @Test
  void testGetChildWithBadIndex() {
    var token = mock(Token.class);
    when(token.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(token.getValue()).thenReturn("PI");
    var parent = new AstNode(new NodeType(), "parent", token);
    var child1 = new AstNode(new NodeType(), "child1", null);

    var thrown = catchThrowableOfType(() -> {
      parent.addChild(child1);
      parent.getChild(1);
    }, IllegalStateException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void testNextSibling() {
    var expr1 = new AstNode(new NodeType(), "expr1", null);
    var expr2 = new AstNode(new NodeType(), "expr2", null);
    var statement = new AstNode(new NodeType(), "statement", null);

    statement.addChild(expr1);
    statement.addChild(expr2);

    assertThat(expr1.nextSibling()).isSameAs(expr2);
    assertThat(expr2.nextSibling()).isNull();
  }

  @Test
  void testPreviousSibling() {
    var expr1 = new AstNode(new NodeType(), "expr1", null);
    var expr2 = new AstNode(new NodeType(), "expr2", null);
    var statement = new AstNode(new NodeType(), "statement", null);

    statement.addChild(expr1);
    statement.addChild(expr2);

    assertThat(expr1.previousSibling()).isNull();
    assertThat(expr2.previousSibling()).isSameAs(expr1);
  }

  @Test
  void testFindFirstDirectChild() {
    var expr = new AstNode(new NodeType(), "expr", null);
    var statRule = new NodeType();
    var stat = new AstNode(statRule, "stat", null);
    var identifier = new AstNode(new NodeType(), "identifier", null);
    expr.addChild(stat);
    expr.addChild(identifier);

    assertThat(expr.findFirstDirectChild(statRule)).isSameAs(stat);
    var anotherRule = new NodeType();
    assertThat(expr.findFirstDirectChild(anotherRule, statRule)).isSameAs(stat);
  }

  @Test
  void testIs() {
    var declarationNode = parseString("int a = 0;").getFirstChild();

    assertThat(declarationNode.is(MiniCGrammar.DEFINITION)).isTrue();
    assertThat(declarationNode.is(MiniCGrammar.COMPILATION_UNIT, MiniCGrammar.DEFINITION)).isTrue();
    assertThat(declarationNode.is(MiniCGrammar.DEFINITION, MiniCGrammar.COMPILATION_UNIT)).isTrue();
    assertThat(declarationNode.is(MiniCGrammar.COMPILATION_UNIT)).isFalse();
  }

  @Test
  void testIsNot() {
    var declarationNode = parseString("int a = 0;").getFirstChild();

    assertThat(declarationNode.isNot(MiniCGrammar.DEFINITION)).isFalse();
    assertThat(declarationNode.isNot(MiniCGrammar.COMPILATION_UNIT, MiniCGrammar.DEFINITION)).isFalse();
    assertThat(declarationNode.isNot(MiniCGrammar.DEFINITION, MiniCGrammar.COMPILATION_UNIT)).isFalse();
    assertThat(declarationNode.isNot(MiniCGrammar.COMPILATION_UNIT)).isTrue();
  }

  @Test
  void testFindChildren() {
    var fileNode = parseString("int a = 0; int myFunction() { int b = 0; { int c = 0; } }");

    var binVariableDeclarationNodes = fileNode.findChildren(MiniCGrammar.BIN_VARIABLE_DEFINITION);
    assertThat(binVariableDeclarationNodes).hasSize(3);
    assertThat(binVariableDeclarationNodes.get(0).getTokenValue()).isEqualTo("a");
    assertThat(binVariableDeclarationNodes.get(1).getTokenValue()).isEqualTo("b");
    assertThat(binVariableDeclarationNodes.get(2).getTokenValue()).isEqualTo("c");

    var binVDeclarationNodes = fileNode.findChildren(MiniCGrammar.BIN_VARIABLE_DEFINITION,
                                                 MiniCGrammar.BIN_FUNCTION_DEFINITION);
    assertThat(binVDeclarationNodes).hasSize(4);
    assertThat(binVDeclarationNodes.get(0).getTokenValue()).isEqualTo("a");
    assertThat(binVDeclarationNodes.get(1).getTokenValue()).isEqualTo("myFunction");
    assertThat(binVDeclarationNodes.get(2).getTokenValue()).isEqualTo("b");
    assertThat(binVDeclarationNodes.get(3).getTokenValue()).isEqualTo("c");

    assertThat(fileNode.findChildren(MiniCGrammar.MULTIPLICATIVE_EXPRESSION)).isEmpty();
  }

  @Test
  void testFindDirectChildren() {
    var fileNode = parseString("int a = 0; void myFunction() { int b = 0*3; { int c = 0; } }");

    var declarationNodes = fileNode.findDirectChildren(MiniCGrammar.DEFINITION);
    assertThat(declarationNodes).hasSize(2);
    assertThat(declarationNodes.get(0).getTokenValue()).isEqualTo("int");
    assertThat(declarationNodes.get(1).getTokenValue()).isEqualTo("void");

    var binVDeclarationNodes = fileNode.findDirectChildren(MiniCGrammar.BIN_VARIABLE_DEFINITION,
                                                       MiniCGrammar.BIN_FUNCTION_DEFINITION);
    assertThat(binVDeclarationNodes).isEmpty();
  }

  @Test
  void testFindFirstChildAndHasChildren() {
    var expr = new AstNode(new NodeType(), "expr", null);
    var stat = new AstNode(new NodeType(), "stat", null);
    var indentifierRule = new NodeType();
    var identifier = new AstNode(indentifierRule, "identifier", null);
    expr.addChild(stat);
    expr.addChild(identifier);

    assertThat(expr.findFirstChild(indentifierRule)).isSameAs(identifier);
    assertThat(expr.hasChildren(indentifierRule)).isTrue();
    var anotherRule = new NodeType();
    assertThat(expr.findFirstChild(anotherRule)).isNull();
    assertThat(expr.hasChildren(anotherRule)).isFalse();
  }

  @Test
  void testHasParents() {
    var exprRule = new NodeType();
    var expr = new AstNode(exprRule, "expr", null);
    var stat = new AstNode(new NodeType(), "stat", null);
    var identifier = new AstNode(new NodeType(), "identifier", null);
    expr.addChild(stat);
    expr.addChild(identifier);

    assertThat(identifier.hasParents(exprRule)).isTrue();
    assertThat(identifier.hasParents(new NodeType())).isFalse();
  }

  @Test
  void testGetLastChild() {
    var expr1 = new AstNode(new NodeType(), "expr1", null);
    var expr2 = new AstNode(new NodeType(), "expr2", null);
    var statement = new AstNode(new NodeType(), "statement", null);
    statement.addChild(expr1);
    statement.addChild(expr2);

    assertThat(statement.getLastChild()).isSameAs(expr2);
  }

  /**
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ D1
   *  |__ B3
   * </pre>
   */
  @Test
  void test_getDescendants() {
    var a = new NodeType();
    var b = new NodeType();
    var c = new NodeType();
    var d = new NodeType();
    var e = new NodeType();
    var a1 = new AstNode(a, "a1", null);
    var c1 = new AstNode(c, "c1", null);
    var b1 = new AstNode(b, "b1", null);
    var b2 = new AstNode(b, "b2", null);
    var d1 = new AstNode(d, "d1", null);
    var b3 = new AstNode(b, "b3", null);
    a1.addChild(c1);
    c1.addChild(b1);
    a1.addChild(b2);
    a1.addChild(d1);
    a1.addChild(b3);

    assertThat(a1.findChildren(b, c)).containsExactly(c1, b1, b2, b3);
    assertThat(a1.findChildren(b)).containsExactly(b1, b2, b3);
    assertThat(a1.findChildren(e)).isEmpty();
    assertThat(a1.findChildren(a)).as("SSLR-249").containsExactly(a1);

    assertThat(a1.getDescendants(b, c)).containsExactly(c1, b1, b2, b3);
    assertThat(a1.getDescendants(b)).containsExactly(b1, b2, b3);
    assertThat(a1.getDescendants(e)).isEmpty();
    assertThat(a1.getDescendants(a)).as("SSLR-249").isEmpty();
  }

  private class NodeType implements AstNodeSkippingPolicy {

    private boolean skippedFromAst = false;

    public NodeType() {

    }

    public NodeType(boolean skippedFromAst) {
      this.skippedFromAst = skippedFromAst;
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return skippedFromAst;
    }

  }

}
