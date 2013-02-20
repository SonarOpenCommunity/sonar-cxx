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

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.api.AstNode;
import org.junit.Test;
import org.sonar.cxx.CxxConfiguration;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class IfExpressionsTest {

  Parser<CppGrammar> p = CppParser.createConstantExpressionParser(mock(CxxConfiguration.class));
  CppGrammar g = p.getGrammar();

  @Test
  public void ifLine() {
    p.setRootRule(g.ifLine);

    g.constantExpression.mock();

    assertThat(p, parse("#if constantExpression"));
  }

  @Test
  public void elifLine() {
    p.setRootRule(g.elifLine);
    
    g.constantExpression.mock();
    
    assertThat(p, parse("#elif constantExpression"));
  }

  @Test
  public void ifLine_reallive() {
    p.setRootRule(g.ifLine);
    
    assertThat(p, parse("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0"));
    assertThat(p, parse("#if 0   // Re-enable once PR13021 is fixed."));
    
    assert(p.parse("#if A (4, 1)").findFirstChild(g.functionlikeMacro) != null);
    assert(p.parse("#if A ()").findFirstChild(g.functionlikeMacro) != null);
    assert(p.parse("#if A()").findFirstChild(g.functionlikeMacro) != null);
    
    assert(p.parse("#if defined(A)").findFirstChild(g.definedExpression) != null);
    assert(p.parse("#if defined (A)").findFirstChild(g.definedExpression) != null);
    assert(p.parse("#if defined A").findFirstChild(g.definedExpression) != null);
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.constantExpression);

    g.conditionalExpression.mock();

    assertThat(p, parse("conditionalExpression"));
  }

  @Test
  public void constantExpression_reallive() {
    p.setRootRule(g.constantExpression);

    assertThat(p, parse("(1 || 0) && (0 && 1)"));
    assertThat(p, parse("(1)"));
    assertThat(p, parse("( /**/ 1 /**/ )"));
    assertThat(p, parse("__has_feature(cxx_rvalue_references)"));
    assertThat(p, parse("__has_feature(/**/ cxx_rvalue_references /**/ )"));
  }

  @Test
  public void conditionalExpression() {
    p.setRootRule(g.conditionalExpression);

    g.logicalOrExpression.mock();
    g.expression.mock();

    assertThat(p, parse("logicalOrExpression"));
    assertThat(p, parse("logicalOrExpression ? expression : logicalOrExpression"));
  }

  @Test
  public void logicalOrExpression() {
    p.setRootRule(g.logicalOrExpression);

    g.logicalAndExpression.mock();

    assertThat(p, parse("logicalAndExpression"));
    assertThat(p, parse("logicalAndExpression || logicalAndExpression"));
  }

  @Test
  public void logicalAndExpression() {
    p.setRootRule(g.logicalAndExpression);

    g.inclusiveOrExpression.mock();

    assertThat(p, parse("inclusiveOrExpression"));
    assertThat(p, parse("inclusiveOrExpression && inclusiveOrExpression"));
  }

  @Test
  public void inclusiveOrExpression() {
    p.setRootRule(g.inclusiveOrExpression);

    g.exclusiveOrExpression.mock();

    assertThat(p, parse("exclusiveOrExpression"));
    assertThat(p, parse("exclusiveOrExpression | exclusiveOrExpression"));
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.exclusiveOrExpression);

    g.andExpression.mock();

    assertThat(p, parse("andExpression"));
    assertThat(p, parse("andExpression ^ andExpression"));
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.andExpression);

    g.equalityExpression.mock();

    assertThat(p, parse("equalityExpression"));
    assertThat(p, parse("equalityExpression & equalityExpression"));
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.equalityExpression);

    g.relationalExpression.mock();

    assertThat(p, parse("relationalExpression"));
    assertThat(p, parse("relationalExpression == relationalExpression"));
    assertThat(p, parse("relationalExpression != relationalExpression"));
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.relationalExpression);

    g.shiftExpression.mock();

    assertThat(p, parse("shiftExpression"));
    assertThat(p, parse("shiftExpression < shiftExpression"));
    assertThat(p, parse("shiftExpression > shiftExpression"));
    assertThat(p, parse("shiftExpression <= shiftExpression"));
    assertThat(p, parse("shiftExpression >= shiftExpression"));
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.shiftExpression);

    g.additiveExpression.mock();

    assertThat(p, parse("additiveExpression"));
    assertThat(p, parse("additiveExpression << additiveExpression"));
    assertThat(p, parse("additiveExpression >> additiveExpression"));
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.additiveExpression);

    g.multiplicativeExpression.mock();

    assertThat(p, parse("multiplicativeExpression"));
    assertThat(p, parse("multiplicativeExpression + multiplicativeExpression"));
    assertThat(p, parse("multiplicativeExpression - multiplicativeExpression"));
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.multiplicativeExpression);

    g.unaryExpression.mock();

    assertThat(p, parse("unaryExpression"));
    assertThat(p, parse("unaryExpression * unaryExpression"));
    assertThat(p, parse("unaryExpression / unaryExpression"));
    assertThat(p, parse("unaryExpression % unaryExpression"));
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.unaryExpression);

    g.multiplicativeExpression.mock();
    g.primaryExpression.mock();
    g.unaryOperator.mock();

    assertThat(p, parse("unaryOperator multiplicativeExpression"));
    assertThat(p, parse("primaryExpression"));
  }

  @Test
  public void primaryExpression() {
    p.setRootRule(g.primaryExpression);

    g.literal.mock();
    g.expression.mock();
    g.definedExpression.mock();

    assertThat(p, parse("literal"));
    assertThat(p, parse("( expression )"));
    assertThat(p, parse("definedExpression"));
    assertThat(p, parse("foo"));
  }

  @Test
  public void expression() {
    p.setRootRule(g.expression);

    g.conditionalExpression.mock();

    assertThat(p, parse("conditionalExpression"));
    assertThat(p, parse("conditionalExpression, conditionalExpression"));
  }

  @Test
  public void definedExpression() {
    p.setRootRule(g.definedExpression);

    assertThat(p, parse("defined LALA"));
    assertThat(p, parse("defined (LALA)"));
    assertThat(p, parse("defined(LALA)"));
  }

  @Test
  public void functionlikeMacro() {
    p.setRootRule(g.functionlikeMacro);

    g.argumentList.mock();

    assertThat(p, parse("__has_feature(argumentList)"));
  }

  @Test
  public void functionlikeMacro_reallife() {
    p.setRootRule(g.functionlikeMacro);

    assertThat(p, parse("__has_feature(cxx_rvalue)"));
    assertThat(p, parse("__has_feature(cxx_rvalue, bla)"));
    assertThat(p, parse("__GNUC_PREREQ (4, 1)"));
    assertThat(p, parse("A ()"));
    assertThat(p, parse("A()"));
    assertThat(p, parse("BOOST_WORKAROUND(BOOST_MSVC, < 1300)"));
    assertThat(p, parse("BOOST_WORKAROUND(< 1300)"));
    assertThat(p, parse("BOOST_WORKAROUND(a, call())"));
  }
}
