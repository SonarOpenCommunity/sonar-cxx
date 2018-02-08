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
package org.sonar.cxx.preprocessor;

import java.math.BigInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.squidbridge.SquidAstVisitorContext;

public class ExpressionEvaluatorTest {

  private final ExpressionEvaluator evaluator
    = new ExpressionEvaluator(mock(CxxConfiguration.class),
      mock(CxxPreprocessor.class));

  @Test
  public void bools() {
    assertTrue(evaluator.eval("true"));

    assertFalse(evaluator.eval("false"));
  }

  @Test
  public void numbers() {
    assertTrue(evaluator.eval("1"));
    assertTrue(evaluator.eval("0xAA"));
    assertTrue(evaluator.eval("0XAA"));
    assertTrue(evaluator.eval("1L"));
    assertTrue(evaluator.eval("01L"));
    assertTrue(evaluator.eval("1u"));
    assertTrue(evaluator.eval("1000000000UL"));
    assertTrue(evaluator.eval("0xFFFFFFFFFFFFFFFF"));
    assertTrue(evaluator.eval("0xFFFFFFFFFFFFFFFFui64"));
    assertTrue(evaluator.eval("0xFFFFFFFFFFFFFFFFLL"));
    assertTrue(evaluator.eval("0xFFFFFFFFFFFFFFFFuLL"));
    assertTrue(evaluator.eval("0xFFFFFFFFFFFFFFFFll"));
    assertTrue(evaluator.eval("0xFFFFFFFFFFFFFFFFull"));
    assertTrue(evaluator.eval("0xffffffffffffffffui64"));
    assertTrue(evaluator.eval("0xffffffffffffffffi64"));
    assertTrue(evaluator.eval("0x7FFFFFL"));

    assertFalse(evaluator.eval("0"));
    assertFalse(evaluator.eval("0x0"));
  }

  @Test
  public void characters() {
    assertTrue(evaluator.eval("'1'"));
    assertTrue(evaluator.eval("'a'"));

    assertFalse(evaluator.eval("'\0'"));
  }

  @Test
  public void conditional_expression() {
    assertTrue(evaluator.eval("1 ? 1 : 0"));
    assertTrue(evaluator.eval("0 ? 0 : 1"));

    assertFalse(evaluator.eval("1 ? 0 : 1"));
    assertFalse(evaluator.eval("0 ? 1 : 0"));

    assertTrue(evaluator.eval("1 ? : 0"));
    assertTrue(evaluator.eval("0 ? : 1"));
  }

  @Test
  public void logical_or() {
    assertTrue(evaluator.eval("1 || 0"));
    assertTrue(evaluator.eval("0 || 1"));
    assertTrue(evaluator.eval("1 || 1"));
    assertTrue(evaluator.eval("0 || 0 || 1"));

    assertFalse(evaluator.eval("0 || 0"));
    assertFalse(evaluator.eval("0 || 0 || 0"));
  }

  @Test
  public void logical_and() {
    assertTrue(evaluator.eval("1 && 1"));
    assertTrue(evaluator.eval("1 && 1 && 1"));

    assertFalse(evaluator.eval("1 && 0"));
    assertFalse(evaluator.eval("0 && 1"));
    assertFalse(evaluator.eval("0 && 0"));
    assertFalse(evaluator.eval("1 && 1 && 0"));
  }

  @Test
  public void inclusive_or() {
    assertTrue(evaluator.eval("1 | 0"));
    assertTrue(evaluator.eval("0 | 1"));
    assertTrue(evaluator.eval("1 | 1"));
    assertTrue(evaluator.eval("0 | 0 | 1"));

    assertFalse(evaluator.eval("0 | 0 | 0"));
  }

  @Test
  public void exclusive_or() {
    assertTrue(evaluator.eval("1 ^ 0"));
    assertTrue(evaluator.eval("0 ^ 1"));
    assertTrue(evaluator.eval("0 ^ 1 ^ 0"));

    assertFalse(evaluator.eval("0 ^ 0"));
    assertFalse(evaluator.eval("0 ^ 1 ^ 1"));
  }

