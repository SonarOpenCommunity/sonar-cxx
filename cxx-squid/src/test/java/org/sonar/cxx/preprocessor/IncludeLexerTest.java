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
package org.sonar.cxx.preprocessor;

import static com.sonar.sslr.api.GenericTokenType.EOF;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.sonar.cxx.api.CxxTokenType;

public class IncludeLexerTest {

  private final static Lexer lexer = IncludeLexer.create();

  @Test
  public void proper_preprocessor_directives_are_created() {
    assertThat(lexer.lex("#include <iostream>"), hasToken("#include <iostream>", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("#define lala"), hasToken("#define lala", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("#ifdef lala"), hasToken("#ifdef lala", CxxTokenType.PREPROCESSOR));
  }

  @Test
  public void continued_lines_are_handled_correctly() {
    List<Token> tokens = lexer.lex("#define\\\nname");
    assertThat(tokens, hasToken("#define name", CxxTokenType.PREPROCESSOR));
    assertThat(tokens).hasSize(2);
  }

  @Test
  public void multiline_comment_with_Include_is_swallowed() {
    List<Token> tokens = lexer.lex("/* This is a multiline comment\n   #include should be swallowed\n */");
    assertThat(tokens).hasSize(1);
    assertThat(tokens, hasToken("EOF", EOF));
  }

  @Test
  public void singleline_comment_with_Include_is_swallowed() {
    List<Token> tokens = lexer.lex("// #include should be swallowed\n");
    assertThat(tokens).hasSize(1);
    assertThat(tokens, hasToken("EOF", EOF));
  }

  @Test
  public void all_but_preprocessor_stuff_is_swallowed() {
    // all the other stuff should be consumed by the lexer without
    // generating any tokens
    List<Token> tokens = lexer.lex("void foo();");
    assertThat(tokens).hasSize(1);
    assertThat(tokens, hasToken("EOF", EOF));
  }
}
