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
package org.sonar.cxx.lexer;

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.config.CxxSquidConfiguration;
import static org.sonar.cxx.lexer.LexerAssert.assertThat;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxLexer;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.preprocessor.SourceCodeProvider;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class CxxLexerWithPreprocessingTest {

  private static Lexer lexer;
  private final SquidAstVisitorContext<Grammar> context;

  public CxxLexerWithPreprocessingTest() {
    var file = new File("snippet.cpp").getAbsoluteFile();
    context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    var cxxpp = new CxxPreprocessor(context);
    lexer = CxxLexer.create(cxxpp, new JoinStringsPreprocessor());
  }

  @Test
  void escaping_newline() {
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
  void joining_strings() {
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

  @Test
  void expanding_objectlike_macros() {
    List<Token> tokens = lexer.lex("#define lala \"haha\"\nlala");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"haha\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void expanding_functionlike_macros() {
    List<Token> tokens = lexer.lex("#define plus(a, b) a + b\n plus(1, 2)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void expanding_functionlike_macros_with_varargs() {
    List<Token> tokens = lexer.lex("#define wrapper(...) __VA_ARGS__\n wrapper(1, 2)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("2").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void expanding_functionlike_macros_withnamed_varargs() {
    List<Token> tokens = lexer.lex("#define wrapper(args...) args\n wrapper(1, 2)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("2").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void expanding_functionlike_macros_with_empty_varargs() {
    List<Token> tokens = lexer.lex("#define wrapper(...) (__VA_ARGS__)\n wrapper()");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(3);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue(")").hasType(CxxPunctuator.BR_RIGHT));
    softly.assertAll();
  }

  @Test
  void expanding_macro_with_empty_parameter_list() {
    List<Token> tokens = lexer.lex("#define M() 0\n M()");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("0").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void expanding_functionlike_macros_withextraparantheses() {
    List<Token> tokens = lexer.lex("#define neg(a) -a\n neg((1))");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
    softly.assertAll();
  }

  @Test
  void expanding_hashoperator() {
    List<Token> tokens = lexer.lex("#define str(a) # a\n str(x)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"x\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void expanding_hashhash_operator() {
    List<Token> tokens = lexer.lex("#define concat(a,b) a ## b\n concat(x,y)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // xy + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("xy").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void expanding_hashhash_operator_withoutparams() {
    List<Token> tokens = lexer.lex("#define hashhash c ## c\n hashhash");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // cc + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("cc").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void expanding_sequenceof_hashhash_operators() {
    List<Token> tokens = lexer.lex("#define concat(a,b) a ## ## ## b\n concat(x,y)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // xy + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("xy").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void expanding_many_hashhash_operators() {
    List<Token> tokens = lexer.lex("#define concat(a,b) c ## c ## c ## c\n concat(x,y)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // cccc + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("cccc").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void hashhash_arguments_with_whitespace_before_comma() {
    // The blank behind FOO finds its way into the expansion.
    // This leads to expression evaluation errors. Found in boost.
    // Problem: cannot find defined behaviour in the C++ Standard for
    // such cases.
    // Corresponds to the Jira issue SONARPLUGINS-3060
    List<Token> tokens = lexer.lex("#define FOOBAR 1\n"
                                     + "#define CHECK(a, b) (( a ## b + 1 == 2))\n"
                                     + "#if CHECK(FOO , BAR)\n"
                                     + "yes\n"
                                     + "#endif");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // yes + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("yes").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void expanding_hashhash_operator_sampleFromCPPStandard() {
    // TODO: think about implementing this behavior. This is a sample from the standard, which is
    // not working yet. Because the current implementation throws away all 'irrelevant'
    // preprocessor directives too early, I guess.

    List<Token> tokens = lexer.lex("#define hash_hash(x) # ## #\n"
                                     + "#define mkstr(a) # a\n"
                                     + "#define in_between(a) mkstr(a)\n"
                                     + "#define join(c, d) in_between(c hash_hash(x) d)\n"
                                     + "join(x,y)");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); //"x ## y" + EOF
//     softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"x ## y\"").hasType(CxxTokenType.STRING));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"x y\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void expanding_hashoperator_quoting1() {
    List<Token> tokens = lexer.lex("#define str(a) # a\n str(\"x\")");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"\\\"x\\\"\"")
      .hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void expanding_chained_macros() {
    List<Token> tokens = lexer.lex("#define M1 \"a\"\n"
                                     + "#define M2 M1\n"
                                     + "M2");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"a\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void expanding_chained_macros2() {
    List<Token> tokens = lexer.lex("#define M1 \"a\"\n"
                                     + "#define M2 foo(M1)\n"
                                     + "M2");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"a\"").hasType(CxxTokenType.STRING));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("foo").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void expanding_chained_macros3() {
    List<Token> tokens = lexer.lex("#define M1(a) \"a\"\n"
                                     + "#define M2 foo(M1)\n"
                                     + "M2");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("M1").hasType(GenericTokenType.IDENTIFIER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("foo").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void joining_strings_after_macro_expansion() {
    List<Token> tokens = lexer.lex("#define Y \"hello, \" \n"
                                     + "#define X Y \"world\" \n"
                                     + "X");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // string + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"hello, world\"").hasType(
      CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void joining_strings_after_macro_expansion2() {
    List<Token> tokens = lexer.lex("#define M \"A\" \"B\" \"C\" \n"
                                     + "#define N M \n"
                                     + "N");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // string + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"ABC\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void joining_strings_after_macro_expansion3() {
    List<Token> tokens = lexer.lex("#define M \"B\" \n"
                                     + "\"A\" M");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // string + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("\"AB\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void preserving_whitespace() {
    List<Token> tokens = lexer.lex("#define CODE(x) x\n"
                                     + "CODE(new B())");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("B").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void bodyless_defines() {
    assertThat(lexer.lex("#define M\n" + "M")).noneMatch(token -> token.getValue().contentEquals("M"))
      .noneMatch(token -> token.getType().equals(GenericTokenType.IDENTIFIER));
  }

  @Test
  void external_define() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
                    "M body");
    var cxxpp = new CxxPreprocessor(context, squidConfig);
    lexer = CxxLexer.create(squidConfig.getCharset(), cxxpp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("M");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("body").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void external_defines_with_params() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
                    "minus(a, b) a - b");
    var cxxpp = new CxxPreprocessor(context, squidConfig);
    lexer = CxxLexer.create(squidConfig.getCharset(), cxxpp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("minus(1, 2)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void using_keyword_as_macro_name() {
    List<Token> tokens = lexer.lex("#define new new_debug\n"
                                     + "new");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // identifier + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new_debug").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void using_keyword_as_macro_parameter() {
    List<Token> tokens = lexer.lex("#define macro(new) new\n"
                                     + "macro(a)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // identifier + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void using_macro_name_as_macro_identifier() {
    List<Token> tokens = lexer.lex("#define X(a) a X(a)\n"
                                     + "X(new)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(6); // new + X + ( + new + ) + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("X").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void using_keyword_as_macro_argument() {
    List<Token> tokens = lexer.lex("#define X(a) a\n"
                                     + "X(new)");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // kw + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertAll();
  }

  @Test
  void includes_are_working() throws IOException {
    SourceCodeProvider scp = mock(SourceCodeProvider.class);
    when(scp.getSourceCodeFile(anyString(), eq(false))).thenReturn(new File("file"));
    when(scp.getSourceCode(any(File.class), any(Charset.class))).thenReturn("#define A B\n");

    SquidAstVisitorContext<Grammar> ctx = mock(SquidAstVisitorContext.class);
    when(ctx.getFile()).thenReturn(new File("/home/joe/file.cc"));

    var pp = new CxxPreprocessor(ctx, new CxxSquidConfiguration(), scp);
    lexer = CxxLexer.create(pp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("#include <file>\n"
                                     + "A");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // B + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("B").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void macro_replacement_in_includes_is_working() {
    List<Token> tokens = lexer.lex("#define A \"B\"\n"
                                     + "#include A\n");
    assertThat(tokens).hasSize(1); // EOF
  }

  @Test
  void conditional_compilation_ifdef_undefined() {
    List<Token> tokens = lexer.lex("#ifdef LALA\n"
                                     + "111\n"
                                     + "#else\n"
                                     + "222\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).noneMatch(token -> token.getValue().contentEquals("111") && token.getType().equals(
      CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("222").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_ifdef_defined() {
    List<Token> tokens = lexer.lex("#define LALA\n"
                                     + "#ifdef LALA\n"
                                     + "  111\n"
                                     + "#else\n"
                                     + "  222\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).noneMatch(token -> token.getValue().contentEquals("222") && token.getType().equals(
      CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("111").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_ifndef_undefined() {
    List<Token> tokens = lexer.lex("#ifndef LALA\n"
                                     + "111\n"
                                     + "#else\n"
                                     + "222\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // 111 + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("111").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_ifndef_defined() {
    List<Token> tokens = lexer.lex("#define X\n"
                                     + "#ifndef X\n"
                                     + "  111\n"
                                     + "#else\n"
                                     + "  222\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // 222 + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("222").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_ifdef_nested1() {
    List<Token> tokens = lexer.lex("#define B\n"
                                     + "#ifdef A\n"
                                     + "  a\n"
                                     + "  #ifdef B\n"
                                     + "    b\n"
                                     + "  #else\n"
                                     + "    notb\n"
                                     + "  #endif\n"
                                     + "  a1\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_ifdef_nested2() {
    List<Token> tokens = lexer.lex("#if !defined A\n"
                                     + "#define A\n"
                                     + "#ifdef B\n"
                                     + "  b\n"
                                     + "#else\n"
                                     + "  notb\n"
                                     + "#endif\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // notb + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("notb").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_if_false() {
    List<Token> tokens = lexer.lex("#if 0\n"
                                     + "  a\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_if_true() {
    List<Token> tokens = lexer.lex("#if 1\n"
                                     + "  a\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_if_identifier_true() {
    List<Token> tokens = lexer.lex("#define LALA 1\n"
                                     + "#if LALA\n"
                                     + "  a\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void conditional_compilation_if_identifier_false() {
    List<Token> tokens = lexer.lex("#if LALA\n"
                                     + "  a\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void nested_ifs() {
    List<Token> tokens = lexer.lex("#if 0\n"
                                     + "  #if 1\n"
                                     + "    b\n"
                                     + "  #else\n"
                                     + "    notb\n"
                                     + "  #endif\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  // Proper separation of parameterized macros and macros expand to a string enclosed
  // in parentheses
  @Test
  void macro_expanding_to_parantheses() {
    // a macro is identified as 'functionlike' if and only if the parentheses
    // aren't separated from the identifier by whitespace. Otherwise, the parentheses
    // belong to the replacement string
    List<Token> tokens = lexer.lex("#define macro ()\n"
                                     + "macro\n");
    assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
  }

  @Test
  void assume_true_if_cannot_evaluate_if_expression() {
    List<Token> tokens = lexer.lex("#if (\"\")\n"
                                     + "  a\n"
                                     + "#else\n"
                                     + "  nota\n"
                                     + "#endif\n");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void ignore_irrelevant_preprocessor_directives() {
    List<Token> tokens = lexer.lex("#pragma lala\n");
    assertThat(tokens).hasSize(1); // EOF
  }

  @Test
  void overwriteGlobalMacro() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.DEFINES, "macro globalvalue");
    var cxxpp = new CxxPreprocessor(context, squidConfig);
    lexer = CxxLexer.create(squidConfig.getCharset(), cxxpp);

    List<Token> tokens = lexer.lex("#define macro overriden\n"
                                     + "macro");

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
    var cxxpp = new CxxPreprocessor(context, squidConfig);

    Lexer l = CxxLexer.create(squidConfig.getCharset(), cxxpp);
    List<Token> tokens = l.lex("__LINE__");

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
    var cxxpp = new CxxPreprocessor(context, squidConfig);

    Lexer l = CxxLexer.create(squidConfig.getCharset(), cxxpp);
    List<Token> tokens = l.lex("__LINE__");

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

    SourceCodeProvider provider = mock(SourceCodeProvider.class);
    when(provider.getSourceCodeFile(Mockito.eq(forceIncludePath), Mockito.anyBoolean()))
      .thenReturn(forceIncludeFile);
    when(provider.getSourceCode(Mockito.eq(forceIncludeFile), Mockito.any(Charset.class)))
      .thenReturn("#define __LINE__ 345");

    var cxxpp = new CxxPreprocessor(context, squidConfig, provider);
    Lexer l = CxxLexer.create(squidConfig.getCharset(), cxxpp);

    List<Token> tokens = l.lex("__LINE__\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // __LINE__ + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("345").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  void elif_expression() {
    List<Token> tokens = lexer.lex("#if 0\n"
                                     + "  if\n"
                                     + "#elif 1\n"
                                     + "  elif\n"
                                     + "#endif\n");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // elif + EOF
    softly.assertThat(tokens)
      .anySatisfy(token -> assertThat(token).isValue("elif").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void continued_preprocessor_directive() {
    // continuations mask only one succeeding newline
    List<Token> tokens = lexer.lex("#define M macrobody\\\n"
                                     + "\n"
                                     + "external");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // external + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("external").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void misc_preprocessor_lines() {
    // a line which begins with a hash is a preprocessor line
    // it doesn't have any meaning in our context and should be just ignored

    var softly = new SoftAssertions();
    softly.assertThat(lexer.lex("#")).hasSize(1); // EOF
    softly.assertThat(lexer.lex("#lala")).hasSize(1); // EOF
    softly.assertThat(lexer.lex("# lala")).hasSize(1); // EOF
    softly.assertAll();
  }

  @Test
  void undef_works() {
    List<Token> tokens = lexer.lex("#define a b\n"
                                     + "#undef a\n"
                                     + "a\n");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void function_like_macros_in_if_expressions() {
    List<Token> tokens = lexer.lex("#define A() 0\n"
                                     + "#define B() 0\n"
                                     + "#if A() & B()\n"
                                     + "truecase\n"
                                     + "#else\n"
                                     + "falsecase\n"
                                     + "#endif");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // falsecase + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("falsecase").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void proper_expansion_of_function_like_macros_in_if_expressions() {
    List<Token> tokens = lexer.lex("#define A() 0 ## 1\n"
                                     + "#if A()\n"
                                     + "truecase\n"
                                     + "#else\n"
                                     + "falsecase\n"
                                     + "#endif");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // falsecase + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("truecase").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void problem_with_chained_defined_expressions() {
    List<Token> tokens = lexer.lex("#define _C_\n"
                                     + "#if !defined(_A_) && !defined(_B_) && !defined(_C_)\n"
                                     + "truecase\n"
                                     + "#else\n"
                                     + "falsecase\n"
                                     + "#endif");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // falsecase + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("falsecase").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void problem_evaluating_elif_expressions() {
    List<Token> tokens = lexer.lex("#define foo(a) 1\n"
                                     + "#if 0\n"
                                     + "body\n"
                                     + "#elif foo(10)\n"
                                     + "truecase\n"
                                     + "#else\n"
                                     + "falsecase\n"
                                     + "endif\n");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // truecase + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("truecase").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void built_in_macros() {
    List<Token> tokens = lexer.lex("__DATE__");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // date + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  void dont_look_at_subsequent_clauses_if_elif() {
    // When a if clause has been evaluated to true, dont look at
    // subsequent elif clauses
    List<Token> tokens = lexer.lex("#define A 1\n"
                                     + "#if A\n"
                                     + "   ifbody\n"
                                     + "#elif A\n"
                                     + "   elifbody\n"
                                     + "#endif");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // ifbody + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("ifbody").hasType(
      GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  void dont_look_at_subsequent_clauses_elif_elif() {
    List<Token> tokens = lexer.lex("#define SDS_ARCH_darwin_15_i86\n"
                                     + "#ifdef SDS_ARCH_freebsd_61_i86\n"
                                     + "   case1\n"
                                     + "#elif defined(SDS_ARCH_darwin_15_i86)\n"
                                     + "   case2\n"
                                     + "#elif defined(SDS_ARCH_winxp) || defined(SDS_ARCH_Interix)\n"
                                     + "   case3\n"
                                     + "#else\n"
                                     + "   case4\n"
                                     + "#endif\n");
    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // case2 + EOF
    softly.assertThat(tokens).anySatisfy(token -> assertThat(token).isValue("case2")
      .hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  //@Test @todo
  void hashhash_operator_problem() {
    // Corresponds to the Jira Issue SONARPLUGINS-3055.
    // The problem here is that 0x##n is splitted into
    // [0, x, ##, n] sequence of tokens by the initial parsing routine.
    // After this, 0 and the rest of the number get never concatenated again.

    List<Token> tokens = lexer.lex("#define A B(cf)\n"
                                     + "#define B(n) 0x##n\n"
                                     + "A");
    assertThat(tokens).anySatisfy(token -> assertThat(token)
      .isValue("0xcf")
      .hasType(CxxKeyword.INT));
  }

  @Test
  void string_problem_1903() {
    List<Token> tokens = lexer.lex("void f1() {}\n"
                                     + "#define BROKEN_DEFINE \" /a/path/*\"\n"
                                     + "void f2() {}");

    var softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(13);
    softly.assertAll();
  }

}
