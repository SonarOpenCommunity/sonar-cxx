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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * Utility for recursive subtree traversal of AstNode trees.
 *
 * <p>This class provides an {@code accept(Visitor)} pattern for AstNode.
 * It enables applying a visitor callback to all nodes in a subtree that match
 * specified node types.
 *
 * <p>This is essential for implementing the {@code IBaseMethodVisitor} pattern,
 * where detection rules need to traverse function bodies and process specific
 * node types within them.
 *
 * <p>Usage example:
 * <pre>
 * // Traverse a function body, visiting all function call expressions
 * AstNode functionBody = CxxAstNodeHelper.getFunctionDefinitionBody(funcDef);
 * AstNodeTraversal.traverse(functionBody, node -&gt; {
 *     if (CxxAstNodeHelper.isFunctionCall(node)) {
 *         // Process function call
 *     }
 * });
 *
 * // Or with type filtering:
 * AstNodeTraversal.traverse(functionBody,
 *     new AstNodeType[]{CxxGrammarImpl.postfixExpression, CxxGrammarImpl.newExpression},
 *     node -&gt; {
 *         // Only called for postfixExpression and newExpression nodes
 *     });
 * </pre>
 */
public final class AstNodeTraversal {

  private AstNodeTraversal() {
  }

  /**
   * Traverse a subtree, invoking a callback for every node.
   *
   * <p>Performs a depth-first traversal of the subtree rooted at the given node.
   * The callback is invoked for every node, including the root.
   *
   * @param root the root of the subtree to traverse
   * @param visitor the callback to invoke for each node
   */
  public static void traverse(@Nullable AstNode root, Consumer<AstNode> visitor) {
    if (root == null || visitor == null) {
      return;
    }
    visitor.accept(root);
    for (AstNode child : root.getChildren()) {
      traverse(child, visitor);
    }
  }

  /**
   * Traverse a subtree, invoking a callback only for nodes matching the specified types.
   *
   * <p>Performs a depth-first traversal of the subtree rooted at the given node.
   * The callback is only invoked for nodes whose type matches one of the specified
   * node types.
   *
   * @param root the root of the subtree to traverse
   * @param types the node types to match (callback is only invoked for matching nodes)
   * @param visitor the callback to invoke for matching nodes
   */
  public static void traverse(@Nullable AstNode root, AstNodeType[] types, Consumer<AstNode> visitor) {
    if (root == null || visitor == null || types == null || types.length == 0) {
      return;
    }
    Set<AstNodeType> typeSet = new HashSet<>(Arrays.asList(types));
    traverseFiltered(root, typeSet, visitor);
  }

  /**
   * Traverse a subtree with enter/leave callbacks for every node.
   *
   * <p>Performs a depth-first traversal of the subtree rooted at the given node.
   * The enterVisitor is called when entering a node (before children),
   * and the leaveVisitor is called when leaving a node (after children).
   *
   * @param root the root of the subtree to traverse
   * @param enterVisitor callback invoked when entering a node (before processing children)
   * @param leaveVisitor callback invoked when leaving a node (after processing children)
   */
  public static void traverse(@Nullable AstNode root,
                               Consumer<AstNode> enterVisitor,
                               Consumer<AstNode> leaveVisitor) {
    if (root == null) {
      return;
    }
    if (enterVisitor != null) {
      enterVisitor.accept(root);
    }
    for (AstNode child : root.getChildren()) {
      traverse(child, enterVisitor, leaveVisitor);
    }
    if (leaveVisitor != null) {
      leaveVisitor.accept(root);
    }
  }

  /**
   * Traverse a subtree with enter/leave callbacks, only for nodes matching specified types.
   *
   * @param root the root of the subtree to traverse
   * @param types the node types to match
   * @param enterVisitor callback invoked when entering a matching node
   * @param leaveVisitor callback invoked when leaving a matching node
   */
  public static void traverse(@Nullable AstNode root, AstNodeType[] types,
                               Consumer<AstNode> enterVisitor,
                               Consumer<AstNode> leaveVisitor) {
    if (root == null || types == null || types.length == 0) {
      return;
    }
    Set<AstNodeType> typeSet = new HashSet<>(Arrays.asList(types));
    traverseFilteredWithLeave(root, typeSet, enterVisitor, leaveVisitor);
  }

  /**
   * Collect all nodes of specified types from a subtree.
   *
   * <p>Performs a depth-first traversal and collects all nodes matching the
   * specified types into a list.
   *
   * @param root the root of the subtree to search
   * @param types the node types to collect
   * @return list of matching nodes in depth-first order
   */
  public static List<AstNode> collectNodes(@Nullable AstNode root, AstNodeType... types) {
    List<AstNode> result = new ArrayList<>();
    if (root == null || types == null || types.length == 0) {
      return result;
    }
    traverse(root, types, result::add);
    return result;
  }

  /**
   * Check if a subtree contains any node of the specified types.
   *
   * @param root the root of the subtree to search
   * @param types the node types to check for
   * @return true if any descendant (including root) matches the specified types
   */
  public static boolean containsNodeOfType(@Nullable AstNode root, AstNodeType... types) {
    if (root == null || types == null || types.length == 0) {
      return false;
    }
    if (root.is(types)) {
      return true;
    }
    for (AstNode child : root.getChildren()) {
      if (containsNodeOfType(child, types)) {
        return true;
      }
    }
    return false;
  }

  private static void traverseFiltered(AstNode node, Set<AstNodeType> typeSet,
                                        Consumer<AstNode> visitor) {
    if (typeSet.contains(node.getType())) {
      visitor.accept(node);
    }
    for (AstNode child : node.getChildren()) {
      traverseFiltered(child, typeSet, visitor);
    }
  }

  private static void traverseFilteredWithLeave(AstNode node, Set<AstNodeType> typeSet,
                                                  Consumer<AstNode> enterVisitor,
                                                  Consumer<AstNode> leaveVisitor) {
    boolean matches = typeSet.contains(node.getType());
    if (matches && enterVisitor != null) {
      enterVisitor.accept(node);
    }
    for (AstNode child : node.getChildren()) {
      traverseFilteredWithLeave(child, typeSet, enterVisitor, leaveVisitor);
    }
    if (matches && leaveVisitor != null) {
      leaveVisitor.accept(node);
    }
  }
}
