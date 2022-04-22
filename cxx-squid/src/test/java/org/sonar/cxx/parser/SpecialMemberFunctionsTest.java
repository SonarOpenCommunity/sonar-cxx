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

class SpecialMemberFunctionsTest extends ParserBaseTestHelper {

  @Test
  void ctorInitializer_reallife() {
    setRootRule(CxxGrammarImpl.ctorInitializer);

    assertThatParser()
      .matches(": theValue(v)");
  }

  @Test
  void memInitializerList() {
    setRootRule(CxxGrammarImpl.memInitializerList);

    mockRule(CxxGrammarImpl.memInitializer);

    assertThatParser()
      .matches("memInitializer")
      .matches("memInitializer ...")
      .matches("memInitializer , memInitializer")
      .matches("memInitializer , memInitializer ...");
  }

  @Test
  void memInitializer() {
    setRootRule(CxxGrammarImpl.memInitializer);

    mockRule(CxxGrammarImpl.memInitializerId);
    mockRule(CxxGrammarImpl.expressionList);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThatParser()
      .matches("memInitializerId ( )")
      .matches("memInitializerId ( expressionList )")
      .matches("memInitializerId bracedInitList");
  }

  @Test
  void memInitializer_reallife() {
    setRootRule(CxxGrammarImpl.memInitializer);

    assertThatParser()
      .matches("theValue(v)");
  }

  @Test
  void memInitializerId_reallife() {
    setRootRule(CxxGrammarImpl.memInitializerId);

    assertThatParser()
      .matches("theValue");
  }

}
