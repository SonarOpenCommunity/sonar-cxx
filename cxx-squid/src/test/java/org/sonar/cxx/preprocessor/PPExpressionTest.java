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
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class PPExpressionTest {

  private static PPExpression constantExpression;

  private static boolean evaluate(String constExpr, CxxPreprocessor preprocessor) {
    return new PPExpression(preprocessor).evaluate(constExpr);
  }

  private boolean evaluate(String constExpr) {
    return constantExpression.evaluate(constExpr);
  }

  @BeforeAll
  public static void init() {
    var pp = mock(CxxPreprocessor.class);
    constantExpression = new PPExpression(pp);
  }

  @Test
  void bools() {
    assertThat(evaluate("true")).isTrue();
    assertThat(evaluate("false")).isFalse();
  }

  @Test
  void numbers() {
    assertThat(evaluate("1")).isTrue();
    assertThat(evaluate("0xAA")).isTrue();
    assertThat(evaluate("0XAA")).isTrue();
    assertThat(evaluate("1L")).isTrue();
    assertThat(evaluate("01L")).isTrue();
    assertThat(evaluate("1u")).isTrue();
    assertThat(evaluate("1000000000UL")).isTrue();
    assertThat(evaluate("0xFFFFFFFFFFFFFFFF")).isTrue();
    assertThat(evaluate("0xFFFFFFFFFFFFFFFFui64")).isTrue();
    assertThat(evaluate("0xFFFFFFFFFFFFFFFFLL")).isTrue();
    assertThat(evaluate("0xFFFFFFFFFFFFFFFFuLL")).isTrue();
    assertThat(evaluate("0xFFFFFFFFFFFFFFFFll")).isTrue();
    assertThat(evaluate("0xFFFFFFFFFFFFFFFFull")).isTrue();
    assertThat(evaluate("0xffffffffffffffffui64")).isTrue();
    assertThat(evaluate("0xffffffffffffffffi64")).isTrue();
    assertThat(evaluate("0x7FFFFFL")).isTrue();

    assertThat(evaluate("0")).isFalse();
    assertThat(evaluate("0x0")).isFalse();
  }

  @Test
  void characters() {
    assertThat(evaluate("'1'")).isTrue();
    assertThat(evaluate("'a'")).isTrue();

    assertThat(evaluate("'\0'")).isFalse();
  }

  @Test
  void conditional_expression() {
    assertThat(evaluate("1 ? 1 : 0")).isTrue();
    assertThat(evaluate("0 ? 0 : 1")).isTrue();

    assertThat(evaluate("1 ? 0 : 1")).isFalse();
    assertThat(evaluate("0 ? 1 : 0")).isFalse();

    assertThat(evaluate("1 ? : 0")).isTrue();
    assertThat(evaluate("0 ? : 1")).isTrue();
  }

  @Test
  void logical_or() {
    assertThat(evaluate("1 || 0")).isTrue();
    assertThat(evaluate("0 || 1")).isTrue();
    assertThat(evaluate("1 || 1")).isTrue();
    assertThat(evaluate("0 || 0 || 1")).isTrue();

    assertThat(evaluate("0 || 0")).isFalse();
    assertThat(evaluate("0 || 0 || 0")).isFalse();
  }

  @Test
  void logical_and() {
    assertThat(evaluate("1 && 1")).isTrue();
    assertThat(evaluate("1 && 1 && 1")).isTrue();

    assertThat(evaluate("1 && 0")).isFalse();
    assertThat(evaluate("0 && 1")).isFalse();
    assertThat(evaluate("0 && 0")).isFalse();
    assertThat(evaluate("1 && 1 && 0")).isFalse();
  }

  @Test
  void inclusive_or() {
    assertThat(evaluate("1 | 0")).isTrue();
    assertThat(evaluate("0 | 1")).isTrue();
    assertThat(evaluate("1 | 1")).isTrue();
    assertThat(evaluate("0 | 0 | 1")).isTrue();

    assertThat(evaluate("0 | 0 | 0")).isFalse();
  }

  @Test
  void exclusive_or() {
    assertThat(evaluate("1 ^ 0")).isTrue();
    assertThat(evaluate("0 ^ 1")).isTrue();
    assertThat(evaluate("0 ^ 1 ^ 0")).isTrue();

    assertThat(evaluate("0 ^ 0")).isFalse();
    assertThat(evaluate("0 ^ 1 ^ 1")).isFalse();
  }

  @Test
  void and_expr() {
    assertThat(evaluate("1 & 1")).isTrue();
    assertThat(evaluate("2 & 2 & 2")).isTrue();

    assertThat(evaluate("0 & 1")).isFalse();
    assertThat(evaluate("1 & 0")).isFalse();
    assertThat(evaluate("0 & 0")).isFalse();
    assertThat(evaluate("2 & 4")).isFalse();
    assertThat(evaluate("1 & 1 & 4")).isFalse();
  }

  @Test
  void equality_expr() {
    assertThat(evaluate("1 == 1")).isTrue();
    assertThat(evaluate("1 == true")).isTrue();
    assertThat(evaluate("true == true")).isTrue();
    assertThat(evaluate("true == 1")).isTrue();
    assertThat(evaluate("false == 0")).isTrue();
    assertThat(evaluate("0 == false")).isTrue();

    assertThat(evaluate("true != 2")).isTrue();
    assertThat(evaluate("false != 1")).isTrue();
    assertThat(evaluate("1 != 2")).isTrue();

    assertThat(evaluate("1 == 0")).isFalse();
    assertThat(evaluate("3 != 3")).isFalse();
    assertThat(evaluate("2 != 3 != 4")).isFalse();
    assertThat(evaluate("0 != 1 != true")).isFalse();

    assertThat(evaluate("1 == 1 == true")).isTrue();
  }

  @Test
  void relational_expr() {
    assertThat(evaluate("0 < 1")).isTrue();
    assertThat(evaluate("0 <= 1")).isTrue();
    assertThat(evaluate("1 > 0")).isTrue();
    assertThat(evaluate("1 >= 0")).isTrue();
    assertThat(evaluate("0 < 0 < 2")).isTrue();

    assertThat(evaluate("3 < 2")).isFalse();
    assertThat(evaluate("3 <= 2")).isFalse();
    assertThat(evaluate("0 > 1")).isFalse();
    assertThat(evaluate("0 >= 1")).isFalse();
    assertThat(evaluate("0 < 1 < 1")).isFalse();

    assertThat(evaluate("2 > 1 > false")).isTrue();
    assertThat(evaluate("0 >= 0 >= false")).isTrue();
    assertThat(evaluate("0 <= 0  >= true")).isTrue();

    assertThat(evaluate("1 < 1 > false")).isFalse();
    assertThat(evaluate("0 >= 1 >= true")).isFalse();
    assertThat(evaluate("2 <= 2 <= false")).isFalse();
  }

  @Test
  void shift_expr() {
    assertThat(evaluate("1 << 2")).isTrue();
    assertThat(evaluate("1 >> 0")).isTrue();

    assertThat(evaluate("0 << 1")).isFalse();
    assertThat(evaluate("0 >> 1")).isFalse();
    assertThat(evaluate("10 >> 1 >> 10")).isFalse();
  }

  @Test
  void additive_expr() {
    assertThat(evaluate("1 + 1")).isTrue();
    assertThat(evaluate("2 - 1")).isTrue();
    assertThat(evaluate("3 - 3 + 2")).isTrue();

    assertThat(evaluate("0 + 0")).isFalse();
    assertThat(evaluate("1 - 1")).isFalse();
    assertThat(evaluate("3 - 2 - 1")).isFalse();
  }

  @Test
  void multiplicative_expr() {
    assertThat(evaluate("1 * 2")).isTrue();
    assertThat(evaluate("1 / 1")).isTrue();
    assertThat(evaluate("1 % 2")).isTrue();

    assertThat(evaluate("0 * 1")).isFalse();
    assertThat(evaluate("0 / 1")).isFalse();
    assertThat(evaluate("1 % 1")).isFalse();
    assertThat(evaluate("1 * 1 * 0")).isFalse();
  }

  @Test
  void primary_expr() {
    assertThat(evaluate("(1)")).isTrue();

    assertThat(evaluate("(0)")).isFalse();
    assertThat(evaluate("( 0 )")).isFalse();
    assertThat(evaluate("(1 || 0) && 0")).isFalse();
  }

  @Test
  void unary_expression() {
    assertThat(evaluate("+1")).isTrue();
    assertThat(evaluate("-1")).isTrue();
    assertThat(evaluate("!0")).isTrue();
    assertThat(evaluate("~0")).isTrue();

    assertThat(evaluate("+0")).isFalse();
    assertThat(evaluate("-0")).isFalse();
    assertThat(evaluate("!1")).isFalse();
    assertThat(evaluate("~0xFFFFFFFFFFFFFFFF")).isFalse();
  }

  @Test
  void identifier_defined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("1");
    assertThat(evaluate("LALA", pp)).isTrue();
  }

  @Test
  void self_referential_identifier0() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("A")).thenReturn("A");

    var softly = new SoftAssertions();
    softly.assertThat(evaluate("A", pp)).isTrue();
    softly.assertThat(evaluate("A && A", pp)).isTrue();
    softly.assertThat(evaluate("A && !A", pp)).isFalse();
    softly.assertAll();
  }

  @Test
  void self_referential_identifier1() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("A")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("A");

    assertThat(evaluate("A", pp)).isTrue();
  }

  @Test
  void self_referential_identifier2() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("C")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("C");
    when(pp.valueOf("A")).thenReturn("B");

    assertThat(evaluate("A", pp)).isTrue();
  }

  @Test
  void self_referential_identifier3() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("C")).thenReturn("B");
    when(pp.valueOf("B")).thenReturn("C");
    when(pp.valueOf("A1")).thenReturn("1");
    when(pp.valueOf("A0")).thenReturn("0");
    when(pp.valueOf("A")).thenReturn("A0 + A1 + B");

    assertThat(evaluate("A", pp)).isTrue();
  }

  @Test
  void self_referential_identifier4() {
    // https://gcc.gnu.org/onlinedocs/gcc-3.0.1/cpp_3.html#SEC31
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf("x")).thenReturn("(4 + y)");
    when(pp.valueOf("y")).thenReturn("(2 * x)");

    var softly = new SoftAssertions();
    softly.assertThat(evaluate("x", pp)).isTrue();
    softly.assertThat(evaluate("y", pp)).isTrue();
    softly.assertAll();
  }

  @Test
  void identifier_undefined() {
    assertThat(evaluate("LALA")).isFalse();
  }

  @Test
  void functionlike_macro_defined_true() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.expandFunctionLikeMacro(anyString(), anyList())).thenReturn("1");
    assertThat(evaluate("has_feature(URG)", pp)).isTrue();
  }

  @Test
  void functionlike_macro_defined_false() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn("0");
    assertThat(evaluate("has_feature(URG)", pp)).isFalse();
  }

  @Test
  void functionlike_macro_undefined() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    when(pp.valueOf(anyString())).thenReturn(null);
    assertThat(evaluate("has_feature(URG)", pp)).isFalse();
  }

  @Test
  void defined_true_without_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    var macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    assertThat(evaluate("defined " + macro, pp)).isTrue();
  }

  @Test
  void defined_false_without_parantheses() {
    assertThat(evaluate("defined LALA")).isFalse();
  }

  @Test
  void defined_true_with_parantheses() {
    CxxPreprocessor pp = mock(CxxPreprocessor.class);
    var macro = "LALA";
    when(pp.valueOf(macro)).thenReturn("1");
    assertThat(evaluate("defined (" + macro + ")", pp)).isTrue();
    assertThat(evaluate("defined(" + macro + ")", pp)).isTrue();
  }

  @Test
  void defined_false_with_parantheses() {
    assertThat(evaluate("defined (LALA)")).isFalse();
    assertThat(evaluate("defined(LALA)")).isFalse();
  }

  @Test
  void throw_on_invalid_expressions() {
    EvaluationException thrown = catchThrowableOfType(() -> {
      evaluate("\"\"");
    }, EvaluationException.class);
    assertThat(thrown).isExactlyInstanceOf(EvaluationException.class);
  }

  @Test
  void std_macro_evaluated_as_expected() {
    var file = new File("dummy.cpp");
    SquidAstVisitorContext<Grammar> context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    var pp = new CxxPreprocessor(context);
    pp.init();

    // evaluate numbers only, constantExpression can't be a string
    // assertThat(evaluate("__FILE__", pp)).isTrue(); => STRING
    assertThat(evaluate("__LINE__", pp)).isTrue();
    // assertThat(evaluate("__DATE__", pp)).isTrue(); => STRING
    // assertThat(evaluate("__TIME__", pp)).isTrue(); => STRING
    assertThat(evaluate("__STDC__", pp)).isTrue();
    assertThat(evaluate("__STDC_HOSTED__", pp)).isTrue();
    assertThat(evaluate("__cplusplus", pp)).isTrue();
    assertThat(evaluate("__has_include", pp)).isTrue();
  }

}
