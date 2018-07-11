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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.squidbridge.SquidAstVisitorContext;

public class ExpressionEvaluatorTest {

  static boolean eval(String constExpr, CxxPreprocessor pp) {
    return ExpressionEvaluator.eval(mock(CxxConfiguration.class), pp, constExpr);
  }

  static boolean eval(String constExpr) {
    return eval(constExpr, mock(CxxPreprocessor.class));
  }

  @Test
  public void bools() {
    assertTrue(eval("true"));

    assertFalse(eval("false"));
  }

  @Test
  public void numbers() {
    assertTrue(eval("1"));
    assertTrue(eval("0xAA"));
    assertTrue(eval("0XAA"));
    assertTrue(eval("1L"));
    assertTrue(eval("01L"));
    assertTrue(eval("1u"));
    assertTrue(eval("1000000000UL"));
    assertTrue(eval("0xFFFFFFFFFFFFFFFF"));
    assertTrue(eval("0xFFFFFFFFFFFFFFFFui64"));
    assertTrue(eval("0xFFFFFFFFFFFFFFFFLL"));
    assertTrue(eval("0xFFFFFFFFFFFFFFFFuLL"));
    assertTrue(eval("0xFFFFFFFFFFFFFFFFll"));
    assertTrue(eval("0xFFFFFFFFFFFFFFFFull"));
    assertTrue(eval("0xffffffffffffffffui64"));
    assertTrue(eval("0xffffffffffffffffi64"));
    assertTrue(eval("0x7FFFFFL"));

    assertFalse(eval("0"));
    assertFalse(eval("0x0"));
  }

  @Test
  public void characters() {
    assertTrue(eval("'1'"));
    assertTrue(eval("'a'"));

    assertFalse(eval("'\0'"));
  }

  @Test
  public void conditional_expression() {
    assertTrue(eval("1 ? 1 : 0"));
    assertTrue(eval("0 ? 0 : 1"));

    assertFalse(eval("1 ? 0 : 1"));
    assertFalse(eval("0 ? 1 : 0"));

    assertTrue(eval("1 ? : 0"));
    assertTrue(eval("0 ? : 1"));
  }

  @Test
  public void logical_or() {
    assertTrue(eval("1 || 0"));
    assertTrue(eval("0 || 1"));
    assertTrue(eval("1 || 1"));
    assertTrue(eval("0 || 0 || 1"));

    assertFalse(eval("0 || 0"));
    assertFalse(eval("0 || 0 || 0"));
  }

  @Test
  public void logical_and() {
    assertTrue(eval("1 && 1"));
    assertTrue(eval("1 && 1 && 1"));

    assertFalse(eval("1 && 0"));
    assertFalse(eval("0 && 1"));
    assertFalse(eval("0 && 0"));
    assertFalse(eval("1 && 1 && 0"));
  }

  @Test
  public void inclusive_or() {
    assertTrue(eval("1 | 0"));
    assertTrue(eval("0 | 1"));
    assertTrue(eval("1 | 1"));
    assertTrue(eval("0 | 0 | 1"));

    assertFalse(eval("0 | 0 | 0"));
  }

  @Test
  public void exclusive_or() {
    assertTrue(eval("1 ^ 0"));
    assertTrue(eval("0 ^ 1"));
    assertTrue(eval("0 ^ 1 ^ 0"));

    assertFalse(eval("0 ^ 0"));
    assertFalse(eval("0 ^ 1 ^ 1"));
  }

  @Test
  public void and_expr() {
    assertTrue(eval("1 & 1"));
    assertTrue(eval("2 & 2 & 2"));

    assertFalse(eval("0 & 1"));
    assertFalse(eval("1 & 0"));
    assertFalse(eval("0 & 0"));
    assertFalse(eval("2 & 4"));
    assertFalse(eval("1 & 1 & 4"));
  }

  @Test
  public void equality_expr() {
    assertTrue(eval("1 == 1"));
    assertTrue(eval("1 == true"));
    assertTrue(eval("true == true"));
    assertTrue(eval("true == 1"));
    assertTrue(eval("false == 0"));
    assertTrue(eval("0 == false"));

    assertTrue(eval("true != 2"));
    assertTrue(eval("false != 1"));
    assertTrue(eval("1 != 2"));

    assertFalse(eval("1 == 0"));
    assertFalse(eval("3 != 3"));
    assertFalse(eval("2 != 3 != 4"));
    assertFalse(eval("0 != 1 != true"));

    assertTrue(eval("1 == 1 == true"));
  }

  @Test
  public void relational_expr() {
    assertTrue(eval("0 < 1"));
    assertTrue(eval("0 <= 1"));
    assertTrue(eval("1 > 0"));
    assertTrue(eval("1 >= 0"));
    assertTrue(eval("0 < 0 < 2"));

    assertFalse(eval("3 < 2"));
    assertFalse(eval("3 <= 2"));
    assertFalse(eval("0 > 1"));
    assertFalse(eval("0 >= 1"));
    assertFalse(eval("0 < 1 < 1"));

    assertTrue(eval("2 > 1 > false"));
    assertTrue(eval("0 >= 0 >= false"));
    assertTrue(eval("0 <= 0  >= true"));

    assertFalse(eval("1 < 1 > false"));
    assertFalse(eval("0 >= 1 >= true"));
    assertFalse(eval("2 <= 2 <= false"));
  }

