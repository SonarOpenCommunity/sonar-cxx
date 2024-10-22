/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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

class ConfigurationPropertyTest {

  @Test
  void getName() {
    assertThat(new ConfigurationProperty("foo", "", "").getName()).isEqualTo("foo");
    assertThat(new ConfigurationProperty("bar", "", "").getName()).isEqualTo("bar");
  }

  @Test
  void getDescription() {
    assertThat(new ConfigurationProperty("", "foo", "").getDescription()).isEqualTo("foo");
    assertThat(new ConfigurationProperty("", "bar", "").getDescription()).isEqualTo("bar");
  }

  @Test
  void validate() {
    assertThat(new ConfigurationProperty("", "", "").validate("")).isEmpty();
    assertThat(new ConfigurationProperty("", "", "").validate("foo")).isEmpty();

    var property = new ConfigurationProperty("", "", "foo", (String newValueCandidate) -> "foo".equals(newValueCandidate)
      ? ""
      : "Only the value \"foo\" is allowed.");
    assertThat(property.validate("")).isEqualTo("Only the value \"foo\" is allowed.");
    assertThat(property.validate("foo")).isEmpty();
    assertThat(property.validate("bar")).isEqualTo("Only the value \"foo\" is allowed.");
  }

  @Test
  void setValueShouldSucceedIfValidationPasses() {
    new ConfigurationProperty("", "", "").setValue("");
    new ConfigurationProperty("", "", "").setValue("foo");
  }

  @Test
  void setValueShouldFailIfValidationFails() {
    var thrown = catchThrowableOfType(IllegalArgumentException.class,
      () -> new ConfigurationProperty("", "", "", (String newValueCandidate)
        -> newValueCandidate.isEmpty() ? "" : "The value \"" + newValueCandidate + "\" did not pass validation: Not valid!")
        .setValue("foo")
    );
    assertThat(thrown).hasMessageContaining("The value \"foo\" did not pass validation: Not valid!");
  }

  @Test
  void getValue() {
    assertThat(new ConfigurationProperty("", "", "").getValue()).isEmpty();
    assertThat(new ConfigurationProperty("", "", "foo").getValue()).isEqualTo("foo");

    var property = new ConfigurationProperty("", "", "");
    assertThat(property.getValue()).isEmpty();
    property.setValue("foo");
    assertThat(property.getValue()).isEqualTo("foo");
  }

}
