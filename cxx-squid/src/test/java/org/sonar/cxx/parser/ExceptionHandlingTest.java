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
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ExceptionHandlingTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void exceptionDeclaration() {
    p.setRootRule(g.exceptionDeclaration);

    g.typeSpecifierSeq.mock();
    g.declarator.mock();
    g.attributeSpecifierSeq.mock();
    g.abstractDeclarator.mock();

    assertThat(p, parse("..."));

    assertThat(p, parse("typeSpecifierSeq declarator"));
    assertThat(p, parse("attributeSpecifierSeq typeSpecifierSeq declarator"));

    assertThat(p, parse("typeSpecifierSeq"));
    assertThat(p, parse("attributeSpecifierSeq typeSpecifierSeq abstractDeclarator"));
  }

  @Test
  public void exceptionSpecification_reallife() {
    p.setRootRule(g.exceptionSpecification);

    assertThat(p, parse("throw()"));
  }

  @Test
  public void typeIdList() {
    p.setRootRule(g.typeIdList);

    g.typeId.mock();

    assertThat(p, parse("typeId"));
    assertThat(p, parse("typeId ..."));
    assertThat(p, parse("typeId , typeId"));
    assertThat(p, parse("typeId , typeId ..."));
  }

  @Test
  public void noexceptSpecification() {
    p.setRootRule(g.noexceptSpecification);

    g.constantExpression.mock();

    assertThat(p, parse("noexcept"));
    assertThat(p, parse("noexcept ( constantExpression )"));
  }
}
