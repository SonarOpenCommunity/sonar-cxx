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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Symbol table for managing symbols within a scope.
 *
 * <p>This class provides a registry for symbols discovered during AST analysis,
 * allowing lookup and management of symbol information. It supports hierarchical
 * scopes through parent/child relationships.
 */
public class SymbolTable {

  private final Map<String, Symbol> symbols;
  private final SymbolTable parent;
  private final List<SymbolTable> children;

  /**
   * Creates a new symbol table with no parent (global scope).
   */
  public SymbolTable() {
    this(null);
  }

  /**
   * Creates a new symbol table with the specified parent scope.
   *
   * @param parent the parent symbol table, or null for global scope
   */
  public SymbolTable(@Nullable SymbolTable parent) {
    this.symbols = new HashMap<>();
    this.parent = parent;
    this.children = new ArrayList<>();
    if (parent != null) {
      parent.addChild(this);
    }
  }

  /**
   * Adds a symbol to this symbol table.
   *
   * @param symbol the symbol to add
   */
  public void addSymbol(Symbol symbol) {
    if (symbol != null && symbol.name() != null) {
      symbols.put(symbol.name(), symbol);
    }
  }

  /**
   * Looks up a symbol by name in this scope.
   *
   * <p>Only searches the current scope, not parent scopes.
   *
   * @param name the name of the symbol to find
   * @return the symbol, or null if not found in this scope
   */
  @CheckForNull
  public Symbol getSymbol(String name) {
    return symbols.get(name);
  }

  /**
   * Looks up a symbol by name, searching parent scopes if not found in this scope.
   *
   * @param name the name of the symbol to find
   * @return the symbol, or null if not found in any accessible scope
   */
  @CheckForNull
  public Symbol lookupSymbol(String name) {
    Symbol symbol = symbols.get(name);
    if (symbol != null) {
      return symbol;
    }
    if (parent != null) {
      return parent.lookupSymbol(name);
    }
    return null;
  }

  /**
   * Gets all symbols defined in this scope.
   *
   * @return collection of all symbols in this scope
   */
  public Collection<Symbol> getSymbols() {
    return symbols.values();
  }

  /**
   * Gets the parent symbol table.
   *
   * @return the parent symbol table, or null if this is the global scope
   */
  @Nullable
  public SymbolTable getParent() {
    return parent;
  }

  /**
   * Gets child symbol tables (nested scopes).
   *
   * @return list of child symbol tables
   */
  public List<SymbolTable> getChildren() {
    return new ArrayList<>(children);
  }

  private void addChild(SymbolTable child) {
    children.add(child);
  }

  /**
   * Creates a new child symbol table for a nested scope.
   *
   * @return a new child symbol table
   */
  public SymbolTable createChildScope() {
    return new SymbolTable(this);
  }

  /**
   * Checks if this symbol table contains a symbol with the given name.
   *
   * @param name the name to check
   * @return true if a symbol with this name exists in this scope
   */
  public boolean hasSymbol(String name) {
    return symbols.containsKey(name);
  }

  /**
   * Gets the number of symbols in this scope.
   *
   * @return the number of symbols
   */
  public int size() {
    return symbols.size();
  }

  /**
   * Checks if this symbol table is empty.
   *
   * @return true if this symbol table contains no symbols
   */
  public boolean isEmpty() {
    return symbols.isEmpty();
  }

  /**
   * Clears all symbols from this symbol table.
   */
  public void clear() {
    symbols.clear();
    children.clear();
  }

  @Override
  public String toString() {
    return "SymbolTable[symbols=" + symbols.size() + ", children=" + children.size() + "]";
  }
}
