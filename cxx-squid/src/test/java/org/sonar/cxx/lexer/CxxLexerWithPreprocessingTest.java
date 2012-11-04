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
package org.sonar.cxx.lexer;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.preprocessor.SourceCodeProvider;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CxxLexerWithPreprocessingTest {

  private static Lexer lexer;

  public CxxLexerWithPreprocessingTest(){
    lexer = CxxLexer.create(new CxxPreprocessor(), new JoinStringsPreprocessor());
  }

  @Test
  public void escaping_newline() {
    assertThat("dos style", lexer.lex("line\\\r\nline"), not(hasToken("\\", GenericTokenType.UNKNOWN_CHAR)));
    assertThat("mac(old) style", lexer.lex("line\\\rline"), not(hasToken("\\", GenericTokenType.UNKNOWN_CHAR)));
    assertThat("unix style", lexer.lex("line\\\nline"), not(hasToken("\\", GenericTokenType.UNKNOWN_CHAR)));

    assertThat(lexer.lex("line\\\n    line")).hasSize(3);
  }

  @Test
  public void joining_strings() {
    assertThat(lexer.lex("\"string\""), hasToken("\"string\"", CxxTokenType.STRING));
    assertThat(lexer.lex("\"string\"\"string\""), hasToken("\"stringstring\"", CxxTokenType.STRING));
    assertThat(lexer.lex("\"string\"\n\"string\""), hasToken("\"stringstring\"", CxxTokenType.STRING));
    assertThat(lexer.lex("\"string\"\"string\"")).hasSize(2); // string + EOF
  }

  @Test
  public void expanding_objectlike_macros() {
    List<Token> tokens = lexer.lex("#define lala \"haha\"\nlala");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("\"haha\"", CxxTokenType.STRING));
  }

  @Test
  public void expanding_functionlike_macros() {
    List<Token> tokens = lexer.lex("#define plus(a, b) a + b\n plus(1, 2)");
    assertThat(tokens).hasSize(4);
    assertThat(tokens, hasToken("1", CxxTokenType.NUMBER));
  }

  @Test
  public void expanding_macro_with_empty_parameterlist() {
    List<Token> tokens = lexer.lex("#define M() 0\n M()");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("0", CxxTokenType.NUMBER));
  }
  
  @Test
  public void expanding_functionlike_macros_withextraparantheses() {
    List<Token> tokens = lexer.lex("#define neg(a) -a\n neg((1))");
    assertThat(tokens).hasSize(5);
    assertThat(tokens, hasToken("(", CxxPunctuator.BR_LEFT));
  }

  @Test
  public void expanding_hashoperator() {
    List<Token> tokens = lexer.lex("#define str(a) # a\n str(x)");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("\"x\"", CxxTokenType.STRING));
  }

  @Test
  public void expanding_hashoperator_quoting1() {
    List<Token> tokens = lexer.lex("#define str(a) # a\n str(\"x\")");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("\"\\\"x\\\"\"", CxxTokenType.STRING));
  }

  @Test
  public void expanding_chained_macros() {
    List<Token> tokens = lexer.lex("#define M1 \"a\"\n"
      + "#define M2 M1\n"
      + "M2");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("\"a\"", CxxTokenType.STRING));
  }

  @Test
  public void expanding_chained_macros2() {
    List<Token> tokens = lexer.lex("#define M1 \"a\"\n"
      + "#define M2 foo(M1)\n"
      + "M2");
    assertThat(tokens).hasSize(5);
    assertThat(tokens, hasToken("\"a\"", CxxTokenType.STRING));
    assertThat(tokens, hasToken("foo", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void expanding_chained_macros3() {
    List<Token> tokens = lexer.lex("#define M1(a) \"a\"\n"
      + "#define M2 foo(M1)\n"
      + "M2");
    assertThat(tokens).hasSize(5);
    assertThat(tokens, hasToken("M1", GenericTokenType.IDENTIFIER));
    assertThat(tokens, hasToken("foo", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void joining_strings_after_macro_expansion() {
    List<Token> tokens = lexer.lex("#define Y \"hello, \" \n"
      + "#define X Y \"world\" \n"
      + "X");
    assertThat(tokens).hasSize(2); // string + EOF
    assertThat(tokens, hasToken("\"hello, world\"", CxxTokenType.STRING));
  }

  @Test
  public void joining_strings_after_macro_expansion2() {
    List<Token> tokens = lexer.lex("#define M \"A\" \"B\" \"C\" \n"
      + "#define N M \n"
      + "N");
    assertThat(tokens).hasSize(2); // string + EOF
    assertThat(tokens, hasToken("\"ABC\"", CxxTokenType.STRING));
  }

  @Test
  public void joining_strings_after_macro_expansion3() {
    List<Token> tokens = lexer.lex("#define M \"B\" \n"
      + "\"A\" M");
    assertThat(tokens).hasSize(2); // string + EOF
    assertThat(tokens, hasToken("\"AB\"", CxxTokenType.STRING));
  }

  @Test
  public void preserving_whitespace() {
    List<Token> tokens = lexer.lex("#define CODE(x) x\n"
      + "CODE(new B())");
    assertThat(tokens).hasSize(5);
    assertThat(tokens, hasToken("new", CxxKeyword.NEW));
    assertThat(tokens, hasToken("B", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void bodyless_defines() {
    assertThat(lexer.lex("#define M\n" + "M"), not(hasToken("M", GenericTokenType.IDENTIFIER)));
  }

  @Test
  public void external_define(){
    CxxConfiguration conf = new CxxConfiguration();
    conf.setDefines(Arrays.asList("M body"));
    lexer = CxxLexer.create(conf, new CxxPreprocessor(conf), new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("M");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("body", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void external_defines_with_params(){
    CxxConfiguration conf = new CxxConfiguration();
    conf.setDefines(Arrays.asList("minus(a, b) a - b"));
    lexer = CxxLexer.create(conf, new CxxPreprocessor(conf), new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("minus(1, 2)");
    assertThat(tokens).hasSize(4);
    assertThat(tokens, hasToken("1", CxxTokenType.NUMBER));
  }

  @Test
  public void using_keyword_as_macro_name(){
    List<Token> tokens = lexer.lex("#define new new_debug\n"
                                   + "new");
    assertThat(tokens).hasSize(2); // identifier + EOF
    assertThat(tokens, hasToken("new_debug", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void using_keyword_as_macro_parameter(){
    List<Token> tokens = lexer.lex("#define macro(new) new\n"
                                   + "macro(a)");
    assertThat(tokens).hasSize(2); // identifier + EOF
    assertThat(tokens, hasToken("a", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void using_macro_name_as_macro_identifier(){
    List<Token> tokens = lexer.lex("#define X(a) a X(a)\n"
                                   + "X(new)");
    assertThat(tokens).hasSize(6); // new + X + ( + new + ) + EOF
    assertThat(tokens, hasToken("new", CxxKeyword.NEW));
    assertThat(tokens, hasToken("X",  GenericTokenType.IDENTIFIER));
  }
  
  @Test
  public void using_keyword_as_macro_argument(){
    List<Token> tokens = lexer.lex("#define X(a) a\n"
                                   + "X(new)");
    assertThat(tokens).hasSize(2); // kw + EOF
    assertThat(tokens, hasToken("new", CxxKeyword.NEW));
  }

  @Test
  public void includes_are_working(){
    SourceCodeProvider scp = mock(SourceCodeProvider.class);
    when(scp.getSourceCodeFile(anyString(), anyString())).thenReturn(new File(""));
    when(scp.getSourceCode(any(File.class))).thenReturn("#define A B\n");

    SquidAstVisitorContext ctx = mock(SquidAstVisitorContext.class);
    when(ctx.getFile()).thenReturn(new File("/home/joe/file.cc"));

    CxxPreprocessor pp = new CxxPreprocessor(new CxxConfiguration(), ctx, scp);
    lexer = CxxLexer.create(pp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("#include <file>\n"
                                   + "A");
    assertThat(tokens).hasSize(2); // B + EOF
    assertThat(tokens, hasToken("B", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void conditional_compilation_ifdef_undefined() {
    List<Token> tokens = lexer.lex("#ifdef LALA\n"
                                   + "111\n"
                                   + "#else\n"
                                   + "222\n"
                                   + "#endif\n");
    
    assertThat(tokens, not(hasToken("111", CxxTokenType.NUMBER)));
    assertThat(tokens, hasToken("222", CxxTokenType.NUMBER));
  }
  
  @Test
  public void conditional_compilation_ifdef_defined() {
    List<Token> tokens = lexer.lex("#define LALA\n"
                                   + "#ifdef LALA\n"
                                   + "  111\n"
                                   + "#else\n"
                                   + "  222\n"
                                   + "#endif\n");
    
    assertThat(tokens, hasToken("111", CxxTokenType.NUMBER));
    assertThat(tokens, not(hasToken("222", CxxTokenType.NUMBER)));
  }

  @Test
  public void conditional_compilation_ifndef_undefined() {
    List<Token> tokens = lexer.lex("#ifndef LALA\n"
                                   + "111\n"
                                   + "#else\n"
                                   + "222\n"
                                   + "#endif\n");
    
    assertThat(tokens, hasToken("111", CxxTokenType.NUMBER));
    assertThat(tokens).hasSize(2); // 111 + EOF
  }
  
  @Test
  public void conditional_compilation_ifndef_defined() {
    List<Token> tokens = lexer.lex("#define X\n"
                                   + "#ifndef X\n"
                                   + "  111\n"
                                   + "#else\n"
                                   + "  222\n"
                                   + "#endif\n");
    
    assertThat(tokens, hasToken("222", CxxTokenType.NUMBER));
    assertThat(tokens).hasSize(2); // 222 + EOF
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
    
    assertThat(tokens, hasToken("nota", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // nota + EOF
  }
}
