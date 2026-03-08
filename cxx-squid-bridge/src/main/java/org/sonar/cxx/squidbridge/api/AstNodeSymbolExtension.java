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
 * Extension to associate Symbols with AstNodes.
 *
 * <p>Since AstNode is part of the SSLR core library (which we cannot modify),
 * this class provides a way to attach Symbol information to AstNode instances
 * without modifying the AstNode class itself.
 *
 * <p>The symbol mapping is stored using WeakHashMap to prevent memory leaks
 * when AstNodes are garbage collected.
 */
public final class AstNodeSymbolExtension {

  private static final WeakHashMap<AstNode, Symbol> SYMBOL_MAP = new WeakHashMap<>();

  private AstNodeSymbolExtension() {
  }

  /**
   * Associates a Symbol with an AstNode.
   *
   * @param node the AST node
   * @param symbol the symbol to associate with the node
   */
  public static void setSymbol(AstNode node, Symbol symbol) {
    if (node != null && symbol != null) {
      SYMBOL_MAP.put(node, symbol);
    }
  }

  /**
   * Retrieves the Symbol associated with an AstNode.
   *
   * @param node the AST node
   * @return the associated symbol, or null if none exists
   */
  @CheckForNull
  public static Symbol getSymbol(AstNode node) {
    if (node == null) {
      return null;
    }
    return SYMBOL_MAP.get(node);
  }

  /**
   * Checks if an AstNode has an associated Symbol.
   *
   * @param node the AST node
   * @return true if a symbol is associated with this node
   */
  public static boolean hasSymbol(AstNode node) {
    return node != null && SYMBOL_MAP.containsKey(node);
  }

  /**
   * Removes the symbol association for an AstNode.
   *
   * @param node the AST node
   */
  public static void removeSymbol(AstNode node) {
    if (node != null) {
      SYMBOL_MAP.remove(node);
    }
  }

  /**
   * Clears all symbol associations.
   */
  public static void clear() {
    SYMBOL_MAP.clear();
  }

  /**
   * Returns the number of currently mapped symbols.
   *
   * @return the number of AstNode-Symbol associations
   */
  public static int size() {
    return SYMBOL_MAP.size();
  }
}
