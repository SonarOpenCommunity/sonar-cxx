/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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
import org.junit.jupiter.api.Test;

class SourceCodeSymbolTest {

  @Test
  void typeSymbolFlagsAreSettableAndReflectedInIsMethods() {
    SourceCodeSymbol.SourceCodeTypeSymbol typeSymbol =
        new SourceCodeSymbol.SourceCodeTypeSymbol("MyStruct", null);

    assertThat(typeSymbol.isClass()).isFalse();
    assertThat(typeSymbol.isStruct()).isFalse();
    assertThat(typeSymbol.isUnion()).isFalse();
    assertThat(typeSymbol.isEnum()).isFalse();
    assertThat(typeSymbol.isTypedef()).isFalse();
    assertThat(typeSymbol.typeKind()).isEqualTo(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.UNKNOWN);

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.STRUCT);

    assertThat(typeSymbol.isStruct()).isTrue();
    assertThat(typeSymbol.isClass()).isFalse();
    assertThat(typeSymbol.isUnion()).isFalse();
    assertThat(typeSymbol.isEnum()).isFalse();
    assertThat(typeSymbol.isTypedef()).isFalse();
  }

  @Test
  void typeSymbolCanBeMarkedAsEnum() {
    SourceCodeSymbol.SourceCodeTypeSymbol typeSymbol =
        new SourceCodeSymbol.SourceCodeTypeSymbol("Color", null);

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.ENUM);

    assertThat(typeSymbol.isEnum()).isTrue();
  }

  @Test
  void typeSymbolCanBeMarkedAsTypedef() {
    SourceCodeSymbol.SourceCodeTypeSymbol typeSymbol =
        new SourceCodeSymbol.SourceCodeTypeSymbol("MyAlias", null);

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.TYPEDEF);

    assertThat(typeSymbol.isTypedef()).isTrue();
  }

  @Test
  void typeSymbolSettingNewKindReplacesThePreviousOne() {
    // A type symbol represents exactly one kind of declaration at a time.
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Thing", null);

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.STRUCT);
    assertThat(typeSymbol.isStruct()).isTrue();
    assertThat(typeSymbol.isEnum()).isFalse();

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.ENUM);
    assertThat(typeSymbol.isStruct()).isFalse();
    assertThat(typeSymbol.isEnum()).isTrue();
  }

  @Test
  void typeSymbolIsScopedEnumDefaultsToFalse() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Color", null);
    assertThat(typeSymbol.isScopedEnum()).isFalse();
  }

  @Test
  void typeSymbolIsScopedEnumReturnsSetValue() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Mode", null);
    typeSymbol.setScopedEnum(true);
    assertThat(typeSymbol.isScopedEnum()).isTrue();
  }

  @Test
  void typeSymbolMemberScopeDefaultsToNull() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Mode", null);
    assertThat(typeSymbol.memberScope()).isNull();
  }

  @Test
  void typeSymbolMemberScopeReturnsSetValue() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Mode", null);
    var scope = new SymbolTable();
    typeSymbol.setMemberScope(scope);
    assertThat(typeSymbol.memberScope()).isSameAs(scope);
  }

  @Test
  void variableSymbolInitializerDefaultsToNull() {
    var variableSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("x", null);
    assertThat(variableSymbol.initializer()).isNull();
  }

  @Test
  void variableSymbolInitializerReturnsSetValue() {
    var variableSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("x", null);
    var token = Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue("42")
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    var initializerNode = new AstNode(token);
    variableSymbol.setInitializer(initializerNode);
    assertThat(variableSymbol.initializer()).isSameAs(initializerNode);
  }

  @Test
  void typeSymbolCanBeMarkedAsClassDirectly() {
    // Only the SourceClass-taking constructor defaults to TypeKind.CLASS.
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("MyClass", null);
    assertThat(typeSymbol.isClass()).isFalse();

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.CLASS);

    assertThat(typeSymbol.isClass()).isTrue();
  }

  @Test
  void ownerFallbackIsCachedSoMutationsThroughItPersist() {
    var outerClass = new SourceClass("Outer", "Outer");
    var innerFunction = new SourceFunction(outerClass, "Outer::method", "method()", 1);
    outerClass.addChild(innerFunction);
    var functionSymbol = new SourceCodeSymbol(innerFunction, Symbol.Kind.FUNCTION);

    Symbol firstCall = functionSymbol.owner();
    Symbol secondCall = functionSymbol.owner();

    // Both calls must return the exact same fallback-constructed instance.
    assertThat(secondCall).isSameAs(firstCall);
  }

  @Test
  void enclosingClassFallbackIsCachedSoMutationsThroughItPersist() {
    var outerClass = new SourceClass("Outer", "Outer");
    var innerFunction = new SourceFunction(outerClass, "Outer::method", "method()", 1);
    outerClass.addChild(innerFunction);
    var functionSymbol = new SourceCodeSymbol(innerFunction, Symbol.Kind.FUNCTION);

    Symbol.TypeSymbol firstCall = functionSymbol.enclosingClass();
    assertThat(firstCall).isNotNull();
    var memberScope = new SymbolTable();
    firstCall.memberScope(); // no-op read to document the getter exists before mutating below
    ((SourceCodeSymbol.SourceCodeTypeSymbol) firstCall).setMemberScope(memberScope);

    Symbol.TypeSymbol secondCall = functionSymbol.enclosingClass();

    assertThat(secondCall).isSameAs(firstCall);
    assertThat(secondCall.memberScope()).isSameAs(memberScope);
  }

  @Test
  void changingTypeKindOnAClassBackedInstanceUpdatesIsClass() {
    // SourceCodeTypeSymbol(SourceClass) sets typeKind=CLASS; isClass() must track that field.
    var sourceClass = new SourceClass("Outer", "Outer");
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol(sourceClass);
    assertThat(typeSymbol.isClass()).isTrue();

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.STRUCT);

    assertThat(typeSymbol.isClass()).isFalse();
    assertThat(typeSymbol.isStruct()).isTrue();
  }

  @Test
  void typeSymbolCanBeMarkedAsUnion() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Variant", null);
    assertThat(typeSymbol.isUnion()).isFalse();

    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.UNION);

    assertThat(typeSymbol.isUnion()).isTrue();
  }

  @Test
  void memberSymbolsMapUnrecognizedSourceCodeKindToUnknown() {
    // SourceFile falls through deriveKindFromSourceCode to the UNKNOWN branch.
    var sourceClass = new SourceClass("Outer", "Outer");
    var unrelatedChild = new SourceFile("Outer.cpp", "Outer.cpp");
    sourceClass.addChild(unrelatedChild);

    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol(sourceClass);
    var members = typeSymbol.memberSymbols();

    assertThat(members).hasSize(1);
    assertThat(members.iterator().next().kind()).isEqualTo(Symbol.Kind.UNKNOWN);
  }

  private static class TestTokenType implements com.sonar.cxx.sslr.api.TokenType {
    @Override
    public String getName() {
      return "TEST";
    }

    @Override
    public String getValue() {
      return "TEST";
    }

    @Override
    public String toString() {
      return getName();
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }
  }
}
