/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import static org.assertj.core.api.Assertions.*;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Token;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AstNodeTraversalTest {

  private static final AstNodeType TYPE_A = new TestNodeType("TYPE_A");
  private static final AstNodeType TYPE_B = new TestNodeType("TYPE_B");
  private static final AstNodeType TYPE_C = new TestNodeType("TYPE_C");

  @Test
  void testTraverseAllNodes() {
    var root = createNode(TYPE_A, "root");
    var child1 = createNode(TYPE_B, "child1");
    var child2 = createNode(TYPE_C, "child2");
    root.addChild(child1);
    root.addChild(child2);

    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(root, node -> visited.add(node.getTokenValue()));

    assertThat(visited).containsExactly("root", "child1", "child2");
  }

  @Test
  void testTraverseNullRoot() {
    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(null, node -> visited.add(node.getTokenValue()));
    assertThat(visited).isEmpty();
  }

  @Test
  void testTraverseNullVisitor() {
    var root = createNode(TYPE_A, "root");
    // Should not throw
    AstNodeTraversal.traverse(root, (java.util.function.Consumer<AstNode>) null);
  }

  @Test
  void testTraverseDeepTree() {
    var root = createNode(TYPE_A, "root");
    var level1 = createNode(TYPE_B, "level1");
    var level2 = createNode(TYPE_C, "level2");
    var level3 = createNode(TYPE_A, "level3");
    root.addChild(level1);
    level1.addChild(level2);
    level2.addChild(level3);

    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(root, node -> visited.add(node.getTokenValue()));

    assertThat(visited).containsExactly("root", "level1", "level2", "level3");
  }

  @Test
  void testTraverseFilteredByType() {
    var root = createNode(TYPE_A, "root");
    var child1 = createNode(TYPE_B, "child1");
    var child2 = createNode(TYPE_C, "child2");
    var grandchild = createNode(TYPE_B, "grandchild");
    root.addChild(child1);
    root.addChild(child2);
    child2.addChild(grandchild);

    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(root, new AstNodeType[]{TYPE_B},
      node -> visited.add(node.getTokenValue()));

    assertThat(visited).containsExactly("child1", "grandchild");
  }

  @Test
  void testTraverseFilteredMultipleTypes() {
    var root = createNode(TYPE_A, "root");
    var child1 = createNode(TYPE_B, "child1");
    var child2 = createNode(TYPE_C, "child2");
    root.addChild(child1);
    root.addChild(child2);

    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(root, new AstNodeType[]{TYPE_A, TYPE_C},
      node -> visited.add(node.getTokenValue()));

    assertThat(visited).containsExactly("root", "child2");
  }

  @Test
  void testTraverseFilteredNullTypes() {
    var root = createNode(TYPE_A, "root");
    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(root, (AstNodeType[]) null,
      node -> visited.add(node.getTokenValue()));
    assertThat(visited).isEmpty();
  }

  @Test
  void testTraverseFilteredEmptyTypes() {
    var root = createNode(TYPE_A, "root");
    List<String> visited = new ArrayList<>();
    AstNodeTraversal.traverse(root, new AstNodeType[]{},
      node -> visited.add(node.getTokenValue()));
    assertThat(visited).isEmpty();
  }

  @Test
  void testTraverseWithEnterAndLeave() {
    var root = createNode(TYPE_A, "root");
    var child1 = createNode(TYPE_B, "child1");
    var child2 = createNode(TYPE_C, "child2");
    root.addChild(child1);
    root.addChild(child2);

    List<String> events = new ArrayList<>();
    AstNodeTraversal.traverse(root,
      node -> events.add("enter:" + node.getTokenValue()),
      node -> events.add("leave:" + node.getTokenValue()));

    assertThat(events).containsExactly(
      "enter:root", "enter:child1", "leave:child1",
      "enter:child2", "leave:child2", "leave:root");
  }

  @Test
  void testTraverseWithEnterAndLeaveNullCallbacks() {
    var root = createNode(TYPE_A, "root");
    // Should not throw - use explicit cast to resolve ambiguity
    java.util.function.Consumer<AstNode> nullConsumer = null;
    AstNodeTraversal.traverse(root, nullConsumer, null);
  }

  @Test
  void testTraverseFilteredWithEnterAndLeave() {
    var root = createNode(TYPE_A, "root");
    var child1 = createNode(TYPE_B, "child1");
    var child2 = createNode(TYPE_A, "child2");
    root.addChild(child1);
    root.addChild(child2);

    List<String> events = new ArrayList<>();
    AstNodeTraversal.traverse(root, new AstNodeType[]{TYPE_A},
      node -> events.add("enter:" + node.getTokenValue()),
      node -> events.add("leave:" + node.getTokenValue()));

    // Only TYPE_A nodes get callbacks, but traversal still visits all children
    assertThat(events).containsExactly(
      "enter:root", "enter:child2", "leave:child2", "leave:root");
  }

  @Test
  void testCollectNodes() {
    var root = createNode(TYPE_A, "root");
    var child1 = createNode(TYPE_B, "child1");
    var child2 = createNode(TYPE_C, "child2");
    var grandchild = createNode(TYPE_B, "grandchild");
    root.addChild(child1);
    root.addChild(child2);
    child2.addChild(grandchild);

    List<AstNode> nodes = AstNodeTraversal.collectNodes(root, TYPE_B);
    assertThat(nodes).hasSize(2);
    assertThat(nodes.get(0).getTokenValue()).isEqualTo("child1");
    assertThat(nodes.get(1).getTokenValue()).isEqualTo("grandchild");
  }

  @Test
  void testCollectNodesEmpty() {
    var root = createNode(TYPE_A, "root");
    List<AstNode> nodes = AstNodeTraversal.collectNodes(root, TYPE_B);
    assertThat(nodes).isEmpty();
  }

  @Test
  void testCollectNodesNull() {
    assertThat(AstNodeTraversal.collectNodes(null, TYPE_A)).isEmpty();
  }

  @Test
  void testContainsNodeOfType() {
    var root = createNode(TYPE_A, "root");
    var child = createNode(TYPE_B, "child");
    var grandchild = createNode(TYPE_C, "grandchild");
    root.addChild(child);
    child.addChild(grandchild);

    assertThat(AstNodeTraversal.containsNodeOfType(root, TYPE_A)).isTrue();
    assertThat(AstNodeTraversal.containsNodeOfType(root, TYPE_B)).isTrue();
    assertThat(AstNodeTraversal.containsNodeOfType(root, TYPE_C)).isTrue();
  }

  @Test
  void testContainsNodeOfTypeNotFound() {
    var root = createNode(TYPE_A, "root");
    var child = createNode(TYPE_B, "child");
    root.addChild(child);

    assertThat(AstNodeTraversal.containsNodeOfType(root, TYPE_C)).isFalse();
  }

  @Test
  void testContainsNodeOfTypeNull() {
    assertThat(AstNodeTraversal.containsNodeOfType(null, TYPE_A)).isFalse();
  }

  @Test
  void testContainsNodeOfTypeNullTypes() {
    var root = createNode(TYPE_A, "root");
    assertThat(AstNodeTraversal.containsNodeOfType(root, (AstNodeType[]) null)).isFalse();
  }

  @Test
  void testContainsNodeOfTypeEmptyTypes() {
    var root = createNode(TYPE_A, "root");
    assertThat(AstNodeTraversal.containsNodeOfType(root)).isFalse();
  }

  @Test
  void testBroadTree() {
    var root = createNode(TYPE_A, "root");
    for (int i = 0; i < 5; i++) {
      var child = createNode(TYPE_B, "child" + i);
      root.addChild(child);
      for (int j = 0; j < 3; j++) {
        child.addChild(createNode(TYPE_C, "grandchild" + i + "_" + j));
      }
    }

    List<AstNode> typeC = AstNodeTraversal.collectNodes(root, TYPE_C);
    assertThat(typeC).hasSize(15);

    List<AstNode> typeB = AstNodeTraversal.collectNodes(root, TYPE_B);
    assertThat(typeB).hasSize(5);
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

  private static class TestNodeType implements AstNodeType {
    private final String name;

    TestNodeType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
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
