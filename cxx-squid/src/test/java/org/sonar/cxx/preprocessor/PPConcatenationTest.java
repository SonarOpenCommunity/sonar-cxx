/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  @Test
  void testConcatenateTwo() {
    List<Token> tokens = lexer.lex("x \t ## \t y");
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(2)
      .matches(t -> "xy".equals(t.get(0).getValue()));
  }

  @Test
  void testConcatenateThree() {
    List<Token> tokens = lexer.lex("x ## y ## z");
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(2)
      .matches(t -> "xyz".equals(t.get(0).getValue()));
  }

  @Test
  void testConcatenateHexNumber() {
    List<Token> tokens = lexer.lex("0x##10");
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(2)
      .matches(t -> "0x10".equals(t.get(0).getValue()));
  }

  @Test
  void testConcatenateLeftError() {
    List<Token> tokens = lexer.lex("##B");
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(2)
      .matches(t -> "B".equals(t.get(0).getValue()));
  }

  @Test
  void testConcatenateRightError() {
    List<Token> tokens = lexer.lex("A##");
    List<Token> result = PPConcatenation.concatenate(tokens);
    assertThat(result)
      .hasSize(2)
      .matches(t -> "A".equals(t.get(0).getValue()));
  }

}
