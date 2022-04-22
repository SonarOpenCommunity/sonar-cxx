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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AstNodeTest {

  /**
   * <pre>
   *   A1
   *   |- A2
   *   |   \- B1
   *   |- B2
   *   |   \- C1
   *   |- B3
   *   \- C2
   * </pre>
   */
  @Test
  void test() {
    var a = mock(AstNodeType.class);
    var b = mock(AstNodeType.class);
    var c = mock(AstNodeType.class);
    var a1 = new AstNode(a, "a1", null);
    var a2 = new AstNode(a, "a2", null);
    var b1 = new AstNode(b, "b1", null);
    var b2 = new AstNode(b, "b2", null);
    var b3 = new AstNode(b, "b3", null);
    var c1 = new AstNode(c, "c1", null);
    var c2 = new AstNode(c, "c2", null);
    a1.addChild(a2);
    a2.addChild(b1);
    a1.addChild(b2);
    b2.addChild(c1);
    a1.addChild(b3);
    a1.addChild(c2);

    assertThat(a1.hasChildren()).isTrue();
    assertThat(c1.hasChildren()).isFalse();

    assertThat(a1.getFirstChild()).isSameAs(a2);

    assertThat(a1.getLastChild()).isSameAs(c2);

    assertThat(a1.hasDirectChildren(mock(AstNodeType.class))).isFalse();
    assertThat(a1.hasDirectChildren(a)).isTrue();
    assertThat(a1.hasDirectChildren(a, b)).isTrue();

    assertThat(a1.getFirstChild(mock(AstNodeType.class))).isNull();
    assertThat(a1.getFirstChild(a)).isSameAs(a2);
    assertThat(a1.getFirstChild(b)).isSameAs(b2);
    assertThat(a1.getFirstChild(a, b)).isSameAs(a2);

    assertThat(a1.getLastChild(mock(AstNodeType.class))).isNull();
    assertThat(a1.getLastChild(a)).isSameAs(a2);
    assertThat(a1.getLastChild(b)).isSameAs(b3);
    assertThat(a1.getLastChild(a, b)).isSameAs(b3);

    assertThat(a1.getChildren(mock(AstNodeType.class))).isEmpty();
    assertThat(a1.getChildren(a)).containsExactly(a2);
    assertThat(a1.getChildren(b)).containsExactly(b2, b3);
    assertThat(a1.getChildren(a, b)).containsExactly(a2, b2, b3);

    assertThat(a1.hasDescendant(mock(AstNodeType.class))).isFalse();
    assertThat(a1.hasDescendant(a)).isTrue();
    assertThat(a1.hasDescendant(a, b)).isTrue();

    assertThat(a1.getFirstDescendant(mock(AstNodeType.class))).isNull();
    assertThat(a1.getFirstDescendant(a)).isSameAs(a2);
    assertThat(a1.getFirstDescendant(b)).isSameAs(b1);
    assertThat(a1.getFirstDescendant(a, b)).isSameAs(a2);

    assertThat(a1.getDescendants(mock(AstNodeType.class))).isEmpty();
    assertThat(a1.getDescendants(a)).containsExactly(a2);
    assertThat(a1.getDescendants(b)).containsExactly(b1, b2, b3);
    assertThat(a1.getDescendants(a, b)).containsExactly(a2, b1, b2, b3);

    assertThat(a1.getNextSibling()).isNull();
    assertThat(c1.getNextSibling()).isNull();
    assertThat(b3.getNextSibling()).isSameAs(c2);

    assertThat(a1.getPreviousSibling()).isNull();
    assertThat(a2.getPreviousSibling()).isNull();
    assertThat(b2.getPreviousSibling()).isSameAs(a2);

    assertThat(a1.getNextAstNode()).isNull();
    assertThat(b1.getNextAstNode()).isSameAs(b2);

    assertThat(a1.getPreviousAstNode()).isNull();
    assertThat(b2.getPreviousAstNode()).isSameAs(a2);

    assertThat(c1.hasAncestor(mock(AstNodeType.class))).isFalse();
    assertThat(c1.hasAncestor(a)).isTrue();
    assertThat(c1.hasAncestor(c)).isFalse();
    assertThat(c1.hasAncestor(a, c)).isTrue();

    assertThat(c1.getFirstAncestor(a)).isSameAs(a1);
    assertThat(c1.getFirstAncestor(c)).isNull();
    assertThat(c1.getFirstAncestor(a, c)).isSameAs(a1);
    assertThat(c1.getFirstAncestor(c, c)).isNull();

    assertThat(a1.hasParent()).isFalse();
    assertThat(a2.hasParent(a)).isTrue();
    assertThat(a2.hasParent(b)).isFalse();
    assertThat(a2.hasParent(a, b)).isTrue();
  }

  /**
   * <pre>
   *   root
   *   |- empty
   *   |- nonempty
   *   \- intermediate empty
   *      \- empty
   * </pre>
   */
  @Test
  void test_getLastToken() {
    var token = mock(Token.class);
    var a = mock(AstNodeType.class);
    var rootNode = new AstNode(a, "root", token);
    var firstEmptyNode = new AstNode(a, "empty", null);
    var nonemptyNode = new AstNode(a, "nonempty", token);
    var intermediateEmptyNode = new AstNode(a, "intermediate empty", null);
    var lastEmptyNode = new AstNode(a, "empty", null);
    rootNode.addChild(firstEmptyNode);
    rootNode.addChild(nonemptyNode);
    rootNode.addChild(intermediateEmptyNode);
    intermediateEmptyNode.addChild(lastEmptyNode);

    assertThat(rootNode.getLastToken()).isSameAs(token);
    assertThat(firstEmptyNode.getLastToken()).isNull();
    assertThat(intermediateEmptyNode.getLastToken()).isNull();
    assertThat(lastEmptyNode.getLastToken()).isNull();
  }

  @Test
  void test_getTokens() {
    var token = mock(Token.class);
    var a = mock(AstNodeType.class);
    var rootNode = new AstNode(a, "root", token);
    var firstEmptyNode = new AstNode(a, "empty", null);
    var nonemptyNode = new AstNode(a, "nonempty", token);
    var lastEmptyNode = new AstNode(a, "empty", null);
    rootNode.addChild(firstEmptyNode);
    rootNode.addChild(nonemptyNode);
    rootNode.addChild(lastEmptyNode);

    assertThat(rootNode.getTokens()).containsExactly(token);
    assertThat(firstEmptyNode.getTokens()).isEmpty();
    assertThat(nonemptyNode.getTokens()).containsExactly(token);
    assertThat(lastEmptyNode.getTokens()).isEmpty();
  }

  @Test
  void test_toString() {
    var token = mock(Token.class);
    when(token.getValue()).thenReturn("foo");
    when(token.getLine()).thenReturn(42);
    when(token.getColumn()).thenReturn(24);
    var node = new AstNode(mock(AstNodeType.class), "node_name", token);
    assertThat(node).hasToString("node_name tokenValue='foo' tokenLine=42 tokenColumn=24");

    node = new AstNode(mock(AstNodeType.class), "node_name", null);
    assertThat(node).hasToString("node_name");
  }

}
