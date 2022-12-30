/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import org.junit.jupiter.api.Test;

/**
 * @author jmecosta
 */
class LamdaExpressionsTest extends ParserBaseTestHelper {

  @Test
  void lambdaExpression() {
    setRootRule(CxxGrammarImpl.lambdaExpression);

    mockRule(CxxGrammarImpl.lambdaIntroducer);
    mockRule(CxxGrammarImpl.lambdaDeclarator);
    mockRule(CxxGrammarImpl.compoundStatement);
    mockRule(CxxGrammarImpl.templateParameterList);
    mockRule(CxxGrammarImpl.requiresClause);

    assertThatParser()
      .matches("lambdaIntroducer compoundStatement")
      .matches("lambdaIntroducer < templateParameterList > compoundStatement")
      .matches("lambdaIntroducer < templateParameterList > requiresClause compoundStatement")
      .matches("lambdaIntroducer lambdaDeclarator compoundStatement");
  }

  @Test
  void lambdaExpression_reallife() {
    setRootRule(CxxGrammarImpl.lambdaExpression);

    assertThatParser()
      .matches("[] ( ) { }")
      .matches("[] ( ) mutable { }")
      .matches("[] ( ) noexcept { }")
      .matches("[] ( ) constexpr { }")
      .matches("[] ( ) throw(X,Y) { }")
      .matches("[] ( ) mutable noexcept { }")
      .matches("[] ( ) mutable throw(X,Y) { }")
      .matches("[] ( ) constexpr mutable { }")
      .matches("[] (int n) { }")
      .matches("[&] ( ) { }")
      .matches("[&foo] (int n) { }")
      .matches("[=] (int n) { }")
      .matches("[=,&foo] (int n) { }")
      .matches("[&foo1,&foo2,&foo3] (int n, int y, int z) { }")
      .matches("[] () throw () { }")
      .matches("[] () -> int { return 1; }")
      .matches("[] () -> long long { return 1; }")
      .matches("[] (const string& addr) { return addr.find( \".org\" ) != string::npos; }")
      .matches("[this] () { cout << _x; }")
      .matches("[*this] () { cout << _x; }")
      .matches("[=, *this] { }")
      .matches("[] (int x, int y) -> int { return x + y; }")
      .matches("[](auto x, auto y) { return x + y; }")
      .matches("[](const auto& m) { return m.size(); }")
      .matches("[value = 1] { return value; }")
      .matches("[value = std::move(ptr)] { return *value; }")
      .matches("[&r = x, y = x+1] { r += 2; return y+2; }");
  }

  @Test
  void lambdaIntroducer() {
    setRootRule(CxxGrammarImpl.lambdaIntroducer);
    mockRule(CxxGrammarImpl.lambdaCapture);

    assertThatParser()
      .matches("[]")
      .matches("[lambdaCapture]");
  }

  @Test
  void lambdaIntroducer_reallife() {
    setRootRule(CxxGrammarImpl.lambdaIntroducer);

    assertThatParser()
      .matches("[&]")
      .matches("[=]")
      .matches("[bar]")
      .matches("[this]")
      .matches("[&foo]")
      .matches("[=,&foo]");
  }

  @Test
  void lambdaCapture() {
    setRootRule(CxxGrammarImpl.lambdaCapture);

    mockRule(CxxGrammarImpl.captureDefault);
    mockRule(CxxGrammarImpl.captureList);

    assertThatParser()
      .matches("captureDefault")
      .matches("captureList")
      .matches("captureDefault , captureList");
  }

  @Test
  void captureDefault() {
    setRootRule(CxxGrammarImpl.captureDefault);

    assertThatParser()
      .matches("&")
      .matches("=");
  }

  @Test
  void captureList() {
    setRootRule(CxxGrammarImpl.captureList);
    mockRule(CxxGrammarImpl.capture);

    assertThatParser()
      .matches("capture")
      .matches("capture , capture");
  }

  @Test
  void capture() {
    setRootRule(CxxGrammarImpl.capture);

    mockRule(CxxGrammarImpl.simpleCapture);
    mockRule(CxxGrammarImpl.initCapture);

    assertThatParser()
      .matches("simpleCapture")
      .matches("initCapture");
  }

  @Test
  void simpleCapture() {
    setRootRule(CxxGrammarImpl.simpleCapture);

    assertThatParser()
      .matches("foo")
      .matches("foo ...")
      .matches("&foo")
      .matches("&foo ")
      .matches("this")
      .matches("*this");
  }

  @Test
  void initCapture() {
    setRootRule(CxxGrammarImpl.initCapture);
    mockRule(CxxGrammarImpl.initializer);

    assertThatParser()
      .matches("foo initializer")
      .matches("... foo initializer")
      .matches("&foo initializer")
      .matches("&... foo initializer");
  }

  @Test
  void initCapture_reallife() {
    setRootRule(CxxGrammarImpl.initCapture);

    assertThatParser()
      .matches("value = 1")
      .matches("&r = x")
      .matches("u = move(u)")
      .matches("value = std::move(ptr)")
      .matches("...args=std::move(args)");
  }

  @Test
  void lambdaDeclarator() {
    setRootRule(CxxGrammarImpl.lambdaDeclarator);

    mockRule(CxxGrammarImpl.parameterDeclarationClause);
    mockRule(CxxGrammarImpl.noexceptSpecifier);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.trailingReturnType);
    mockRule(CxxGrammarImpl.requiresClause);

    assertThatParser()
      .matches("( parameterDeclarationClause ) ") // all opt out

      .matches("( parameterDeclarationClause ) mutable") // mutable in

      .matches("( parameterDeclarationClause ) noexceptSpecifier") // noexceptSpecifier in

      .matches("( parameterDeclarationClause ) attributeSpecifierSeq") // attributeSpecifierSeq in

      .matches("( parameterDeclarationClause ) trailingReturnType") // trailingReturnType in

      .matches("( parameterDeclarationClause ) mutable noexceptSpecifier") // complex 1

      .matches("( parameterDeclarationClause ) mutable noexceptSpecifier attributeSpecifierSeq") // complex 2

      .matches(
        "( parameterDeclarationClause ) mutable noexceptSpecifier attributeSpecifierSeq trailingReturnType") // complex
      // 3

      .matches("( parameterDeclarationClause ) noexceptSpecifier attributeSpecifierSeq") // complex 4

      .matches("( parameterDeclarationClause ) noexceptSpecifier attributeSpecifierSeq trailingReturnType") // complex 5

      .matches("( parameterDeclarationClause ) attributeSpecifierSeq trailingReturnType") // complex 6

      .matches(
        "( parameterDeclarationClause ) noexceptSpecifier attributeSpecifierSeq trailingReturnType requiresClause");
  }

}
