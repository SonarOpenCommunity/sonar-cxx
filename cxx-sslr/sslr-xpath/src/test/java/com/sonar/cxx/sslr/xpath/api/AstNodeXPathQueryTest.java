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
package com.sonar.cxx.sslr.xpath.api;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AstNodeXPathQueryTest {

  @Test
  void selectSingleNodeTest() {
    var expr = AstNodeXPathQuery.create("branch/leaf");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);
    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectSingleNode(tree)).isEqualTo(leaf);
  }

  @Test
  void selectSingleNodeNoResultTest() {
    var expr = AstNodeXPathQuery.create("branch");
    var tree = new AstNode(new NodeType(), "tree", null);

    assertThat(expr.selectSingleNode(tree)).isNull();
  }

  @Test
  void selectNodesTest() {
    var expr = AstNodeXPathQuery.create("//leaf");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf1 = new AstNode(new NodeType(), "leaf", null);
    var leaf2 = new AstNode(new NodeType(), "leaf", null);
    tree.addChild(branch);
    branch.addChild(leaf1);
    branch.addChild(leaf2);

    assertThat(expr.selectNodes(tree)).hasSize(2);
  }

  @Test
  void selectNodesNoResultTest() {
    var expr = AstNodeXPathQuery.create("//branch");
    var tree = new AstNode(new NodeType(), "tree", null);

    assertThat(expr.selectNodes(tree)).isEmpty();
  }

  @Test
  void relativePathTest() {
    var expr = AstNodeXPathQuery.create("leaf");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectSingleNode(branch)).isEqualTo(leaf);
  }

  @Test
  void parentPathTest() {
    var expr = AstNodeXPathQuery.create("..");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectSingleNode(branch)).isEqualTo(tree);
  }

  @Test
  void parentAndDescendingPathTest() {
    var expr = AstNodeXPathQuery.create("../branch2");
    var tree = new AstNode(new NodeType(), "tree", null);

    var branch1 = new AstNode(new NodeType(), "branch1", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    var branch2 = new AstNode(new NodeType(), "branch2", null);

    tree.addChild(branch1);
    tree.addChild(branch2);

    branch1.addChild(leaf);

    assertThat(expr.selectSingleNode(branch1)).isEqualTo(branch2);
  }

  @Test
  void absolutePathTest() {
    var expr = AstNodeXPathQuery.create("/tree");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectSingleNode(tree)).isEqualTo(tree);
  }

  @Test
  void currentPathTest() {
    var expr = AstNodeXPathQuery.create(".");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectSingleNode(branch)).isEqualTo(branch);
  }

  @Test
  void currentPathWithDescendantTest() {
    var expr = AstNodeXPathQuery.create("./leaf");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectSingleNode(branch)).isEqualTo(leaf);
  }

  @Test
  void singleDocumentRoot() {
    var expr = AstNodeXPathQuery.create("//tree");
    var tree = new AstNode(new NodeType(), "tree", null);
    var branch = new AstNode(new NodeType(), "branch", null);
    var leaf = new AstNode(new NodeType(), "leaf", null);

    tree.addChild(branch);
    branch.addChild(leaf);

    assertThat(expr.selectNodes(tree)).hasSize(1);
  }

  @Test
  void relativeNamePredicate() {
    var expr = AstNodeXPathQuery.create(".[name() = \"tree\"]");
    var tree = new AstNode(new NodeType(), "tree", null);

    assertThat(expr.selectSingleNode(tree)).isEqualTo(tree);
  }

  @Test
  void relativeCountPredicate() {
    var expr = AstNodeXPathQuery.create(".[count(*) = 3]");
    var tree = new AstNode(new NodeType(), "tree", null);

    var branch1 = new AstNode(new NodeType(), "branch1", null);
    var branch2 = new AstNode(new NodeType(), "branch2", null);
    var branch3 = new AstNode(new NodeType(), "branch3", null);

    tree.addChild(branch1);
    tree.addChild(branch2);
    tree.addChild(branch3);

    assertThat(expr.selectSingleNode(tree)).isEqualTo(tree);
  }

  @Test
  void noCacheTest() {
    var expr = AstNodeXPathQuery.create("//branch");

    var tree1 = new AstNode(new NodeType(), "tree", null);
    var branch11 = new AstNode(new NodeType(), "branch", null);
    var branch12 = new AstNode(new NodeType(), "branch", null);
    var branch13 = new AstNode(new NodeType(), "branch", null);
    tree1.addChild(branch11);
    tree1.addChild(branch12);
    tree1.addChild(branch13);

    assertThat(expr.selectNodes(tree1)).hasSize(3);

    var tree2 = new AstNode(new NodeType(), "tree", null);
    var branch21 = new AstNode(new NodeType(), "branch", null);
    tree2.addChild(branch21);

    assertThat(expr.selectNodes(tree2)).hasSize(1);
  }

  static class NodeType implements AstNodeType {

  }

}
