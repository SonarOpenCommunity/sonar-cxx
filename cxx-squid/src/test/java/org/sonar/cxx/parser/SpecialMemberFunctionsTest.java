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
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class SpecialMemberFunctionsTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void ctor_initializer_reallife() {
    p.setRootRule(g.ctor_initializer);

    assertThat(p, parse(": theValue(v)"));
  }

  @Test
  public void mem_initializer_list() {
    p.setRootRule(g.mem_initializer_list);

    g.mem_initializer.mock();

    assertThat(p, parse("mem_initializer"));
    assertThat(p, parse("mem_initializer ..."));
    assertThat(p, parse("mem_initializer , mem_initializer"));
    assertThat(p, parse("mem_initializer , mem_initializer ..."));
  }

  @Test
  public void mem_initializer() {
    p.setRootRule(g.mem_initializer);

    g.mem_initializer_id.mock();
    g.expression_list.mock();
    g.braced_init_list.mock();

    assertThat(p, parse("mem_initializer_id ( )"));
    assertThat(p, parse("mem_initializer_id ( expression_list )"));
    assertThat(p, parse("mem_initializer_id braced_init_list"));
  }

  @Test
  public void mem_initializer_reallife() {
    p.setRootRule(g.mem_initializer);

    assertThat(p, parse("theValue(v)"));
  }

  @Test
  public void mem_initializer_id_reallife() {
    p.setRootRule(g.mem_initializer_id);

    assertThat(p, parse("theValue"));
  }
}
