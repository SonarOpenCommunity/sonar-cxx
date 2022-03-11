/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.Grammar;
import java.io.File;
import java.math.BigInteger;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

public class ExpressionEvaluatorTest {

  static boolean eval(String constExpr, CxxPreprocessor pp) {
    return ExpressionEvaluator.eval(pp, constExpr);
  }

  static boolean eval(String constExpr) {
    return eval(constExpr, mock(CxxPreprocessor.class));
  }

  @Test
  public void bools() {
    assertThat(eval("true")).isTrue();
    assertThat(eval("false")).isFalse();
  }

  @Test
  public void numbers() {
    assertThat(eval("1")).isTrue();
    assertThat(eval("0xAA")).isTrue();
    assertThat(eval("0XAA")).isTrue();
    assertThat(eval("1L")).isTrue();
    assertThat(eval("01L")).isTrue();
    assertThat(eval("1u")).isTrue();
    assertThat(eval("1000000000UL")).isTrue();
    assertThat(eval("0xFFFFFFFFFFFFFFFF")).isTrue();
    assertThat(eval("0xFFFFFFFFFFFFFFFFui64")).isTrue();
    assertThat(eval("0xFFFFFFFFFFFFFFFFLL")).isTrue();
    assertThat(eval("0xFFFFFFFFFFFFFFFFuLL")).isTrue();
    assertThat(eval("0xFFFFFFFFFFFFFFFFll")).isTrue();
    assertThat(eval("0xFFFFFFFFFFFFFFFFull")).isTrue();
    assertThat(eval("0xffffffffffffffffui64")).isTrue();
    assertThat(eval("0xffffffffffffffffi64")).isTrue();
    assertThat(eval("0x7FFFFFL")).isTrue();

    assertThat(eval("0")).isFalse();
    assertThat(eval("0x0")).isFalse();
  }

  @Test
  public void characters() {
    assertThat(eval("'1'")).isTrue();
    assertThat(eval("'a'")).isTrue();

    assertThat(eval("'\0'")).isFalse();
  }

  @Test
  public void conditional_expression() {
    assertThat(eval("1 ? 1 : 0")).isTrue();
    assertThat(eval("0 ? 0 : 1")).isTrue();

    assertThat(eval("1 ? 0 : 1")).isFalse();
    assertThat(eval("0 ? 1 : 0")).isFalse();

    assertThat(eval("1 ? : 0")).isTrue();
    assertThat(eval("0 ? : 1")).isTrue();
  }

  @Test
  public void logical_or() {
    assertThat(eval("1 || 0")).isTrue();
    assertThat(eval("0 || 1")).isTrue();
    assertThat(eval("1 || 1")).isTrue();
    assertThat(eval("0 || 0 || 1")).isTrue();

    assertThat(eval("0 || 0")).isFalse();
    assertThat(eval("0 || 0 || 0")).isFalse();
  }

  @Test
  public void logical_and() {
    assertThat(eval("1 && 1")).isTrue();
    assertThat(eval("1 && 1 && 1")).isTrue();

    assertThat(eval("1 && 0")).isFalse();
    assertThat(eval("0 && 1")).isFalse();
    assertThat(eval("0 && 0")).isFalse();
    assertThat(eval("1 && 1 && 0")).isFalse();
  }

  @Test
  public void inclusive_or() {
    assertThat(eval("1 | 0")).isTrue();
    assertThat(eval("0 | 1")).isTrue();
    assertThat(eval("1 | 1")).isTrue();
    assertThat(eval("0 | 0 | 1")).isTrue();

    assertThat(eval("0 | 0 | 0")).isFalse();
  }

  @Test
  public void exclusive_or() {
    assertThat(eval("1 ^ 0")).isTrue();
    assertThat(eval("0 ^ 1")).isTrue();
    assertThat(eval("0 ^ 1 ^ 0")).isTrue();

    assertThat(eval("0 ^ 0")).isFalse();
    assertThat(eval("0 ^ 1 ^ 1")).isFalse();
  }

  @Test
  public void and_expr() {
    assertThat(eval("1 & 1")).isTrue();
    assertThat(eval("2 & 2 & 2")).isTrue();

    assertThat(eval("0 & 1")).isFalse();
    assertThat(eval("1 & 0")).isFalse();
    assertThat(eval("0 & 0")).isFalse();
    assertThat(eval("2 & 4")).isFalse();
    assertThat(eval("1 & 1 & 4")).isFalse();
  }

