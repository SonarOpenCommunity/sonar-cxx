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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PPConcatenationTest {

  private Lexer lexer;

  @BeforeEach
  void setUp() {
    lexer = PPLexer.create();
  }

  @Test
  void testConcatenateNone() {
    List<Token> tokens = lexer.lex(" x y z ");
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(4);
  }

  static Stream<Arguments> sourceCodeToConcatenate() {
    return Stream.of(
      Arguments.of("x \t ## \t y", "xy"),
      Arguments.of("x ## y ## z", "xyz"),
      Arguments.of("0x##10", "0x10"),
      Arguments.of("##B", "B"),
      Arguments.of("A##", "A")
    );
  }

  @ParameterizedTest
  @MethodSource("sourceCodeToConcatenate")
  void testConcatenate(String sourceCode, String concatenated) {
    List<Token> tokens = lexer.lex(sourceCode);
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(2)
      .matches(t -> concatenated.equals(t.get(0).getValue()));
  }

}
