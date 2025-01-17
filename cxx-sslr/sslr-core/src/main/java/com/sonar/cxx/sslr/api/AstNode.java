/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package com.sonar.cxx.sslr.api; // cxx: in use

import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;

/**
 * the parser is in charge to construct an abstract syntax tree (AST) which is a tree representation of the abstract
 * syntactic structure of source code. Each node of the tree is an AstNode and each node denotes a construct occurring
 * in the source code which starts at a given Token.
 *
 * @see Token
 */
public class AstNode {

  protected AstNodeType type;
  private final String name;
  private final Token token;
  private List<AstNode> children = Collections.emptyList();
  private int childIndex = -1;
  private AstNode parent;
  private int fromIndex;
  private int toIndex;

  /**
   * Node of abstract syntax tree (AST).
   *
   * @param token first token associated to this node
   */
  public AstNode(Token token) {
    this(token.getType(), token.getType().getName(), token);
  }

  /**
   * Node of abstract syntax tree (AST).
   *
   * @param type type of the node
   * @param name name of the node
   * @param token first token associated to this node
   */
  public AstNode(AstNodeType type, String name, @Nullable Token token) {
    this.type = type;
    this.token = token;
    this.name = name;
  }

  /**
   * Get the parent of this node in the tree.
   *
   * @return parent node
   */
  public AstNode getParent() {
    return parent;
  }

  /**
   * Add a child to this node.
   *
   * @param child AstNode to add
   */
  public void addChild(@Nullable AstNode child) {
    if (child != null) {
      if (children.isEmpty()) {
        children = new ArrayList<>();
      }
      if (child.hasToBeSkippedFromAst()) {
        if (child.hasChildren()) {
          for (var subChild : child.children) {
            addChildToList(subChild);
          }
        }
      } else {
        addChildToList(child);
      }
    }
  }

  private void addChildToList(AstNode child) {
    children.add(child);
    child.childIndex = children.size() - 1;
    child.parent = this;
  }

  /**
   * Check if this node has children.
   *
   * @return true if this AstNode has at least one child.
   */
  public boolean hasChildren() {
    return !children.isEmpty();
  }

  /**
   * Get the list of children for this node.
   *
   * @return list of children
   */
  public List<AstNode> getChildren() {
    return children;
  }

  /**
   * Get the number of children for this node.
   *
   * @return number of children
   */
  public int getNumberOfChildren() {
    return children.size();
  }

  /**
   * Get the next sibling node in the tree and if this node doesn't exist try to get the next AST Node of the parent.
   *
   * @return matching node (@CheckForNull -> normally already ensured via grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getNextAstNode() {
    var nextSibling = getNextSibling();
    if (nextSibling != null) {
      return nextSibling;
    }
    if (parent != null) {
      return parent.getNextAstNode();
    }
    return null;
  }

  /**
   * Get the previous sibling node in the tree and if this node doesn't exist try to get the next AST Node of the
   * parent.
   *
   * @return matching node (@CheckForNull -> normally already ensured via grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getPreviousAstNode() {
    var previousSibling = getPreviousSibling();
    if (previousSibling != null) {
      return previousSibling;
    }
    if (parent != null) {
      return parent.getPreviousAstNode();
    }
    return null;
  }

  /**
   * Get the next sibling node if exists in the tree.
   *
   * @return next sibling, or null if not exists (@CheckForNull -> normally already ensured via grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getNextSibling() {
    if (parent == null) {
      return null;
    }
    if (parent.getNumberOfChildren() > childIndex + 1) {
      return parent.children.get(childIndex + 1);
    }
    return null;
  }

  /**
   * Get the previous sibling node if exists in the tree.
   *
   * @return previous sibling, or null if not exists (@CheckForNull -> normally already ensured via grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getPreviousSibling() {
    if (parent == null) {
      return null;
    }
    if (childIndex > 0) {
      return parent.children.get(childIndex - 1);
    }
    return null;
  }

  /**
   * Get the Token's value associated to this node
   *
   * @return token's value
   */
  public String getTokenValue() {
    return token != null ? token.getValue() : "";
  }

  /**
   * Get the Token's original value associated to this node
   *
   * @return token's original value
   */
  public String getTokenOriginalValue() {
    return token != null ? token.getOriginalValue() : "";
  }

  /**
   * Get the Token associated to this node
   */
  public Token getToken() {
    return token;
  }

  /**
   * Get the Token's line associated to this node
   *
   * @return token's line
   */
  public int getTokenLine() {
    return token.getLine();
  }

  /**
   * Check if this node has a token.
   *
   * @return true if node has a token
   */
  public boolean hasToken() {
    return token != null;
  }

  /**
   * Returns name of this node.
   *
   * @return name of the node
   */
  public String getName() {
    return name;
  }

  /**
   * First index in the line the node belongs to.
   *
   * @return first index in the line (starting with 0)
   */
  public int getFromIndex() {
    return fromIndex;
  }

  /**
   * Set first index in the line the node belongs to.
   *
   * @param fromIndex first index in the line (starting with 0)
   */
  public void setFromIndex(int fromIndex) {
    this.fromIndex = fromIndex;
  }

