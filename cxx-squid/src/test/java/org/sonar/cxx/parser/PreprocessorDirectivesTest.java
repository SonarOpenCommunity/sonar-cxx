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

public class PreprocessorDirectivesTest {

  Parser<Grammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  Grammar g = p.getGrammar();

  @Test
  public void preprocessorDirectives() {
    assertThat(p).matches("#define IDX 10\n"
                          + "array[IDX];");
  }

  //@Test
  public void hashhash_related_parsing_problem() {
    // this reproduces a macros expansion problem where
    // necessary whitespaces get lost

    assertThat(p).matches("#define CASES CASE(00)\n"
                        + "#define CASE(n) case 0x##n:\n"
                        + "void foo()  {\n"
                        + "switch (1) {\n"
                        + "CASES\n"
                        + "break;\n"
                        + "}\n"
                        + "}\n");
  }
}
