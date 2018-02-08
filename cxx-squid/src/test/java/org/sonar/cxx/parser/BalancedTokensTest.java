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
package org.sonar.cxx.parser;

import org.junit.Test;
import static org.sonar.sslr.tests.Assertions.assertThat;

/**
 *
 * @author jmecosta
 */
public class BalancedTokensTest extends ParserBaseTestHelper {

  @Test
  public void attributeSpecifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifierSeq));
    mockRule(CxxGrammarImpl.attributeSpecifier);

    assertThat(p)
      .matches("attributeSpecifier")
      .matches("attributeSpecifier attributeSpecifier");
  }

  @Test
  public void attributeSpecifierSeq_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifierSeq));

    assertThat(p).matches("[ [ using CC : opt ( 1 ), debug]]");
    assertThat(p).matches("[ [ using CC : opt ( 1 ) ] ] [ [ CC :: debug ] ]");
    assertThat(p).matches("[ [ foo :: bar ( { foo }  [ bar ] ) ] ] [ [ foo :: bar ( { foo }  [ bar ] ) ] ]");
  }

  @Test
  public void attributeSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifier));
    mockRule(CxxGrammarImpl.attributeList);
    mockRule(CxxGrammarImpl.attributeUsingPrefix);
    mockRule(CxxGrammarImpl.alignmentSpecifier);

    assertThat(p).matches("[ [ attributeList ] ]");
    assertThat(p).matches("[ [ attributeUsingPrefix attributeList ] ]");
    assertThat(p).matches("alignmentSpecifier");
  }

  @Test
  public void attributeSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeSpecifier));

    assertThat(p).matches("[ [ foo :: bar ( { foo }  [ bar ] ) ] ]");
  }

  @Test
  public void alignmentSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.alignmentSpecifier));
    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeUsingPrefix);
    mockRule(CxxGrammarImpl.attributeNamespace);

    assertThat(p).matches("alignas ( typeId )");
    assertThat(p).matches("alignas ( typeId ... )");
    assertThat(p).matches("alignas ( constantExpression )");
    assertThat(p).matches("attributeUsingPrefix :");
    assertThat(p).matches("using attributeNamespace :");
  }

  @Test
  public void attributeUsingPrefix() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeUsingPrefix));
    mockRule(CxxGrammarImpl.attributeNamespace);

    assertThat(p).matches("using attributeNamespace :");
  }

  @Test
  public void attributeList() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeList));
    mockRule(CxxGrammarImpl.attribute);

    assertThat(p).matches("");
    assertThat(p).matches("attribute");
    assertThat(p).matches("attribute ...");
    assertThat(p).matches(",");
    assertThat(p).matches(", attribute");
    assertThat(p).matches("attribute , attribute");
    assertThat(p).matches("attribute ... , attribute");
    assertThat(p).matches("attribute ... , attribute ...");
    assertThat(p).matches("attribute , attribute , attribute");
  }

  @Test
  public void attributeList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeList));

    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] )");
    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] ) , foo :: bar ( { foo }  [ bar ] )");
    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] ) , ");
    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] ) ...");
    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] ) ... , foo :: bar ( { foo }  [ bar ] ) ...");
  }

  @Test
  public void attribute() {
    p.setRootRule(g.rule(CxxGrammarImpl.attribute));
    mockRule(CxxGrammarImpl.attributeToken);
    mockRule(CxxGrammarImpl.attributeArgumentClause);

    assertThat(p).matches("attributeToken");
    assertThat(p).matches("attributeToken attributeArgumentClause");
  }

  @Test
  public void attribute_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.attribute));

    assertThat(p).matches("foo");
    assertThat(p).matches("foo :: bar");
    assertThat(p).matches("foo :: bar ( { foo }  [ bar ] )");
  }

  @Test
  public void attributeToken() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeToken));
    mockRule(CxxGrammarImpl.attributeScopedToken);

    assertThat(p).matches("foo");
    assertThat(p).matches("attributeScopedToken");
  }

  @Test
  public void attributeScopedToken() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeScopedToken));
    mockRule(CxxGrammarImpl.attributeNamespace);

    assertThat(p).matches("attributeNamespace :: foo");
  }

  @Test
  public void attributeScopedToken_reallife() {
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
    mockRule(CxxGrammarImpl.balancedTokenSeq);

    assertThat(p).matches("( balancedTokenSeq )");
  }

  @Test
  public void attributeArgumentClause_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.attributeArgumentClause));

    assertThat(p).matches("( foo )");
  }

  @Test
  public void balancedTokenSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedTokenSeq));
    mockRule(CxxGrammarImpl.balancedToken);

    assertThat(p)
      .matches("balancedToken")
      .matches("balancedToken balancedToken")
      .matches("balancedToken balancedToken balancedToken");
  }

  @Test
  public void balancedTokenSeq_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedTokenSeq));

    assertThat(p).matches("[ ( foo ) { } ( bar ) ]");
  }

  @Test
  public void balancedToken() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedToken));
    mockRule(CxxGrammarImpl.balancedTokenSeq);

    assertThat(p).matches("foo")
      .matches("( balancedTokenSeq )")
      .matches("[ balancedTokenSeq ]")
      .matches("{ balancedTokenSeq }");
  }

  @Test
  public void balancedToken_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.balancedToken));

    assertThat(p).matches("[ foo ]")
      .matches("{ foo }")
      .matches("( foo )")
      .matches("( ( foo ) ( bar ) )")
      .matches("[ ( foo ) { } ( bar ) ]");
  }

}
