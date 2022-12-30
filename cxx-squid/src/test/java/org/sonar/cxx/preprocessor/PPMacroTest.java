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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PPMacroTest {

  @Test
  void testCreateMacro() {
    PPMacro result = PPMacro.create("#define MACRO(P1, P2) REPLACEMENT_LIST");

    assertThat(result.identifier).isEqualTo("MACRO");
    assertThat(result.parameterList)
      .hasSize(2)
      .matches(t -> "P1".equals(t.get(0).getValue()))
      .matches(t -> "P2".equals(t.get(1).getValue()));
    assertThat(result.replacementList)
      .hasSize(1)
      .matches(t -> "REPLACEMENT_LIST".equals(t.get(0).getValue()));
    assertThat(result.isVariadic).isFalse();
    assertThat(result.checkArgumentsCount(2)).isTrue();
    assertThat(result.toString()).isEqualTo("{MACRO(P1, P2):REPLACEMENT_LIST}");
  }

  @Test
  void testCreateVariadicMacro() {
    PPMacro result = PPMacro.create("#define MACRO(...) REPLACEMENT_LIST");

    assertThat(result.identifier).isEqualTo("MACRO");
    assertThat(result.parameterList)
      .hasSize(1)
      .matches(t -> "__VA_ARGS__".equals(t.get(0).getValue()));
    assertThat(result.replacementList)
      .hasSize(1)
      .matches(t -> "REPLACEMENT_LIST".equals(t.get(0).getValue()));
    assertThat(result.isVariadic).isTrue();
    assertThat(result.checkArgumentsCount(10)).isTrue();
    assertThat(result.toString()).isEqualTo("{MACRO(__VA_ARGS__...):REPLACEMENT_LIST}");
  }

  @Test
  void testGetParameterIndex() {
    PPMacro macro = PPMacro.create("#define MACRO(P1, P2) REPLACEMENT_LIST");
    assertThat(macro.getParameterIndex("P1")).isEqualTo(0);
    assertThat(macro.getParameterIndex("P2")).isEqualTo(1);

    var parameterNames = macro.getParameterNames();
    assertThat(parameterNames.indexOf("P1")).isEqualTo(0);
    assertThat(parameterNames.indexOf("P2")).isEqualTo(1);
  }

}
