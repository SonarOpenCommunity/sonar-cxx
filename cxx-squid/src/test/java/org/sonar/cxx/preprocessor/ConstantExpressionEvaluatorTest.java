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

import org.sonar.cxx.CxxConfiguration;
import com.sonar.sslr.squid.SquidAstVisitorContext;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class ConstantExpressionEvaluatorTest {
  
  private ConstantExpressionEvaluator evaluator = new ConstantExpressionEvaluator(mock(CxxConfiguration.class), mock(CxxPreprocessor.class));
  
  // @Test
  // public void bools() {
  //   assertTrue(evaluator.eval("true"));
    
  //   assertFalse(evaluator.eval("false"));
  // }

  // @Test
  // public void numbers() {
  //   assertTrue(evaluator.eval("1"));
  //   assertTrue(evaluator.eval("-1"));
  //   assertTrue(evaluator.eval("0xAA"));
  //   assertTrue(evaluator.eval("0XAA"));
  //   assertTrue(evaluator.eval("-0XAA"));
    
  //   assertFalse(evaluator.eval("0"));
  //   assertFalse(evaluator.eval("0x0"));
  // }

  // @Test
  // public void characters() {
  //   assertTrue(evaluator.eval("'1'"));
  //   assertTrue(evaluator.eval("'a'"));

  //   assertFalse(evaluator.eval("'\0'"));
  // }
  
  // @Test
  // public void conditional_expression() {
  //   assertTrue(evaluator.eval("1 ? 1 : 0"));
  //   assertTrue(evaluator.eval("0 ? 0 : 1"));

  //   assertFalse(evaluator.eval("1 ? 0 : 1"));
  //   assertFalse(evaluator.eval("0 ? 1 : 0"));
  // }

  // @Test
  // public void logical_or() {
  //   assertTrue(evaluator.eval("1 || 0"));
    
  //   assertFalse(evaluator.eval("0 || 0"));
  // }

  // @Test
  // public void logical_and() {
  //   assertTrue(evaluator.eval("1 && 1"));
    
  //   assertFalse(evaluator.eval("0 && 1"));
  // }

  // @Test
  // public void inclusive_or() {
  //   assertTrue(evaluator.eval("1 | 0"));
    
  //   assertFalse(evaluator.eval("0 | 0"));
  // }
  
  // @Test
  // public void exclusive_or() {
  //   assertTrue(evaluator.eval("1 ^ 0"));
  //   assertTrue(evaluator.eval("0 ^ 1"));
    
  //   assertFalse(evaluator.eval("0 ^ 0"));
  //   assertFalse(evaluator.eval("1 ^ 1"));
  // }

  // @Test
  // public void and_expr() {
  //   assertTrue(evaluator.eval("1 & 1"));
    
  //   assertFalse(evaluator.eval("0 & 1"));
  //   assertFalse(evaluator.eval("1 & 0"));
  //   assertFalse(evaluator.eval("0 & 0"));
  // }

  // @Test
  // public void equality_expr() {
  //   assertTrue(evaluator.eval("1 == 1"));
  //   assertTrue(evaluator.eval("0 != 1"));
    
  //   assertFalse(evaluator.eval("1 == 0"));
  //   assertFalse(evaluator.eval("0 != 0"));
  // }

  // @Test
  // public void relational_expr() {
  //   assertTrue(evaluator.eval("0 < 1"));
  //   assertTrue(evaluator.eval("0 <= 1"));
  //   assertTrue(evaluator.eval("1 > 0"));
  //   assertTrue(evaluator.eval("1 >= 0"));

  //   assertFalse(evaluator.eval("1 < 0"));
  //   assertFalse(evaluator.eval("1 <= 0"));
  //   assertFalse(evaluator.eval("0 > 1"));
  //   assertFalse(evaluator.eval("0 >= 1"));
  // }

  // @Test
  // public void shift_expr() {
  //   assertTrue(evaluator.eval("1 << 2"));
  //   assertTrue(evaluator.eval("1 >> 0"));
    
  //   assertFalse(evaluator.eval("0 << 1"));
  //   assertFalse(evaluator.eval("0 >> 1"));
  // }

  // @Test
  // public void additive_expr() {
  //   assertTrue(evaluator.eval("1 + 1"));
  //   assertTrue(evaluator.eval("2 - 1"));
    
  //   assertFalse(evaluator.eval("0 + 0"));
  //   assertFalse(evaluator.eval("1 - 1"));
  // }

  // @Test
  // public void multiplicative_expr() {
  //   assertTrue(evaluator.eval("1 * 1"));
  //   assertTrue(evaluator.eval("1 / 1"));
  //   assertTrue(evaluator.eval("1 % 2"));
    
  //   assertFalse(evaluator.eval("0 * 1"));
  //   assertFalse(evaluator.eval("0 / 1"));
  //   assertFalse(evaluator.eval("1 % 1"));
  // }

  // @Test
  // public void primary_expr() {
  //   assertTrue(evaluator.eval("(1)"));
    
  //   assertFalse(evaluator.eval("(0)"));
  //   assertFalse(evaluator.eval("(1 || 0) && 0"));
  // }

  // @Test
  // public void unary_expression() {
  //   assertTrue(evaluator.eval("+1"));
  //   assertTrue(evaluator.eval("-1"));
  //   assertTrue(evaluator.eval("!0"));
  //   assertTrue(evaluator.eval("~0"));
    
  //   assertFalse(evaluator.eval("+0"));
  //   assertFalse(evaluator.eval("-0"));
  //   assertFalse(evaluator.eval("!1"));
  //   //assertFalse(evaluator.eval("~OxFFFFFFFFFFFFFFFF"));
  // }

  @Test
  public void identifier_defined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("1");
    ConstantExpressionEvaluator evaluator = new ConstantExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertTrue(evaluator.eval("LALA"));
  }

  @Test
  public void identifier_undefined() {
    ConstantExpressionEvaluator evaluator = new ConstantExpressionEvaluator(mock(CxxConfiguration.class), mock(CxxPreprocessor.class));
    assertFalse(evaluator.eval("LALA"));
  }

}
