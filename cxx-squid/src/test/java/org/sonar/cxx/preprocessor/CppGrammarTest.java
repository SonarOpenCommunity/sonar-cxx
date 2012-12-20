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

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

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
    g.ifndef_line.mock();
    
    assertThat(p, parse("define_line"));
    assertThat(p, parse("include_line"));
    assertThat(p, parse("ifdef_line"));
    assertThat(p, parse("ifndef_line"));
  }
  
  @Test
  public void define_line() {
    p.setRootRule(g.define_line);

    g.replacement_list.mock();
    g.identifier_list.mock();
    g.pp_token.mock();

    assertThat(p, parse("#define pp_token replacement_list"));
    assertThat(p, parse("#define pp_token ( ) replacement_list"));
    assertThat(p, parse("#define pp_token ( identifier_list ) replacement_list"));
    assertThat(p, parse("#define pp_token ( ... ) replacement_list"));
    assertThat(p, parse("#define pp_token ( identifier_list, ... ) replacement_list"));
  }

  @Test
  public void define_line_reallife() {
    assertThat(p, parse("#define ALGOSTUFF_HPPEOF"));
    assertThat(p, parse("#define lala(a, b) a b"));
    assertThat(p, parse("#define new dew_debug"));
    assertThat(p, parse("#define macro(true, false) a"));
    assertThat(p, parse("#define TRUE true"));
    assertThat(p, parse("#define true TRUE"));
  }

  @Test
  public void include_line() {
    p.setRootRule(g.include_line);

    g.pp_token.mock();

    assertThat(p, parse("#include < pp_token >"));
    assertThat(p, parse("#include < pp_token pp_token >"));
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
  }

  @Test
  public void define_containing_identifier_list() {
    AstNode define = p.parse("#define lala(a, b) a b");
    assert (define.findFirstChild(g.identifier_list) != null);
  }

  @Test
  public void ifdef_line() {
    p.setRootRule(g.ifdef_line);
    assertThat(p, parse("#ifdef foo"));
  }

  @Test
  public void ifndef_line() {
    p.setRootRule(g.ifndef_line);
    assertThat(p, parse("#ifndef foo"));
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
  public void identifier_list() {
    p.setRootRule(g.identifier_list);

    assertThat(p, parse("foo"));
    assertThat(p, parse("foo, bar"));
  }

  @Test
  public void pp_token() {
    p.setRootRule(g.pp_token);

    assertThat(p, parse("foo"));
    assertThat(p, parse("("));
    assertThat(p, parse(")"));
    assertThat(p, parse("*"));
  }
}
