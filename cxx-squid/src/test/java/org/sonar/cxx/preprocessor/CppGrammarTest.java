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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import org.junit.Test;
import org.sonar.cxx.CxxConfiguration;
import static org.mockito.Mockito.mock;

import org.apache.commons.io.FileUtils;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.io.File;

public class CppGrammarTest {

  private Parser<CppGrammar> p = null;
  private CppGrammar g = null;

  public CppGrammarTest() {
    p = Parser.builder(new CppGrammar()).withLexer(CppLexer.create()).build();
    g = p.getGrammar();
  }

  @Test
  public void preprocessor_line() {
    g.define_line.mock();
    g.include_line.mock();
    g.ifdef_line.mock();
    g.if_line.mock();
    g.elif_line.mock();
    g.else_line.mock();
    g.endif_line.mock();
    g.undef_line.mock();
    g.line_line.mock();
    g.error_line.mock();
    g.pragma_line.mock();
    g.warning_line.mock();

    assertThat(p, parse("define_line"));
    assertThat(p, parse("include_line"));
    assertThat(p, parse("ifdef_line"));
    assertThat(p, parse("if_line"));
    assertThat(p, parse("elif_line"));
    assertThat(p, parse("else_line"));
    assertThat(p, parse("endif_line"));
    assertThat(p, parse("undef_line"));
    assertThat(p, parse("line_line"));
    assertThat(p, parse("error_line"));
    assertThat(p, parse("pragma_line"));
    assertThat(p, parse("warning_line"));
  }

  @Test
  public void preprocessor_line_reallife() {
    assertThat(p, parse("#include      <ace/config-all.h>"));
    assertThat(p, parse("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H"));
    assertThat(p, parse("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0"));
    assertThat(p, parse("#include <algorithm>"));
    assertThat(p, parse("# /* See http://www.boost.org for most recent version. */"));
  }
  
  @Test
  public void define_line_reallife() {
    p.setRootRule(g.define_line);
    
    assertThat(p, parse("#define ALGOSTUFF_HPPEOF"));
    assertThat(p, parse("#define lala(a, b) a b"));
    assertThat(p, parse("#define new dew_debug"));
    assertThat(p, parse("#define macro(true, false) a"));
    assertThat(p, parse("#define TRUE true"));
    assertThat(p, parse("#define true TRUE"));
    assertThat(p, parse("# define __glibcxx_assert(_Condition)"));
  }

  @Test
  public void include_line() {
    p.setRootRule(g.include_line);

    g.pp_token.mock();

    assertThat(p, parse("#include <pp_token>"));
    assertThat(p, parse("#include_next <pp_token>"));
    assertThat(p, parse("#include \"jabadu\""));
  }

  @Test
  public void include_line_reallife() {
    p.setRootRule(g.include_line);

    assertThat(p, parse("#include <file>"));
    assertThat(p, parse("#include <file.h>"));
    assertThat(p, parse("#include <fi_le.h>"));
    assertThat(p, parse("#include \"file\""));
    assertThat(p, parse("#include \"file.h\""));
    assertThat(p, parse("#include \"fi_le.h\""));
    assertThat(p, parse("#include <bits/typesizes.h>	/* Defines __*_T_TYPE macros.  */"));
    assertThat(p, parse("#include /**/ <ace/config-all.h>"));
    assertThat(p, parse("#include <math.h> /**/ /**/"));
  }

  @Test
  public void define_containing_argument_list() {
    AstNode define = p.parse("#define lala(a, b) a b");
    assert (define.findFirstChild(g.parameter_list) != null);
  }

  @Test
  public void ifdef_line() {
    p.setRootRule(g.ifdef_line);
    
    assertThat(p, parse("#ifdef foo"));
    assertThat(p, parse("#ifndef foo"));
    assertThat(p, parse("#ifdef __GNUC__ // aka CONST but following LLVM Conventions."));
    assertThat(p, parse("#ifdef /**/ lala /**/ "));
  }

