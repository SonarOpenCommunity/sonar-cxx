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
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 * @author jmecosta
 */
public class BalancedTokensTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void attributeSpecifierSeq() {
    p.setRootRule(g.attributeSpecifierSeq);
    g.attributeSpecifier.mock();

    assertThat(p, parse("attributeSpecifier"));
    assertThat(p, parse("attributeSpecifier attributeSpecifier"));
  }

  @Test
  public void attributeSpecifierSeqXXXX() {
    p.setRootRule(g.attributeSpecifierSeq);

    assertThat(p, parse("[ [ foo :: bar ( { foo }  [ bar ] ) ] ] [ [ foo :: bar ( { foo }  [ bar ] ) ] ]"));
  }

  @Test
  public void attributeSpecifier() {
    p.setRootRule(g.attributeSpecifier);
    g.attributeList.mock();

    assertThat(p, parse("[ [ attributeList ] ]"));
  }

  @Test
  public void attributeSpecifierXXXX() {
    p.setRootRule(g.attributeSpecifier);

    assertThat(p, parse("[ [ foo :: bar ( { foo }  [ bar ] ) ] ]"));
  }

  @Test
  public void alignmentSpecifier() {
    p.setRootRule(g.alignmentSpecifier);
    g.typeId.mock();
    g.assignmentExpression.mock();

    assertThat(p, parse("alignas ( typeId )"));
    assertThat(p, parse("alignas ( typeId ... )"));
    assertThat(p, parse("alignas ( assignmentExpression )"));
    assertThat(p, parse("alignas ( assignmentExpression ... )"));
  }

  @Test
  public void attributeList() {
    p.setRootRule(g.attributeList);
    g.attribute.mock();

    assertThat(p, parse(""));
    assertThat(p, parse("attribute"));
    assertThat(p, parse("attribute , attribute"));
    assertThat(p, parse("attribute , "));
    assertThat(p, parse("attribute , attribute , attribute"));
    assertThat(p, parse("attribute ..."));
    assertThat(p, parse("attribute ... , attribute ..."));
  }

  @Test
  public void attributeListXXXX() {
    p.setRootRule(g.attributeList);

    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] )"));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) , foo :: bar ( { foo }  [ bar ] )"));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) , "));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) ..."));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) ... , foo :: bar ( { foo }  [ bar ] ) ..."));
  }

  @Test
  public void attribute() {
    p.setRootRule(g.attribute);
    g.attributeToken.mock();
    g.attributeArgumentClause.mock();

    assertThat(p, parse("attributeToken attributeArgumentClause"));
    assertThat(p, parse("attributeToken"));
  }

  @Test
  public void attributeXXXX() {
    p.setRootRule(g.attribute);

    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] )"));
  }

  @Test
  public void attributeToken() {
    p.setRootRule(g.attributeToken);
    g.attributeScopedToken.mock();

    assertThat(p, parse("foo"));
    assertThat(p, parse("attributeScopedToken"));
  }

  @Test
  public void attributeTokenXXXX() {
    p.setRootRule(g.attributeToken);

    assertThat(p, parse("foo"));
    assertThat(p, parse("foo :: bar"));
  }

  @Test
  public void attributeScopedToken() {
    p.setRootRule(g.attributeScopedToken);
    g.attributeNamespace.mock();

    assertThat(p, parse("attributeNamespace :: foo"));
  }

  @Test
  public void attributeScopedTokenXXXX() {
    p.setRootRule(g.attributeScopedToken);

    assertThat(p, parse("foo :: bar"));
  }

  @Test
  public void attributeNamespace() {
    p.setRootRule(g.attributeNamespace);

    assertThat(p, parse("foo"));
  }

  @Test
  public void attributeArgumentClause() {
    p.setRootRule(g.attributeArgumentClause);
    g.balancedTokenSeq.mock();

    assertThat(p, parse("( balancedTokenSeq )"));
  }

  public void attributeArgumentClauseXXXX() {
    p.setRootRule(g.attributeArgumentClause);

    assertThat(p, parse("( foo )"));
  }

  @Test
  public void balancedTokenSeq() {
    p.setRootRule(g.balancedTokenSeq);
    g.balancedToken.mock();

    assertThat(p, parse("balancedToken"));
    assertThat(p, parse("balancedToken balancedToken"));
    assertThat(p, parse("balancedToken balancedToken balancedToken"));
  }

  @Test
  public void balancedTokenSeqXXXX() {
    p.setRootRule(g.balancedTokenSeq);

    assertThat(p, parse("[ ( foo ) { } ( bar ) ]"));
  }

  @Test
  public void balancedToken() {
    p.setRootRule(g.balancedToken);
    g.balancedTokenSeq.mock();

    assertThat(p, parse("foo"));
    assertThat(p, parse("( balancedTokenSeq )"));
    assertThat(p, parse("[ balancedTokenSeq ]"));
    assertThat(p, parse("{ balancedTokenSeq }"));
  }

  @Test
  public void balancedTokenXXXX() {
    p.setRootRule(g.balancedToken);

    assertThat(p, parse("[ foo ]"));
    assertThat(p, parse("{ foo }"));
    assertThat(p, parse("( foo )"));
    assertThat(p, parse("( ( foo ) ( bar ) )"));
    assertThat(p, parse("[ ( foo ) { } ( bar ) ]"));
  }

}
