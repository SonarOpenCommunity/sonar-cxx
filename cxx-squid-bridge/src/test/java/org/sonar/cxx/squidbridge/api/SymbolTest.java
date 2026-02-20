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
import org.junit.jupiter.api.Test;

class SymbolTest {

  @Test
  void testUnknownSymbol() {
    var symbol = Symbol.UNKNOWN_SYMBOL;
    assertThat(symbol.name()).isEqualTo("<unknown>");
    assertThat(symbol.isUnknown()).isTrue();
    assertThat(symbol.kind()).isEqualTo(Symbol.Kind.UNKNOWN);
    assertThat(symbol.is(Symbol.Kind.UNKNOWN)).isTrue();
    assertThat(symbol.usages()).isEmpty();
  }

  @Test
  void testSymbolKind() {
    var symbol = new SourceCodeSymbol("foo", Symbol.Kind.VARIABLE, null);
    assertThat(symbol.kind()).isEqualTo(Symbol.Kind.VARIABLE);
    assertThat(symbol.is(Symbol.Kind.VARIABLE)).isTrue();
    assertThat(symbol.is(Symbol.Kind.FUNCTION)).isFalse();
    assertThat(symbol.is(Symbol.Kind.VARIABLE, Symbol.Kind.FUNCTION)).isTrue();
  }

  @Test
  void testSymbolName() {
    var symbol = new SourceCodeSymbol("myVariable", Symbol.Kind.VARIABLE, null);
    assertThat(symbol.name()).isEqualTo("myVariable");
    assertThat(symbol.isVariableSymbol()).isTrue();
    assertThat(symbol.isFunctionSymbol()).isFalse();
  }

  @Test
  void testFullyQualifiedName() {
    var namespace = new SourceCodeSymbol("std", Symbol.Kind.NAMESPACE, null);
    var vector = new SourceCodeSymbol("vector", Symbol.Kind.TYPE, null);
    vector.setOwner(namespace);
    assertThat(vector.fullyQualifiedName()).isEqualTo("std::vector");
  }

  @Test
  void testSymbolOwner() {
    var parent = new SourceCodeSymbol("Parent", Symbol.Kind.TYPE, null);
    var child = new SourceCodeSymbol("child", Symbol.Kind.FUNCTION, null);
    child.setOwner(parent);
    assertThat(child.owner()).isEqualTo(parent);
  }

  @Test
  void testSourceCodeBridge() {
    var sourceClass = new SourceClass("MyClass", "MyClass");
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol(sourceClass);

    assertThat(typeSymbol.name()).isEqualTo("MyClass");
    assertThat(typeSymbol.isTypeSymbol()).isTrue();
    assertThat(typeSymbol.sourceCode()).isEqualTo(sourceClass);
  }

  @Test
  void testFunctionSymbol() {
    var sourceFunc = new SourceFunction("myFunction", "myFunction()");
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol(sourceFunc);

    assertThat(funcSymbol.name()).isEqualTo("myFunction()");
    assertThat(funcSymbol.isFunctionSymbol()).isTrue();
    assertThat(funcSymbol.parameters()).isEmpty();

    var param = new SourceCodeSymbol.SourceCodeVariableSymbol("param1", null);
    param.setParameter(true);
    funcSymbol.addParameter(param);

    assertThat(funcSymbol.parameters()).hasSize(1);
    assertThat(funcSymbol.parameters().get(0).name()).isEqualTo("param1");
    assertThat(funcSymbol.parameters().get(0).isParameter()).isTrue();
  }

  @Test
  void testFunctionSymbolReturnType() {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol("myFunc", null);

    // Default return type is UNKNOWN_TYPE
    assertThat(funcSymbol.returnType()).isNotNull();
    assertThat(funcSymbol.returnType().isUnknown()).isTrue();

    // Set a specific return type
    var returnType = Type.Types.of("int");
    funcSymbol.setReturnType(returnType);
    assertThat(funcSymbol.returnType()).isEqualTo(returnType);
    assertThat(funcSymbol.returnType().is("int")).isTrue();
  }

  @Test
  void testFunctionSymbolSetReturnTypeNull() {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol("myFunc", null);
    funcSymbol.setReturnType(null);
    // Setting null should default to UNKNOWN_TYPE
    assertThat(funcSymbol.returnType()).isNotNull();
    assertThat(funcSymbol.returnType().isUnknown()).isTrue();
  }

  @Test
  void testVariableSymbol() {
    var varSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("myVar", null);

    assertThat(varSymbol.isVariableSymbol()).isTrue();
    assertThat(varSymbol.isLocalVariable()).isFalse();
    assertThat(varSymbol.isParameter()).isFalse();

    varSymbol.setLocalVariable(true);
    assertThat(varSymbol.isLocalVariable()).isTrue();

    varSymbol.setParameter(true);
    assertThat(varSymbol.isParameter()).isTrue();
  }

  @Test
  void testTypeSymbol() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("MyClass", null);

    assertThat(typeSymbol.isTypeSymbol()).isTrue();
    assertThat(typeSymbol.memberSymbols()).isEmpty();
    assertThat(typeSymbol.baseClasses()).isEmpty();
  }

  @Test
  void testUnknownTypeSymbol() {
    var typeSymbol = Symbol.TypeSymbol.UNKNOWN_TYPE;
    assertThat(typeSymbol.isUnknown()).isTrue();
    assertThat(typeSymbol.baseClasses()).isEmpty();
    assertThat(typeSymbol.memberSymbols()).isEmpty();
  }

  @Test
  void testUnknownFunctionSymbol() {
    var funcSymbol = Symbol.FunctionSymbol.UNKNOWN_FUNCTION;
    assertThat(funcSymbol.isUnknown()).isTrue();
    assertThat(funcSymbol.parameters()).isEmpty();
    assertThat(funcSymbol.returnType()).isNotNull();
    assertThat(funcSymbol.returnType().isUnknown()).isTrue();
    assertThat(funcSymbol.parameterTypes()).isEmpty();
  }

  @Test
  void testFunctionSymbolParameterTypes() {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol("myFunc", null);

    // No parameters → empty list
    assertThat(funcSymbol.parameterTypes()).isEmpty();

    // Add parameters without type info → returns UNKNOWN_TYPE for each
    var param1 = new SourceCodeSymbol.SourceCodeVariableSymbol("param1", null);
    param1.setParameter(true);
    var param2 = new SourceCodeSymbol.SourceCodeVariableSymbol("param2", null);
    param2.setParameter(true);
    funcSymbol.addParameter(param1);
    funcSymbol.addParameter(param2);

    var types = funcSymbol.parameterTypes();
    assertThat(types).hasSize(2);
    assertThat(types.get(0).isUnknown()).isTrue();
    assertThat(types.get(1).isUnknown()).isTrue();
  }
}
