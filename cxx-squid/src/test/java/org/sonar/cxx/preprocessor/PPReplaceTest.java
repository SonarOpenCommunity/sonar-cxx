/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class PPReplaceTest {

  private PPReplace replace;
  private Lexer lexer;
  private CxxPreprocessor pp;

  @BeforeEach
  void setUp() {
    var context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(new File("dummy")); // necessary for pp init
    pp = spy(new CxxPreprocessor(context));
    pp.init();
    lexer = PPLexer.create();
    replace = new PPReplace(pp);
  }

  @Test
  void testReplaceObjectLikeMacro() {
    PPMacro macro = pp.parseMacroDefinition("#define DUMMY"); // only necessary to call replaceObjectLikeMacro
    List<Token> result = replace.replaceObjectLikeMacro(macro, "__LINE__");
    assertThat(result)
      .hasSize(1)
      .matches(t -> "1".equals(t.get(0).getValue()));
  }

  @Test
  void testReplaceFunctionLikeMacro() {
    List<Token> args = lexer.lex("(1, 2)");
    PPMacro macro = pp.parseMacroDefinition("#define TEST(a, b) a+b");

    var result = new ArrayList<Token>();
    int num = replace.replaceFunctionLikeMacro(macro, args, result);
    assertThat(result)
      .hasSize(3)
      .matches(t -> "1".equals(t.get(0).getValue()))
      .matches(t -> "+".equals(t.get(1).getValue()))
      .matches(t -> "2".equals(t.get(2).getValue()));
  }

}
