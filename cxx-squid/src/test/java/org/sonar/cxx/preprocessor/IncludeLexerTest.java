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

import static com.sonar.sslr.api.GenericTokenType.EOF;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.cxx.parser.CxxTokenType;

public class IncludeLexerTest {

  private final static Lexer LEXER = IncludeLexer.create();

  @Test
  public void proper_preprocessor_directives_are_created() {
    assertThat(hasToken("#include <iostream>", CxxTokenType.PREPROCESSOR)
      .matches(LEXER.lex("#include <iostream>"))).isTrue();
    assertThat(hasToken("#define lala", CxxTokenType.PREPROCESSOR)
      .matches(LEXER.lex("#define lala"))).isTrue();
    assertThat(hasToken("#ifdef lala", CxxTokenType.PREPROCESSOR)
      .matches(LEXER.lex("#ifdef lala"))).isTrue();
  }

  @Test
  public void continued_lines_are_handled_correctly() {
    List<Token> tokens = LEXER.lex("#define\\\nname");
    assertThat(hasToken("#define name", CxxTokenType.PREPROCESSOR)
      .matches(tokens)).isTrue();
    assertThat(tokens).hasSize(2);
  }

  @Test
  public void multiline_comment_with_Include_is_swallowed() {
    List<Token> tokens = LEXER.lex("/* This is a multiline comment\n   #include should be swallowed\n */");
    assertThat(tokens).hasSize(1);
    assertThat(hasToken("EOF", EOF)
      .matches(tokens)).isTrue();
  }

  @Test
  public void singleline_comment_with_Include_is_swallowed() {
    List<Token> tokens = LEXER.lex("// #include should be swallowed\n");
    assertThat(tokens).hasSize(1);
    assertThat(hasToken("EOF", EOF)
      .matches(tokens)).isTrue();
  }

  @Test
  public void all_but_preprocessor_stuff_is_swallowed() {
    // all the other stuff should be consumed by the lexer without
    // generating any tokens
    List<Token> tokens = LEXER.lex("void foo();");
    assertThat(tokens).hasSize(1);
    assertThat(hasToken("EOF", EOF)
      .matches(tokens)).isTrue();
  }

}
