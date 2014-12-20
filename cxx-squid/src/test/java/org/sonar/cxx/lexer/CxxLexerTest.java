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
package org.sonar.cxx.lexer;

import static com.sonar.sslr.test.lexer.LexerMatchers.hasComment;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.squidbridge.SquidAstVisitorContext;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.impl.Lexer;

public class CxxLexerTest {

  private static Lexer lexer;

  @BeforeClass
  public static void init() {
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class));
    lexer = CxxLexer.create(cxxpp, new JoinStringsPreprocessor());
  }

  /**
   * C++ Standard, Section 2.8 "Comments"
   */
  @Test
  public void comments_cxx() {
    assertThat("comment c++: empty", lexer.lex("//\n new line"), hasComment("//"));
    assertThat("comment c++: simple", lexer.lex("// My comment \n new line"), hasComment("// My comment "));
    assertThat("comment c++: nested", lexer.lex("// // \n new line"), hasComment("// // "));
    assertThat("comment c++: nested2", lexer.lex("// /**/ \n new line"), hasComment("// /**/ "));
  }

  /**
   * C++ Standard, Section 2.8 "Comments"
   */
  @Test
  public void comments_c() {
    assertThat("comment c: empty", lexer.lex("/**/"), hasComment("/**/"));
    assertThat("comment c: simple", lexer.lex("/* My comment */"), hasComment("/* My comment */"));
    assertThat("comment c: with newline", lexer.lex("/*\\\n*/"), hasComment("/*\\\n*/"));
    assertThat("comment c: nested", lexer.lex("/*//*/"), hasComment("/*//*/"));
    assertThat("comment c: nested2", lexer.lex("/* /* */"), hasComment("/* /* */"));
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  public void decimal_integer_literals() {
    // Decimal integer
    assertThat(lexer.lex("0"), hasToken("0", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7"), hasToken("7", CxxTokenType.NUMBER));

    // With "UnsignedSuffix"
    assertThat(lexer.lex("7u"), hasToken("7u", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7U"), hasToken("7U", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongSuffix"
    assertThat(lexer.lex("7ul"), hasToken("7ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7uL"), hasToken("7uL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7Ul"), hasToken("7Ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7UL"), hasToken("7UL", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongLongSuffix"
    assertThat(lexer.lex("7ull"), hasToken("7ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7uLL"), hasToken("7uLL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7Ull"), hasToken("7Ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7ULL"), hasToken("7ULL", CxxTokenType.NUMBER));

    // With "LongSuffix"
    assertThat(lexer.lex("7l"), hasToken("7l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7L"), hasToken("7L", CxxTokenType.NUMBER));

    // With "LongSuffix UnsignedSuffix"
    assertThat(lexer.lex("7lu"), hasToken("7lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7lU"), hasToken("7lU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7Lu"), hasToken("7Lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7LU"), hasToken("7LU", CxxTokenType.NUMBER));

    // With "LongLongSuffix"
    assertThat(lexer.lex("7ll"), hasToken("7ll", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7LL"), hasToken("7LL", CxxTokenType.NUMBER));

    // With "LongLongSuffix UnsignedSuffix"
    assertThat(lexer.lex("7llu"), hasToken("7llu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7llU"), hasToken("7llU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7LLu"), hasToken("7LLu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("7LLU"), hasToken("7LLU", CxxTokenType.NUMBER));
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  public void octal_integer_literals() {
    // Octal integer
    assertThat(lexer.lex("07"), hasToken("07", CxxTokenType.NUMBER));

    // With "UnsignedSuffix"
    assertThat(lexer.lex("07u"), hasToken("07u", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07U"), hasToken("07U", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongSuffix"
    assertThat(lexer.lex("07ul"), hasToken("07ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07uL"), hasToken("07uL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07Ul"), hasToken("07Ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07UL"), hasToken("07UL", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongLongSuffix"
    assertThat(lexer.lex("07ull"), hasToken("07ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07uLL"), hasToken("07uLL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07Ull"), hasToken("07Ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07ULL"), hasToken("07ULL", CxxTokenType.NUMBER));

    // With "LongSuffix"
    assertThat(lexer.lex("07l"), hasToken("07l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07L"), hasToken("07L", CxxTokenType.NUMBER));

    // With "LongSuffix UnsignedSuffix"
    assertThat(lexer.lex("07lu"), hasToken("07lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07lU"), hasToken("07lU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07Lu"), hasToken("07Lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07LU"), hasToken("07LU", CxxTokenType.NUMBER));

    // With "LongLongSuffix"
    assertThat(lexer.lex("07ll"), hasToken("07ll", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07LL"), hasToken("07LL", CxxTokenType.NUMBER));

    // With "LongLongSuffix UnsignedSuffix"
    assertThat(lexer.lex("07llu"), hasToken("07llu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07llU"), hasToken("07llU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07LLu"), hasToken("07LLu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("07LLU"), hasToken("07LLU", CxxTokenType.NUMBER));
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  public void hex_integer_literals() {
    // Hex integer
    assertThat(lexer.lex("0x7"), hasToken("0x7", CxxTokenType.NUMBER));

    // With "UnsignedSuffix"
    assertThat(lexer.lex("0x7u"), hasToken("0x7u", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7U"), hasToken("0x7U", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongSuffix"
    assertThat(lexer.lex("0x7ul"), hasToken("0x7ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7uL"), hasToken("0x7uL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7Ul"), hasToken("0x7Ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7UL"), hasToken("0x7UL", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongLongSuffix"
    assertThat(lexer.lex("0x7ull"), hasToken("0x7ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7uLL"), hasToken("0x7uLL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7Ull"), hasToken("0x7Ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7ULL"), hasToken("0x7ULL", CxxTokenType.NUMBER));

    // With "LongSuffix"
    assertThat(lexer.lex("0x7l"), hasToken("0x7l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7L"), hasToken("0x7L", CxxTokenType.NUMBER));

    // With "LongSuffix UnsignedSuffix"
    assertThat(lexer.lex("0x7lu"), hasToken("0x7lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7lU"), hasToken("0x7lU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7Lu"), hasToken("0x7Lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7LU"), hasToken("0x7LU", CxxTokenType.NUMBER));

    // With "LongLongSuffix"
    assertThat(lexer.lex("0x7ll"), hasToken("0x7ll", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7LL"), hasToken("0x7LL", CxxTokenType.NUMBER));

    // With "LongLongSuffix UnsignedSuffix"
    assertThat(lexer.lex("0x7llu"), hasToken("0x7llu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7llU"), hasToken("0x7llU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7LLu"), hasToken("0x7LLu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0x7LLU"), hasToken("0x7LLU", CxxTokenType.NUMBER));
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  public void hex_integer_literals_bigX() {
    // Hex integer (big X)
    assertThat(lexer.lex("0X7"), hasToken("0X7", CxxTokenType.NUMBER));

    // With "UnsignedSuffix"
    assertThat(lexer.lex("0X7u"), hasToken("0X7u", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7U"), hasToken("0X7U", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongSuffix"
    assertThat(lexer.lex("0X7ul"), hasToken("0X7ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7uL"), hasToken("0X7uL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7Ul"), hasToken("0X7Ul", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7UL"), hasToken("0X7UL", CxxTokenType.NUMBER));

    // With "UnsignedSuffix LongLongSuffix"
    assertThat(lexer.lex("0X7ull"), hasToken("0X7ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7uLL"), hasToken("0X7uLL", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7Ull"), hasToken("0X7Ull", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7ULL"), hasToken("0X7ULL", CxxTokenType.NUMBER));

    // With "LongSuffix"
    assertThat(lexer.lex("0X7l"), hasToken("0X7l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7L"), hasToken("0X7L", CxxTokenType.NUMBER));

    // With "LongSuffix UnsignedSuffix"
    assertThat(lexer.lex("0X7lu"), hasToken("0X7lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7lU"), hasToken("0X7lU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7Lu"), hasToken("0X7Lu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7LU"), hasToken("0X7LU", CxxTokenType.NUMBER));

    // With "LongLongSuffix"
    assertThat(lexer.lex("0X7ll"), hasToken("0X7ll", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7LL"), hasToken("0X7LL", CxxTokenType.NUMBER));

    // With "LongLongSuffix UnsignedSuffix"
    assertThat(lexer.lex("0X7llu"), hasToken("0X7llu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7llU"), hasToken("0X7llU", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7LLu"), hasToken("0X7LLu", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0X7LLU"), hasToken("0X7LLU", CxxTokenType.NUMBER));
  }

  /**
   * C++ Standard, Section 2.14.4 "Floating literals"
   */
  @Test
  public void floating_point_literals() {
    assertThat(lexer.lex("3.14"), hasToken("3.14", CxxTokenType.NUMBER));
    assertThat(lexer.lex("10."), hasToken("10.", CxxTokenType.NUMBER));
    assertThat(lexer.lex(".001"), hasToken(".001", CxxTokenType.NUMBER));
    assertThat(lexer.lex("1e100"), hasToken("1e100", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14e-10"), hasToken("3.14e-10", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14E-10"), hasToken("3.14E-10", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0e0"), hasToken("0e0", CxxTokenType.NUMBER));

    assertThat(lexer.lex("3.14f"), hasToken("3.14f", CxxTokenType.NUMBER));
    assertThat(lexer.lex("10.f"), hasToken("10.f", CxxTokenType.NUMBER));
    assertThat(lexer.lex(".001f"), hasToken(".001f", CxxTokenType.NUMBER));
    assertThat(lexer.lex("1e100f"), hasToken("1e100f", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14e-10f"), hasToken("3.14e-10f", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14E-10f"), hasToken("3.14E-10f", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0e0f"), hasToken("0e0f", CxxTokenType.NUMBER));

    assertThat(lexer.lex("3.14F"), hasToken("3.14F", CxxTokenType.NUMBER));
    assertThat(lexer.lex("10.F"), hasToken("10.F", CxxTokenType.NUMBER));
    assertThat(lexer.lex(".001F"), hasToken(".001F", CxxTokenType.NUMBER));
    assertThat(lexer.lex("1e100F"), hasToken("1e100F", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14e-10F"), hasToken("3.14e-10F", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14E-10F"), hasToken("3.14E-10F", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0e0F"), hasToken("0e0F", CxxTokenType.NUMBER));

    assertThat(lexer.lex("3.14l"), hasToken("3.14l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("10.l"), hasToken("10.l", CxxTokenType.NUMBER));
    assertThat(lexer.lex(".001l"), hasToken(".001l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("1e100l"), hasToken("1e100l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14e-10l"), hasToken("3.14e-10l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14E-10l"), hasToken("3.14E-10l", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0e0l"), hasToken("0e0l", CxxTokenType.NUMBER));

    assertThat(lexer.lex("3.14L"), hasToken("3.14L", CxxTokenType.NUMBER));
    assertThat(lexer.lex("10.L"), hasToken("10.L", CxxTokenType.NUMBER));
    assertThat(lexer.lex(".001L"), hasToken(".001L", CxxTokenType.NUMBER));
    assertThat(lexer.lex("1e100L"), hasToken("1e100L", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14e-10L"), hasToken("3.14e-10L", CxxTokenType.NUMBER));
    assertThat(lexer.lex("3.14E-10L"), hasToken("3.14E-10L", CxxTokenType.NUMBER));
    assertThat(lexer.lex("0e0L"), hasToken("0e0L", CxxTokenType.NUMBER));
  }

  /**
   * C++ Standard, Section 2.14.6 "Boolean literals"
   */
  @Test
  public void boolean_literals() {
    assertThat(lexer.lex("true"), hasToken("true", CxxKeyword.TRUE));
    assertThat(lexer.lex("false"), hasToken("false", CxxKeyword.FALSE));
  }

  /**
   * C++ Standard, Section 2.14.7 "Pointer literals"
   */
  @Test
  public void pointer_literals() {
    assertThat(lexer.lex("nullptr"), hasToken("nullptr", CxxTokenType.NUMBER));
  }

  /**
   * C++ Standard, Section 2.14.3 "Character literals"
   */
  @Test
  public void character_literals() {
    assertThat("char: empty", lexer.lex("''"), hasToken("''", CxxTokenType.CHARACTER));
    assertThat("char: prefix u", lexer.lex("u''"), hasToken("u''", CxxTokenType.CHARACTER));
    assertThat("char: prefix U", lexer.lex("U''"), hasToken("U''", CxxTokenType.CHARACTER));
    assertThat("char: prefix L", lexer.lex("L''"), hasToken("L''", CxxTokenType.CHARACTER));

    assertThat("char: trivial", lexer.lex("'a'"), hasToken("'a'", CxxTokenType.CHARACTER));
    assertThat("char: more than one", lexer.lex("'ab'"), hasToken("'ab'", CxxTokenType.CHARACTER));

    assertThat("char: escaped quote", lexer.lex("'\\''"), hasToken("'\\''", CxxTokenType.CHARACTER));
    assertThat("char: escaped backslash", lexer.lex("'\\\\'"), hasToken("'\\\\'", CxxTokenType.CHARACTER));

    assertThat("char: unterminated", lexer.lex("'"), hasToken("'", GenericTokenType.UNKNOWN_CHAR));
    assertThat("char: unescaped backslash", lexer.lex("'\\'"), hasToken("'", GenericTokenType.UNKNOWN_CHAR));
  }

  /**
   * C++ Standard, Section 2.14.5 "String literals"
   */
  @Test
  public void string_literals() {
    assertThat("string: empty", lexer.lex("\"\""), hasToken("\"\"", CxxTokenType.STRING));
    assertThat("string: prefix u", lexer.lex("u\"\""), hasToken("u\"\"", CxxTokenType.STRING));
    assertThat("string: prefix u8", lexer.lex("u8\"\""), hasToken("u8\"\"", CxxTokenType.STRING));
    assertThat("string: prefix U", lexer.lex("U\"\""), hasToken("U\"\"", CxxTokenType.STRING));
    assertThat("string: prefix L", lexer.lex("L\"\""), hasToken("L\"\"", CxxTokenType.STRING));

    assertThat("string: trivial", lexer.lex("\"a\""), hasToken("\"a\"", CxxTokenType.STRING));

    assertThat("string: escaped backslash", lexer.lex("\" \\\\ \""), hasToken("\" \\\\ \"", CxxTokenType.STRING));
    assertThat("string: escaped quote", lexer.lex("\" \\\" \""), hasToken("\" \\\" \"", CxxTokenType.STRING));

    assertThat("string: unterminated", lexer.lex("\""), hasToken("\"", GenericTokenType.UNKNOWN_CHAR));
    assertThat("string: unescaped backslash", lexer.lex("\"\\\""), hasToken("\\", GenericTokenType.UNKNOWN_CHAR));
  }

  @Test
  public void rawstring_literals() {
    assertThat("raw string: empty", lexer.lex("R\"\""), hasToken("R\"\"", CxxTokenType.STRING));
    assertThat("raw string: prefix u", lexer.lex("uR\"\""), hasToken("uR\"\"", CxxTokenType.STRING));
    assertThat("raw string: prefix u8R", lexer.lex("u8R\"\""), hasToken("u8R\"\"", CxxTokenType.STRING));
    assertThat("raw string: prefix UR", lexer.lex("UR\"\""), hasToken("UR\"\"", CxxTokenType.STRING));
    assertThat("raw string: prefix LR", lexer.lex("LR\"\""), hasToken("LR\"\"", CxxTokenType.STRING));

    // examples from the standard
    assertThat("raw string: std example 1", lexer.lex("R\"(...)\""), hasToken("R\"(...)\"", CxxTokenType.STRING));
    assertThat("raw string: std example 2", lexer.lex("u8R\"**(...)**\""), hasToken("u8R\"**(...)**\"", CxxTokenType.STRING));
    assertThat("raw string: std example 3", lexer.lex("uR\"*∼(...)*∼\""), hasToken("uR\"*∼(...)*∼\"", CxxTokenType.STRING));
    assertThat("raw string: std example 4", lexer.lex("UR\"zzz(...)zzz\""), hasToken("UR\"zzz(...)zzz\"", CxxTokenType.STRING));
    assertThat("raw string: std example 5", lexer.lex("LR\"(...)\""), hasToken("LR\"(...)\"", CxxTokenType.STRING));
  }

  @Test
  public void operators_and_delimiters() {
    assertThat(lexer.lex(":"), hasToken(":", CxxPunctuator.COLON));
    assertThat(lexer.lex("="), hasToken("=", CxxPunctuator.ASSIGN));
    assertThat(lexer.lex("~"), hasToken("~", CxxPunctuator.BW_NOT));
  }

  @Test
  public void keywords_and_identifiers() {
    assertThat(lexer.lex("return"), hasToken("return", CxxKeyword.RETURN));
    assertThat(lexer.lex("identifier"), hasToken("identifier", GenericTokenType.IDENTIFIER));
    assertThat(lexer.lex("a1"), hasToken("a1", GenericTokenType.IDENTIFIER));
    assertThat(lexer.lex("A1"), hasToken("A1", GenericTokenType.IDENTIFIER));
    assertThat(lexer.lex("A_a_A_1"), hasToken("A_a_A_1", GenericTokenType.IDENTIFIER));

    assertThat("identifier: containing boolean constant", lexer.lex("truetype"), hasToken("truetype", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void blank_lines() {
    assertThat(lexer.lex("    // comment\n")).hasSize(1);
    assertThat(lexer.lex("    \n")).hasSize(1);
    assertThat(lexer.lex("    ")).hasSize(1);
    assertThat(lexer.lex("line\n\n")).hasSize(2);
  }
}
