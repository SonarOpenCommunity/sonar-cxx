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
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.sonar.cxx.config.CxxSquidConfiguration;
import static org.sonar.cxx.lexer.LexerAssert.assertThat;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxLexerPool;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.preprocessor.PPInclude;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class CxxLexerWithPreprocessingTest {

  private Lexer lexer;
  private SquidAstVisitorContext<Grammar> context;

  @BeforeEach
  public void setUp() {
    var file = new File("snippet.cpp").getAbsoluteFile();
    context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    var pp = new CxxPreprocessor(context);
    lexer = CxxLexerPool.create(pp, new JoinStringsPreprocessor()).getLexer();
  }

  @Test
  void escapingNewline() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("line\\\r\nline")).as("dos style").noneMatch(token
      -> token.getValue().contentEquals("\\") && token.getType().equals(GenericTokenType.UNKNOWN_CHAR));
    softly.assertThat(lexer.lex("line\\\rline")).as("mac(old) style").noneMatch(token
      -> token.getValue().contentEquals("\\") && token.getType().equals(GenericTokenType.UNKNOWN_CHAR));
    softly.assertThat(lexer.lex("line\\\nline")).as("unix style").noneMatch(token
      -> token.getValue().contentEquals("\\") && token.getType().equals(GenericTokenType.UNKNOWN_CHAR));
    softly.assertThat(lexer.lex("line\\\n    line")).hasSize(3);
    softly.assertAll();
  }

  @Test
  void joiningStrings() {
    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("\"string\"")).anySatisfy(token
      -> assertThat(token).isValue("\"string\"").hasType(CxxTokenType.STRING));
    softly.assertThat(lexer.lex("\"string\"\"string\"")).anySatisfy(token
      -> assertThat(token).isValue("\"stringstring\"").hasType(CxxTokenType.STRING));
    softly.assertThat(lexer.lex("\"string\"\n\"string\"")).anySatisfy(token
      -> assertThat(token).isValue("\"stringstring\"").hasType(CxxTokenType.STRING));
    softly.assertThat(lexer.lex("\"string\"\"string\"")).hasSize(2); // string + EOF
    softly.assertAll();
  }

  static Stream<Arguments> macroToString() {
    return Stream.of(
      Arguments.of("""
         #define lala "haha"
         lala
         """, "\"haha\""),
      Arguments.of("""
         #define str(a) # a
         str(x)
         """, "\"x\""),
      Arguments.of("""
         #define hash_hash(x) # ## #
         #define mkstr(a) # a
         #define in_between(a) mkstr(a)
         #define join(c, d) in_between(c hash_hash(x) d)
         join(x,y)
         """, "\"x y\""),
      Arguments.of("""
         #define str(a) # a
         str("x")
         """, "\"\\\"x\\\"\""),
      Arguments.of("""
         #define M1 "a"
         #define M2 M1
         M2
         """, "\"a\""),
      Arguments.of("""
         #define Y "hello, "
         #define X Y "world"
         X
         """, "\"hello, world\""),
      Arguments.of("""
         #define M "A" "B" "C"
         #define N M
         N
         """, "\"ABC\""),
      Arguments.of("""
         #define M "B"
         "A" M
         """, "\"AB\"")
    );
  }

  @ParameterizedTest
  @MethodSource("macroToString")
  void expandMacroToString(String macro, String value) {
    List<Token> tokens = lexer.lex(macro);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // STRING EOF
    softly.assertThat(tokens).anySatisfy(token
      -> assertThat(token).isValue(value).hasType(CxxTokenType.STRING)
    );
    softly.assertAll();
  }

  static Stream<Arguments> macroToNumber() {
    return Stream.of(
      Arguments.of("""
         #define plus(a, b) a + b
         plus(1, 2)
         """, "1", 4),
      Arguments.of("""
         #define M() 0
         M()
         """, "0", 2),
      Arguments.of("""
         #ifndef LALA
         111
         #else
         222
         #endif
         """, "111", 2),
      Arguments.of("""
         #define X
         #ifndef X
           111
         #else
           222
         #endif
         """, "222", 2),
      Arguments.of("""
         #define BOOST_PP_EXPAND(p) p
         BOOST_PP_EXPAND(#) define BOOST_FT_config_valid 1
         BOOST_FT_config_valid
         """, "1", 5),
      Arguments.of("""
         #define A B(cf)
         #define B(n) 0x##n
         A
         """, "0xcf", 2)
    );
  }

  @ParameterizedTest
  @MethodSource("macroToNumber")
  void expandMacroToNumber(String macro, String value, int size) {
    List<Token> tokens = lexer.lex(macro);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(size);
    softly.assertThat(tokens).anySatisfy(token
      -> assertThat(token).isValue(value).hasType(CxxTokenType.NUMBER)
    );
    softly.assertAll();
  }

  static Stream<Arguments> macroToIdentifier() {
    return Stream.of(
      Arguments.of("""
         #if 0
           a
         #else
           nota
         #endif
         """, "nota"),
      Arguments.of("""
         #if 1
           a
         #else
           nota
         #endif
         """, "a"),
      Arguments.of("""
         #define LALA 1
         #if LALA
           a
         #else
           nota
         #endif
         """, "a"),
      Arguments.of("""
         #if LALA
           a
         #else
           nota
         #endif
         """, "nota"),
      Arguments.of("""
         #if 0
           #if 1
             b
           #else
             notb
           #endif
         #else
           nota
         #endif
         """, "nota"),
      Arguments.of("""
         #if ("")
           a
         #else
           nota
         #endif
         """, "a"),
      Arguments.of("""
         #if 0
           if
         #elif 1
           elif
         #endif
         """, "elif"),
      Arguments.of("""
         #define A 1
         #if A
            ifbody
         #elif A
            elifbody
         #endif
         """, "ifbody"),
      Arguments.of("""
         #define B
         #ifdef A
           a
           #ifdef B
             b
           #else
             notb
           #endif
           a1
         #else
           nota
         #endif
         """, "nota"),
      Arguments.of("""
         #if !defined A
           #define A
           #ifdef B
             b
           #else
             notb
           #endif
         #endif
         """, "notb"),
      Arguments.of("""
         #define SDS_ARCH_darwin_15_i86
         #ifdef SDS_ARCH_freebsd_61_i86
            case1
         #elif defined(SDS_ARCH_darwin_15_i86)
            case2
         #elif defined(SDS_ARCH_winxp) || defined(SDS_ARCH_Interix)
            case3
         #else
            case4
         #endif
         """, "case2"),
      Arguments.of("""
         #define CPU
         #ifdef CPU
            ifbody
         #elifdef GPU
            elifdefbody
         #elifndef RAM
            elifndefbody
         #else
            elsebody
         #endif
         """, "ifbody"),
      Arguments.of("""
         #define GPU
         #ifdef CPU
            ifbody
         #elifdef GPU
            elifdefbody
         #elifndef RAM
            elifndefbody
         #else
            elsebody
         #endif
         """, "elifdefbody"),
      Arguments.of("""
         #define XXX
         #ifdef CPU
            ifbody
         #elifdef GPU
            elifdefbody
         #elifndef RAM
            elifndefbody
         #else
            elsebody
         #endif
         """, "elifndefbody"),
      Arguments.of("""
         #define RAM
         #ifdef CPU
            ifbody
         #elifdef GPU
            elifdefbody
         #elifndef RAM
            elifndefbody
         #else
            elsebody
         #endif
         """, "elsebody"),
      Arguments.of("""
         #define A() 0
         #define B() 0
         #if A() & B()
           truecase
         #else
           falsecase
         #endif
         """, "falsecase"),
      Arguments.of("""
         #define A() 0 ## 1
         #if A()
           truecase
         #else
           falsecase
         #endif
         """, "truecase"),
      Arguments.of("""
         #define _C_
         #if !defined(_A_) && !defined(_B_) && !defined(_C_)
           truecase
         #else
           falsecase
         #endif
         """, "falsecase"),
      Arguments.of("""
         #define foo(a) 1
         #if 0
           body
         #elif foo(10)
           truecase
         #else
           falsecase
         endif
         """, "truecase"),
      Arguments.of("""
         #define a b
         #undef a
         a
         """, "a"),
      Arguments.of("""
         #define RECURSIVE RECURSIVE
         RECURSIVE
         """, "RECURSIVE"),
      Arguments.of("""
         #define concat(a,b) a ## b
         concat(x,y)
         """, "xy"),
      Arguments.of("""
         #define hashhash c ## c
         hashhash
         """, "cc"),
      Arguments.of("""
         #define concat(a,b) a ## ## ## b
         concat(x,y)
         """, "xy"),
      Arguments.of("""
         #define concat(a,b) c ## c ## c ## c
         concat(x,y)
         """, "cccc"),
      Arguments.of("""
         #define FOOBAR 1
         #define CHECK(a, b) (( a ## b + 1 == 2))
         #if CHECK(FOO , BAR)
         yes
         #endif
         """, "yes"),
      Arguments.of("""
         #define macro(new) new
         macro(a)
         """, "a"),
      Arguments.of("""
         #define new new_debug
         new
         """, "new_debug"),
      Arguments.of("""
         #define M macrobody\\

         external
         """, "external")
    );
  }

  @ParameterizedTest
  @MethodSource("macroToIdentifier")
  void expandMacroToIdentifier(String macro, String value) {
    List<Token> tokens = lexer.lex(macro);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token
      -> assertThat(token).isValue(value).hasType(GenericTokenType.IDENTIFIER)
    );
    softly.assertAll();
  }

  @Test
  void recursiveFunctionlikeMacros() {
    List<Token> tokens = lexer.lex("""
                                   #define RECURSIVE(a) RECURSIVE(a)
                                   RECURSIVE(1)
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(
      token -> assertThat(token).isValue("RECURSIVE").hasType(GenericTokenType.IDENTIFIER)
    );
    softly.assertThat(tokens).anySatisfy(
      token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER)
    );
    softly.assertAll();
  }

  @Test
  void expandingFunctionlikeMacrosWithVarargs() {
    List<Token> tokens = lexer.lex("""
                                   #define wrapper(...) __VA_ARGS__
                                   wrapper(1, 2)
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("2").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void expandingFunctionlikeMacrosWithnamedVarargs() {
    List<Token> tokens = lexer.lex("""
                                   #define wrapper(args...) args
                                   wrapper(1, 2)
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("2").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void expandingFunctionlikeMacrosWithEmptyVarargs() {
    List<Token> tokens = lexer.lex("""
                                   #define wrapper(...) (__VA_ARGS__)
                                   wrapper()
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(3);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue(")").hasType(CxxPunctuator.BR_RIGHT));
    softly.assertAll();
  }

  @Test
  void expandingFunctionlikeMacrosWithextraparantheses() {
    List<Token> tokens = lexer.lex("""
                                   #define neg(a) -a
                                   neg((1))
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
    softly.assertAll();
  }

  @Test
  void expandingChainedMacros2() {
    List<Token> tokens = lexer.lex("""
                                   #define M1 "a"
                                   #define M2 foo(M1)
                                   M2
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"a\"").hasType(CxxTokenType.STRING));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("foo").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void expandingChainedMacros3() {
    List<Token> tokens = lexer.lex("""
                                   #define M1(a) "a"
                                   #define M2 foo(M1)
                                   M2
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("M1").hasType(GenericTokenType.IDENTIFIER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("foo").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void preservingWhitespace() {
    List<Token> tokens = lexer.lex("""
                                   #define CODE(x) x
                                   CODE(new B())
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("B").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void bodylessDefines() {
    assertThat(lexer.lex("""
                         #define M
                         M
                         """)).noneMatch(token -> token.getValue().contentEquals("M"))
      .noneMatch(token -> token.getType().equals(GenericTokenType.IDENTIFIER));
  }

  @Test
  void externalDefine() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
      "M body");
    var pp = new CxxPreprocessor(context, squidConfig);
    lexer = CxxLexerPool.create(squidConfig.getCharset(), pp, new JoinStringsPreprocessor()).getLexer();

    List<Token> tokens = lexer.lex("M");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("body").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void externalDefinesWithParams() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
      "minus(a, b) a - b");
    var pp = new CxxPreprocessor(context, squidConfig);
    lexer = CxxLexerPool.create(squidConfig.getCharset(), pp, new JoinStringsPreprocessor()).getLexer();

    List<Token> tokens = lexer.lex("minus(1, 2)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void usingMacroNameAsMacroIdentifier() {
    List<Token> tokens = lexer.lex("""
                                   #define X(a) a X(a)
                                   X(new)
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(6); // new + X + ( + new + ) + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("X").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void usingKeywordAsMacroArgument() {
    List<Token> tokens = lexer.lex("""
                                   #define X(a) a
                                   X(new)
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // kw + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertAll();
  }

  @Test
  void includesAreWorking() throws IOException {
    context = mock(SquidAstVisitorContext.class);
    var file = new File("/home/joe/file.cc");
    when(context.getFile()).thenReturn(file);
    var pp = spy(new CxxPreprocessor(context, new CxxSquidConfiguration()));
    var include = spy(new PPInclude(pp, file.toPath()));
    when(include.searchFile(anyString(), eq(false))).thenReturn(Path.of("file"));
    doReturn("#define A B\n").when(include).getSourceCode(any(), any());
    when(pp.include()).thenReturn(include);

    lexer = CxxLexerPool.create(pp, new JoinStringsPreprocessor()).getLexer();
    List<Token> tokens = lexer.lex("""
                                   #include <file>
                                   A
                                   """);
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // B + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("B").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void macroReplacementInIncludesIsWorking() {
    List<Token> tokens = lexer.lex("""
                                   #define A "B"
                                   #include A
                                   """);
    assertThat(tokens).hasSize(1); // EOF
  }

  @Test
  void conditionalCompilationIfdefUndefined() {
    List<Token> tokens = lexer.lex("""
                                   #ifdef LALA
                                     111
                                   #else
                                     222
                                   #endif
                                   """);

    var softly = new SoftAssertions();
    softly.assertThat(tokens).noneMatch(token -> token.getValue().contentEquals("111") && token.getType().equals(
      CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("222").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void conditionalCompilationIfdefDefined() {
    List<Token> tokens = lexer.lex("""
                                   #define LALA
                                   #ifdef LALA
                                     111
                                   #else
                                     222
                                   #endif
                                   """);

    var softly = new SoftAssertions();
    softly.assertThat(tokens).noneMatch(token -> token.getValue().contentEquals("222") && token.getType().equals(
      CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("111").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  // Proper separation of parameterized macros and macros expand to a string enclosed
  // in parentheses
  @Test
  void macroExpandingToParantheses() {
    // a macro is identified as 'functionlike' if and only if the parentheses
    // aren't separated from the identifier by whitespace. Otherwise, the parentheses
    // belong to the replacement string
    List<Token> tokens = lexer.lex("""
                                   #define macro ()
                                   macro
                                   """);
    assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
  }

  @Test
  void ignoreIrrelevantPreprocessorDirectives() {
    List<Token> tokens = lexer.lex("#pragma lala\n");
    assertThat(tokens).hasSize(1); // EOF
  }

  @Test
  void overwriteGlobalMacro() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.DEFINES, "macro globalvalue");
    var pp = new CxxPreprocessor(context, squidConfig);
    lexer = CxxLexerPool.create(squidConfig.getCharset(), pp).getLexer();

    List<Token> tokens = lexer.lex("""
                                   #define macro overriden
                                   macro
                                   """);

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // goodvalue + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("overriden").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  /**
   * Test the expansion of default macros. Document the reference value of __LINE__ == 1
   */
  @Test
  void defaultMacros() {
    var squidConfig = new CxxSquidConfiguration();
    var pp = new CxxPreprocessor(context, squidConfig);

    lexer = CxxLexerPool.create(squidConfig.getCharset(), pp).getLexer();
    List<Token> tokens = lexer.lex("__LINE__");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // __LINE__ + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  /**
   * Configured defines override default macros. This is equivalent to the standard preprocessor behavior:<br>
   * <code>
   * main.cpp: printf("%d", __LINE__);
   * g++ -D__LINE__=123 main.cpp && ./a.out
   *
   * Expected Output: 123
   * </code>
   */
  @Test
  void configuredDefinesOverrideDefaultMacros() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.DEFINES, "__LINE__ 123");
    var pp = new CxxPreprocessor(context, squidConfig);

    lexer = CxxLexerPool.create(squidConfig.getCharset(), pp).getLexer();
    List<Token> tokens = lexer.lex("__LINE__");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // __LINE__ + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("123").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  /**
   * Forced includes override configured defines and default macros (similar to the fact that [included] #define
   * directives override configured defines). This is equivalent to the standard preprocessor behavior: <br>
   * <code>
   * main.cpp: #define __LINE__ 345
   *           printf("%d", __LINE__);
   * g++ -D__LINE__=123 main.cpp && ./a.out
   *
   * Expected Output: 345
   * </code>
   */
  @Test
  void forcedIncludesOverrideConfiguredDefines() throws IOException {
    String forceIncludePath = "/home/user/force.h";
    var forceIncludeFile = new File(forceIncludePath);

    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.FORCE_INCLUDES,
      Collections.singletonList(forceIncludePath));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
      "__LINE__ 123");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED,
      "false");

    var pp = spy(new CxxPreprocessor(context, squidConfig));
    var include = spy(new PPInclude(pp, forceIncludeFile.toPath()));
    when(include.searchFile(anyString(), anyBoolean())).thenReturn(forceIncludeFile.toPath());
    doReturn("#define __LINE__ 345\n").when(include).getSourceCode(any(), any());
    when(pp.include()).thenReturn(include);

    lexer = CxxLexerPool.create(squidConfig.getCharset(), pp).getLexer();
    List<Token> tokens = lexer.lex("__LINE__\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // __LINE__ + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("345").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void miscPreprocessorLines() {
    // a line which begins with a hash is a preprocessor line
    // it doesn't have any meaning in our context and should be just ignored

    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("#")).hasSize(1); // EOF
    softly.assertThat(lexer.lex("#lala")).hasSize(1); // EOF
    softly.assertThat(lexer.lex("# lala")).hasSize(1); // EOF
    softly.assertAll();
  }

  @Test
  void builtInMacros() {
    List<Token> tokens = lexer.lex("__DATE__");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // date + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void stringProblem1903() {
    List<Token> tokens = lexer.lex("""
                                   void f1() {}
                                   #define BROKEN_DEFINE " /a/path/*"
                                   void f2() {}
                                   """);

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(13);
    softly.assertAll();
  }

  @Test
  void undefinedMacroWithHashParameter() {
    List<Token> tokens = lexer.lex("BOOST_PP_EXPAND(#) define BOOST_FT_config_valid 1");
    assertThat(tokens).hasSize(8);
  }

}
