/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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

import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import static com.sonar.cxx.sslr.test.lexer.LexerConditions.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PPLexerTest {

  private final static Lexer LEXER = PPLexer.create();

  @Test
  void cpp_keywords() {
    assertThat(hasToken("#define", PPKeyword.DEFINE)
      .matches(LEXER.lex("#define"))).isTrue();
    assertThat(hasToken("#define", PPKeyword.DEFINE)
      .matches(LEXER.lex("#define"))).isTrue();
    assertThat(hasToken("#include", PPKeyword.INCLUDE)
      .matches(LEXER.lex("#include"))).isTrue();
  }

  @Test
  void cpp_keywords_with_whitespaces() {
    assertThat(hasToken("#define", PPKeyword.DEFINE)
      .matches(LEXER.lex("#  define"))).isTrue();
    assertThat(hasToken("#include", PPKeyword.INCLUDE)
      .matches(LEXER.lex("#\tinclude"))).isTrue();
  }

  @Test
  void cpp_keywords_indented() {
    assertThat(hasToken("#define", PPKeyword.DEFINE)
      .matches(LEXER.lex(" #define"))).isTrue();
    assertThat(hasToken("#define", PPKeyword.DEFINE)
      .matches(LEXER.lex("\t#define"))).isTrue();
  }

  @Test
  void cpp_identifiers() {
    assertThat(hasToken("lala", IDENTIFIER)
      .matches(LEXER.lex("lala"))).isTrue();
  }

  @Test
  void cpp_operators() {
    assertThat(hasToken("#", PPPunctuator.HASH)
      .matches(LEXER.lex("#"))).isTrue();
    assertThat(hasToken("##", PPPunctuator.HASHHASH)
      .matches(LEXER.lex("##"))).isTrue();
  }

  @Test
  void hashhash_followed_by_word() {
    List<Token> tokens = LEXER.lex("##a");
    assertThat(hasToken("##", PPPunctuator.HASHHASH)
      .matches(tokens)).isTrue();
    assertThat(hasToken("a", IDENTIFIER)
      .matches(tokens)).isTrue();
  }

  @Test
  void hash_followed_by_word() {
    List<Token> tokens = LEXER.lex("#a");
    assertThat(hasToken("#", PPPunctuator.HASH)
      .matches(tokens)).isTrue();
    assertThat(hasToken("a", IDENTIFIER)
      .matches(tokens)).isTrue();
  }

}
