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
import java.util.WeakHashMap;
import javax.annotation.CheckForNull;

/**
 * Extension to associate Type information with AstNodes.
 *
 * <p>Since AstNode is part of the SSLR core library (which cannot be modified),
 * this class provides a way to attach type information to AstNode instances
 * without modifying the AstNode class itself.
 *
 * <p>The type mapping is stored using WeakHashMap to prevent memory leaks
 * when AstNodes are garbage collected.
 *
 * <p>Usage example:
 * <pre>
 * // During semantic analysis, associate types with expression nodes
 * AstNodeTypeExtension.setType(expressionNode, Type.Types.of("std::string"));
 *
 * // During rule checking, retrieve type information
 * Type type = AstNodeTypeExtension.getType(expressionNode);
 * if (type != null &amp;&amp; type.is("std::string")) {
 *     // handle std::string expression
 * }
 * </pre>
 */
public final class AstNodeTypeExtension {

  private static final WeakHashMap<AstNode, Type> TYPE_MAP = new WeakHashMap<>();

  private AstNodeTypeExtension() {
  }

  /**
   * Associates a Type with an AstNode.
   *
   * @param node the AST node (typically an expression node)
   * @param type the type to associate with the node
   */
  public static void setType(AstNode node, Type type) {
    if (node != null && type != null) {
      TYPE_MAP.put(node, type);
    }
  }

  /**
   * Retrieves the Type associated with an AstNode.
   *
   * <p>If no type has been explicitly set for the node, this method attempts
   * to derive the type from the node's associated symbol (if any). This
   * provides a fallback for nodes that have symbol information but haven't
   * had their type explicitly set during semantic analysis.
   *
   * @param node the AST node
   * @return the associated type, or null if no type information is available
   */
  @CheckForNull
  public static Type getType(AstNode node) {
    if (node == null) {
      return null;
    }
    Type type = TYPE_MAP.get(node);
    if (type != null) {
      return type;
    }
    // Fallback: derive type from symbol if available
    Symbol symbol = AstNodeSymbolExtension.getSymbol(node);
    if (symbol != null && !symbol.isUnknown()) {
      return Type.Types.fromSymbol(symbol);
    }
    return null;
  }

  /**
   * Checks if an AstNode has an associated Type.
   *
   * @param node the AST node
   * @return true if a type is associated with this node (either explicit or derived from symbol)
   */
  public static boolean hasType(AstNode node) {
    return getType(node) != null;
  }

  /**
   * Checks if an AstNode has an explicitly set Type (not derived from symbol).
   *
   * @param node the AST node
   * @return true if a type was explicitly set on this node
   */
  public static boolean hasExplicitType(AstNode node) {
    return node != null && TYPE_MAP.containsKey(node);
  }

  /**
   * Removes the type association for an AstNode.
   *
   * @param node the AST node
   */
  public static void removeType(AstNode node) {
    if (node != null) {
      TYPE_MAP.remove(node);
    }
  }

  /**
   * Clears all type associations.
   */
  public static void clear() {
    TYPE_MAP.clear();
  }

  /**
   * Returns the number of currently mapped types.
   *
   * @return the number of AstNode-Type associations
   */
  public static int size() {
    return TYPE_MAP.size();
  }
}
