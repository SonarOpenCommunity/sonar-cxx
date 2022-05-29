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

import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PPPredefinedMacrosTest {

  @Test
  void testPredefinedMacroValues() {
    var expResult = Arrays.asList(
      "__FILE__",
      "__LINE__",
      "__DATE__",
      "__TIME__",
      "__STDC__",
      "__STDC_HOSTED__",
      "__cplusplus",
      "__has_include"
    );
    String[] result = PPPredefinedMacros.predefinedMacroValues();
    assertThat(result)
      .hasSize(8)
      .allMatch(s -> expResult.contains(s.split(" ")[0]));
  }

}
