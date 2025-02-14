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
package com.sonar.cxx.sslr.impl.channel;

import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RegexpChannelBuilderTest {

  @Test
  void testOpt() {
    assertThat(opt("L")).isEqualTo("L?+");
  }

  @Test
  void testOne2n() {
    assertThat(one2n("L")).isEqualTo("L++");
  }

  @Test
  void testO2n() {
    assertThat(o2n("L")).isEqualTo("L*+");
  }

  @Test
  void testg() {
    assertThat(g("L")).isEqualTo("(L)");
    assertThat(g("L", "l")).isEqualTo("(Ll)");
  }

  @Test
  void testOr() {
    assertThat(or("L", "l", "U", "u")).isEqualTo("(L|l|U|u)");
  }

  @Test
  void testAnyButNot() {
    assertThat(anyButNot("L", "l")).isEqualTo("[^Ll]");
  }

}
