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
import com.sonar.cxx.sslr.api.Token;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Extension methods for AstNode operations.
 *
 * <p>Since AstNode is part of the SSLR core library and cannot be modified,
 * this class provides static utility methods for common AST node operations.
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * import static org.sonar.cxx.squidbridge.api.AstNodeExtensions.*;
 *
 * AstNode node = ...;
 * Token first = firstToken(node);
 * Token last = lastToken(node);
 * AstNode parentNode = parent(node);
 * </pre>
 */
public final class AstNodeExtensions {

  private AstNodeExtensions() {
  }

  /**
   * Get the first token of an AST node.
   *
   * @param node the AST node
   * @return the first token of the node, or null if the node has no token
   */
  @CheckForNull
  public static Token firstToken(@Nullable AstNode node) {
    return node != null ? node.getToken() : null;
  }

  /**
   * Get the last token of an AST node.
   *
   * @param node the AST node
   * @return the last token of the node, or null if the node has no token
   */
  @CheckForNull
  public static Token lastToken(@Nullable AstNode node) {
    return node != null ? node.getLastToken() : null;
  }

  /**
   * Get the parent node of an AST node.
   *
   * @param node the AST node
   * @return the parent node, or null if this is the root node
   */
  @CheckForNull
  public static AstNode parent(@Nullable AstNode node) {
    return node != null ? node.getParent() : null;
  }

  /**
   * Get the symbol associated with an AST node.
   *
   * @param node the AST node
   * @return the symbol associated with the node, or null if none exists
   */
  @CheckForNull
  public static Symbol symbol(@Nullable AstNode node) {
    return AstNodeSymbolExtension.getSymbol(node);
  }

  /**
   * Get the type associated with an AST node (expression type).
   *
   * <p>Returns the type information associated with the node, either explicitly set
   * or derived from the node's symbol.
   *
   * @param node the AST node (typically an expression node)
   * @return the type of the expression, or null if no type information is available
   */
  @CheckForNull
  public static Type symbolType(@Nullable AstNode node) {
    return AstNodeTypeExtension.getType(node);
  }

  /**
   * Check if a node has a specific type.
   *
   * @param node the AST node to check
   * @param type the expected type
   * @return true if the node has the specified type
   */
  public static boolean is(@Nullable AstNode node, AstNodeType type) {
    return node != null && node.is(type);
  }

  /**
   * Check if a node has any of the specified types.
   *
   * @param node the AST node to check
   * @param types the expected types
   * @return true if the node has any of the specified types
   */
  public static boolean is(@Nullable AstNode node, AstNodeType... types) {
    return node != null && node.is(types);
  }
}
