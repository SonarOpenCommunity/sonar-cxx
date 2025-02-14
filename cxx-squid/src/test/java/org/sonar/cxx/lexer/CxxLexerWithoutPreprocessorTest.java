/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.lexer;

import com.sonar.cxx.sslr.impl.Lexer;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.lexer.LexerAssert.assertThat;
import org.sonar.cxx.parser.CxxLexerPool;
import org.sonar.cxx.parser.CxxTokenType;

class CxxLexerWithoutPreprocessorTest {

  private Lexer lexer;

  @BeforeEach
  public void setUp() {
    lexer = CxxLexerPool.create().getLexer();
  }

  @Test
  void preprocessorDirectives() {
    var softly = new SoftAssertions();

    softly.assertThat(lexer.lex("#")).anySatisfy(token -> assertThat(token).isValue(
      "#").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#include <iostream>")).anySatisfy(token -> assertThat(token).isValue(
      "#include <iostream>").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("# include <iostream>")).anySatisfy(token -> assertThat(token).isValue(
      "# include <iostream>").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex(" # include <iostream>")).anySatisfy(token -> assertThat(token).isValue(
      "# include <iostream>").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("#define lala")).anySatisfy(token -> assertThat(token).isValue("#define lala").hasType(
      CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("# define lala")).anySatisfy(token -> assertThat(token).isValue("# define lala")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex(" # define lala")).anySatisfy(token -> assertThat(token).isValue("# define lala")
      .hasType(CxxTokenType.PREPROCESSOR));

    softly.assertThat(lexer.lex("#include <iostream>")).hasSize(2);
    softly.assertThat(lexer.lex("#define\\\ncontinued line")).hasSize(2);
    softly.assertThat(lexer.lex("#include <iostream>\n1")).anySatisfy(token -> assertThat(token).isValue("1").hasType(
      CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void preprocessorContinuedDefine() {
    assertThat(lexer.lex("""
      #define M\\
      0
      """)).anySatisfy(token -> assertThat(token).isValue("#define M0").hasType(
      CxxTokenType.PREPROCESSOR));
  }

  @Test
  void preprocessorDirectiveWithComment() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("""
      #define A1 B*/
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A1 B*/")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A2 B/*CCC*/
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A2 B ")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A3 B/**/C
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A3 B C")
      .hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A4 B/*C


      C*/D
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A4 B D").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A5 "a/*" B
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A5 \"a/*\" B").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A6 "-str/*"-/*CCC*/
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A6 \"-str/*\"- ").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A7 B/*-"str"-*/C
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A7 B C").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertThat(lexer.lex("""
      #define A8 B//-/*-"str"-*/
      """)).anySatisfy(token -> assertThat(token)
      .isValue("#define A8 B").hasType(CxxTokenType.PREPROCESSOR));
    softly.assertAll();
  }

}
