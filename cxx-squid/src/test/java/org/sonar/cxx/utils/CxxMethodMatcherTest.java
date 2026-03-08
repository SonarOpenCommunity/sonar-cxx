/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.utils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.AstNodeTypeExtension;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.SourceCodeSymbol;
import org.sonar.cxx.squidbridge.api.Type;

class CxxMethodMatcherTest {

  @AfterEach
  void cleanup() {
    AstNodeSymbolExtension.clear();
    AstNodeTypeExtension.clear();
  }

  @Test
  void testBuilderRequiresAllSteps() {
    var builder = CxxMethodMatcher.create();
    assertThat(builder).isNotNull();
  }

  @Test
  void testBuilderValidation() {
    var builder = CxxMethodMatcher.create()
      .ofAnyType()
      .names("test");

    assertThatThrownBy(() -> builder.build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("At least one parameter matcher must be defined");
  }

  @Test
  void testMatchesWithAnyType() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("testFunc")
      .withAnyParameters()
      .build();

    var symbol = createFunctionSymbol("testFunc", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(symbol)).isTrue();
  }

  @Test
  void testMatchesExactType() {
    var matcher = CxxMethodMatcher.create()
      .ofTypes("int")
      .names("getValue")
      .withAnyParameters()
      .build();

    var intType = new Type.CxxType("int", true, false, false, false, false, null);
    var symbol = createFunctionSymbol("getValue", intType, List.of());
    assertThat(matcher.matches(symbol)).isTrue();

    var stringType = new Type.CxxType("std::string");
    var wrongTypeSymbol = createFunctionSymbol("getValue", stringType, List.of());
    assertThat(matcher.matches(wrongTypeSymbol)).isFalse();
  }

  @Test
  void testMatchesMultipleNames() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("init", "initialize", "setup")
      .withAnyParameters()
      .build();

    var symbol1 = createFunctionSymbol("init", Type.UNKNOWN_TYPE, List.of());
    var symbol2 = createFunctionSymbol("initialize", Type.UNKNOWN_TYPE, List.of());
    var symbol3 = createFunctionSymbol("setup", Type.UNKNOWN_TYPE, List.of());
    var symbol4 = createFunctionSymbol("other", Type.UNKNOWN_TYPE, List.of());

