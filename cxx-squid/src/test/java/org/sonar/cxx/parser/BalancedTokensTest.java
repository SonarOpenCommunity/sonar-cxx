/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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

import org.junit.jupiter.api.Test;

/**
 *
 * @author jmecosta
 */
class BalancedTokensTest extends ParserBaseTestHelper {

  @Test
  void attributeSpecifierSeq() {
    setRootRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.attributeSpecifier);

    assertThatParser()
      .matches("attributeSpecifier")
      .matches("attributeSpecifier attributeSpecifier");
  }

  @Test
  void attributeSpecifierSeqReallife() {
    setRootRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("[ [ using CC : opt ( 1 ), debug]]")
      .matches("[ [ using CC : opt ( 1 ) ] ] [ [ CC :: debug ] ]")
      .matches("[ [ foo :: bar ( { foo }  [ bar ] ) ] ] [ [ foo :: bar ( { foo }  [ bar ] ) ] ]");
  }

  @Test
  void attributeSpecifier() {
    setRootRule(CxxGrammarImpl.attributeSpecifier);

    mockRule(CxxGrammarImpl.attributeList);
    mockRule(CxxGrammarImpl.attributeUsingPrefix);
    mockRule(CxxGrammarImpl.alignmentSpecifier);

    assertThatParser()
      .matches("[ [ attributeList ] ]")
      .matches("[ [ attributeUsingPrefix attributeList ] ]")
      .matches("alignmentSpecifier");
  }

  @Test
  void attributeSpecifierReallife() {
    setRootRule(CxxGrammarImpl.attributeSpecifier);

    assertThatParser()
      .matches("[ [ foo :: bar ( { foo }  [ bar ] ) ] ]");
  }

  @Test
  void alignmentSpecifier() {
    setRootRule(CxxGrammarImpl.alignmentSpecifier);

    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeUsingPrefix);
    mockRule(CxxGrammarImpl.attributeNamespace);

    assertThatParser()
      .matches("alignas ( typeId )")
      .matches("alignas ( typeId ... )")
      .matches("alignas ( constantExpression )")
      .matches("attributeUsingPrefix :")
      .matches("using attributeNamespace :");
  }

  @Test
  void attributeUsingPrefix() {
    setRootRule(CxxGrammarImpl.attributeUsingPrefix);
    mockRule(CxxGrammarImpl.attributeNamespace);

    assertThatParser()
      .matches("using attributeNamespace :");
  }

  @Test
  void attributeList() {
    setRootRule(CxxGrammarImpl.attributeList);
    mockRule(CxxGrammarImpl.attribute);

    assertThatParser()
      .matches("")
      .matches("attribute")
      .matches("attribute ...")
      .matches(",")
      .matches(", attribute")
      .matches("attribute , attribute")
      .matches("attribute ... , attribute")
      .matches("attribute ... , attribute ...")
      .matches("attribute , attribute , attribute");
  }

  @Test
  void attributeListReallife() {
    setRootRule(CxxGrammarImpl.attributeList);

    assertThatParser()
      .matches("foo :: bar ( { foo }  [ bar ] )")
      .matches("foo :: bar ( { foo }  [ bar ] ) , foo :: bar ( { foo }  [ bar ] )")
      .matches("foo :: bar ( { foo }  [ bar ] ) , ")
      .matches("foo :: bar ( { foo }  [ bar ] ) ...")
      .matches("foo :: bar ( { foo }  [ bar ] ) ... , foo :: bar ( { foo }  [ bar ] ) ...");
  }

  @Test
  void attribute() {
    setRootRule(CxxGrammarImpl.attribute);

    mockRule(CxxGrammarImpl.attributeToken);
    mockRule(CxxGrammarImpl.attributeArgumentClause);

    assertThatParser()
      .matches("attributeToken")
      .matches("attributeToken attributeArgumentClause");
  }

  @Test
  void attributeReallife() {
    setRootRule(CxxGrammarImpl.attribute);

    assertThatParser()
      .matches("foo")
      .matches("foo :: bar")
      .matches("foo :: bar ( { foo }  [ bar ] )");
  }

  @Test
  void attributeToken() {
    setRootRule(CxxGrammarImpl.attributeToken);
    mockRule(CxxGrammarImpl.attributeScopedToken);

    assertThatParser()
      .matches("foo")
      .matches("attributeScopedToken");
  }

  @Test
  void attributeScopedToken() {
    setRootRule(CxxGrammarImpl.attributeScopedToken);
    mockRule(CxxGrammarImpl.attributeNamespace);

    assertThatParser()
      .matches("attributeNamespace :: foo");
  }

  @Test
  void attributeScopedTokenReallife() {
    setRootRule(CxxGrammarImpl.attributeScopedToken);

    assertThatParser()
      .matches("foo :: bar");
  }

  @Test
  void attributeNamespace() {
    setRootRule(CxxGrammarImpl.attributeNamespace);

    assertThatParser()
      .matches("foo");
  }

  @Test
  void attributeArgumentClause() {
    setRootRule(CxxGrammarImpl.attributeArgumentClause);
    mockRule(CxxGrammarImpl.balancedTokenSeq);

    assertThatParser()
      .matches("( balancedTokenSeq )");
  }

  @Test
  void attributeArgumentClauseReallife() {
    setRootRule(CxxGrammarImpl.attributeArgumentClause);

    assertThatParser()
      .matches("( foo )");
  }

  @Test
  void balancedTokenSeq() {
    setRootRule(CxxGrammarImpl.balancedTokenSeq);
    mockRule(CxxGrammarImpl.balancedToken);

    assertThatParser()
      .matches("balancedToken")
      .matches("balancedToken balancedToken")
      .matches("balancedToken balancedToken balancedToken");
  }

  @Test
  void balancedTokenSeqReallife() {
    setRootRule(CxxGrammarImpl.balancedTokenSeq);

    assertThatParser()
      .matches("[ ( foo ) { } ( bar ) ]");
  }

  @Test
  void balancedToken() {
    setRootRule(CxxGrammarImpl.balancedToken);
    mockRule(CxxGrammarImpl.balancedTokenSeq);

    assertThatParser()
      .matches("foo")
      .matches("( balancedTokenSeq )")
      .matches("[ balancedTokenSeq ]")
      .matches("{ balancedTokenSeq }");
  }

  @Test
  void balancedTokenReallife() {
    setRootRule(CxxGrammarImpl.balancedToken);

    assertThatParser()
      .matches("[ foo ]")
      .matches("{ foo }")
      .matches("( foo )")
      .matches("( ( foo ) ( bar ) )")
      .matches("[ ( foo ) { } ( bar ) ]");
  }

}
