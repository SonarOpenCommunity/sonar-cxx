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
  public void preprocessorLine() {
    g.defineLine.mock();
    g.includeLine.mock();
    g.ifdefLine.mock();
    g.ifLine.mock();
    g.elifLine.mock();
    g.elseLine.mock();
    g.endifLine.mock();
    g.undefLine.mock();
    g.lineLine.mock();
    g.errorLine.mock();
    g.pragmaLine.mock();
    g.warningLine.mock();

    assertThat(p, parse("defineLine"));
    assertThat(p, parse("includeLine"));
    assertThat(p, parse("ifdefLine"));
    assertThat(p, parse("ifLine"));
    assertThat(p, parse("elifLine"));
    assertThat(p, parse("elseLine"));
    assertThat(p, parse("endifLine"));
    assertThat(p, parse("undefLine"));
    assertThat(p, parse("lineLine"));
    assertThat(p, parse("errorLine"));
    assertThat(p, parse("pragmaLine"));
    assertThat(p, parse("warningLine"));
  }

  @Test
  public void preprocessorLine_reallife() {
    assertThat(p, parse("#include      <ace/config-all.h>"));
    assertThat(p, parse("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H"));
    assertThat(p, parse("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0"));
    assertThat(p, parse("#include <algorithm>"));
    assertThat(p, parse("# /* See http://www.boost.org for most recent version. */"));
  }
  
  @Test
  public void defineLine_reallife() {
    p.setRootRule(g.defineLine);
    
    assertThat(p, parse("#define ALGOSTUFF_HPPEOF"));
    assertThat(p, parse("#define lala(a, b) a b"));
    assertThat(p, parse("#define new dew_debug"));
    assertThat(p, parse("#define macro(true, false) a"));
    assertThat(p, parse("#define TRUE true"));
    assertThat(p, parse("#define true TRUE"));
    assertThat(p, parse("# define __glibcxx_assert(_Condition)"));
  }

  @Test
  public void includeLine() {
    p.setRootRule(g.includeLine);

    g.ppToken.mock();

    assertThat(p, parse("#include <ppToken>"));
    assertThat(p, parse("#include_next <ppToken>"));
    assertThat(p, parse("#include \"jabadu\""));
  }

  @Test
  public void includeLine_reallife() {
    p.setRootRule(g.includeLine);

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
  public void define_containing_argumentList() {
    AstNode define = p.parse("#define lala(a, b) a b");
    assert (define.findFirstChild(g.parameterList) != null);
  }

  @Test
  public void ifdefLine() {
    p.setRootRule(g.ifdefLine);
    
    assertThat(p, parse("#ifdef foo"));
    assertThat(p, parse("#ifndef foo"));
    assertThat(p, parse("#ifdef __GNUC__ // aka CONST but following LLVM Conventions."));
    assertThat(p, parse("#ifdef /**/ lala /**/ "));
  }

  @Test
  public void replacementList() {
    p.setRootRule(g.replacementList);

    assertThat(p, parse(""));
    assertThat(p, parse("ppToken"));
    assertThat(p, parse("#ppToken"));
    assertThat(p, parse("ppToken ## ppToken"));
  }

  @Test
  public void argumentList() {
    p.setRootRule(g.argumentList);

    assertThat(p, parse("foo"));
    assertThat(p, parse("foo, bar"));
    assertThat(p, parse("4, 1"));
  }

  @Test
  public void ppToken() {
    p.setRootRule(g.ppToken);

    assertThat(p, parse("foo"));
    assertThat(p, parse("("));
    assertThat(p, parse(")"));
    assertThat(p, parse("*"));
  }

  @Test
  public void functionlikeMacroDefinition() {
    p.setRootRule(g.functionlikeMacroDefinition);
    
    g.replacementList.mock();
    g.argumentList.mock();
    g.ppToken.mock();
    
    assertThat(p, parse("#define ppToken( argumentList ) replacementList"));
    assertThat(p, parse("#define ppToken(argumentList) replacementList"));
    assertThat(p, parse("#define ppToken( ... ) replacementList"));
    assertThat(p, parse("#define ppToken(...) replacementList"));
    assertThat(p, parse("#define ppToken( argumentList, ... ) replacementList"));
    assertThat(p, parse("#define ppToken(argumentList, ...) replacementList"));
  }

  @Test
  public void functionlikeMacroDefinition_reallife() {
    p.setRootRule(g.functionlikeMacroDefinition);
    
    assertThat(p, parse("#define foo() bar"));
    assertThat(p, parse("#define foo() ()"));
    assertThat(p, parse("#define foo(a) bar"));
    assertThat(p, parse("#define foo(a,b) ab"));
  }
  
  @Test
  public void objectlikeMacroDefinition() {
    p.setRootRule(g.objectlikeMacroDefinition);
    
    g.replacementList.mock();
    g.ppToken.mock();
    
    assertThat(p, parse("#define ppToken replacementList"));
  }

  @Test
  public void objectlikeMacroDefinition_reallife() {
    p.setRootRule(g.objectlikeMacroDefinition);
    
    assertThat(p, parse("#define foo"));
    assertThat(p, parse("#define foo bar"));
    assertThat(p, parse("#define foo ()"));
    assertThat(p, parse("#define new new_debug"));
  }

  @Test
  public void elseLine() {
    p.setRootRule(g.elseLine);
    
    assertThat(p, parse("#else"));
    assertThat(p, parse("#else  // if lala"));
  }    

  @Test
  public void endifLine() {
    p.setRootRule(g.endifLine);
    
    assertThat(p, parse("#endif"));
    assertThat(p, parse("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H"));
  }    

  @Test
  public void undefLine() {
    p.setRootRule(g.undefLine);
    
    assertThat(p, parse("#undef foo"));
  }    

  @Test
  public void lineLine() {
    p.setRootRule(g.lineLine);
    
    assertThat(p, parse("#line foo bar"));
  }    

  @Test
  public void errorLine() {
    p.setRootRule(g.errorLine);
    
    assertThat(p, parse("#error foo"));
    assertThat(p, parse("#error"));
  }    

  @Test
  public void pragmaLine() {
    p.setRootRule(g.pragmaLine);
    
    assertThat(p, parse("#pragma foo"));
  }    
  
  @Test
  public void warningLine() {
    p.setRootRule(g.warningLine);
    
    assertThat(p, parse("#warning foo"));
  }

  @Test
  public void miscLine() {
    p.setRootRule(g.miscLine);
    
    assertThat(p, parse("#"));
    assertThat(p, parse("# lala"));
    assertThat(p, parse("#lala"));
  }
}