  @Test
  public void shift_expr() {
    assertTrue(eval("1 << 2"));
    assertTrue(eval("1 >> 0"));

    assertFalse(eval("0 << 1"));
    assertFalse(eval("0 >> 1"));
    assertFalse(eval("10 >> 1 >> 10"));
  }

  @Test
  public void additive_expr() {
    assertTrue(eval("1 + 1"));
    assertTrue(eval("2 - 1"));
    assertTrue(eval("3 - 3 + 2"));

    assertFalse(eval("0 + 0"));
    assertFalse(eval("1 - 1"));
    assertFalse(eval("3 - 2 - 1"));
  }

  @Test
  public void multiplicative_expr() {
    assertTrue(eval("1 * 2"));
    assertTrue(eval("1 / 1"));
    assertTrue(eval("1 % 2"));

    assertFalse(eval("0 * 1"));
    assertFalse(eval("0 / 1"));
    assertFalse(eval("1 % 1"));
    assertFalse(eval("1 * 1 * 0"));
  }

  @Test
  public void primary_expr() {
    assertTrue(eval("(1)"));

    assertFalse(eval("(0)"));
    assertFalse(eval("( 0 )"));
    assertFalse(eval("(1 || 0) && 0"));
  }

  @Test
  public void unary_expression() {
    assertTrue(eval("+1"));
    assertTrue(eval("-1"));
    assertTrue(eval("!0"));
    assertTrue(eval("~0"));

    assertFalse(eval("+0"));
    assertFalse(eval("-0"));
    assertFalse(eval("!1"));
    assertFalse(eval("~0xFFFFFFFFFFFFFFFF"));
  }

  @Test
  public void identifier_defined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("1");
    assertTrue(eval("LALA", pp));
  }

  @Test
  public void self_referential_identifier0() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("A")).thenReturn("A");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(eval("A", pp)).isTrue();
    softly.assertThat(eval("A && A", pp)).isTrue();
    softly.assertThat(eval("A && !A", pp)).isFalse();
    softly.assertAll();
  }

  @Test
  public void self_referential_identifier1() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("A")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("A");

    assertTrue(eval("A", pp));
  }

  @Test
  public void self_referential_identifier2() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("C")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("C");
    when(pp.valueOf("A")).thenReturn("B");

    assertTrue(eval("A", pp));
  }

  @Test
  public void self_referential_identifier3() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("C")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("C");
    when(pp.valueOf("A1")).thenReturn("1");
    when(pp.valueOf("A0")).thenReturn("0");
    when(pp.valueOf("A")).thenReturn("A0 + A1 + B");

    assertTrue(eval("A", pp));
  }

  @Test
  public void self_referential_identifier4() {
    // https://gcc.gnu.org/onlinedocs/gcc-3.0.1/cpp_3.html#SEC31
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("x")).thenReturn("(4 + y)");
    when(pp.valueOf("y")).thenReturn("(2 * x)");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(eval("x", pp)).isTrue();
    softly.assertThat(eval("y", pp)).isTrue();
    softly.assertAll();
  }


  @Test
  public void identifier_undefined() {
    assertFalse(eval("LALA"));
  }

  @Test
  public void functionlike_macro_defined_true() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.expandFunctionLikeMacro(anyString(), anyList())).thenReturn("1");
    assertTrue(eval("has_feature(URG)", pp));
  }

  @Test
  public void functionlike_macro_defined_false() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("0");
    assertFalse(eval("has_feature(URG)", pp));
  }

  @Test
  public void functionlike_macro_undefined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn(null);
    assertFalse(eval("has_feature(URG)", pp));
  }

  @Test
  public void defined_true_without_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    String macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    assertTrue(eval("defined " + macro, pp));
  }

  @Test
  public void defined_false_without_parantheses() {
    assertFalse(eval("defined LALA"));
  }

  @Test
  public void defined_true_with_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    String macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    assertTrue(eval("defined (" + macro + ")", pp));
    assertTrue(eval("defined(" + macro + ")", pp));
  }

  @Test
  public void defined_false_with_parantheses() {
    assertFalse(eval("defined (LALA)"));
    assertFalse(eval("defined(LALA)"));
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
    eval("\"\"");
  }

  @Test
  public void std_macro_evaluated_as_expected() {
    CxxPreprocessor pp = new CxxPreprocessor(mock(SquidAstVisitorContext.class), CxxFileTesterHelper.mockCxxLanguage());

    assertTrue(eval("__LINE__", pp));
    assertTrue(eval("__STDC__", pp));
    assertTrue(eval("__STDC_HOSTED__", pp));
    assertTrue(eval("__cplusplus", pp));
  }
}