    assertThat(matcher.matches(symbol1)).isTrue();
    assertThat(matcher.matches(symbol2)).isTrue();
    assertThat(matcher.matches(symbol3)).isTrue();
    assertThat(matcher.matches(symbol4)).isFalse();
  }

  @Test
  void testMatchesWithoutParameters() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("getValue")
      .addWithoutParametersMatcher()
      .build();

    var noParamsSymbol = createFunctionSymbol("getValue", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(noParamsSymbol)).isTrue();

    var intType = new Type.CxxType("int");
    var withParamsSymbol = createFunctionSymbol("getValue", Type.UNKNOWN_TYPE, List.of(intType));
    assertThat(matcher.matches(withParamsSymbol)).isFalse();
  }

  @Test
  void testMatchesExactParameters() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("setValue")
      .addParametersMatcher("int")
      .build();

    var intType = new Type.CxxType("int");
    var matchingSymbol = createFunctionSymbol("setValue", Type.UNKNOWN_TYPE, List.of(intType));
    assertThat(matcher.matches(matchingSymbol)).isTrue();

    var stringType = new Type.CxxType("std::string");
    var wrongParamSymbol = createFunctionSymbol("setValue", Type.UNKNOWN_TYPE, List.of(stringType));
    assertThat(matcher.matches(wrongParamSymbol)).isFalse();
  }

  @Test
  void testMatchesMultipleParameters() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("process")
      .addParametersMatcher("int", "std::string")
      .build();

    var intType = new Type.CxxType("int");
    var stringType = new Type.CxxType("std::string");
    var matchingSymbol = createFunctionSymbol("process", Type.UNKNOWN_TYPE,
      List.of(intType, stringType));
    assertThat(matcher.matches(matchingSymbol)).isTrue();

    var wrongOrderSymbol = createFunctionSymbol("process", Type.UNKNOWN_TYPE,
      List.of(stringType, intType));
    assertThat(matcher.matches(wrongOrderSymbol)).isFalse();
  }

  @Test
  void testMatchesWithWildcard() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("callback")
      .addParametersMatcher("*", "int")
      .build();

    var intType = new Type.CxxType("int");
    var stringType = new Type.CxxType("std::string");
    var doubleType = new Type.CxxType("double");

    var symbol1 = createFunctionSymbol("callback", Type.UNKNOWN_TYPE,
      List.of(stringType, intType));
    assertThat(matcher.matches(symbol1)).isTrue();

    var symbol2 = createFunctionSymbol("callback", Type.UNKNOWN_TYPE,
      List.of(doubleType, intType));
    assertThat(matcher.matches(symbol2)).isTrue();

    var wrongSymbol = createFunctionSymbol("callback", Type.UNKNOWN_TYPE,
      List.of(stringType, stringType));
    assertThat(matcher.matches(wrongSymbol)).isFalse();
  }

  @Test
  void testMatchesMultipleParameterMatchers() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("setValue")
      .addParametersMatcher("int")
      .addParametersMatcher("std::string")
      .build();

    var intType = new Type.CxxType("int");
    var stringType = new Type.CxxType("std::string");

    var intSymbol = createFunctionSymbol("setValue", Type.UNKNOWN_TYPE, List.of(intType));
    assertThat(matcher.matches(intSymbol)).isTrue();

    var stringSymbol = createFunctionSymbol("setValue", Type.UNKNOWN_TYPE, List.of(stringType));
    assertThat(matcher.matches(stringSymbol)).isTrue();

    var doubleType = new Type.CxxType("double");
    var wrongSymbol = createFunctionSymbol("setValue", Type.UNKNOWN_TYPE, List.of(doubleType));
    assertThat(matcher.matches(wrongSymbol)).isFalse();
  }

  @Test
  void testOrCombination() {
    var matcher1 = CxxMethodMatcher.create()
      .ofAnyType()
      .names("init")
      .withAnyParameters()
      .build();

    var matcher2 = CxxMethodMatcher.create()
      .ofAnyType()
      .names("setup")
      .withAnyParameters()
      .build();

    var combined = CxxMethodMatcher.or(matcher1, matcher2);

    var initSymbol = createFunctionSymbol("init", Type.UNKNOWN_TYPE, List.of());
    var setupSymbol = createFunctionSymbol("setup", Type.UNKNOWN_TYPE, List.of());
    var otherSymbol = createFunctionSymbol("other", Type.UNKNOWN_TYPE, List.of());

    assertThat(combined.matches(initSymbol)).isTrue();
    assertThat(combined.matches(setupSymbol)).isTrue();
    assertThat(combined.matches(otherSymbol)).isFalse();
  }

  @Test
  void testMatchesNullNode() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("test")
      .withAnyParameters()
      .build();

    assertThat(matcher.matches((AstNode) null)).isFalse();
  }

  @Test
  void testMatchesNullSymbol() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("test")
      .withAnyParameters()
      .build();

    assertThat(matcher.matches((Symbol) null)).isFalse();
  }

  @Test
  void testMatchesNonFunctionSymbol() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("test")
      .withAnyParameters()
      .build();

    var varSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("test", null);
    assertThat(matcher.matches(varSymbol)).isFalse();
  }

  @Test
  void testNullParameterArrayThrows() {
    assertThatThrownBy(() -> {
      CxxMethodMatcher.create()
        .ofAnyType()
        .names((String[]) null);
    }).isInstanceOf(NullPointerException.class);
  }

  @Test
  void testNullPredicateThrows() {
    assertThatThrownBy(() -> {
      CxxMethodMatcher.create()
        .ofType(null);
    }).isInstanceOf(NullPointerException.class);
  }

  // =========================================================================
  // matches(AstNode) tests
  // =========================================================================

  @Test
  void testMatchesAstNodeNull() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .anyName()
      .withAnyParameters()
      .build();

    assertThat(matcher.matches((AstNode) null)).isFalse();
  }

  @Test
  void testMatchesAstNodeNotFunctionCall() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .anyName()
      .withAnyParameters()
      .build();

    // A primaryExpression is not a function call
    var node = createNode(CxxGrammarImpl.primaryExpression, "foo");
    assertThat(matcher.matches(node)).isFalse();
  }

  @Test
  void testMatchesAstNodeFunctionCallNoSymbol() {
    // A postfixExpression with a "(" child but no symbol attached → falls back to
    // typePredicate.test(UNKNOWN_TYPE) and parameterMatchers on empty list
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("myFunc")
      .withAnyParameters()
      .build();

    // Build: postfixExpression → [idExpression → [IDENTIFIER "myFunc"], "(", ")"]
    var postfix = createNode(CxxGrammarImpl.postfixExpression, "myFunc");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "myFunc");
    idExpr.addChild(createIdentifierNode("myFunc"));
    postfix.addChild(idExpr);
    postfix.addChild(createTokenNode("("));
    postfix.addChild(createTokenNode(")"));

    assertThat(matcher.matches(postfix)).isTrue();
  }

  @Test
  void testMatchesAstNodeFunctionCallNameMismatch() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("expectedName")
      .withAnyParameters()
      .build();

    var postfix = createNode(CxxGrammarImpl.postfixExpression, "differentName");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "differentName");
    idExpr.addChild(createIdentifierNode("differentName"));
    postfix.addChild(idExpr);
    postfix.addChild(createTokenNode("("));
    postfix.addChild(createTokenNode(")"));

    assertThat(matcher.matches(postfix)).isFalse();
  }

  @Test
  void testMatchesAstNodeWithAttachedSymbol() {
    var matcher = CxxMethodMatcher.create()
      .ofTypes("int")
      .names("getValue")
      .withAnyParameters()
      .build();

    var postfix = createNode(CxxGrammarImpl.postfixExpression, "getValue");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "getValue");
    idExpr.addChild(createIdentifierNode("getValue"));
    postfix.addChild(idExpr);
    postfix.addChild(createTokenNode("("));
    postfix.addChild(createTokenNode(")"));

    var intType = new Type.CxxType("int", true, false, false, false, false, null);
    var funcSymbol = (SourceCodeSymbol.SourceCodeFunctionSymbol) createFunctionSymbol(
      "getValue", intType, List.of());
    AstNodeSymbolExtension.setSymbol(postfix, funcSymbol);

    assertThat(matcher.matches(postfix)).isTrue();
  }

  @Test
  void testMatchesAstNodeWithAttachedSymbolTypeMismatch() {
    var matcher = CxxMethodMatcher.create()
      .ofTypes("int")
      .names("getValue")
      .withAnyParameters()
      .build();

    var postfix = createNode(CxxGrammarImpl.postfixExpression, "getValue");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "getValue");
    idExpr.addChild(createIdentifierNode("getValue"));
    postfix.addChild(idExpr);
    postfix.addChild(createTokenNode("("));
    postfix.addChild(createTokenNode(")"));

    var stringType = new Type.CxxType("std::string");
    var funcSymbol = (SourceCodeSymbol.SourceCodeFunctionSymbol) createFunctionSymbol(
      "getValue", stringType, List.of());
    AstNodeSymbolExtension.setSymbol(postfix, funcSymbol);

    assertThat(matcher.matches(postfix)).isFalse();
  }

  // =========================================================================
  // matchesDefinition tests
  // =========================================================================

  @Test
  void testMatchesDefinitionNull() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .anyName()
      .withAnyParameters()
      .build();

    assertThat(matcher.matchesDefinition(null)).isFalse();
  }

  @Test
  void testMatchesDefinitionWrongNodeType() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .anyName()
      .withAnyParameters()
      .build();

    var node = createNode(CxxGrammarImpl.primaryExpression, "foo");
    assertThat(matcher.matchesDefinition(node)).isFalse();
  }

  @Test
  void testMatchesDefinitionNoSymbol() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .names("compute")
      .withAnyParameters()
      .build();

    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "compute");
    var decl = createNode(CxxGrammarImpl.declarator, "compute");
    var declId = createNode(CxxGrammarImpl.declaratorId, "compute");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "compute");
    idExpr.addChild(createIdentifierNode("compute"));
    declId.addChild(idExpr);
    decl.addChild(declId);
    funcDef.addChild(decl);

    assertThat(matcher.matchesDefinition(funcDef)).isTrue();
  }

  @Test
  void testMatchesDefinitionWithSymbol() {
    var matcher = CxxMethodMatcher.create()
      .ofTypes("void")
      .names("process")
      .withAnyParameters()
      .build();

    var funcDef = createNode(CxxGrammarImpl.functionDefinition, "process");
    var decl = createNode(CxxGrammarImpl.declarator, "process");
    var declId = createNode(CxxGrammarImpl.declaratorId, "process");
    var idExpr = createNode(CxxGrammarImpl.idExpression, "process");
    idExpr.addChild(createIdentifierNode("process"));
    declId.addChild(idExpr);
    decl.addChild(declId);
    funcDef.addChild(decl);

    var voidType = new Type.CxxType("void", true, false, false, false, false, null);
    var funcSymbol = (SourceCodeSymbol.SourceCodeFunctionSymbol) createFunctionSymbol(
      "process", voidType, List.of());
    AstNodeSymbolExtension.setSymbol(funcDef, funcSymbol);

    assertThat(matcher.matchesDefinition(funcDef)).isTrue();
  }

  // =========================================================================
  // Builder method tests
  // =========================================================================

  @Test
  void testAnyName() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .anyName()
      .withAnyParameters()
      .build();

    var symbol1 = createFunctionSymbol("anything", Type.UNKNOWN_TYPE, List.of());
    var symbol2 = createFunctionSymbol("somethingElse", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(symbol1)).isTrue();
    assertThat(matcher.matches(symbol2)).isTrue();
  }

  @Test
  void testOfSubTypesNoMatch() {
    var matcher = CxxMethodMatcher.create()
      .ofSubTypes("BaseClass")
      .names("method")
      .withAnyParameters()
      .build();

    // Type.UNKNOWN_TYPE → isUnknown() = true → ofSubTypes returns false
    var symbol = createFunctionSymbol("method", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(symbol)).isFalse();
  }

  @Test
  void testOfSubTypesWithConcreteType() {
    var matcher = CxxMethodMatcher.create()
      .ofSubTypes("int")
      .names("getValue")
      .withAnyParameters()
      .build();

    // CxxType.isSubtypeOf("int") → checks if the type name equals "int"
    var intType = new Type.CxxType("int", true, false, false, false, false, null);
    var symbol = createFunctionSymbol("getValue", intType, List.of());
    assertThat(matcher.matches(symbol)).isTrue();
  }

  @Test
  void testConstructorMatcherDoesNotMatchRegularFunctions() {
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .constructor()
      .withAnyParameters()
      .build();

    var symbol = createFunctionSymbol("myFunc", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(symbol)).isFalse();
  }

  @Test
  void testConstructorMatcherMatchesQualifiedConstructor() {
    // constructor() matches names like "Foo::Foo" where class == method part
    var matcher = CxxMethodMatcher.create()
      .ofAnyType()
      .constructor()
      .withAnyParameters()
      .build();

    // "MyClass::MyClass" — qualified constructor: part before "::" == part after "::"
    var ctorSymbol = createFunctionSymbol("MyClass::MyClass", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(ctorSymbol)).isTrue();

    // "Foo::Bar" — not a constructor (class name != method name)
    var nonCtorSymbol = createFunctionSymbol("Foo::Bar", Type.UNKNOWN_TYPE, List.of());
    assertThat(matcher.matches(nonCtorSymbol)).isFalse();
  }

  @Test
  void testOfSubTypesWithDerivedType() {
    // Use a TypeSymbol with a non-empty baseClasses() to exercise inheritance path
    var baseTypeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Base", null) {
      @Override
      public java.util.List<Symbol.TypeSymbol> baseClasses() { return List.of(); }
      @Override
      public String fullyQualifiedName() { return "Base"; }
    };

    // Create a TypeSymbol for Derived that has Base as a base class
    var derivedTypeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("Derived", null) {
      @Override
      public java.util.List<Symbol.TypeSymbol> baseClasses() { return List.of(baseTypeSymbol); }
      @Override
      public String fullyQualifiedName() { return "Derived"; }
    };

    var derivedType = new Type.CxxType("Derived", derivedTypeSymbol);

    var matcher = CxxMethodMatcher.create()
      .ofSubTypes("Base")
      .names("process")
      .withAnyParameters()
      .build();

    var symbol = createFunctionSymbol("process", derivedType, List.of());
    assertThat(matcher.matches(symbol)).isTrue();

    // Unrelated type: no match
    var unrelatedType = new Type.CxxType("Other");
    var unrelatedSymbol = createFunctionSymbol("process", unrelatedType, List.of());
    assertThat(matcher.matches(unrelatedSymbol)).isFalse();
  }

  // =========================================================================
  // Private helpers
  // =========================================================================

  private AstNode createNode(com.sonar.cxx.sslr.api.AstNodeType type, String value) {
    var token = Token.builder()
      .setLine(1).setColumn(0)
      .setValueAndOriginalValue(value)
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(type, value, token);
  }

  private AstNode createIdentifierNode(String name) {
    var token = Token.builder()
      .setLine(1).setColumn(0)
      .setValueAndOriginalValue(name)
      .setType(com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER)
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(token);
  }

  private AstNode createTokenNode(String value) {
    var token = Token.builder()
      .setLine(1).setColumn(0)
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

  private Symbol.FunctionSymbol createFunctionSymbol(String name, Type returnType,
                                                      List<Type> paramTypes) {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol(name, null);
    funcSymbol.setReturnType(returnType);

    for (Type paramType : paramTypes) {
      var paramSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("param", null);
      paramSymbol.setParameter(true);

      var paramDeclNode = mock(AstNode.class);
      lenient().when(paramDeclNode.getTokenValue()).thenReturn("param");
      paramSymbol.setDeclaration(paramDeclNode);

      AstNodeTypeExtension.setType(paramDeclNode, paramType);

      funcSymbol.addParameter(paramSymbol);
    }

    return funcSymbol;
  }

}
