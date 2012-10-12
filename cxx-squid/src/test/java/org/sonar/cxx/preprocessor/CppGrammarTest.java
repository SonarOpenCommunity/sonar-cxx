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
import org.sonar.cxx.lexer.CxxLexer;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

public class CppGrammarTest {

  private Parser<CppGrammar> p = null;
  private CppGrammar g = null;

  public CppGrammarTest() {
    CxxConfiguration conf = new CxxConfiguration();
    conf.setPreprocessorChannelEnabled(false);
    p = Parser.builder(new CppGrammar()).withLexer(CxxLexer.create(conf)).build();
    g = p.getGrammar();
  }

  @Test
  public void define_line() {
    g.replacement_list.mock();
    g.identifier_list.mock();

    assertThat(p, parse("foo replacement_list"));
    assertThat(p, parse("foo ( ) replacement_list"));
    assertThat(p, parse("foo ( identifier_list ) replacement_list"));
    assertThat(p, parse("foo ( ... ) replacement_list"));
    assertThat(p, parse("foo ( identifier_list, ... ) replacement_list"));
  }

  @Test
  public void define_line_reallife() {
    assertThat(p, parse("ALGOSTUFF_HPPEOF"));
    assertThat(p, parse("lala(a, b) a b"));
    assertThat(p, parse("new dew_debug"));
    assertThat(p, parse("macro(true, false) a"));
    assertThat(p, parse("TRUE true"));
    assertThat(p, parse("true TRUE"));
  }

  @Test
  public void define_containing_identifier_list() {
    AstNode define = p.parse("lala(a, b) a b");
    assert (define.findFirstChild(g.identifier_list) != null);
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
