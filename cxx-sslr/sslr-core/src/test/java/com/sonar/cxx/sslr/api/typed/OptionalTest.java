/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
package com.sonar.cxx.sslr.api.typed;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OptionalTest {

  private final Optional<String> present = Optional.of("foo");
  private final Optional<String> absent = Optional.absent();

  @Test
  void present() {
    assertThat(present.isPresent()).isTrue();

    assertThat(present.orNull()).isSameAs("foo");

    assertThat(present.or("bar")).isSameAs("foo");

    assertThat(present.get()).isSameAs("foo");

    assertThat(present).hasToString("Optional.of(foo)");

    assertThat(present.equals(present)).isTrue();
    assertThat(present.equals(Optional.of("foo"))).isTrue();
    assertThat(present.equals(Optional.of("bar"))).isFalse();
    assertThat(present.equals(absent)).isFalse();

    assertThat(present.hashCode()).isEqualTo(0x598df91c + "foo".hashCode());
  }

  @Test
  void absent() {
    assertThat(absent.isPresent()).isFalse();

    assertThat(absent.orNull()).isNull();

    assertThat(absent.or("bar")).isSameAs("bar");

    assertThat(absent).hasToString("Optional.absent()");

    assertThat(absent.equals(present)).isFalse();
    assertThat(absent.equals(absent)).isTrue();

    assertThat(absent.hashCode()).isEqualTo(0x598df91c);

    var thrown = catchThrowableOfType(absent::get, IllegalStateException.class);
    assertThat(thrown).hasMessage("value is absent");
  }

  @Test
  void present_or_null() {
    var thrown = catchThrowableOfType(() -> present.or(null), NullPointerException.class);
    assertThat(thrown).hasMessage("use orNull() instead of or(null)");
  }

  @Test
  void absent_or_null() {
    var thrown = catchThrowableOfType(() -> absent.or(null), NullPointerException.class);
    assertThat(thrown).hasMessage("use orNull() instead of or(null)");
  }

}