  @Test
  public void and_expr() {
    assertTrue(evaluator.eval("1 & 1"));
    assertTrue(evaluator.eval("2 & 2 & 2"));

    assertFalse(evaluator.eval("0 & 1"));
    assertFalse(evaluator.eval("1 & 0"));
    assertFalse(evaluator.eval("0 & 0"));
    assertFalse(evaluator.eval("2 & 4"));
    assertFalse(evaluator.eval("1 & 1 & 4"));
  }

  @Test
  public void equality_expr() {
    assertTrue(evaluator.eval("1 == 1"));
    assertTrue(evaluator.eval("1 == true"));
    assertTrue(evaluator.eval("true == true"));
    assertTrue(evaluator.eval("true == 1"));
    assertTrue(evaluator.eval("false == 0"));
    assertTrue(evaluator.eval("0 == false"));

    assertTrue(evaluator.eval("true != 2"));
    assertTrue(evaluator.eval("false != 1"));
    assertTrue(evaluator.eval("1 != 2"));

    assertFalse(evaluator.eval("1 == 0"));
    assertFalse(evaluator.eval("3 != 3"));
    assertFalse(evaluator.eval("2 != 3 != 4"));
    assertFalse(evaluator.eval("0 != 1 != true"));

    assertTrue(evaluator.eval("1 == 1 == true"));
  }

  @Test
  public void relational_expr() {
    assertTrue(evaluator.eval("0 < 1"));
    assertTrue(evaluator.eval("0 <= 1"));
    assertTrue(evaluator.eval("1 > 0"));
    assertTrue(evaluator.eval("1 >= 0"));
    assertTrue(evaluator.eval("0 < 0 < 2"));

    assertFalse(evaluator.eval("3 < 2"));
    assertFalse(evaluator.eval("3 <= 2"));
    assertFalse(evaluator.eval("0 > 1"));
    assertFalse(evaluator.eval("0 >= 1"));
    assertFalse(evaluator.eval("0 < 1 < 1"));

    assertTrue(evaluator.eval("2 > 1 > false"));
    assertTrue(evaluator.eval("0 >= 0 >= false"));
    assertTrue(evaluator.eval("0 <= 0  >= true"));

    assertFalse(evaluator.eval("1 < 1 > false"));
    assertFalse(evaluator.eval("0 >= 1 >= true"));
    assertFalse(evaluator.eval("2 <= 2 <= false"));
  }

  @Test
  public void shift_expr() {
    assertTrue(evaluator.eval("1 << 2"));
    assertTrue(evaluator.eval("1 >> 0"));

    assertFalse(evaluator.eval("0 << 1"));
    assertFalse(evaluator.eval("0 >> 1"));
    assertFalse(evaluator.eval("10 >> 1 >> 10"));
  }

  @Test
  public void additive_expr() {
    assertTrue(evaluator.eval("1 + 1"));
    assertTrue(evaluator.eval("2 - 1"));
    assertTrue(evaluator.eval("3 - 3 + 2"));

    assertFalse(evaluator.eval("0 + 0"));
    assertFalse(evaluator.eval("1 - 1"));
    assertFalse(evaluator.eval("3 - 2 - 1"));
  }

  @Test
  public void multiplicative_expr() {
    assertTrue(evaluator.eval("1 * 2"));
    assertTrue(evaluator.eval("1 / 1"));
    assertTrue(evaluator.eval("1 % 2"));

    assertFalse(evaluator.eval("0 * 1"));
    assertFalse(evaluator.eval("0 / 1"));
    assertFalse(evaluator.eval("1 % 1"));
    assertFalse(evaluator.eval("1 * 1 * 0"));
  }

  @Test
  public void primary_expr() {
    assertTrue(evaluator.eval("(1)"));

    assertFalse(evaluator.eval("(0)"));
    assertFalse(evaluator.eval("( 0 )"));
    assertFalse(evaluator.eval("(1 || 0) && 0"));
  }

