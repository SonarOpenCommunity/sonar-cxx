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

class AttributeTest extends ParserBaseTestHelper {

  @Test
  void classSpecifier_reallife() {
    setRootRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("[[attr]]")
      .matches("[[attr(a)]]")
      .matches("[[attr(\"text\")]]")
      .matches("[[attr(true)]]")
      .matches("[[attr(int)]]")
      .matches("[[attr(a, b, c)]]")
      .matches("[[nmspc::attr]]")
      .matches("[[nmspc::attr(args)]]")
      .matches("[[attr1, attr2, attr3(args)]]")
      .matches("[[db::id, db::test, db::type(\"INT\")]]")
      .matches("[[omp::parallel(clause,clause)]]")
      .matches("[[noreturn]]")
      .matches("[[carries_dependency]]")
      .matches("[[deprecated]]")
      .matches("[[deprecated(\"reason\")]]")
      .matches("[[fallthrough]]")
      .matches("[[nodiscard]]")
      .matches("[[maybe_unused]]")
      .matches("[[attr1]] [[attr2]] [[attr3]]");
  }

}
