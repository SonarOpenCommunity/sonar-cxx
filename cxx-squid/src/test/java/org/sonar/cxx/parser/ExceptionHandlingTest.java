/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.parser;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import com.sonar.sslr.api.Grammar;

import static org.sonar.sslr.tests.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ExceptionHandlingTest {

  Parser<Grammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  Grammar g = p.getGrammar();

  @Test
  public void exceptionDeclaration() {
    p .setRootRule(g.rule(CxxGrammarImpl.exceptionDeclaration));

    g.rule(CxxGrammarImpl.typeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.abstractDeclarator).mock();

    assertThat(p).matches("...");

    assertThat(p).matches("typeSpecifierSeq declarator");
    assertThat(p).matches("attributeSpecifierSeq typeSpecifierSeq declarator");

    assertThat(p).matches("typeSpecifierSeq");
    assertThat(p).matches("attributeSpecifierSeq typeSpecifierSeq abstractDeclarator");
  }

  @Test
  public void exceptionSpecification_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.exceptionSpecification));

    assertThat(p).matches("throw()");
  }

  @Test
  public void typeIdList() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeIdList));

    g.rule(CxxGrammarImpl.typeId).mock();

    assertThat(p).matches("typeId");
    assertThat(p).matches("typeId ...");
    assertThat(p).matches("typeId , typeId");
    assertThat(p).matches("typeId , typeId ...");
  }

  @Test
  public void noexceptSpecification() {
    p.setRootRule(g.rule(CxxGrammarImpl.noexceptSpecification));

    g.rule(CxxGrammarImpl.constantExpression).mock();

    assertThat(p).matches("noexcept");
    assertThat(p).matches("noexcept ( constantExpression )");
  }
}