  @Test
  public void replacement_list() {
    p.setRootRule(g.replacement_list);

    assertThat(p, parse(""));
    assertThat(p, parse("pp_token"));
    assertThat(p, parse("#pp_token"));
    assertThat(p, parse("pp_token ## pp_token"));
  }

  @Test
  public void argument_list() {
    p.setRootRule(g.argument_list);

    assertThat(p, parse("foo"));
    assertThat(p, parse("foo, bar"));
    assertThat(p, parse("4, 1"));
  }

  @Test
  public void pp_token() {
    p.setRootRule(g.pp_token);

    assertThat(p, parse("foo"));
    assertThat(p, parse("("));
    assertThat(p, parse(")"));
    assertThat(p, parse("*"));
  }

  @Test
  public void functionlike_macro_definition() {
    p.setRootRule(g.functionlike_macro_definition);
    
    g.replacement_list.mock();
    g.argument_list.mock();
    g.pp_token.mock();
    
    assertThat(p, parse("#define pp_token( argument_list ) replacement_list"));
    assertThat(p, parse("#define pp_token(argument_list) replacement_list"));
    assertThat(p, parse("#define pp_token( ... ) replacement_list"));
    assertThat(p, parse("#define pp_token(...) replacement_list"));
    assertThat(p, parse("#define pp_token( argument_list, ... ) replacement_list"));
    assertThat(p, parse("#define pp_token(argument_list, ...) replacement_list"));
  }

  @Test
  public void functionlike_macro_definition_reallife() {
    p.setRootRule(g.functionlike_macro_definition);
    
    assertThat(p, parse("#define foo() bar"));
    assertThat(p, parse("#define foo() ()"));
    assertThat(p, parse("#define foo(a) bar"));
    assertThat(p, parse("#define foo(a,b) ab"));
  }
  
  @Test
  public void objectlike_macro_definition() {
    p.setRootRule(g.objectlike_macro_definition);
    
    g.replacement_list.mock();
    g.pp_token.mock();
    
    assertThat(p, parse("#define pp_token replacement_list"));
  }

  @Test
  public void objectlike_macro_definition_reallife() {
    p.setRootRule(g.objectlike_macro_definition);
    
    assertThat(p, parse("#define foo"));
    assertThat(p, parse("#define foo bar"));
    assertThat(p, parse("#define foo ()"));
    assertThat(p, parse("#define new new_debug"));
  }

  @Test
  public void else_line() {
    p.setRootRule(g.else_line);
    
    assertThat(p, parse("#else"));
    assertThat(p, parse("#else  // if lala"));
  }    

  @Test
  public void endif_line() {
    p.setRootRule(g.endif_line);
    
    assertThat(p, parse("#endif"));
    assertThat(p, parse("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H"));
  }    

  @Test
  public void undef_line() {
    p.setRootRule(g.undef_line);
    
    assertThat(p, parse("#undef foo"));
  }    

  @Test
  public void line_line() {
    p.setRootRule(g.line_line);
    
    assertThat(p, parse("#line foo bar"));
  }    

  @Test
  public void error_line() {
    p.setRootRule(g.error_line);
    
    assertThat(p, parse("#error foo"));
    assertThat(p, parse("#error"));
  }    

  @Test
  public void pragma_line() {
    p.setRootRule(g.pragma_line);
    
    assertThat(p, parse("#pragma foo"));
  }    
  
  @Test
  public void warning_line() {
    p.setRootRule(g.warning_line);
    
    assertThat(p, parse("#warning foo"));
  }

  @Test
  public void misc_line() {
    p.setRootRule(g.misc_line);
    
    assertThat(p, parse("#"));
    assertThat(p, parse("# lala"));
    assertThat(p, parse("#lala"));
  }
  
  @Test
  public void stress_test() {
    try{
    List<String> lines = FileUtils.readLines(new File("/home/wen/pptokens"));
    for(String line: lines)
      assertThat(p, parse(line));

    } catch(Exception e) {}
  }    
}