  /**
   * Last index in the line the node belongs to.
   *
   * @return last index in the line (starting with 0)
   */
  public int getToIndex() {
    return toIndex;
  }

  /**
   * For internal use only.
   *
   * @return true if node has to be skipped from AST
   */
  public boolean hasToBeSkippedFromAst() {
    if (type == null) {
      return true;
    }
    boolean result;
    if (AstNodeSkippingPolicy.class.isAssignableFrom(type.getClass())) {
      result = ((AstNodeSkippingPolicy) type).hasToBeSkippedFromAst(this);
    } else {
      result = false;
    }
    // For LexerlessGrammarBuilder and LexerfulGrammarBuilder
    // unwrap AstNodeType to get a real one, i.e. detach node from tree of matchers:
    if (type instanceof MutableParsingRule mutableParsingRule) {
      type = mutableParsingRule.getRealAstNodeType();
    } else if (type instanceof RuleDefinition ruleDefinition) {
      type = ruleDefinition.getRealAstNodeType();
    }
    return result;
  }

  /**
   * Set last index in the line the node belongs to.
   *
   * @param toIndex last index in the line (starting with 0)
   */
  public void setToIndex(int toIndex) {
    this.toIndex = toIndex;
  }

  /**
   * Check wether this node is one of the types in the list.
   *
   * @param types to be checked
   * @return true if node is one of the types in the list.
   */
  public boolean is(AstNodeType... types) {
    for (var expectedType : types) {
      if (this.type == expectedType) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check wether this node is not one of the types in the list.
   *
   * @param types to be checked
   * @return true if node is not one of the types in the list.
   */
  public boolean isNot(AstNodeType... types) {
    return !is(types);
  }

  /**
   * Returns first child of one of specified types.
   * <p>
   * In the following case, {@code getFirstChild("B")} would return "B2":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ B3
   * </pre>
   *
   * @param nodeTypes to be checked
   * @return first child of one of specified types, or null if not found (@CheckForNull -> normally already ensured via
   * grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getFirstChild(AstNodeType... nodeTypes) {
    for (var child : children) {
      for (var nodeType : nodeTypes) {
        if (child.type == nodeType) {
          return child;
        }
      }
    }
    return null;
  }

  /**
   * Returns first descendant of one of specified types.
   * <p>
   * In the following case, {@code getFirstDescendant("B")} would return "B1":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ B3
   * </pre>
   *
   * @param nodeTypes to be checked
   * @return first descendant of one of specified types, or null if not found (@CheckForNull -> normally already ensured
   * via grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getFirstDescendant(AstNodeType... nodeTypes) {
    for (var child : children) {
      if (child.is(nodeTypes)) {
        return child;
      }
      var node = child.getFirstDescendant(nodeTypes);
      if (node != null) {
        return node;
      }
    }
    return null;
  }

  /**
   * Returns the first child of this node.
   *
   * @return the first child, or null if there is no child
   */
  public AstNode getFirstChild() {
    return children.isEmpty() ? null : children.get(0);
  }

  /**
   * Returns children of specified types. In the following case, {@code getChildren("B")} would return "B2" and "B3":
   * <p>
   * <
   * pre>
   * A1 |__ C1 | |__ B1 |__ B2 |__ B3
   * </pre>
   *
   * @param nodeTypes to be included
   * @return children of specified types, never null
   * @since 1.17
   */
  public List<AstNode> getChildren(AstNodeType... nodeTypes) {
    List<AstNode> result = new ArrayList<>();
    for (var child : children) {
      for (var nodeType : nodeTypes) {
        if (child.type == nodeType) {
          result.add(child);
        }
      }
    }
    return result;
  }

  /**
   * Returns descendants of specified types. Be careful, this method searches among all descendants whatever is their
   * depth, so favor {@link #getChildren(AstNodeType...)} when possible.
   * <p>
   * In the following case, {@code getDescendants("B", "C")} would return "C1", "B1", "B2" and "B3":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ D1
   *  |__ B3
   * </pre>
   *
   * @param nodeTypes to be included
   * @return descendants of specified types, never null
   *
   * @since 1.17
   */
  public List<AstNode> getDescendants(AstNodeType... nodeTypes) {
    List<AstNode> result = new ArrayList<>();
    if (hasChildren()) {
      for (var child : children) {
        child.getDescendants(result, nodeTypes);
      }
    }
    return result;
  }

  private void getDescendants(List<AstNode> result, AstNodeType... nodeTypes) {
    for (var nodeType : nodeTypes) {
      if (is(nodeType)) {
        result.add(this);
      }
    }
    if (hasChildren()) {
      for (var child : children) {
        child.getDescendants(result, nodeTypes);
      }
    }
  }

  /**
   * Returns the last child of this node.
   *
   * @return the last child, or null if there is no child
   */
  public AstNode getLastChild() {
    return children.isEmpty() ? null : children.get(children.size() - 1);
  }

  /**
   * Returns last child of one of specified types.
   * <p>
   * In the following case, {@code getLastChild("B")} would return "B3":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ B3
   *       |__ B4
   * </pre>
   *
   * @param nodeTypes to be checked
   * @return last child of one of specified types, or null if not found
   *
   * @since 1.20
   */
  @CheckForNull // -> normally already ensured via grammar
  public AstNode getLastChild(AstNodeType... nodeTypes) {
    for (int i = children.size() - 1; i >= 0; i--) {
      var child = children.get(i);
      for (var nodeType : nodeTypes) {
        if (child.type == nodeType) {
          return child;
        }
      }
    }
    return null;
  }

  /**
   * Check if this node has some children with the requested node types.
   *
   * @param nodeTypes to be checked
   * @return true if this node has some children with the requested node types
   */
  public boolean hasDirectChildren(AstNodeType... nodeTypes) {
    return getFirstChild(nodeTypes) != null;
  }

  /**
   * Check if this node has a descendant of one of specified types.
   *
   * @param nodeTypes to be checked
   * @return true if this node has a descendant of one of specified types
   *
   * @since 1.17
   */
  public boolean hasDescendant(AstNodeType... nodeTypes) {
    return getFirstDescendant(nodeTypes) != null;
  }

  /**
   * Check if this node has a parent of one of specified types
   *
   * @param nodeTypes to be checked
   * @return true if this node has a parent of one of specified types
   *
   * @since 1.19.2
   */
  public boolean hasParent(AstNodeType... nodeTypes) {
    return parent != null && parent.is(nodeTypes);
  }

  /**
   * Check if this node has an ancestor of the specified type.
   *
   * @param nodeType to be checked
   * @return true if this node has an ancestor of the specified type
   *
   * @since 1.17
   */
  public boolean hasAncestor(AstNodeType nodeType) {
    return getFirstAncestor(nodeType) != null;
  }

  /**
   * Check if this node has an ancestor of one of specified types.
   *
   * @param nodeTypes to be checked
   * @return true if this node has an ancestor of one of specified types
   *
   * @since 1.19.2
   */
  public boolean hasAncestor(AstNodeType... nodeTypes) {
    return getFirstAncestor(nodeTypes) != null;
  }

  /**
   * Search first ancestor of the specified type.
   *
   * @param nodeType to be checked
   * @return first ancestor of the specified type, or null if not found (@CheckForNull -> normally already ensured via
   * grammar)
   *
   * @since 1.17
   */
  @CheckForNull
  public AstNode getFirstAncestor(AstNodeType nodeType) {
    if (parent == null) {
      return null;
    } else if (parent.is(nodeType)) {
      return parent;
    } else {
      return parent.getFirstAncestor(nodeType);
    }
  }

  /**
   * Search first ancestor of the specified types.
   *
   * @param nodeTypes to be checked
   * @return first ancestor of one of specified types, or null if not found (@CheckForNull -> normally already ensured
   * via grammar)
   *
   * @since 1.19.2
   */
  @CheckForNull
  public AstNode getFirstAncestor(AstNodeType... nodeTypes) {
    var result = parent;
    while (result != null) {
      if (result.is(nodeTypes)) {
        return result;
      }
      result = result.parent;
    }
    return null;
  }

  /**
   * Check if this node is a copy or generated code.
   *
   * @return true if this node is a copy or generated code
   */
  public boolean isCopyBookOrGeneratedNode() {
    return getToken().isCopyBook() || getToken().isGeneratedCode();
  }

  /**
   * Get type of this node.
   *
   * @return type of this node
   */
  public AstNodeType getType() {
    return type;
  }

  /**
   * Return all tokens contained in this tree node. Those tokens can be directly or indirectly attached to this node.
   *
   * @return node list
   */
  public List<Token> getTokens() {
    List<Token> tokens = new ArrayList<>();
    getTokens(tokens);
    return tokens;
  }

  private void getTokens(List<Token> tokens) {
    if (!hasChildren()) {
      if (token != null) {
        tokens.add(token);
      }
    } else {
      for (int i = 0; i < children.size(); i++) {
        children.get(i).getTokens(tokens);
      }
    }
  }

  /**
   * String representation of this node.
   *
   * @return this node as string
   */
  @Override
  public String toString() {
    var result = new StringBuilder();
    result.append(name);
    if (token != null) {
      result.append(" tokenValue='").append(token.getValue()).append("'");
      result.append(" tokenLine=").append(token.getLine());
      result.append(" tokenColumn=").append(token.getColumn());
    }
    return result.toString();
  }

  /**
   * Get the last token of this node.
   *
   * @return last token of this node (@CheckForNull -> normally already ensured via grammar)
   */
  @CheckForNull
  public Token getLastToken() {
    if (!this.hasToken()) {
      return null;
    }
    var currentNode = this;
    while (currentNode.hasChildren()) {
      for (int i = currentNode.children.size() - 1; i >= 0; i--) {
        var child = currentNode.children.get(i);
        if (child.hasToken()) {
          currentNode = child;
          break;
        }
      }
    }
    return currentNode.getToken();
  }

}
