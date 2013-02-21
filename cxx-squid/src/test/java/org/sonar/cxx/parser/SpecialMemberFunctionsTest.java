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

public class SpecialMemberFunctionsTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void ctorInitializer_reallife() {
    p.setRootRule(g.ctorInitializer);

    assertThat(p, parse(": theValue(v)"));
  }

  @Test
  public void memInitializerList() {
    p.setRootRule(g.memInitializerList);

    g.memInitializer.mock();

    assertThat(p, parse("memInitializer"));
    assertThat(p, parse("memInitializer ..."));
    assertThat(p, parse("memInitializer , memInitializer"));
    assertThat(p, parse("memInitializer , memInitializer ..."));
  }

  @Test
  public void memInitializer() {
    p.setRootRule(g.memInitializer);

    g.memInitializerId.mock();
    g.expressionList.mock();
    g.bracedInitList.mock();

    assertThat(p, parse("memInitializerId ( )"));
    assertThat(p, parse("memInitializerId ( expressionList )"));
    assertThat(p, parse("memInitializerId bracedInitList"));
  }

  @Test
  public void memInitializer_reallife() {
    p.setRootRule(g.memInitializer);

    assertThat(p, parse("theValue(v)"));
  }

  @Test
  public void memInitializerId_reallife() {
    p.setRootRule(g.memInitializerId);

    assertThat(p, parse("theValue"));
  }
}
