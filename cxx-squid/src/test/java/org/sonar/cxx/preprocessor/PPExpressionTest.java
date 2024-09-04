/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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

import java.io.File;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class PPExpressionTest {

  private CxxPreprocessor pp;
  private PPExpression constantExpression;

  private boolean evaluate(String constExpr) {
    return constantExpression.evaluate(constExpr);
  }

  @BeforeEach
  void setUp() {
    var context = mock(SquidAstVisitorContext.class);
    var file = new File("dummy"); // necessary for init()
    when(context.getFile()).thenReturn(file);
    pp = spy(new CxxPreprocessor(context, new CxxSquidConfiguration()));
    pp.init();
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
    assertThat(evaluate("1z")).isTrue();
    assertThat(evaluate("1ZU")).isTrue();
    assertThat(evaluate("0x1z")).isTrue();
    assertThat(evaluate("0x1ZU")).isTrue();
    assertThat(evaluate("0b1z")).isTrue();
    assertThat(evaluate("0b1ZU")).isTrue();
    assertThat(evaluate("1.")).isTrue();
    assertThat(evaluate("1.f")).isTrue();
    assertThat(evaluate("1.F")).isTrue();
    assertThat(evaluate("1.l")).isTrue();
    assertThat(evaluate("1.L")).isTrue();
    assertThat(evaluate("1.f16")).isTrue();
    assertThat(evaluate("1.f32")).isTrue();
    assertThat(evaluate("1.f64")).isTrue();
    assertThat(evaluate("1.f128")).isTrue();
    assertThat(evaluate("1.bf16")).isTrue();
    assertThat(evaluate("1.F16")).isTrue();
    assertThat(evaluate("1.F32")).isTrue();
    assertThat(evaluate("1.F64")).isTrue();
    assertThat(evaluate("1.F128")).isTrue();
    assertThat(evaluate("1.BF16")).isTrue();

    assertThat(evaluate("0")).isFalse();
    assertThat(evaluate("0x0")).isFalse();
    assertThat(evaluate("0b0")).isFalse();
    assertThat(evaluate("0z")).isFalse();
    assertThat(evaluate("0ZU")).isFalse();
    assertThat(evaluate("0x0z")).isFalse();
    assertThat(evaluate("0x0ZU")).isFalse();
    assertThat(evaluate("0b0z")).isFalse();
    assertThat(evaluate("0b0ZU")).isFalse();
    assertThat(evaluate("0.")).isFalse();
    assertThat(evaluate("0.f")).isFalse();
    assertThat(evaluate("0.F")).isFalse();
    assertThat(evaluate("0.l")).isFalse();
    assertThat(evaluate("0.L")).isFalse();
    assertThat(evaluate("0.f16")).isFalse();
    assertThat(evaluate("0.f32")).isFalse();
    assertThat(evaluate("0.f64")).isFalse();
    assertThat(evaluate("0.f128")).isFalse();
    assertThat(evaluate("0.bf16")).isFalse();
    assertThat(evaluate("0.F16")).isFalse();
    assertThat(evaluate("0.F32")).isFalse();
    assertThat(evaluate("0.F64")).isFalse();
    assertThat(evaluate("0.F128")).isFalse();
    assertThat(evaluate("0.BF16")).isFalse();
  }

  @Test
  void characters() {
    assertThat(evaluate("")).isFalse();
    assertThat(evaluate("'\0'")).isFalse();
    assertThat(evaluate("'\\x00'")).isFalse();

    assertThat(evaluate("'1'")).isTrue();
    assertThat(evaluate("'a'")).isTrue();
    assertThat(evaluate("'\\1'")).isTrue();
    assertThat(evaluate("'\\x01'")).isTrue();
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
    doReturn(PPMacro.create("#define LALA 1")).when(pp).getMacro("LALA");
    assertThat(evaluate("LALA")).isTrue();
  }

  @Test
  void self_referential_identifier0() {
    doReturn(PPMacro.create("#define A A")).when(pp).getMacro("A");

    var softly = new SoftAssertions();
    softly.assertThat(evaluate("A")).isTrue();
    softly.assertThat(evaluate("A && A")).isTrue();
    softly.assertThat(evaluate("A && !A")).isFalse();
    softly.assertAll();
  }

  @Test
  void self_referential_identifier1() {
    doReturn(PPMacro.create("#define A B")).when(pp).getMacro("A");
    doReturn(PPMacro.create("#define B A")).when(pp).getMacro("B");

    assertThat(evaluate("A")).isTrue();
  }

  @Test
  void self_referential_identifier2() {
    doReturn(PPMacro.create("#define C B")).when(pp).getMacro("C");
    doReturn(PPMacro.create("#define B C")).when(pp).getMacro("B");
    doReturn(PPMacro.create("#define A B")).when(pp).getMacro("A");

    assertThat(evaluate("A")).isTrue();
  }

  @Test
  void self_referential_identifier3() {
    doReturn(PPMacro.create("#define C B")).when(pp).getMacro("C");
    doReturn(PPMacro.create("#define B C")).when(pp).getMacro("B");
    doReturn(PPMacro.create("#define A1 1")).when(pp).getMacro("A1");
    doReturn(PPMacro.create("#define A0 0")).when(pp).getMacro("A0");
    doReturn(PPMacro.create("#define A A0 + A1 + B")).when(pp).getMacro("A");

    assertThat(evaluate("A")).isTrue();
  }

  @Test
  void self_referential_identifier4() {
    // https://gcc.gnu.org/onlinedocs/gcc-3.0.1/cpp_3.html#SEC31
    doReturn(PPMacro.create("#define x (4 + y)")).when(pp).getMacro("x");
    doReturn(PPMacro.create("#define y (2 * x)")).when(pp).getMacro("y");

    var softly = new SoftAssertions();
    softly.assertThat(evaluate("x")).isTrue();
    softly.assertThat(evaluate("y")).isTrue();
    softly.assertAll();
  }

  @Test
  void identifier_undefined() {
    assertThat(evaluate("LALA")).isFalse();
  }

  @Test
  void functionlike_macro_defined_true() {
    doReturn(PPMacro.create("#define has_feature(a) 1")).when(pp).getMacro(any());
    assertThat(evaluate("has_feature(URG)")).isTrue();
  }

  @Test
  void functionlike_macro_defined_false() {
    doReturn(PPMacro.create("#define has_feature(a) 0")).when(pp).getMacro(any());
    assertThat(evaluate("has_feature(URG)")).isFalse();
  }

  @Test
  void functionlike_macro_undefined() {
    doReturn(null).when(pp).getMacro(any());
    assertThat(evaluate("has_feature(URG)")).isFalse();
  }

  @Test
  void defined_true_without_parantheses() {
    var macro = "LALA";
    doReturn(PPMacro.create("#define " + macro + " 1")).when(pp).getMacro(any());
    assertThat(evaluate("defined " + macro)).isTrue();
  }

  @Test
  void defined_false_without_parantheses() {
    assertThat(evaluate("defined LALA")).isFalse();
  }

  @Test
  void defined_true_with_parantheses() {
    var macro = "LALA";
    doReturn(PPMacro.create("#define " + macro + " 1")).when(pp).getMacro(any());
    assertThat(evaluate("defined (" + macro + ")")).isTrue();
    assertThat(evaluate("defined(" + macro + ")")).isTrue();
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
    // evaluate numbers only, constantExpression can't be a string
    // assertThat(evaluate("__FILE__")).isTrue(); => STRING
    assertThat(evaluate("__LINE__")).isTrue();
    // assertThat(evaluate("__DATE__")).isTrue(); => STRING
    // assertThat(evaluate("__TIME__")).isTrue(); => STRING
    assertThat(evaluate("__STDC__")).isTrue();
    assertThat(evaluate("__STDC_HOSTED__")).isTrue();
    assertThat(evaluate("__cplusplus")).isTrue();
    assertThat(evaluate("__has_builtin")).isFalse();
    assertThat(evaluate("__has_feature")).isFalse();
    assertThat(evaluate("__has_extension")).isFalse();
    assertThat(evaluate("__has_cpp_attribute")).isFalse();
    assertThat(evaluate("__has_c_attribute")).isFalse();
    assertThat(evaluate("__has_attribute")).isFalse();
    assertThat(evaluate("__has_declspec_attribute")).isFalse();
    assertThat(evaluate("__is_identifier")).isTrue();
    assertThat(evaluate("__has_include")).isTrue();
    assertThat(evaluate("__has_include_next")).isTrue();
    assertThat(evaluate("__has_warning")).isFalse();
  }

}
