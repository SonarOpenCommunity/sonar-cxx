/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
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
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import static org.mockito.Mockito.mock;

public class ExceptionHandlingTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void exception_declaration() {
    p.setRootRule(g.exception_declaration);

    g.type_specifier_seq.mock();
    g.declarator.mock();
    g.attribute_specifier_seq.mock();
    g.abstract_declarator.mock();

    assertThat(p, parse("..."));

    assertThat(p, parse("type_specifier_seq declarator"));
    assertThat(p, parse("attribute_specifier_seq type_specifier_seq declarator"));

    assertThat(p, parse("type_specifier_seq"));
    assertThat(p, parse("attribute_specifier_seq type_specifier_seq abstract_declarator"));
  }

  @Test
  public void exception_specification_reallife() {
    p.setRootRule(g.exception_specification);

    assertThat(p, parse("throw()"));
  }

  @Test
  public void type_id_list() {
    p.setRootRule(g.type_id_list);

    g.type_id.mock();

    assertThat(p, parse("type_id"));
    assertThat(p, parse("type_id ..."));
    assertThat(p, parse("type_id , type_id"));
    assertThat(p, parse("type_id , type_id ..."));
  }

  @Test
  public void noexcept_specification() {
    p.setRootRule(g.noexcept_specification);

    g.constant_expression.mock();

    assertThat(p, parse("noexcept"));
    assertThat(p, parse("noexcept ( constant_expression )"));
  }
}
