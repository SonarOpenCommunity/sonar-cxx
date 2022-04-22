/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

public class ExceptionHandlingTest extends ParserBaseTestHelper {

  @Test
  void exceptionDeclaration() {
    setRootRule(CxxGrammarImpl.exceptionDeclaration);

    mockRule(CxxGrammarImpl.typeSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThatParser()
      .matches("...")
      .matches("typeSpecifierSeq declarator")
      .matches("attributeSpecifierSeq typeSpecifierSeq declarator")
      .matches("typeSpecifierSeq")
      .matches("attributeSpecifierSeq typeSpecifierSeq abstractDeclarator");
  }

  @Test
  void exceptionSpecification_reallife() {
    setRootRule(CxxGrammarImpl.noexceptSpecifier);

    assertThatParser()
      .matches("throw()")
      .matches("throw(...)");
  }

  @Test
  void typeIdList() {
    setRootRule(CxxGrammarImpl.typeIdList);

    mockRule(CxxGrammarImpl.typeId);

    assertThatParser()
      .matches("typeId")
      .matches("typeId ...")
      .matches("typeId , typeId")
      .matches("typeId , typeId ...")
      .matches("...");
  }

  @Test
  void noexceptSpecification() {
    setRootRule(CxxGrammarImpl.noexceptSpecifier);

    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("noexcept")
      .matches("noexcept ( constantExpression )");
  }

}