  @Test
  public void equality_expr() {
    assertThat(eval("1 == 1")).isTrue();
    assertThat(eval("1 == true")).isTrue();
    assertThat(eval("true == true")).isTrue();
    assertThat(eval("true == 1")).isTrue();
    assertThat(eval("false == 0")).isTrue();
    assertThat(eval("0 == false")).isTrue();

    assertThat(eval("true != 2")).isTrue();
    assertThat(eval("false != 1")).isTrue();
    assertThat(eval("1 != 2")).isTrue();

    assertThat(eval("1 == 0")).isFalse();
    assertThat(eval("3 != 3")).isFalse();
    assertThat(eval("2 != 3 != 4")).isFalse();
    assertThat(eval("0 != 1 != true")).isFalse();

    assertThat(eval("1 == 1 == true")).isTrue();
  }

  @Test
  public void relational_expr() {
    assertThat(eval("0 < 1")).isTrue();
    assertThat(eval("0 <= 1")).isTrue();
    assertThat(eval("1 > 0")).isTrue();
    assertThat(eval("1 >= 0")).isTrue();
    assertThat(eval("0 < 0 < 2")).isTrue();

    assertThat(eval("3 < 2")).isFalse();
    assertThat(eval("3 <= 2")).isFalse();
    assertThat(eval("0 > 1")).isFalse();
    assertThat(eval("0 >= 1")).isFalse();
    assertThat(eval("0 < 1 < 1")).isFalse();

    assertThat(eval("2 > 1 > false")).isTrue();
    assertThat(eval("0 >= 0 >= false")).isTrue();
    assertThat(eval("0 <= 0  >= true")).isTrue();

    assertThat(eval("1 < 1 > false")).isFalse();
    assertThat(eval("0 >= 1 >= true")).isFalse();
    assertThat(eval("2 <= 2 <= false")).isFalse();
  }

  @Test
  public void shift_expr() {
    assertThat(eval("1 << 2")).isTrue();
    assertThat(eval("1 >> 0")).isTrue();

    assertThat(eval("0 << 1")).isFalse();
    assertThat(eval("0 >> 1")).isFalse();
    assertThat(eval("10 >> 1 >> 10")).isFalse();
  }

  @Test
  public void additive_expr() {
    assertThat(eval("1 + 1")).isTrue();
    assertThat(eval("2 - 1")).isTrue();
    assertThat(eval("3 - 3 + 2")).isTrue();

    assertThat(eval("0 + 0")).isFalse();
    assertThat(eval("1 - 1")).isFalse();
    assertThat(eval("3 - 2 - 1")).isFalse();
  }

  @Test
  public void multiplicative_expr() {
    assertThat(eval("1 * 2")).isTrue();
    assertThat(eval("1 / 1")).isTrue();
    assertThat(eval("1 % 2")).isTrue();

    assertThat(eval("0 * 1")).isFalse();
    assertThat(eval("0 / 1")).isFalse();
    assertThat(eval("1 % 1")).isFalse();
    assertThat(eval("1 * 1 * 0")).isFalse();
  }

  @Test
  public void primary_expr() {
    assertThat(eval("(1)")).isTrue();

    assertThat(eval("(0)")).isFalse();
    assertThat(eval("( 0 )")).isFalse();
    assertThat(eval("(1 || 0) && 0")).isFalse();
  }

  @Test
  public void unary_expression() {
    assertThat(eval("+1")).isTrue();
    assertThat(eval("-1")).isTrue();
    assertThat(eval("!0")).isTrue();
    assertThat(eval("~0")).isTrue();

    assertThat(eval("+0")).isFalse();
    assertThat(eval("-0")).isFalse();
    assertThat(eval("!1")).isFalse();
    assertThat(eval("~0xFFFFFFFFFFFFFFFF")).isFalse();
  }

