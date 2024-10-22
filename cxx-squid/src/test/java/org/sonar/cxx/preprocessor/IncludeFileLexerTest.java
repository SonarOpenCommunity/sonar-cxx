/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import static com.sonar.cxx.sslr.api.GenericTokenType.EOF;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import static com.sonar.cxx.sslr.test.lexer.LexerConditions.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.parser.CxxTokenType;

class IncludeFileLexerTest {

  private final static Lexer LEXER = IncludeFileLexer.create();

  @Test
  void properPreprocessorDirectivesAreCreated() {
    assertThat(hasToken("#include <iostream>", CxxTokenType.PREPROCESSOR)
      .matches(LEXER.lex("#include <iostream>"))).isTrue();
    assertThat(hasToken("#define lala", CxxTokenType.PREPROCESSOR)
      .matches(LEXER.lex("#define lala"))).isTrue();
    assertThat(hasToken("#ifdef lala", CxxTokenType.PREPROCESSOR)
      .matches(LEXER.lex("#ifdef lala"))).isTrue();
  }

  @Test
  void continuedLinesAreHandledCorrectly() {
    List<Token> tokens = LEXER.lex("""
                                   #define \\
                                   name \\
                                   10
                                   """);
    assertThat(hasToken("#define name 10", CxxTokenType.PREPROCESSOR)
      .matches(tokens)).isTrue();
    assertThat(tokens).hasSize(2);
  }

  @Test
  void multilineCommentWithIncludeIsSwallowed() {
    List<Token> tokens = LEXER.lex("""
                                   /* This is a multiline comment
                                      #include should be swallowed
                                    */
                                   """);
    assertThat(tokens).hasSize(1);
    assertThat(hasToken("EOF", EOF)
      .matches(tokens)).isTrue();
  }

  @Test
  void singlelineCommentWithIncludeIsSwallowed() {
    List<Token> tokens = LEXER.lex("// #include should be swallowed\n");
    assertThat(tokens).hasSize(1);
    assertThat(hasToken("EOF", EOF)
      .matches(tokens)).isTrue();
  }

  @Test
  void allButPreprocessorStuffIsSwallowed() {
    // all the other stuff should be consumed by the lexer without
    // generating any tokens
    List<Token> tokens = LEXER.lex("void foo();");
    assertThat(tokens).hasSize(1);
    assertThat(hasToken("EOF", EOF)
      .matches(tokens)).isTrue();
  }

}
