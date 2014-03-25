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

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.CxxConfiguration;
//import org.sonar.cxx.api.CxxGrammar;
import com.sonar.sslr.api.Grammar;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CxxLexerWithPreprocessingTest {

  private static Lexer lexer;

  public CxxLexerWithPreprocessingTest() {
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class));
    lexer = CxxLexer.create(cxxpp, new JoinStringsPreprocessor());
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
  public void expanding_functionlike_macros_withvarargs() {
    List<Token> tokens = lexer.lex("#define wrapper(...) __VA_ARGS__\n wrapper(1, 2)");
    assertThat(tokens).hasSize(4);
    assertThat(tokens, hasToken("1", CxxTokenType.NUMBER));
    assertThat(tokens, hasToken("2", CxxTokenType.NUMBER));
  }

  @Test
  public void expanding_functionlike_macros_withnamedvarargs() {
    List<Token> tokens = lexer.lex("#define wrapper(args...) args\n wrapper(1, 2)");
    assertThat(tokens).hasSize(4);
    assertThat(tokens, hasToken("1", CxxTokenType.NUMBER));
    assertThat(tokens, hasToken("2", CxxTokenType.NUMBER));
  }

  @Test
  public void expanding_functionlike_macros_withemptyvarargs() {
    List<Token> tokens = lexer.lex("#define wrapper(...) (__VA_ARGS__)\n wrapper()");
    assertThat(tokens).hasSize(3);
    assertThat(tokens, hasToken("(", CxxPunctuator.BR_LEFT));
    assertThat(tokens, hasToken(")", CxxPunctuator.BR_RIGHT));
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
  public void expanding_hashhash_operator() {
    List<Token> tokens = lexer.lex("#define concat(a,b) a ## b\n concat(x,y)");
    assertThat(tokens).hasSize(2); // xy + EOF
    assertThat(tokens, hasToken("xy", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void expanding_hashhash_operator_withoutparams() {
    List<Token> tokens = lexer.lex("#define hashhash c ## c\n hashhash");

    assertThat(tokens).hasSize(2); // cc + EOF
    assertThat(tokens, hasToken("cc", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void expanding_sequenceof_hashhash_operators() {
    List<Token> tokens = lexer.lex("#define concat(a,b) a ## ## ## b\n concat(x,y)");
    assertThat(tokens).hasSize(2); // xy + EOF
    assertThat(tokens, hasToken("xy", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void expanding_many_hashhash_operators() {
    List<Token> tokens = lexer.lex("#define concat(a,b) c ## c ## c ## c\n concat(x,y)");
    assertThat(tokens).hasSize(2); // cccc + EOF
    assertThat(tokens, hasToken("cccc", GenericTokenType.IDENTIFIER));
  }
  
  //@Test
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
    
    assertThat(tokens).hasSize(2); // yes + EOF
    assertThat(tokens, hasToken("yes", GenericTokenType.IDENTIFIER));
  }
  
  //@Test
  public void expanding_hashhash_operator_sampleFromCPPStandard() {
    // TODO: think about implementing this behavior. This is a sample from the standard, which is
    // not working yet. Because the current implementation throws away all 'irrelevant'
    // preprocessor directives too early, I guess.

    // List<Token> tokens = lexer.lex("#define hash_hash(x) # ## #\n"
    // + "#define mkstr(a) # a\n"
    // + "#define in_between(a) mkstr(a)\n"
    // + "#define join(c, d) in_between(c hash_hash(x) d)\n"
    // + "join(x,y)");
    // assertThat(tokens).hasSize(2); //"x ## y" + EOF
    // assertThat(tokens, hasToken("\"x ## y\"", GenericTokenType.STRING));
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
  public void external_define() {
    CxxConfiguration conf = new CxxConfiguration();
    conf.setDefines(Arrays.asList("M body"));
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), conf);
    lexer = CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("M");
    assertThat(tokens).hasSize(2);
    assertThat(tokens, hasToken("body", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void external_defines_with_params() {
    CxxConfiguration conf = new CxxConfiguration();
    conf.setDefines(Arrays.asList("minus(a, b) a - b"));
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), conf);
    lexer = CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("minus(1, 2)");
    assertThat(tokens).hasSize(4);
    assertThat(tokens, hasToken("1", CxxTokenType.NUMBER));
  }

  @Test
  public void using_keyword_as_macro_name() {
    List<Token> tokens = lexer.lex("#define new new_debug\n"
      + "new");
    assertThat(tokens).hasSize(2); // identifier + EOF
    assertThat(tokens, hasToken("new_debug", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void using_keyword_as_macro_parameter() {
    List<Token> tokens = lexer.lex("#define macro(new) new\n"
      + "macro(a)");
    assertThat(tokens).hasSize(2); // identifier + EOF
    assertThat(tokens, hasToken("a", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void using_macro_name_as_macro_identifier() {
    List<Token> tokens = lexer.lex("#define X(a) a X(a)\n"
      + "X(new)");
    assertThat(tokens).hasSize(6); // new + X + ( + new + ) + EOF
    assertThat(tokens, hasToken("new", CxxKeyword.NEW));
    assertThat(tokens, hasToken("X", GenericTokenType.IDENTIFIER));
  }

  @Test
  public void using_keyword_as_macro_argument() {
    List<Token> tokens = lexer.lex("#define X(a) a\n"
      + "X(new)");
    assertThat(tokens).hasSize(2); // kw + EOF
    assertThat(tokens, hasToken("new", CxxKeyword.NEW));
  }

  @Test
  public void includes_are_working() {
    SourceCodeProvider scp = mock(SourceCodeProvider.class);
    when(scp.getSourceCodeFile(anyString(), anyString(), eq(false))).thenReturn(new File(""));
    when(scp.getSourceCode(any(File.class))).thenReturn("#define A B\n");

    SquidAstVisitorContext<Grammar> ctx = mock(SquidAstVisitorContext.class);
    when(ctx.getFile()).thenReturn(new File("/home/joe/file.cc"));

    CxxPreprocessor pp = new CxxPreprocessor(ctx, new CxxConfiguration(), scp);
    lexer = CxxLexer.create(pp, new JoinStringsPreprocessor());

    List<Token> tokens = lexer.lex("#include <file>\n"
      + "A");
    assertThat(tokens).hasSize(2); // B + EOF
    assertThat(tokens, hasToken("B", GenericTokenType.IDENTIFIER));
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

  @Test
  public void conditional_compilation_if_false() {
    List<Token> tokens = lexer.lex("#if 0\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    assertThat(tokens, hasToken("nota", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // nota + EOF
  }

  @Test
  public void conditional_compilation_if_true() {
    List<Token> tokens = lexer.lex("#if 1\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    assertThat(tokens, hasToken("a", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // a + EOF
  }

  @Test
  public void conditional_compilation_if_identifier_true() {
    List<Token> tokens = lexer.lex("#define LALA 1\n"
      + "#if LALA\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    assertThat(tokens, hasToken("a", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // a + EOF
  }

  @Test
  public void conditional_compilation_if_identifier_false() {
    List<Token> tokens = lexer.lex("#if LALA\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");

    assertThat(tokens, hasToken("nota", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // nota + EOF
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

    assertThat(tokens, hasToken("nota", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // nota + EOF
  }

  // Proper separation of parametrized macros and macros expand to a string enclosed
  // in parentheses
  @Test
  public void macro_expanding_to_parantheses() {
    // a macro is identified as 'functionlike' if and only if the parentheses
    // arent separated from the identifier by whitespace. Otherwise, the parentheses
    // belong to the replacement string
    List<Token> tokens = lexer.lex("#define macro ()\n"
      + "macro\n");
    assertThat(tokens, hasToken("(", CxxPunctuator.BR_LEFT));
  }

  @Test
  public void assume_true_if_cannot_evaluate_if_expression() {
    List<Token> tokens = lexer.lex("#if (\"\")\n"
      + "  a\n"
      + "#else\n"
      + "  nota\n"
      + "#endif\n");
    assertThat(tokens, hasToken("a", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // a + EOF
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
    CxxPreprocessor cxxpp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), conf);
    lexer = CxxLexer.create(conf, cxxpp);

    List<Token> tokens = lexer.lex("#define name badvalue\n"
      + "name");

    assertThat(tokens, hasToken("goodvalue", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // goodvalue + EOF
  }

  @Test
  public void elif_expression() {
    List<Token> tokens = lexer.lex("#if 0\n"
      + "  if\n"
      + "#elif 1\n"
      + "  elif\n"
      + "#endif\n");

    assertThat(tokens, hasToken("elif", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // elif + EOF
  }

  @Test
  public void continued_preprocessor_directive() {
    // continuations mask only one succeeding newline
    List<Token> tokens = lexer.lex("#define M macrobody\\\n"
      + "\n"
      + "external");
    assertThat(tokens, hasToken("external", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // external + EOF
  }

  @Test
  public void misc_preprocessor_lines() {
    // a line which begins with a hash is a preprocessor line
    // it doesnt have any meaning in our context and should be just ignored

    assertThat(lexer.lex("#")).hasSize(1); // EOF
    assertThat(lexer.lex("#lala")).hasSize(1); // EOF
    assertThat(lexer.lex("# lala")).hasSize(1); // EOF
  }

  @Test
  public void undef_works() {
    List<Token> tokens = lexer.lex("#define a b\n"
      + "#undef a\n"
      + "a\n");
    assertThat(tokens, hasToken("a", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // a + EOF
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
    assertThat(tokens, hasToken("falsecase", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // falsecase + EOF
  }

  @Test
  public void proper_expansion_of_function_like_macros_in_if_expressions() {
    List<Token> tokens = lexer.lex("#define A() 0 ## 1\n"
      + "#if A()\n"
      + "truecase\n"
      + "#else\n"
      + "falsecase\n"
      + "#endif");
    assertThat(tokens, hasToken("truecase", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // falsecase + EOF
  }

  @Test
  public void problem_with_chained_defined_expressions() {
    List<Token> tokens = lexer.lex("#define _C_\n"
                                   + "#if !defined(_A_) && !defined(_B_) && !defined(_C_)\n"
                                   + "truecase\n"
                                   + "#else\n"
                                   + "falsecase\n"
                                   + "#endif");
    assertThat(tokens, hasToken("falsecase", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // falsecase + EOF
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
    assertThat(tokens, hasToken("truecase", GenericTokenType.IDENTIFIER));
    assertThat(tokens).hasSize(2); // truecase + EOF
  }

  @Test
  public void built_in_macros() {
    List<Token> tokens = lexer.lex("__DATE__");
    assertThat(tokens).hasSize(2); // date + EOF
    assertEquals(tokens.get(0).getType(), CxxTokenType.STRING);
  }

  @Test
  public void dont_look_at_subsequent_elif_clauses() {
    // When a if clause has been evaluated to true, dont look at
    // subsequent elif clauses
    List<Token> tokens = lexer.lex("#define A 1\n"
                                   + "#if A\n"
                                   + "ifbody\n"
                                   + "#elif A\n"
                                   + "elifbody\n"
                                   + "#endif");
    assertThat(tokens).hasSize(2); // ifbody + EOF
    assertThat(tokens, hasToken("ifbody", GenericTokenType.IDENTIFIER));
  }
  
  //@Test
  public void hashhash_operator_problem() {
    // Corresponds to the Jira Issue SONARPLUGINS-3055.
    // The problem here is that 0x##n is splitted into
    // [0, x, ##, n] sequence of tokens by the initial parsing routine.
    // After this, 0 and the rest of the number get never concatenated again.
    
    List<Token> tokens = lexer.lex("#define A B(cf)\n"
                                   + "#define B(n) 0x##n\n"
                                   + "A");
    assertThat(tokens, hasToken("0xcf", CxxKeyword.INT));
  }
}