  @Test
  public void unary_expression() {
    assertTrue(evaluator.eval("+1"));
    assertTrue(evaluator.eval("-1"));
    assertTrue(evaluator.eval("!0"));
    assertTrue(evaluator.eval("~0"));

    assertFalse(evaluator.eval("+0"));
    assertFalse(evaluator.eval("-0"));
    assertFalse(evaluator.eval("!1"));
    assertFalse(evaluator.eval("~0xFFFFFFFFFFFFFFFF"));
  }

  @Test
  public void identifier_defined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("1");
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertTrue(evaluator.eval("LALA"));
  }

  @Test
  public void identifier_undefined() {
    ExpressionEvaluator evaluator
      = new ExpressionEvaluator(mock(CxxConfiguration.class),
        mock(CxxPreprocessor.class));
    assertFalse(evaluator.eval("LALA"));
  }

  @Test
  public void functionlike_macro_defined_true() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.expandFunctionLikeMacro(anyString(), anyList())).thenReturn("1");
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertTrue(evaluator.eval("has_feature(URG)"));
  }

  @Test
  public void functionlike_macro_defined_false() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("0");
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertFalse(evaluator.eval("has_feature(URG)"));
  }

  @Test
  public void functionlike_macro_undefined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn(null);
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertFalse(evaluator.eval("has_feature(URG)"));
  }

  @Test
  public void defined_true_without_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    String macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertTrue(evaluator.eval("defined " + macro));
  }

  @Test
  public void defined_false_without_parantheses() {
    ExpressionEvaluator evaluator
      = new ExpressionEvaluator(mock(CxxConfiguration.class),
        mock(CxxPreprocessor.class));
    assertFalse(evaluator.eval("defined LALA"));
  }

  @Test
  public void defined_true_with_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    String macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);
    assertTrue(evaluator.eval("defined (" + macro + ")"));
    assertTrue(evaluator.eval("defined(" + macro + ")"));
  }

  @Test
  public void defined_false_with_parantheses() {
    ExpressionEvaluator evaluator
      = new ExpressionEvaluator(mock(CxxConfiguration.class),
        mock(CxxPreprocessor.class));
    assertFalse(evaluator.eval("defined (LALA)"));
    assertFalse(evaluator.eval("defined(LALA)"));
  }

  @Test
  public void decode_numbers() {
    assertEquals(ExpressionEvaluator.decode("1"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("067"), new BigInteger("67", 8));
    assertEquals(ExpressionEvaluator.decode("0b11"), new BigInteger("11", 2));
    assertEquals(ExpressionEvaluator.decode("0xab"), new BigInteger("ab", 16));

    assertEquals(ExpressionEvaluator.decode("1L"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("1l"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("1U"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("1u"), new BigInteger("1", 10));

    assertEquals(ExpressionEvaluator.decode("1ul"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("1ll"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("1i64"), new BigInteger("1", 10));
    assertEquals(ExpressionEvaluator.decode("1ui64"), new BigInteger("1", 10));

    assertEquals(ExpressionEvaluator.decode("067ll"), new BigInteger("67", 8));
    assertEquals(ExpressionEvaluator.decode("0b11ul"), new BigInteger("11", 2));
    assertEquals(ExpressionEvaluator.decode("0xabui64"), new BigInteger("ab", 16));

    assertEquals(ExpressionEvaluator.decode("1'234"), new BigInteger("1234", 10));
    assertEquals(ExpressionEvaluator.decode("0b1111'0000'1111"), new BigInteger("111100001111", 2));
    assertEquals(ExpressionEvaluator.decode("0xAAAA'bbbb"), new BigInteger("AAAAbbbb", 16));
  }

  @Test(expected = EvaluationException.class)
  public void throw_on_invalid_expressions() {
    evaluator.eval("\"\"");
  }

  @Test
  public void std_macro_evaluated_as_expected() {
    CxxPreprocessor pp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), CxxFileTesterHelper.mockCxxLanguage());
    ExpressionEvaluator evaluator = new ExpressionEvaluator(mock(CxxConfiguration.class), pp);

    assertTrue(evaluator.eval("__LINE__"));
    assertTrue(evaluator.eval("__STDC__"));
    assertTrue(evaluator.eval("__STDC_HOSTED__"));
    assertTrue(evaluator.eval("__cplusplus"));
  }
}
