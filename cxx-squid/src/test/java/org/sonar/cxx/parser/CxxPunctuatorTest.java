/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import com.sonar.cxx.sslr.api.AstNode;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class CxxPunctuatorTest {

  @Test
  void test() {
    var softly = new SoftAssertions();
    softly.assertThat(CxxPunctuator.values()).hasSize(50);

    AstNode astNode = mock(AstNode.class);
    for (var punctuator : CxxPunctuator.values()) {
      softly.assertThat(punctuator.hasToBeSkippedFromAst(astNode)).isFalse();
    }
    softly.assertAll();
  }

}
