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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.nio.charset.Charset;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PPMacroTest {

  private Parser<Grammar> lineParser;

  @BeforeEach
  public void setUp() {
    lineParser = PPParser.create(Charset.defaultCharset());
  }

  @Test
  public void testCreateMacro() {
    AstNode lineAst = lineParser.parse("#define MACRO(P1, P2) REPLACEMENT_LIST");
    PPMacro result = PPMacro.create(lineAst);

    assertThat(result.name).isEqualTo("MACRO");
    assertThat(result.params)
      .hasSize(2)
      .matches(t -> "P1".equals(t.get(0).getValue()))
      .matches(t -> "P2".equals(t.get(1).getValue()));
    assertThat(result.body)
      .hasSize(1)
      .matches(t -> "REPLACEMENT_LIST".equals(t.get(0).getValue()));
    assertThat(result.isVariadic).isFalse();
    assertThat(result.checkArgumentsCount(2)).isTrue();
    assertThat(result.toString()).isEqualTo("{MACRO(P1, P2):REPLACEMENT_LIST}");
  }

  @Test
  public void testCreateVariadicMacro() {
    AstNode lineAst = lineParser.parse("#define MACRO(...) REPLACEMENT_LIST");
    PPMacro result = PPMacro.create(lineAst);

    assertThat(result.name).isEqualTo("MACRO");
    assertThat(result.params)
      .hasSize(1)
      .matches(t -> "__VA_ARGS__".equals(t.get(0).getValue()));
    assertThat(result.body)
      .hasSize(1)
      .matches(t -> "REPLACEMENT_LIST".equals(t.get(0).getValue()));
    assertThat(result.isVariadic).isTrue();
    assertThat(result.checkArgumentsCount(10)).isTrue();
    assertThat(result.toString()).isEqualTo("{MACRO(__VA_ARGS__...):REPLACEMENT_LIST}");
  }

}
