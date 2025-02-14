/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.toolkit;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ValidatorsTest {

  @Test
  void charsetValidator() {
    var validator = Validators.charsetValidator();
    assertThat(validator.validate("UTF-8")).isEmpty();
    assertThat(validator.validate("ISO-8859-15")).isEmpty();
    assertThat(validator.validate("foo")).isEqualTo("Unsupported charset: foo");
    assertThat(validator.validate(" ")).isEqualTo("Illegal charset:  ");
  }

  @Test
  void charsetValidatorSingleInstance() {
    assertThat(Validators.charsetValidator()).isSameAs(Validators.charsetValidator());
  }

  @Test
  void integerRangeValidator() {
    var validator = Validators.integerRangeValidator(0, 42);
    assertThat(validator.validate("24")).isEmpty();
    assertThat(validator.validate("-100")).isEqualTo("Must be between 0 and 42: -100");
    assertThat(validator.validate("100")).isEqualTo("Must be between 0 and 42: 100");
    assertThat(validator.validate("foo")).isEqualTo("Not an integer: foo");

    assertThat(Validators.integerRangeValidator(42, 42).validate("43")).isEqualTo("Must be equal to 42: 43");
    assertThat(Validators.integerRangeValidator(Integer.MIN_VALUE, 0).validate("1")).isEqualTo(
      "Must be negative or 0: 1");
    assertThat(Validators.integerRangeValidator(Integer.MIN_VALUE, -1).validate("0")).isEqualTo(
      "Must be strictly negative: 0");
    assertThat(Validators.integerRangeValidator(Integer.MIN_VALUE, 42).validate("43")).isEqualTo(
      "Must be lower or equal to 42: 43");
    assertThat(Validators.integerRangeValidator(0, Integer.MAX_VALUE).validate("-1")).isEqualTo(
      "Must be positive or 0: -1");
    assertThat(Validators.integerRangeValidator(1, Integer.MAX_VALUE).validate("0")).isEqualTo(
      "Must be strictly positive: 0");
    assertThat(Validators.integerRangeValidator(42, Integer.MAX_VALUE).validate("41")).isEqualTo(
      "Must be greater or equal to 42: 41");
  }

  @Test
  void integerRangeValidatorShouldFailWithUpperSmallerThanLowerBound() {
    var thrown = catchThrowableOfType(IllegalArgumentException.class,
      () -> Validators.integerRangeValidator(42, 0)
    );
    assertThat(thrown).hasMessage("lowerBound(42) <= upperBound(0)");
  }

  @Test
  void booleanValidator() {
    var validator = Validators.booleanValidator();
    assertThat(validator.validate("true")).isEmpty();
    assertThat(validator.validate("false")).isEmpty();
    assertThat(validator.validate("foo")).isEqualTo("Must be either \"true\" or \"false\": foo");
  }

  @Test
  void booleanValidatorSingleInstance() {
    assertThat(Validators.booleanValidator()).isSameAs(Validators.booleanValidator());
  }

}
