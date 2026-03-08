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
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
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

  @Test
  void testQualifierFlagsAllFalseByDefault() {
    var symbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    assertThat(symbol.isStatic()).isFalse();
    assertThat(symbol.isConst()).isFalse();
    assertThat(symbol.isVolatile()).isFalse();
    assertThat(symbol.isPublic()).isFalse();
    assertThat(symbol.isPrivate()).isFalse();
    assertThat(symbol.isProtected()).isFalse();
  }

  @Test
  void testDeclarationNode() {
    var symbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    assertThat(symbol.declaration()).isNull();

    var node = createAstNode("x");
    symbol.setDeclaration(node);
    assertThat(symbol.declaration()).isSameAs(node);
  }

  @Test
  void testAddAndRetrieveUsages() {
    var symbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    assertThat(symbol.usages()).isEmpty();

    var node = createAstNode("x");
    var usage = new SourceCodeSymbol.SourceCodeUsage(node, symbol, Symbol.Usage.UsageKind.READ);
    symbol.addUsage(usage);

    assertThat(symbol.usages()).hasSize(1);
    var retrieved = symbol.usages().get(0);
    assertThat(retrieved.node()).isSameAs(node);
    assertThat(retrieved.symbol()).isSameAs(symbol);
    assertThat(retrieved.kind()).isEqualTo(Symbol.Usage.UsageKind.READ);
  }

  @Test
  void testUsageKindValues() {
    var symbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    var node = createAstNode("x");

    for (var kind : Symbol.Usage.UsageKind.values()) {
      symbol.addUsage(new SourceCodeSymbol.SourceCodeUsage(node, symbol, kind));
    }
    // READ, WRITE, READ_WRITE, DECLARATION, OTHER = 5 kinds
    assertThat(symbol.usages()).hasSize(Symbol.Usage.UsageKind.values().length);
  }

  @Test
  void testMultipleUsages() {
    var symbol = new SourceCodeSymbol("counter", Symbol.Kind.VARIABLE, null);
    var node1 = createAstNode("counter");
    var node2 = createAstNode("counter");

    symbol.addUsage(new SourceCodeSymbol.SourceCodeUsage(node1, symbol, Symbol.Usage.UsageKind.READ));
    symbol.addUsage(new SourceCodeSymbol.SourceCodeUsage(node2, symbol, Symbol.Usage.UsageKind.WRITE));

    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages().get(0).kind()).isEqualTo(Symbol.Usage.UsageKind.READ);
    assertThat(symbol.usages().get(1).kind()).isEqualTo(Symbol.Usage.UsageKind.WRITE);
  }

  @Test
  void testUsageListIsDefensiveCopy() {
    var symbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    var node = createAstNode("x");
    symbol.addUsage(new SourceCodeSymbol.SourceCodeUsage(node, symbol, Symbol.Usage.UsageKind.READ));

    // Mutating the returned list should not affect the symbol's internal state
    var usages = symbol.usages();
    usages.clear();
    assertThat(symbol.usages()).hasSize(1);
  }

  @Test
  void testFunctionSymbolIsConstexpr() {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol("constExprFunc", null);
    assertThat(funcSymbol.isConstexpr()).isFalse();
  }

  @Test
  void testFunctionSymbolIsConstructor() {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol("MyClass", null);
    assertThat(funcSymbol.isConstructor()).isFalse();
  }

  @Test
  void testTypeSymbolIsClassAndIsStruct() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("MyClass", null);
    // No sourceCode backing → isClass() = false, isStruct() = false
    assertThat(typeSymbol.isClass()).isFalse();
    assertThat(typeSymbol.isStruct()).isFalse();
  }

  @Test
  void testSymbolIsUnknownForNormalSymbol() {
    var symbol = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    assertThat(symbol.isUnknown()).isFalse();
  }

  @Test
  void testSymbolKindIs() {
    var symbol = new SourceCodeSymbol("ns", Symbol.Kind.NAMESPACE, null);
    assertThat(symbol.is(Symbol.Kind.NAMESPACE)).isTrue();
    assertThat(symbol.is(Symbol.Kind.TYPE)).isFalse();
    assertThat(symbol.is(Symbol.Kind.TYPE, Symbol.Kind.NAMESPACE)).isTrue();
  }

  @Test
  void testUnknownSymbolQualifierFlags() {
    var unknown = Symbol.UNKNOWN_SYMBOL;
    assertThat(unknown.isStatic()).isFalse();
    assertThat(unknown.isConst()).isFalse();
    assertThat(unknown.isVolatile()).isFalse();
    assertThat(unknown.isPublic()).isFalse();
    assertThat(unknown.isPrivate()).isFalse();
    assertThat(unknown.isProtected()).isFalse();
  }

  @Test
  void testUnknownTypeSymbolQualifierFlags() {
    var unknownType = Symbol.TypeSymbol.UNKNOWN_TYPE;
    assertThat(unknownType.isClass()).isFalse();
    assertThat(unknownType.isStruct()).isFalse();
    assertThat(unknownType.isUnion()).isFalse();
    assertThat(unknownType.isEnum()).isFalse();
    assertThat(unknownType.isTypedef()).isFalse();
    assertThat(unknownType.isTemplate()).isFalse();
  }

  @Test
  void testUnknownFunctionSymbolQualifierFlags() {
    var unknownFunc = Symbol.FunctionSymbol.UNKNOWN_FUNCTION;
    assertThat(unknownFunc.isVirtual()).isFalse();
    assertThat(unknownFunc.isPureVirtual()).isFalse();
    assertThat(unknownFunc.isInline()).isFalse();
    assertThat(unknownFunc.isConstexpr()).isFalse();
    assertThat(unknownFunc.isConstructor()).isFalse();
    assertThat(unknownFunc.isDestructor()).isFalse();
    assertThat(unknownFunc.isTemplate()).isFalse();
  }

  @Test
  void testFunctionSymbolQualifierFlags() {
    var func = new SourceCodeSymbol.SourceCodeFunctionSymbol("myMethod", null);
    assertThat(func.isVirtual()).isFalse();
    assertThat(func.isPureVirtual()).isFalse();
    assertThat(func.isInline()).isFalse();
    assertThat(func.isDestructor()).isFalse();
    assertThat(func.isOperator()).isFalse();
    assertThat(func.isTemplate()).isFalse();
  }

  @Test
  void testTypeSymbolRemainingQualifierFlags() {
    var type = new SourceCodeSymbol.SourceCodeTypeSymbol("MyClass", null);
    assertThat(type.isUnion()).isFalse();
    assertThat(type.isEnum()).isFalse();
    assertThat(type.isTypedef()).isFalse();
    assertThat(type.isTemplate()).isFalse();
  }

  @Test
  void testEnclosingClassWithNoSourceCode() {
    // enclosingClass() returns null when the symbol has no SourceCode backing
    var sym = new SourceCodeSymbol("field", Symbol.Kind.VARIABLE, null);
    assertThat(sym.enclosingClass()).isNull();
  }

  @Test
  void testEnclosingClassWithSourceCodeParent() {
    // Symbol with a SourceClass parent in the SourceCode hierarchy
    var parentClass = new SourceClass("Outer", "Outer");
    var memberFunc = new SourceFunction("method", "method()");
    parentClass.addChild(memberFunc);

    var sym = new SourceCodeSymbol(memberFunc, Symbol.Kind.FUNCTION);
    var enclosing = sym.enclosingClass();
    assertThat(enclosing).isNotNull();
    assertThat(enclosing.name()).isEqualTo("Outer");
  }

  @Test
  void testUsageKindContainsAllValues() {
    var kinds = Symbol.Usage.UsageKind.values();
    assertThat(kinds).contains(
      Symbol.Usage.UsageKind.READ,
      Symbol.Usage.UsageKind.WRITE,
      Symbol.Usage.UsageKind.READ_WRITE,
      Symbol.Usage.UsageKind.DECLARATION,
      Symbol.Usage.UsageKind.OTHER
    );
    assertThat(kinds).hasSize(5);
  }

  private AstNode createAstNode(String value) {
    var token = Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue(value)
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(token);
  }

  private static class TestTokenType implements TokenType {
    @Override public String getName() { return "TEST"; }
    @Override public String getValue() { return "test"; }
    @Override public boolean hasToBeSkippedFromAst(AstNode node) { return false; }
  }
}