  @Test
  public void identifier_defined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("1");
    assertThat(eval("LALA", pp)).isTrue();
  }

  @Test
  public void self_referential_identifier0() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("A")).thenReturn("A");

    var softly = new SoftAssertions();
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

    assertThat(eval("A", pp)).isTrue();
  }

  @Test
  public void self_referential_identifier2() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("C")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("C");
    when(pp.valueOf("A")).thenReturn("B");

    assertThat(eval("A", pp)).isTrue();
  }

  @Test
  public void self_referential_identifier3() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("C")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("C");
    when(pp.valueOf("A1")).thenReturn("1");
    when(pp.valueOf("A0")).thenReturn("0");
    when(pp.valueOf("A")).thenReturn("A0 + A1 + B");

    assertThat(eval("A", pp)).isTrue();
  }

  @Test
  public void self_referential_identifier4() {
    // https://gcc.gnu.org/onlinedocs/gcc-3.0.1/cpp_3.html#SEC31
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("x")).thenReturn("(4 + y)");
    when(pp.valueOf("y")).thenReturn("(2 * x)");

    var softly = new SoftAssertions();
    softly.assertThat(eval("x", pp)).isTrue();
    softly.assertThat(eval("y", pp)).isTrue();
    softly.assertAll();
  }

  @Test
  public void identifier_undefined() {
    assertThat(eval("LALA")).isFalse();
  }

  @Test
  public void functionlike_macro_defined_true() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.expandFunctionLikeMacro(anyString(), anyList())).thenReturn("1");
    assertThat(eval("has_feature(URG)", pp)).isTrue();
  }

  @Test
  public void functionlike_macro_defined_false() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("0");
    assertThat(eval("has_feature(URG)", pp)).isFalse();
  }

  @Test
  public void functionlike_macro_undefined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn(null);
    assertThat(eval("has_feature(URG)", pp)).isFalse();
  }

  @Test
  public void defined_true_without_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    var macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    assertThat(eval("defined " + macro, pp)).isTrue();
  }

  @Test
  public void defined_false_without_parantheses() {
    assertThat(eval("defined LALA")).isFalse();
  }

  @Test
  public void defined_true_with_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    var macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    assertThat(eval("defined (" + macro + ")", pp)).isTrue();
    assertThat(eval("defined(" + macro + ")", pp)).isTrue();
  }

  @Test
  public void defined_false_with_parantheses() {
    assertThat(eval("defined (LALA)")).isFalse();
    assertThat(eval("defined(LALA)")).isFalse();
  }

  @Test
  public void decode_numbers() {
    assertThat(ExpressionEvaluator.decode("1")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("067")).isEqualTo(new BigInteger("67", 8));
    assertThat(ExpressionEvaluator.decode("0b11")).isEqualTo(new BigInteger("11", 2));
    assertThat(ExpressionEvaluator.decode("0xab")).isEqualTo(new BigInteger("ab", 16));

    assertThat(ExpressionEvaluator.decode("1L")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("1l")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("1U")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("1u")).isEqualTo(new BigInteger("1", 10));

    assertThat(ExpressionEvaluator.decode("1ul")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("1ll")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("1i64")).isEqualTo(new BigInteger("1", 10));
    assertThat(ExpressionEvaluator.decode("1ui64")).isEqualTo(new BigInteger("1", 10));

    assertThat(ExpressionEvaluator.decode("067ll")).isEqualTo(new BigInteger("67", 8));
    assertThat(ExpressionEvaluator.decode("0b11ul")).isEqualTo(new BigInteger("11", 2));
    assertThat(ExpressionEvaluator.decode("0xabui64")).isEqualTo(new BigInteger("ab", 16));

    assertThat(ExpressionEvaluator.decode("1'234")).isEqualTo(new BigInteger("1234", 10));
    assertThat(ExpressionEvaluator.decode("0b1111'0000'1111")).isEqualTo(new BigInteger("111100001111", 2));
    assertThat(ExpressionEvaluator.decode("0xAAAA'bbbb")).isEqualTo(new BigInteger("AAAAbbbb", 16));
  }

  @Test
  public void throw_on_invalid_expressions() {
    EvaluationException thrown = catchThrowableOfType(() -> {
      eval("\"\"");
    }, EvaluationException.class);
    assertThat(thrown).isExactlyInstanceOf(EvaluationException.class);
  }

  @Test
  public void std_macro_evaluated_as_expected() {
    var file = new File("dummy.cpp");
    SquidAstVisitorContext<Grammar> context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    var pp = new CxxPreprocessor(context);
    pp.init();

    assertThat(eval("__LINE__", pp)).isTrue();
    assertThat(eval("__STDC__", pp)).isTrue();
    assertThat(eval("__STDC_HOSTED__", pp)).isTrue();
    assertThat(eval("__cplusplus", pp)).isTrue();
  }

}
