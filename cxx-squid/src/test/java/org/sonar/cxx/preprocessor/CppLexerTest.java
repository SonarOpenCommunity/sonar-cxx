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
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.junit.Test;
import org.sonar.cxx.api.CppKeyword;
import org.sonar.cxx.api.CppPunctuator;

import java.util.List;

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.junit.Assert.assertThat;

public class CppLexerTest {

  private static Lexer lexer = CppLexer.create();

  @Test
  public void cpp_keywords() {
    assertThat(lexer.lex("#define"), hasToken("#define", CppKeyword.DEFINE));
    assertThat(lexer.lex("#include"), hasToken("#include", CppKeyword.INCLUDE));
  }
  
  @Test
  public void cpp_keywords_with_whitespaces() {
    assertThat(lexer.lex("#  define"), hasToken("#define", CppKeyword.DEFINE));
    assertThat(lexer.lex("#\tinclude"), hasToken("#include", CppKeyword.INCLUDE));
  }

  @Test
  public void cpp_keywords_indented() {
    assertThat(lexer.lex(" #define"), hasToken("#define", CppKeyword.DEFINE));
    assertThat(lexer.lex("\t#define"), hasToken("#define", CppKeyword.DEFINE));
  }

  @Test
  public void cpp_identifiers() {
    assertThat(lexer.lex("lala"), hasToken("lala", IDENTIFIER));
  }

  @Test
  public void cpp_operators() {
    assertThat(lexer.lex("#"), hasToken("#", CppPunctuator.HASH));
    assertThat(lexer.lex("##"), hasToken("##", CppPunctuator.HASHHASH));
  }

  @Test
  public void hashhash_followed_by_word() {
    List<Token> tokens = lexer.lex("##a");
    assertThat(tokens, hasToken("##", CppPunctuator.HASHHASH));
    assertThat(tokens, hasToken("a", IDENTIFIER));
  }

  @Test
  public void hash_followed_by_word() {
    List<Token> tokens = lexer.lex("#a");
    assertThat(tokens, hasToken("#", CppPunctuator.HASH));
    assertThat(tokens, hasToken("a", IDENTIFIER));
  }
}
