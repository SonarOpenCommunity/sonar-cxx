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

  private Symbol.FunctionSymbol createFunctionSymbol(String name, Type returnType,
                                                      List<Type> paramTypes) {
    var funcSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol(name, null);
    funcSymbol.setReturnType(returnType);

    for (Type paramType : paramTypes) {
      var paramSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("param", null);
      paramSymbol.setParameter(true);

      var paramDeclNode = mock(AstNode.class, withSettings().lenient());
      when(paramDeclNode.getTokenValue()).thenReturn("param");
      paramSymbol.setDeclaration(paramDeclNode);

      AstNodeTypeExtension.setType(paramDeclNode, paramType);

      funcSymbol.addParameter(paramSymbol);
    }

    return funcSymbol;
  }

}
