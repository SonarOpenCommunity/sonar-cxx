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

public class PreprocessorDirectivesTest {

  Parser<CxxGrammar> p = CxxParser.create();
  CxxGrammar g = p.getGrammar();

  @Test
  public void preprocessor_directives() {
    assertThat(p, parse("#define IDX 10\n"
      + "array[IDX];"));

    assertThat(p, parse("#define CHECKCODE(candidate,arrayofvalues,truestatement,falsestatement)\\\n"
      + "{\\\n"
      + "  const char **p;\\\n"
      + "  if ((candidate))\\\n"
      + "    for (p=(arrayofvalues);*p;p++)\\\n"
      + "      if (!strcasecmp((candidate), *p))\\\n"
      + "	truestatement;\\\n"
      + "  falsestatement;\\\n"
      + "}"));
  }

  // @Test
  // public void conditional_compilation() {
  //   assertThat(p, parse("#ifdef LALA\n"
  //                       +"void foo(){\n"
  //                       +"#else\n"
  //                       +"void bar(){\n"
  //                       +"#endif\n"
  //                       +"}"));
  // }
  
}
