/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import org.junit.Test;
import static org.sonar.sslr.tests.Assertions.assertThat;

public class ExceptionHandlingTest extends ParserBaseTestHelper {

  @Test
  public void exceptionDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.exceptionDeclaration));

    mockRule(CxxGrammarImpl.typeSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThat(p).matches("...");

    assertThat(p).matches("typeSpecifierSeq declarator");
    assertThat(p).matches("attributeSpecifierSeq typeSpecifierSeq declarator");

    assertThat(p).matches("typeSpecifierSeq");
    assertThat(p).matches("attributeSpecifierSeq typeSpecifierSeq abstractDeclarator");
  }

  @Test
  public void exceptionSpecification_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.noexceptSpecifier));

    assertThat(p).matches("throw()");
    assertThat(p).matches("throw(...)");
  }

  @Test
  public void typeIdList() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeIdList));

    mockRule(CxxGrammarImpl.typeId);

    assertThat(p).matches("typeId");
    assertThat(p).matches("typeId ...");
    assertThat(p).matches("typeId , typeId");
    assertThat(p).matches("typeId , typeId ...");
    assertThat(p).matches("...");
  }

  @Test
  public void noexceptSpecification() {
    p.setRootRule(g.rule(CxxGrammarImpl.noexceptSpecifier));

    mockRule(CxxGrammarImpl.constantExpression);

    assertThat(p).matches("noexcept");
    assertThat(p).matches("noexcept ( constantExpression )");
  }
}
