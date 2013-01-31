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
  public void if_line() {
    p.setRootRule(g.if_line);

    g.constant_expression.mock();

    assertThat(p, parse("#if constant_expression"));
  }

  @Test
  public void elif_line() {
    p.setRootRule(g.elif_line);
    
    g.constant_expression.mock();
    
    assertThat(p, parse("#elif constant_expression"));
  }

  @Test
  public void if_line_reallive() {
    p.setRootRule(g.if_line);
    
    assertThat(p, parse("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0"));
    assertThat(p, parse("#if 0   // Re-enable once PR13021 is fixed."));
    
    assert(p.parse("#if A (4, 1)").findFirstChild(g.functionlike_macro) != null);
    assert(p.parse("#if A ()").findFirstChild(g.functionlike_macro) != null);
    assert(p.parse("#if A()").findFirstChild(g.functionlike_macro) != null);
    
    assert(p.parse("#if defined(A)").findFirstChild(g.defined_expression) != null);
    assert(p.parse("#if defined (A)").findFirstChild(g.defined_expression) != null);
    assert(p.parse("#if defined A").findFirstChild(g.defined_expression) != null);
  }

  @Test
  public void constant_expression() {
    p.setRootRule(g.constant_expression);

    g.conditional_expression.mock();

    assertThat(p, parse("conditional_expression"));
  }

  @Test
  public void constant_expression_reallive() {
    p.setRootRule(g.constant_expression);

    assertThat(p, parse("(1 || 0) && (0 && 1)"));
    assertThat(p, parse("(1)"));
    assertThat(p, parse("( /**/ 1 /**/ )"));
    assertThat(p, parse("__has_feature(cxx_rvalue_references)"));
    assertThat(p, parse("__has_feature(/**/ cxx_rvalue_references /**/ )"));
  }

  @Test
  public void conditional_expression() {
    p.setRootRule(g.conditional_expression);

    g.logical_or_expression.mock();
    g.expression.mock();

    assertThat(p, parse("logical_or_expression"));
    assertThat(p, parse("logical_or_expression ? expression : logical_or_expression"));
  }

  @Test
  public void logical_or_expression() {
    p.setRootRule(g.logical_or_expression);

    g.logical_and_expression.mock();

    assertThat(p, parse("logical_and_expression"));
    assertThat(p, parse("logical_and_expression || logical_and_expression"));
  }

  @Test
  public void logical_and_expression() {
    p.setRootRule(g.logical_and_expression);

    g.inclusive_or_expression.mock();

    assertThat(p, parse("inclusive_or_expression"));
    assertThat(p, parse("inclusive_or_expression && inclusive_or_expression"));
  }

  @Test
  public void inclusive_or_expression() {
    p.setRootRule(g.inclusive_or_expression);

    g.exclusive_or_expression.mock();

    assertThat(p, parse("exclusive_or_expression"));
    assertThat(p, parse("exclusive_or_expression | exclusive_or_expression"));
  }

  @Test
  public void exclusive_or_expression() {
    p.setRootRule(g.exclusive_or_expression);

    g.and_expression.mock();

    assertThat(p, parse("and_expression"));
    assertThat(p, parse("and_expression ^ and_expression"));
  }

  @Test
  public void and_expression() {
    p.setRootRule(g.and_expression);

    g.equality_expression.mock();

    assertThat(p, parse("equality_expression"));
    assertThat(p, parse("equality_expression & equality_expression"));
  }

  @Test
  public void equality_expression() {
    p.setRootRule(g.equality_expression);

    g.relational_expression.mock();

    assertThat(p, parse("relational_expression"));
    assertThat(p, parse("relational_expression == relational_expression"));
    assertThat(p, parse("relational_expression != relational_expression"));
  }

  @Test
  public void relational_expression() {
    p.setRootRule(g.relational_expression);

    g.shift_expression.mock();

    assertThat(p, parse("shift_expression"));
    assertThat(p, parse("shift_expression < shift_expression"));
    assertThat(p, parse("shift_expression > shift_expression"));
    assertThat(p, parse("shift_expression <= shift_expression"));
    assertThat(p, parse("shift_expression >= shift_expression"));
  }

  @Test
  public void shift_expression() {
    p.setRootRule(g.shift_expression);

    g.additive_expression.mock();

    assertThat(p, parse("additive_expression"));
    assertThat(p, parse("additive_expression << additive_expression"));
    assertThat(p, parse("additive_expression >> additive_expression"));
  }

  @Test
  public void additive_expression() {
    p.setRootRule(g.additive_expression);

    g.multiplicative_expression.mock();

    assertThat(p, parse("multiplicative_expression"));
    assertThat(p, parse("multiplicative_expression + multiplicative_expression"));
    assertThat(p, parse("multiplicative_expression - multiplicative_expression"));
  }

  @Test
  public void multiplicative_expression() {
    p.setRootRule(g.multiplicative_expression);

    g.unary_expression.mock();

    assertThat(p, parse("unary_expression"));
    assertThat(p, parse("unary_expression * unary_expression"));
    assertThat(p, parse("unary_expression / unary_expression"));
    assertThat(p, parse("unary_expression % unary_expression"));
  }

  @Test
  public void unary_expression() {
    p.setRootRule(g.unary_expression);

    g.multiplicative_expression.mock();
    g.primary_expression.mock();
    g.unary_operator.mock();

    assertThat(p, parse("unary_operator multiplicative_expression"));
    assertThat(p, parse("primary_expression"));
  }

  @Test
  public void primary_expression() {
    p.setRootRule(g.primary_expression);

    g.literal.mock();
    g.expression.mock();
    g.defined_expression.mock();

    assertThat(p, parse("literal"));
    assertThat(p, parse("( expression )"));
    assertThat(p, parse("defined_expression"));
    assertThat(p, parse("foo"));
  }

  @Test
  public void expression() {
    p.setRootRule(g.expression);

    g.conditional_expression.mock();

    assertThat(p, parse("conditional_expression"));
    assertThat(p, parse("conditional_expression, conditional_expression"));
  }

  @Test
  public void defined_expression() {
    p.setRootRule(g.defined_expression);

    assertThat(p, parse("defined LALA"));
    assertThat(p, parse("defined (LALA)"));
    assertThat(p, parse("defined(LALA)"));
  }

  @Test
  public void functionlike_macro() {
    p.setRootRule(g.functionlike_macro);

    g.argument_list.mock();

    assertThat(p, parse("__has_feature(argument_list)"));
  }

  @Test
  public void functionlike_macro_reallife() {
    p.setRootRule(g.functionlike_macro);

    assertThat(p, parse("__has_feature(cxx_rvalue)"));
    assertThat(p, parse("__has_feature(cxx_rvalue, bla)"));
    assertThat(p, parse("__GNUC_PREREQ (4, 1)"));
    assertThat(p, parse("A ()"));
    assertThat(p, parse("A()"));
  }
}
