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

class OverloadingTest extends ParserBaseTestHelper {

  @Test
  void operatorFunctionId_reallife() {
    setRootRule(CxxGrammarImpl.operatorFunctionId);

    assertThatParser()
      .matches("operator()");
  }

  @Test
  void operator() {
    setRootRule(CxxGrammarImpl.operator);

    assertThatParser()
      .matches("new")
      .matches("delete")
      .matches("new[]")
      .matches("delete[]")
      .matches("co_await")
      .matches("()")
      .matches("[]");
  }

  @Test
  void literalOperatorId_reallife() {
    setRootRule(CxxGrammarImpl.literalOperatorId);

    assertThatParser()
      // operator "" identifier
      //    the identifier to use as the ud-suffix
      .matches("operator \"\" _ud_suffix")
      // operator user-defined-string-literal (since C++14)
      //   the character sequence "" followed, without a space, by the character
      //   sequence that becomes the ud-suffix
      .matches("operator \"\"if")
      .matches("operator \"\"_ud_suffix");
  }
}
