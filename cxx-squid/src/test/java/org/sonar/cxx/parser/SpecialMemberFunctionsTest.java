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

public class SpecialMemberFunctionsTest extends ParserBaseTestHelper {

  @Test
  public void ctorInitializer_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.ctorInitializer));

    assertThat(p).matches(": theValue(v)");
  }

  @Test
  public void memInitializerList() {
    p.setRootRule(g.rule(CxxGrammarImpl.memInitializerList));

    mockRule(CxxGrammarImpl.memInitializer);

    assertThat(p).matches("memInitializer");
    assertThat(p).matches("memInitializer ...");
    assertThat(p).matches("memInitializer , memInitializer");
    assertThat(p).matches("memInitializer , memInitializer ...");
  }

  @Test
  public void memInitializer() {
    p.setRootRule(g.rule(CxxGrammarImpl.memInitializer));

    mockRule(CxxGrammarImpl.memInitializerId);
    mockRule(CxxGrammarImpl.expressionList);
    mockRule(CxxGrammarImpl.bracedInitList);

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
