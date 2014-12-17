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
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.junit.Test;
import com.sonar.sslr.api.Grammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

import static org.mockito.Mockito.mock;

public class SpecialMemberFunctionsTest extends ParserBaseTest {

  @Test
  public void ctorInitializer_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.ctorInitializer));

    assertThat(p).matches(": theValue(v)");
  }

  @Test
  public void memInitializerList() {
    p.setRootRule(g.rule(CxxGrammarImpl.memInitializerList));

    g.rule(CxxGrammarImpl.memInitializer).mock();

    assertThat(p).matches("memInitializer");
    assertThat(p).matches("memInitializer ...");
    assertThat(p).matches("memInitializer , memInitializer");
    assertThat(p).matches("memInitializer , memInitializer ...");
  }

  @Test
  public void memInitializer() {
    p.setRootRule(g.rule(CxxGrammarImpl.memInitializer));

    g.rule(CxxGrammarImpl.memInitializerId).mock();
    g.rule(CxxGrammarImpl.expressionList).mock();
    g.rule(CxxGrammarImpl.bracedInitList).mock();

    assertThat(p).matches("memInitializerId ( )");
    assertThat(p).matches("memInitializerId ( expressionList )");
    assertThat(p).matches("memInitializerId bracedInitList");
  }

  @Test
  public void memInitializer_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.memInitializer));

    assertThat(p).matches("theValue(v)");
  }

  @Test
  public void memInitializerId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.memInitializerId));

    assertThat(p).matches("theValue");
  }
}
