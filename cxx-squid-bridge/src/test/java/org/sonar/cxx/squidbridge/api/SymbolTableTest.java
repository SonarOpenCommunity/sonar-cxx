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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SymbolTableTest {

  private SymbolTable symbolTable;

  @BeforeEach
  void setUp() {
    symbolTable = new SymbolTable();
  }

  @Test
  void testAddAndGetSymbol() {
    var symbol = new SourceCodeSymbol("myVar", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(symbol);

    assertThat(symbolTable.getSymbol("myVar")).isEqualTo(symbol);
    assertThat(symbolTable.hasSymbol("myVar")).isTrue();
    assertThat(symbolTable.size()).isEqualTo(1);
  }

  @Test
  void testLookupSymbol() {
    var symbol = new SourceCodeSymbol("globalVar", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(symbol);

    assertThat(symbolTable.lookupSymbol("globalVar")).isEqualTo(symbol);
    assertThat(symbolTable.lookupSymbol("nonExistent")).isNull();
  }

  @Test
  void testScopeHierarchy() {
    var globalSymbol = new SourceCodeSymbol("global", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(globalSymbol);

    var childScope = symbolTable.createChildScope();
    var localSymbol = new SourceCodeSymbol("local", Symbol.Kind.VARIABLE, null);
    childScope.addSymbol(localSymbol);

    // Child can see parent symbols
    assertThat(childScope.lookupSymbol("global")).isEqualTo(globalSymbol);
    assertThat(childScope.lookupSymbol("local")).isEqualTo(localSymbol);

    // Parent cannot see child symbols
    assertThat(symbolTable.lookupSymbol("local")).isNull();
    assertThat(symbolTable.getSymbol("local")).isNull();

    // Check hierarchy
    assertThat(childScope.getParent()).isEqualTo(symbolTable);
    assertThat(symbolTable.getChildren()).hasSize(1);
  }

  @Test
  void testShadowing() {
    var parentSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(parentSymbol);

    var childScope = symbolTable.createChildScope();
    var childSymbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    childScope.addSymbol(childSymbol);

    // Child scope shadows parent
    assertThat(childScope.lookupSymbol("x")).isEqualTo(childSymbol);
    assertThat(symbolTable.lookupSymbol("x")).isEqualTo(parentSymbol);
  }

  @Test
  void testGetSymbols() {
    var symbol1 = new SourceCodeSymbol("var1", Symbol.Kind.VARIABLE, null);
    var symbol2 = new SourceCodeSymbol("var2", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(symbol1);
    symbolTable.addSymbol(symbol2);

    assertThat(symbolTable.getSymbols()).containsExactlyInAnyOrder(symbol1, symbol2);
  }

  @Test
  void testClear() {
    var symbol = new SourceCodeSymbol("var", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(symbol);
    symbolTable.createChildScope();

    assertThat(symbolTable.isEmpty()).isFalse();
    assertThat(symbolTable.getChildren()).hasSize(1);

    symbolTable.clear();

    assertThat(symbolTable.isEmpty()).isTrue();
    assertThat(symbolTable.size()).isEqualTo(0);
    assertThat(symbolTable.getChildren()).isEmpty();
  }

  @Test
  void testMultipleLevels() {
    var globalSymbol = new SourceCodeSymbol("global", Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(globalSymbol);

    var level1 = symbolTable.createChildScope();
    var level1Symbol = new SourceCodeSymbol("level1", Symbol.Kind.VARIABLE, null);
    level1.addSymbol(level1Symbol);

    var level2 = level1.createChildScope();
    var level2Symbol = new SourceCodeSymbol("level2", Symbol.Kind.VARIABLE, null);
    level2.addSymbol(level2Symbol);

    // Level 2 can see all levels
    assertThat(level2.lookupSymbol("global")).isEqualTo(globalSymbol);
    assertThat(level2.lookupSymbol("level1")).isEqualTo(level1Symbol);
    assertThat(level2.lookupSymbol("level2")).isEqualTo(level2Symbol);

    // Level 1 cannot see level 2
    assertThat(level1.lookupSymbol("level2")).isNull();
  }

  @Test
  void testNullSymbol() {
    symbolTable.addSymbol(null);
    assertThat(symbolTable.size()).isEqualTo(0);
  }

  @Test
  void testSymbolWithNullName() {
    var symbol = new SourceCodeSymbol(null, Symbol.Kind.VARIABLE, null);
    symbolTable.addSymbol(symbol);
    // Symbol with null name should not be added
    assertThat(symbolTable.size()).isEqualTo(0);
  }
}
