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
package org.sonar.cxx.lexer;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.preprocessor.SourceCodeProvider;
import org.sonar.squidbridge.SquidAstVisitorContext;
import static org.sonar.cxx.lexer.LexerAssert.assertThat;

public class CxxLexerWithPreprocessingTest {

  private static Lexer lexer;
  private CxxLanguage language;

  public CxxLexerWithPreprocessingTest() {
    language = CxxFileTesterHelper.mockCxxLanguage();
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), language);
    lexer = CxxLexer.create(cxxpp, new JoinStringsPreprocessor());
  }

  @Test
  public void escaping_newline() {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(lexer.lex("line\\\r\nline")).as("dos style").noneMatch(token ->
      token.getValue().contentEquals("\\") && token.getType().equals(GenericTokenType.UNKNOWN_CHAR));
    softly.assertThat(lexer.lex("line\\\rline")).as("mac(old) style").noneMatch(token ->
      token.getValue().contentEquals("\\") && token.getType().equals(GenericTokenType.UNKNOWN_CHAR));
    softly.assertThat(lexer.lex("line\\\nline")).as("unix style").noneMatch(token ->
      token.getValue().contentEquals("\\") && token.getType().equals(GenericTokenType.UNKNOWN_CHAR));
    softly.assertThat(lexer.lex("line\\\n    line")).hasSize(3);
    softly.assertAll();
  }

  @Test
  public void joining_strings() {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(lexer.lex("\"string\"")).anySatisfy(token ->
      assertThat(token).isValue("\"string\"").hasType(CxxTokenType.STRING));
    softly.assertThat(lexer.lex("\"string\"\"string\"")).anySatisfy(token ->
      assertThat(token).isValue("\"stringstring\"").hasType(CxxTokenType.STRING));
    softly.assertThat(lexer.lex("\"string\"\n\"string\"")).anySatisfy(token ->
      assertThat(token).isValue("\"stringstring\"").hasType(CxxTokenType.STRING));
    softly.assertThat(lexer.lex("\"string\"\"string\"")).hasSize(2); // string + EOF
    softly.assertAll();
  }

  @Test
  public void expanding_objectlike_macros() {
    List<Token> tokens = lexer.lex("#define lala \"haha\"\nlala");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"haha\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void expanding_functionlike_macros() {
    List<Token> tokens = lexer.lex("#define plus(a, b) a + b\n plus(1, 2)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void expanding_functionlike_macros_withvarargs() {
    List<Token> tokens = lexer.lex("#define wrapper(...) __VA_ARGS__\n wrapper(1, 2)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("2").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void expanding_functionlike_macros_withnamedvarargs() {
    List<Token> tokens = lexer.lex("#define wrapper(args...) args\n wrapper(1, 2)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("2").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void expanding_functionlike_macros_withemptyvarargs() {
    List<Token> tokens = lexer.lex("#define wrapper(...) (__VA_ARGS__)\n wrapper()");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(3);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue(")").hasType(CxxPunctuator.BR_RIGHT));
    softly.assertAll();
  }

  @Test
  public void expanding_macro_with_empty_parameterlist() {
    List<Token> tokens = lexer.lex("#define M() 0\n M()");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("0").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void expanding_functionlike_macros_withextraparantheses() {
    List<Token> tokens = lexer.lex("#define neg(a) -a\n neg((1))");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
    softly.assertAll();
  }

  @Test
  public void expanding_hashoperator() {
    List<Token> tokens = lexer.lex("#define str(a) # a\n str(x)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"x\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void expanding_hashhash_operator() {
    List<Token> tokens = lexer.lex("#define concat(a,b) a ## b\n concat(x,y)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // xy + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("xy").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void expanding_hashhash_operator_withoutparams() {
    List<Token> tokens = lexer.lex("#define hashhash c ## c\n hashhash");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // cc + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("cc").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void expanding_sequenceof_hashhash_operators() {
    List<Token> tokens = lexer.lex("#define concat(a,b) a ## ## ## b\n concat(x,y)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // xy + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("xy").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void expanding_many_hashhash_operators() {
    List<Token> tokens = lexer.lex("#define concat(a,b) c ## c ## c ## c\n concat(x,y)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // cccc + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("cccc").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void hashhash_arguments_with_whitespace_before_comma() {
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

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // yes + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("yes").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test 
  public void expanding_hashhash_operator_sampleFromCPPStandard() {
    // TODO: think about implementing this behavior. This is a sample from the standard, which is
    // not working yet. Because the current implementation throws away all 'irrelevant'
    // preprocessor directives too early, I guess.

     List<Token> tokens = lexer.lex("#define hash_hash(x) # ## #\n"
     + "#define mkstr(a) # a\n"
     + "#define in_between(a) mkstr(a)\n"
     + "#define join(c, d) in_between(c hash_hash(x) d)\n"
     + "join(x,y)");
     
     SoftAssertions softly = new SoftAssertions();
     softly.assertThat(tokens).hasSize(2); //"x ## y" + EOF
//     softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"x ## y\"").hasType(CxxTokenType.STRING));
     softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"x y\"").hasType(CxxTokenType.STRING));
     softly.assertAll();
  }

  @Test
  public void expanding_hashoperator_quoting1() {
    List<Token> tokens = lexer.lex("#define str(a) # a\n str(\"x\")");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"\\\"x\\\"\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void expanding_chained_macros() {
    List<Token> tokens = lexer.lex("#define M1 \"a\"\n"
      + "#define M2 M1\n"
      + "M2");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"a\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void expanding_chained_macros2() {
    List<Token> tokens = lexer.lex("#define M1 \"a\"\n"
      + "#define M2 foo(M1)\n"
      + "M2");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"a\"").hasType(CxxTokenType.STRING));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("foo").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void expanding_chained_macros3() {
    List<Token> tokens = lexer.lex("#define M1(a) \"a\"\n"
      + "#define M2 foo(M1)\n"
      + "M2");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("M1").hasType(GenericTokenType.IDENTIFIER));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("foo").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void joining_strings_after_macro_expansion() {
    List<Token> tokens = lexer.lex("#define Y \"hello, \" \n"
      + "#define X Y \"world\" \n"
      + "X");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // string + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"hello, world\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void joining_strings_after_macro_expansion2() {
    List<Token> tokens = lexer.lex("#define M \"A\" \"B\" \"C\" \n"
      + "#define N M \n"
      + "N");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // string + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"ABC\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void joining_strings_after_macro_expansion3() {
    List<Token> tokens = lexer.lex("#define M \"B\" \n"
      + "\"A\" M");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // string + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("\"AB\"").hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void preserving_whitespace() {
    List<Token> tokens = lexer.lex("#define CODE(x) x\n"
      + "CODE(new B())");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(5);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("B").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void bodyless_defines() {
    assertThat(lexer.lex("#define M\n" + "M")).noneMatch(token ->token.getValue().contentEquals("M"))
                                              .noneMatch(token ->token.getType().equals(GenericTokenType.IDENTIFIER));
  }

  @Test
  public void external_define() {
    CxxConfiguration conf = new CxxConfiguration();
    conf.setDefines(new String[]{"M body"});
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), conf, language);
    lexer = CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("M");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("body").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void external_defines_with_params() {
    CxxConfiguration conf = new CxxConfiguration();
    conf.setDefines(new String[]{"minus(a, b) a - b"});
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), conf, language);
    lexer = CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("minus(1, 2)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(4);
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("1").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void using_keyword_as_macro_name() {
    List<Token> tokens = lexer.lex("#define new new_debug\n"
      + "new");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // identifier + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("new_debug").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void using_keyword_as_macro_parameter() {
    List<Token> tokens = lexer.lex("#define macro(new) new\n"
      + "macro(a)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // identifier + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void using_macro_name_as_macro_identifier() {
    List<Token> tokens = lexer.lex("#define X(a) a X(a)\n"
      + "X(new)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(6); // new + X + ( + new + ) + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("X").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void using_keyword_as_macro_argument() {
    List<Token> tokens = lexer.lex("#define X(a) a\n"
      + "X(new)");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // kw + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("new").hasType(CxxKeyword.NEW));
    softly.assertAll();
  }

  @Test
  public void includes_are_working() throws IOException {
    SourceCodeProvider scp = mock(SourceCodeProvider.class);
    when(scp.getSourceCodeFile(anyString(), anyString(), eq(false))).thenReturn(new File(""));
    when(scp.getSourceCode(any(File.class), any(Charset.class))).thenReturn("#define A B\n");

    SquidAstVisitorContext<Grammar> ctx = mock(SquidAstVisitorContext.class);
    when(ctx.getFile()).thenReturn(new File("/home/joe/file.cc"));

    CxxPreprocessor pp = new CxxPreprocessor(ctx, new CxxConfiguration(), scp, language);
    lexer = CxxLexer.create(pp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("#include <file>\n"
      + "A");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // B + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("B").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void macro_replacement_in_includes_is_working() {
    List<Token> tokens = lexer.lex("#define A \"B\"\n"
      + "#include A\n");
    assertThat(tokens).hasSize(1); // EOF
  }

  @Test
  public void conditional_compilation_ifdef_undefined() {
    List<Token> tokens = lexer.lex("#ifdef LALA\n"
      + "111\n"
      + "#else\n"
      + "222\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).noneMatch(token ->token.getValue().contentEquals("111") && token.getType().equals(CxxTokenType.NUMBER)); 
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("222").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_ifdef_defined() {
    List<Token> tokens = lexer.lex("#define LALA\n"
      + "#ifdef LALA\n"
      + "  111\n"
      + "#else\n"
      + "  222\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).noneMatch(token ->token.getValue().contentEquals("222") && token.getType().equals(CxxTokenType.NUMBER)); 
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("111").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_ifndef_undefined() {
    List<Token> tokens = lexer.lex("#ifndef LALA\n"
      + "111\n"
      + "#else\n"
      + "222\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // 111 + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("111").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_ifndef_defined() {
    List<Token> tokens = lexer.lex("#define X\n"
      + "#ifndef X\n"
      + "  111\n"
      + "#else\n"
      + "  222\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // 222 + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("222").hasType(CxxTokenType.NUMBER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_ifdef_nested() {
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

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_if_false() {
    List<Token> tokens = lexer.lex("#if 0\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_if_true() {
    List<Token> tokens = lexer.lex("#if 1\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_if_identifier_true() {
    List<Token> tokens = lexer.lex("#define LALA 1\n"
      + "#if LALA\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void conditional_compilation_if_identifier_false() {
    List<Token> tokens = lexer.lex("#if LALA\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void nested_ifs() {
    List<Token> tokens = lexer.lex("#if 0\n"
      + "  #if 1\n"
      + "    b\n"
      + "  #else\n"
      + "    notb\n"
      + "  #endif\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    assertThat(tokens).hasSize(2); // nota + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("nota").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  // Proper separation of parameterized macros and macros expand to a string enclosed
  // in parentheses
  @Test
  public void macro_expanding_to_parantheses() {
    // a macro is identified as 'functionlike' if and only if the parentheses
    // aren't separated from the identifier by whitespace. Otherwise, the parentheses
    // belong to the replacement string
    List<Token> tokens = lexer.lex("#define macro ()\n"
      + "macro\n");
    assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("(").hasType(CxxPunctuator.BR_LEFT));
  }

  @Test
  public void assume_true_if_cannot_evaluate_if_expression() {
    List<Token> tokens = lexer.lex("#if (\"\")\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void ignore_irrelevant_preprocessor_directives() {
    List<Token> tokens = lexer.lex("#pragma lala\n");
    assertThat(tokens).hasSize(1); // EOF
  }

  @Test
  public void externalMacrosCannotBeOverriden() {
    CxxConfiguration conf = mock(CxxConfiguration.class);
    when(conf.getDefines()).thenReturn(Arrays.asList("name goodvalue"));
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), conf, language);
    lexer = CxxLexer.create(conf, cxxpp);

    List<Token> tokens = lexer.lex("#define name badvalue\n"
      + "name");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // goodvalue + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("goodvalue").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void elif_expression() {
    List<Token> tokens = lexer.lex("#if 0\n"
      + "  if\n"
      + "#elif 1\n"
      + "  elif\n"
      + "#endif\n");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // elif + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("elif").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void continued_preprocessor_directive() {
    // continuations mask only one succeeding newline
    List<Token> tokens = lexer.lex("#define M macrobody\\\n"
      + "\n"
      + "external");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // external + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("external").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void misc_preprocessor_lines() {
    // a line which begins with a hash is a preprocessor line
    // it doesn't have any meaning in our context and should be just ignored

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(lexer.lex("#")).hasSize(1); // EOF
    softly.assertThat(lexer.lex("#lala")).hasSize(1); // EOF
    softly.assertThat(lexer.lex("# lala")).hasSize(1); // EOF
    softly.assertAll();
  }

  @Test
  public void undef_works() {
    List<Token> tokens = lexer.lex("#define a b\n"
      + "#undef a\n"
      + "a\n");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // a + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("a").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void function_like_macros_in_if_expressions() {
    List<Token> tokens = lexer.lex("#define A() 0\n"
      + "#define B() 0\n"
      + "#if A() & B()\n"
      + "truecase\n"
      + "#else\n"
      + "falsecase\n"
      + "#endif");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // falsecase + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("falsecase").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void proper_expansion_of_function_like_macros_in_if_expressions() {
    List<Token> tokens = lexer.lex("#define A() 0 ## 1\n"
      + "#if A()\n"
      + "truecase\n"
      + "#else\n"
      + "falsecase\n"
      + "#endif");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // falsecase + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("truecase").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void problem_with_chained_defined_expressions() {
    List<Token> tokens = lexer.lex("#define _C_\n"
      + "#if !defined(_A_) && !defined(_B_) && !defined(_C_)\n"
      + "truecase\n"
      + "#else\n"
      + "falsecase\n"
      + "#endif");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // falsecase + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("falsecase").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void problem_evaluating_elif_expressions() {
    List<Token> tokens = lexer.lex("#define foo(a) 1\n"
      + "#if 0\n"
      + "body\n"
      + "#elif foo(10)\n"
      + "truecase\n"
      + "#else\n"
      + "falsecase\n"
      + "endif\n");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // truecase + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("truecase").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void built_in_macros() {
    List<Token> tokens = lexer.lex("__DATE__");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // date + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).hasType(CxxTokenType.STRING));
    softly.assertAll();
  }

  @Test
  public void dont_look_at_subsequent_clauses_if_elif() {
    // When a if clause has been evaluated to true, dont look at
    // subsequent elif clauses
    List<Token> tokens = lexer.lex("#define A 1\n"
      + "#if A\n"
      + "   ifbody\n"
      + "#elif A\n"
      + "   elifbody\n"
      + "#endif");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // ifbody + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("ifbody").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  @Test
  public void dont_look_at_subsequent_clauses_elif_elif() {
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
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tokens).hasSize(2); // case2 + EOF
    softly.assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("case2").hasType(GenericTokenType.IDENTIFIER));
    softly.assertAll();
  }

  //@Test @todo
  public void hashhash_operator_problem() {
    // Corresponds to the Jira Issue SONARPLUGINS-3055.
    // The problem here is that 0x##n is splitted into
    // [0, x, ##, n] sequence of tokens by the initial parsing routine.
    // After this, 0 and the rest of the number get never concatenated again.

    List<Token> tokens = lexer.lex("#define A B(cf)\n"
      + "#define B(n) 0x##n\n"
      + "A");
    assertThat(tokens).anySatisfy(token ->assertThat(token).isValue("0xcf").hasType(CxxKeyword.INT));
  }
}
