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

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Lexer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.cxx.lexer.LexerAssert.assertThat;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxLexerPool;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class CxxLexerTest {

  private Lexer lexer;

  @BeforeEach
  public void setUp() {
    var file = new File("snippet.cpp").getAbsoluteFile();
    SquidAstVisitorContext<Grammar> context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    var cxxpp = new CxxPreprocessor(context);
    lexer = CxxLexerPool.create(cxxpp, new JoinStringsPreprocessor()).getLexer();
  }

  /**
   * C++ Standard, line splicing
   */
  @Test
  void lineSplicing() {
    var softly = new SoftAssertions();

    softly.assertThat(lexer.lex(""
    )).as("empty file").allSatisfy(token
      -> assertThat(token).isValue("EOF").isLine(1));
    softly.assertThat(lexer.lex("\\\n"
    )).as("empty file with line splicing").allSatisfy(token
      -> assertThat(token).isValue("EOF").isLine(2));
    softly.assertThat(lexer.lex("\\   \t   \n"
    )).as("empty file with line splicing and whitespaces").allSatisfy(token
      -> assertThat(token).isValue("EOF").isLine(2));

    softly.assertThat(lexer.lex("""
                                //a\\
                                b
                                """)).as("comment c++: line splicing").anySatisfy(token
      -> assertThat(token).hasTrivia().isTrivia("//ab").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("""
                                /\\
                                /ab
                                """)).as("comment c++: line splicing").anySatisfy(token
      -> assertThat(token).hasTrivia().isTrivia("//ab").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("""
                                /\\   \t   \t
                                /ab
                                """)).as("comment c++: line splicing").anySatisfy(token
      -> assertThat(token).hasTrivia().isTrivia("//ab").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("""
                                int main() {
                                int i = 1
                                // \\
                                + 42
                                ;
                                return i;
                                }
                                """ // line splicing
    )).as("comment c++: line splicing").anySatisfy(token
      -> assertThat(token).hasTrivia().isTrivia("// + 42").isComment().isTriviaLine(3));
    softly.assertThat(lexer.lex("""
                                int main() {
                                int i = 1
                                // \\   \t   \t
                                + 42
                                ;
                                return i;
                                }
                                """ // line splicing with whitespaces
    )).as("comment c++: line splicing with whitespaces").anySatisfy(token
      -> assertThat(token).hasTrivia().isTrivia("// + 42").isComment().isTriviaLine(3));

    softly.assertThat(lexer.lex("""
                                /\\
                                **\\
                                /
                                """ // line splicing
    // line splicing
    )).as("comment c: line splicing").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/**/").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("""
                                /\\   \t   \t
                                **\\   \t   \t
                                /
                                """ // line splicing with whitespaces
    // line splicing with whitespaces
    )).as("comment c: line splicing with whitespaces").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/**/").isComment().isTriviaLine(1));

    softly.assertThat(lexer.lex("""
                                /\\
                                *
                                */ # /*
                                */ defi\\
                                ne FO\\
                                O 10\\
                                20
                                """)).as("preprocessor directive with line splicing").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("#   define FOO 1020").isTriviaLine(3));
    softly.assertAll();

    softly.assertThat(lexer.lex("""
                                "str\\
                                i\\
                                ng"
                                """)).as("string with line splicing").anySatisfy(token
      -> assertThat(token).isValue("\"string\"").hasType(CxxTokenType.STRING).isLine(1));
    softly.assertAll();
  }

  /**
   * C++ Standard, Section 2.8 "Comments"
   */
  @Test
  void commentsCxx() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("//\n new line")).as("comment c++: empty").anySatisfy(token
      -> assertThat(token).isValue("new").hasTrivia().isTrivia("//").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("// My comment \\n new line")).as("\"comment c++: simple\"").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("// My comment \\n new line").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("// // \n new line")).as("comment c++: nested").anySatisfy(token
      -> assertThat(token).isValue("new").hasTrivia().isTrivia("// // ").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("// /**/ \n new line")).as("comment c++: nested2").anySatisfy(token
      -> assertThat(token).isValue("new").hasTrivia().isTrivia("// /**/ ").isComment().isTriviaLine(1));
    softly.assertAll();
  }

  /**
   * C++ Standard, Section 2.8 "Comments"
   */
  @Test
  void commentsC() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("/**/")).as("comment c: empty").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/**/").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("/* My comment */")).as("comment c: simple").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/* My comment */").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("/*\\\n*/")).as("comment c: with newline").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/**/").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("/*//*/")).as("comment c: nested").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/*//*/").isComment().isTriviaLine(1));
    softly.assertThat(lexer.lex("/* /* */")).as("comment c: nested2").anySatisfy(token
      -> assertThat(token).isValue("EOF").hasTrivia().isTrivia("/* /* */").isComment().isTriviaLine(1));
    softly.assertAll();
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  void decimalIntegerLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("0").tokenValue("0").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7").tokenValue("7").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix
      LiteralValuesBuilder.builder("7u").tokenValue("7u").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7U").tokenValue("7U").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongSuffix"
      LiteralValuesBuilder.builder("7ul").tokenValue("7ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7uL").tokenValue("7uL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7Ul").tokenValue("7Ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7UL").tokenValue("7UL").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongLongSuffix"
      LiteralValuesBuilder.builder("7ull").tokenValue("7ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7uLL").tokenValue("7uLL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7Ull").tokenValue("7Ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7ULL").tokenValue("7ULL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix"
      LiteralValuesBuilder.builder("7l").tokenValue("7l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7L").tokenValue("7L").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("7lu").tokenValue("7lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7lU").tokenValue("7lU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7Lu").tokenValue("7Lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7LU").tokenValue("7LU").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix"
      LiteralValuesBuilder.builder("7ll").tokenValue("7ll").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7LL").tokenValue("7LL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("7llu").tokenValue("7llu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7llU").tokenValue("7llU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7LLu").tokenValue("7LLu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7LLU").tokenValue("7LLU").tokenType(CxxTokenType.NUMBER).build(),
      // With Microsoft specific 64-bit integer-suffix: i64
      LiteralValuesBuilder.builder("7i64").tokenValue("7i64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7ui64").tokenValue("7ui64").tokenType(CxxTokenType.NUMBER).build(),
      // C++23
      LiteralValuesBuilder.builder("7z").tokenValue("7z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7uz").tokenValue("7uz").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7zu").tokenValue("7zu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7Z").tokenValue("7Z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7UZ").tokenValue("7UZ").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("7ZU").tokenValue("7ZU").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  void octalIntegerLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      // Octal integer
      LiteralValuesBuilder.builder("07").tokenValue("07").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix
      LiteralValuesBuilder.builder("07u").tokenValue("07u").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07U").tokenValue("07U").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongSuffix"
      LiteralValuesBuilder.builder("07ul").tokenValue("07ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07uL").tokenValue("07uL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07Ul").tokenValue("07Ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07UL").tokenValue("07UL").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongLongSuffix"
      LiteralValuesBuilder.builder("07ull").tokenValue("07ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07uLL").tokenValue("07uLL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07Ull").tokenValue("07Ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07ULL").tokenValue("07ULL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix"
      LiteralValuesBuilder.builder("07l").tokenValue("07l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07L").tokenValue("07L").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("07lu").tokenValue("07lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07lU").tokenValue("07lU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07Lu").tokenValue("07Lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07LU").tokenValue("07LU").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix"
      LiteralValuesBuilder.builder("07ll").tokenValue("07ll").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07LL").tokenValue("07LL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("07llu").tokenValue("07llu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07llU").tokenValue("07llU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07LLu").tokenValue("07LLu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07LLU").tokenValue("07LLU").tokenType(CxxTokenType.NUMBER).build(),
      // With Microsoft specific 64-bit integer-suffix: i64
      LiteralValuesBuilder.builder("07i64").tokenValue("07i64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07ui64").tokenValue("07ui64").tokenType(CxxTokenType.NUMBER).build(),
      // C++23
      LiteralValuesBuilder.builder("07z").tokenValue("07z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07uz").tokenValue("07uz").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07zu").tokenValue("07zu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07Z").tokenValue("07Z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07UZ").tokenValue("07UZ").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("07ZU").tokenValue("07ZU").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  void hexIntegerLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      // Hex integer
      LiteralValuesBuilder.builder("0x7").tokenValue("0x7").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix
      LiteralValuesBuilder.builder("0x7u").tokenValue("0x7u").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7U").tokenValue("0x7U").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongSuffix"
      LiteralValuesBuilder.builder("0x7ul").tokenValue("0x7ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7uL").tokenValue("0x7uL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7Ul").tokenValue("0x7Ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7UL").tokenValue("0x7UL").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongLongSuffix"
      LiteralValuesBuilder.builder("0x7ull").tokenValue("0x7ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7uLL").tokenValue("0x7uLL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7Ull").tokenValue("0x7Ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7ULL").tokenValue("0x7ULL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix"
      LiteralValuesBuilder.builder("0x7l").tokenValue("0x7l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7L").tokenValue("0x7L").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("0x7lu").tokenValue("0x7lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7lU").tokenValue("0x7lU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7Lu").tokenValue("0x7Lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7LU").tokenValue("0x7LU").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix"
      LiteralValuesBuilder.builder("0x7ll").tokenValue("0x7ll").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7LL").tokenValue("0x7LL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("0x7llu").tokenValue("0x7llu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7llU").tokenValue("0x7llU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7LLu").tokenValue("0x7LLu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7LLU").tokenValue("0x7LLU").tokenType(CxxTokenType.NUMBER).build(),
      // With Microsoft specific 64-bit integer-suffix: i64
      LiteralValuesBuilder.builder("0x7i64").tokenValue("0x7i64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7ui64").tokenValue("0x7ui64").tokenType(CxxTokenType.NUMBER).build(),
      // C++23
      LiteralValuesBuilder.builder("0x7z").tokenValue("0x7z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7uz").tokenValue("0x7uz").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7zu").tokenValue("0x7zu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7Z").tokenValue("0x7Z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7UZ").tokenValue("0x7UZ").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x7ZU").tokenValue("0x7ZU").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  void binIntegerLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      // bin integer
      LiteralValuesBuilder.builder("0b0").tokenValue("0b0").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1").tokenValue("0B1").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b10101001").tokenValue("0b10101001").tokenType(CxxTokenType.NUMBER).build(),
      // C++23
      LiteralValuesBuilder.builder("0b1z").tokenValue("0b1z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b1uz").tokenValue("0b1uz").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b1zu").tokenValue("0b1zu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b1Z").tokenValue("0b1Z").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b1UZ").tokenValue("0b1UZ").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b1ZU").tokenValue("0b1ZU").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  void hexIntegerLiteralsBigX() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      // Hex integer (big X)
      LiteralValuesBuilder.builder("0X7").tokenValue("0X7").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix
      LiteralValuesBuilder.builder("0X7u").tokenValue("0X7u").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7U").tokenValue("0X7U").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongSuffix"
      LiteralValuesBuilder.builder("0X7ul").tokenValue("0X7ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7uL").tokenValue("0X7uL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7Ul").tokenValue("0X7Ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7UL").tokenValue("0X7UL").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongLongSuffix"
      LiteralValuesBuilder.builder("0X7ull").tokenValue("0X7ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7uLL").tokenValue("0X7uLL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7Ull").tokenValue("0X7Ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7ULL").tokenValue("0X7ULL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix"
      LiteralValuesBuilder.builder("0X7l").tokenValue("0X7l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7L").tokenValue("0X7L").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("0X7lu").tokenValue("0X7lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7lU").tokenValue("0X7lU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7Lu").tokenValue("0X7Lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7LU").tokenValue("0X7LU").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix"
      LiteralValuesBuilder.builder("0X7ll").tokenValue("0X7ll").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7LL").tokenValue("0X7LL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("0X7llu").tokenValue("0X7llu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7llU").tokenValue("0X7llU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7LLu").tokenValue("0X7LLu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7LLU").tokenValue("0X7LLU").tokenType(CxxTokenType.NUMBER).build(),
      // With Microsoft specific 64-bit integer-suffix: i64
      LiteralValuesBuilder.builder("0X7i64").tokenValue("0X7i64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X7ui64").tokenValue("0X7ui64").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.2 "Integer literals"
   */
  @Test
  void binIntegerLiteralsBigB() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      // Binary literal (big B)
      LiteralValuesBuilder.builder("0B1").tokenValue("0B1").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix
      LiteralValuesBuilder.builder("0B1u").tokenValue("0B1u").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1U").tokenValue("0B1U").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongSuffix"
      LiteralValuesBuilder.builder("0B1ul").tokenValue("0B1ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1uL").tokenValue("0B1uL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1Ul").tokenValue("0B1Ul").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1UL").tokenValue("0B1UL").tokenType(CxxTokenType.NUMBER).build(),
      // With "UnsignedSuffix LongLongSuffix"
      LiteralValuesBuilder.builder("0B1ull").tokenValue("0B1ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1uLL").tokenValue("0B1uLL").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1Ull").tokenValue("0B1Ull").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1ULL").tokenValue("0B1ULL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix"
      LiteralValuesBuilder.builder("0B1l").tokenValue("0B1l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1L").tokenValue("0B1L").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("0B1lu").tokenValue("0B1lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1lU").tokenValue("0B1lU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1Lu").tokenValue("0B1Lu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1LU").tokenValue("0B1LU").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix"
      LiteralValuesBuilder.builder("0B1ll").tokenValue("0B1ll").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1LL").tokenValue("0B1LL").tokenType(CxxTokenType.NUMBER).build(),
      // With "LongLongSuffix UnsignedSuffix"
      LiteralValuesBuilder.builder("0B1llu").tokenValue("0B1llu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1llU").tokenValue("0B1llU").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1LLu").tokenValue("0B1LLu").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1LLU").tokenValue("0B1LLU").tokenType(CxxTokenType.NUMBER).build(),
      // With Microsoft specific 64-bit integer-suffix: i64
      LiteralValuesBuilder.builder("0B1i64").tokenValue("0B1i64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0B1ui64").tokenValue("0B1ui64").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.4 "Floating literals"
   */
  @Test
  void floatingPointLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("3.14").tokenValue("3.14").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("10.").tokenValue("10.").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder(".001").tokenValue(".001").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1e100").tokenValue("1e100").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14e-10").tokenValue("3.14e-10").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14E-10").tokenValue("3.14E-10").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0e0").tokenValue("0e0").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14f").tokenValue("3.14f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("10.f").tokenValue("10.f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder(".001f").tokenValue(".001f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1e100f").tokenValue("1e100f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14e-10f").tokenValue("3.14e-10f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14E-10f").tokenValue("3.14E-10f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0e0f").tokenValue("0e0f").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14F").tokenValue("3.14F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("10.F").tokenValue("10.F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder(".001F").tokenValue(".001F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1e100F").tokenValue("1e100F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14e-10F").tokenValue("3.14e-10F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14E-10F").tokenValue("3.14E-10F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0e0F").tokenValue("0e0F").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14l").tokenValue("3.14l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("10.l").tokenValue("10.l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder(".001l").tokenValue(".001l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1e100l").tokenValue("1e100l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14e-10l").tokenValue("3.14e-10l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14E-10l").tokenValue("3.14E-10l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0e0l").tokenValue("0e0l").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14L").tokenValue("3.14L").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("10.L").tokenValue("10.L").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder(".001L").tokenValue(".001L").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1e100L").tokenValue("1e100L").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14e-10L").tokenValue("3.14e-10L").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("3.14E-10L").tokenValue("3.14E-10L").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0e0L").tokenValue("0e0L").tokenType(CxxTokenType.NUMBER).build(),
      // C++17: hexadecimal floating literals
      LiteralValuesBuilder.builder("0x1ffp10").tokenValue("0x1ffp10").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0X0p-1").tokenValue("0X0p-1").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x1.p0").tokenValue("0x1.p0").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0xf.p-1").tokenValue("0xf.p-1").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x0.123p-1").tokenValue("0x0.123p-1").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0xa.bp10l").tokenValue("0xa.bp10l").tokenType(CxxTokenType.NUMBER).build(),
      // C++23
      LiteralValuesBuilder.builder("1.f16").tokenValue("1.f16").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.f32").tokenValue("1.f32").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.f64").tokenValue("1.f64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.f128").tokenValue("1.f128").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.bf16").tokenValue("1.bf16").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.F16").tokenValue("1.F16").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.F32").tokenValue("1.F32").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.F64").tokenValue("1.F64").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.F128").tokenValue("1.F128").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.BF16").tokenValue("1.BF16").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.13.8 "User-defined literals"
   */
  @Test
  void userDefinedLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("12_w").tokenValue("12_w").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1.2_w").tokenValue("1.2_w").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0x123ABC_print").tokenValue("0x123ABC_print").tokenType(CxxTokenType.NUMBER)
        .build(),
      LiteralValuesBuilder.builder("0b101010_print").tokenValue("0b101010_print").tokenType(CxxTokenType.NUMBER)
        .build(),
      LiteralValuesBuilder.builder("\"two\"_w").tokenValue("\"two\"_w").tokenType(CxxTokenType.STRING).build(),
      LiteralValuesBuilder.builder("'X'_w").tokenValue("'X'_w").tokenType(CxxTokenType.CHARACTER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.13 ff "digit separators"
   */
  @Test
  void digitSeparators() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("1'000'000").tokenValue("1'000'000").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("0b0100'1100'0110").tokenValue("0b0100'1100'0110").tokenType(CxxTokenType.NUMBER)
        .build(),
      LiteralValuesBuilder.builder("00'04'00'00'00").tokenValue("00'04'00'00'00").tokenType(CxxTokenType.NUMBER)
        .build(),
      LiteralValuesBuilder.builder("0x10'0000").tokenValue("0x10'0000").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1'000.000'015'3").tokenValue("1'000.000'015'3").tokenType(CxxTokenType.NUMBER)
        .build(),
      LiteralValuesBuilder.builder("1'000.").tokenValue("1'000.").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder(".000'015'3").tokenValue(".000'015'3").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1'000e-10").tokenValue("1'000e-10").tokenType(CxxTokenType.NUMBER).build(),
      LiteralValuesBuilder.builder("1'000e-1'000").tokenValue("1'000e-1'000").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.6 "Boolean literals"
   */
  @Test
  void booleanLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("true").tokenValue("true").tokenType(CxxKeyword.TRUE).build(),
      LiteralValuesBuilder.builder("false").tokenValue("false").tokenType(CxxKeyword.FALSE).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Section 2.14.7 "Pointer literals"
   */
  @Test
  void pointerLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("nullptr").tokenValue("nullptr").tokenType(CxxTokenType.NUMBER).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, Character literals
   */
  @Test
  void characterLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("''").tokenValue("''").tokenType(CxxTokenType.CHARACTER).build(), // char: empty
      LiteralValuesBuilder.builder("u8''").tokenValue("u8''").tokenType(CxxTokenType.CHARACTER).build(), // char: prefix u8
      LiteralValuesBuilder.builder("u''").tokenValue("u''").tokenType(CxxTokenType.CHARACTER).build(), // char: prefix u
      LiteralValuesBuilder.builder("U''").tokenValue("U''").tokenType(CxxTokenType.CHARACTER).build(), // char: prefix U
      LiteralValuesBuilder.builder("L''").tokenValue("L''").tokenType(CxxTokenType.CHARACTER).build(), // char: prefix L

      LiteralValuesBuilder.builder("'a'").tokenValue("'a'").tokenType(CxxTokenType.CHARACTER).build(), // char: trivial
      LiteralValuesBuilder.builder("'ab'").tokenValue("'ab'").tokenType(CxxTokenType.CHARACTER).build(), //char: more than one

      LiteralValuesBuilder.builder("'\\''").tokenValue("'\\''").tokenType(CxxTokenType.CHARACTER).build(), // char: escaped quote
      LiteralValuesBuilder.builder("'\\\\'").tokenValue("'\\\\'").tokenType(CxxTokenType.CHARACTER).build(), // char: escaped backslash

      LiteralValuesBuilder.builder("'").tokenValue("'").tokenType(GenericTokenType.UNKNOWN_CHAR).build(),
      LiteralValuesBuilder.builder("'\\'").tokenValue("'").tokenType(GenericTokenType.UNKNOWN_CHAR).build() // This are 3 Tokens of UNKNOWN_CHAR
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  /**
   * C++ Standard, String literals
   */
  @Test
  void stringLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("\"\"").tokenValue("\"\"").tokenType(CxxTokenType.STRING).build(), // string: empty
      LiteralValuesBuilder.builder("u8\"\"").tokenValue("u8\"\"").tokenType(CxxTokenType.STRING).build(), // string: prefix u8
      LiteralValuesBuilder.builder("u\"\"").tokenValue("u\"\"").tokenType(CxxTokenType.STRING).build(), // string: prefix u
      LiteralValuesBuilder.builder("U\"\"").tokenValue("U\"\"").tokenType(CxxTokenType.STRING).build(), // string: prefix U
      LiteralValuesBuilder.builder("L\"\"").tokenValue("L\"\"").tokenType(CxxTokenType.STRING).build(), // string: prefix L

      LiteralValuesBuilder.builder("\"a\"").tokenValue("\"a\"").tokenType(CxxTokenType.STRING).build(), // string: trivial

      LiteralValuesBuilder.builder("\" \\\\ \"").tokenValue("\" \\\\ \"").tokenType(CxxTokenType.STRING).build(), // string: escaped quote
      LiteralValuesBuilder.builder("\" \\\" \"").tokenValue("\" \\\" \"").tokenType(CxxTokenType.STRING).build(), // string: escaped backslash

      LiteralValuesBuilder.builder("\"").tokenValue("\"").tokenType(GenericTokenType.UNKNOWN_CHAR).build(), // string: unterminated
      LiteralValuesBuilder.builder("\"\\\"").tokenValue("\\").tokenType(GenericTokenType.UNKNOWN_CHAR).build() // string: unescaped backslash
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  @Test
  void rawstringLiterals() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("R\"(...)\"").tokenValue("R\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: empty
      LiteralValuesBuilder.builder("uR\"(...)\"").tokenValue("uR\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: prefix u
      LiteralValuesBuilder.builder("u8R\"(...)\"").tokenValue("u8R\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: prefix u8R
      LiteralValuesBuilder.builder("UR\"(...)\"").tokenValue("UR\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: prefix UR
      LiteralValuesBuilder.builder("LR\"(...)\"").tokenValue("LR\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: prefix LR
      // examples from the standard
      LiteralValuesBuilder.builder("R\"(...)\"").tokenValue("R\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: std example 1
      LiteralValuesBuilder.builder("u8R\"**(...)**\"").tokenValue("u8R\"**(...)**\"").tokenType(CxxTokenType.STRING)
        .build(), // raw string: std example 2
      LiteralValuesBuilder.builder("uR\"*∼(...)*∼\"").tokenValue("uR\"*∼(...)*∼\"").tokenType(CxxTokenType.STRING)
        .build(), // raw string: std example 3
      LiteralValuesBuilder.builder("UR\"zzz(...)zzz\"").tokenValue("UR\"zzz(...)zzz\"").tokenType(CxxTokenType.STRING)
        .build(), // raw string: std example 4
      LiteralValuesBuilder.builder("LR\"(...)\"").tokenValue("LR\"(...)\"").tokenType(CxxTokenType.STRING).build(), // raw string: std example 5

      LiteralValuesBuilder.builder("R\"(An unescaped \\ character)\"").tokenValue("R\"(An unescaped \\ character)\"")
        .tokenType(CxxTokenType.STRING).build(), // raw string: an unescaped \\ character
      LiteralValuesBuilder.builder("R\"(An unescaped \" character)\"").tokenValue("R\"(An unescaped \" character)\"")
        .tokenType(CxxTokenType.STRING).build(), // raw string: an unescaped \" character
      LiteralValuesBuilder.builder("R\"xyz()\")xyz\"").tokenValue("R\"xyz()\")xyz\"")
        .tokenType(CxxTokenType.STRING).build(), // raw string: represent the string: )\"
      LiteralValuesBuilder.builder(
        "R\"X*X(A C++11 raw string literal can be specified like this: R\"(This is my raw string)\" )X*X\"")
        .tokenValue(
          "R\"X*X(A C++11 raw string literal can be specified like this: R\"(This is my raw string)\" )X*X\"")
        .tokenType(CxxTokenType.STRING).build(), // raw string: complex example
      LiteralValuesBuilder.builder("R\"([.^$|()\\[\\]{}*+?\\\\])\"").tokenValue("R\"([.^$|()\\[\\]{}*+?\\\\])\"")
        .tokenType(CxxTokenType.STRING).build(), // raw string: regex sample

      // fix #1748
      LiteralValuesBuilder.builder("R\"(([A-Z\\d]))\"").tokenValue("R\"(([A-Z\\d]))\"")
        .tokenType(CxxTokenType.STRING).build(),
      LiteralValuesBuilder.builder("R\"regex(([A-Z\\d]))regex\"").tokenValue("R\"regex(([A-Z\\d]))regex\"")
        .tokenType(CxxTokenType.STRING).build(),
      LiteralValuesBuilder.builder("R\"(())\"").tokenValue("R\"(())\"")
        .tokenType(CxxTokenType.STRING).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Literal %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  @Test
  void operatorsAndDelimiters() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder(":").tokenValue(":").tokenType(CxxPunctuator.COLON).build(),
      LiteralValuesBuilder.builder("=").tokenValue("=").tokenType(CxxPunctuator.ASSIGN).build(),
      LiteralValuesBuilder.builder("~").tokenValue("~").tokenType(CxxPunctuator.BW_NOT).build()
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Operator|Delimiter %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  @Test
  void keywordsAndIdentifiers() {
    var values = new ArrayList<LiteralValuesBuilder>(Arrays.asList(
      LiteralValuesBuilder.builder("return").tokenValue("return").tokenType(CxxKeyword.RETURN).build(),
      LiteralValuesBuilder.builder("identifier").tokenValue("identifier").tokenType(GenericTokenType.IDENTIFIER)
        .build(),
      LiteralValuesBuilder.builder("a1").tokenValue("a1").tokenType(GenericTokenType.IDENTIFIER).build(),
      LiteralValuesBuilder.builder("A1").tokenValue("A1").tokenType(GenericTokenType.IDENTIFIER).build(),
      LiteralValuesBuilder.builder("A_a_A_1").tokenValue("A_a_A_1").tokenType(GenericTokenType.IDENTIFIER).build(),
      LiteralValuesBuilder.builder("truetype").tokenValue("truetype").tokenType(GenericTokenType.IDENTIFIER).build() //identifier: containing boolean constant
    ));

    values.forEach((LiteralValuesBuilder value)
      -> assertThat(lexer.lex(value.lexerValue)).as("Keyword|Identifier %s", value.lexerValue).anySatisfy(token
        -> assertThat(token).isValue(value.tokenValue).hasType(value.tokenType))
    );
  }

  @Test
  void blankLines() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("    // comment\\n")).hasSize(1);
    softly.assertThat(lexer.lex("    \n")).hasSize(1);
    softly.assertThat(lexer.lex("    ")).hasSize(1);
    softly.assertThat(lexer.lex("line\n\n")).hasSize(2);
    softly.assertAll();
  }

}
