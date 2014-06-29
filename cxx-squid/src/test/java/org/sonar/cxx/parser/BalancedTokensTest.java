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
package org.sonar.cxx.parser;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import com.sonar.sslr.api.Grammar;

import static org.sonar.sslr.tests.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 * @author jmecosta
 */
public class BalancedTokensTest extends ParserBaseTest {

  @Test
  public void attributeSpecifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifierSeq));
    g.rule(CxxGrammarImpl.attributeSpecifier).mock();

    assertThat(p)
      .matches("attributeSpecifier")
      .matches("attributeSpecifier attributeSpecifier");
  }

  @Test
  public void attributeSpecifierSeqXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifierSeq));

    assertThat(p).matches("[ [ foo :: bar ( { foo }  [ bar ] ) ] ] [ [ foo :: bar ( { foo }  [ bar ] ) ] ]");
  }

  @Test
  public void attributeSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifier));
    g.rule(CxxGrammarImpl.attributeList).mock();

    assertThat(p).matches("[ [ attributeList ] ]");
  }

  @Test
  public void attributeSpecifierXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifier));

    assertThat(p).matches("[ [ foo :: bar ( { foo }  [ bar ] ) ] ]");
  }

  @Test
  public void alignmentSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.alignmentSpecifier));
    g.rule(CxxGrammarImpl.typeId).mock();
    g.rule(CxxGrammarImpl.assignmentExpression).mock();

    assertThat(p)
      .matches("alignas ( typeId )")
      .matches("alignas ( typeId ... )")
      .matches("alignas ( assignmentExpression )")
      .matches("alignas ( assignmentExpression ... )");
  }

  @Test
  public void attributeList() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeList));
    g.rule(CxxGrammarImpl.attribute).mock();

    assertThat(p)
      .matches("")
      .matches("attribute")
      .matches("attribute , attribute")
      .matches("attribute , ")
      .matches("attribute , attribute , attribute")
      .matches("attribute ...")
      .matches("attribute ... , attribute ...");
  }

  @Test
  public void attributeListXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeList));

    assertThat(p)
      .matches("foo :: bar ( { foo }  [ bar ] )")
      .matches("foo :: bar ( { foo }  [ bar ] ) , foo :: bar ( { foo }  [ bar ] )")
      .matches("foo :: bar ( { foo }  [ bar ] ) , ")
      .matches("foo :: bar ( { foo }  [ bar ] ) ...")
      .matches("foo :: bar ( { foo }  [ bar ] ) ... , foo :: bar ( { foo }  [ bar ] ) ...");
  }

  @Test
  public void attribute() {
    p.setRootRule(g.rule(CxxGrammarImpl.attribute));
    g.rule(CxxGrammarImpl.attributeToken).mock();
    g.rule(CxxGrammarImpl.attributeArgumentClause).mock();

    assertThat(p)
      .matches("attributeToken attributeArgumentClause")
      .matches("attributeToken");
  }

  @Test
  public void attributeXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attribute));

    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] )");
  }

  @Test
  public void attributeToken() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeToken));
    g.rule(CxxGrammarImpl.attributeScopedToken).mock();

    assertThat(p)
      .matches("foo")
      .matches("attributeScopedToken");
  }

  @Test
  public void attributeTokenXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeToken));

    assertThat(p)
      .matches("foo")
      .matches("foo :: bar");
  }

  @Test
  public void attributeScopedToken() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeScopedToken));
    g.rule(CxxGrammarImpl.attributeNamespace).mock();

    assertThat(p).matches("attributeNamespace :: foo");
  }

  @Test
  public void attributeScopedTokenXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeScopedToken));

    assertThat(p).matches("foo :: bar");
  }

  @Test
  public void attributeNamespace() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeNamespace));

    assertThat(p).matches("foo");
  }

  @Test
  public void attributeArgumentClause() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeArgumentClause));
    g.rule(CxxGrammarImpl.balancedTokenSeq).mock();

    assertThat(p).matches("( balancedTokenSeq )");
  }

  public void attributeArgumentClauseXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeArgumentClause));

    assertThat(p).matches("( foo )");
  }

  @Test
  public void balancedTokenSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedTokenSeq));
    g.rule(CxxGrammarImpl.balancedToken).mock();

    assertThat(p)
      .matches("balancedToken")
      .matches("balancedToken balancedToken")
      .matches("balancedToken balancedToken balancedToken");
  }

  @Test
  public void balancedTokenSeqXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedTokenSeq));

    assertThat(p).matches("[ ( foo ) { } ( bar ) ]");
  }

  @Test
  public void balancedToken() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedToken));
    g.rule(CxxGrammarImpl.balancedTokenSeq).mock();

    assertThat(p).matches("foo")
      .matches("( balancedTokenSeq )")
      .matches("[ balancedTokenSeq ]")
      .matches("{ balancedTokenSeq }");
  }

  @Test
  public void balancedTokenXXXX() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedToken));

    assertThat(p).matches("[ foo ]")
      .matches("{ foo }")
      .matches("( foo )")
      .matches("( ( foo ) ( bar ) )")
      .matches("[ ( foo ) { } ( bar ) ]");
  }

}
