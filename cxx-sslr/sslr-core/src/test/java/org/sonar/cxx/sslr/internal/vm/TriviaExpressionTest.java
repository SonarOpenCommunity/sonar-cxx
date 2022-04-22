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
package org.sonar.cxx.sslr.internal.vm;

import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class TriviaExpressionTest {

  @Test
  void should_compile() {
    var expression = new TriviaExpression(TriviaKind.COMMENT, new SubExpression(1, 2));
    assertThat(expression).hasToString("Trivia COMMENT[SubExpression]");
    var instructions = expression.compile(new CompilationHandler());
    assertThat(instructions).isEqualTo(new Instruction[]{
      Instruction.call(2, expression),
      Instruction.jump(5),
      Instruction.ignoreErrors(),
      SubExpression.mockInstruction(1),
      SubExpression.mockInstruction(2),
      Instruction.ret()
    });
  }

  @Test
  void should_implement_Matcher() {
    var expression = new TriviaExpression(TriviaKind.COMMENT, mock(ParsingExpression.class));
    // Important for AstCreator
    assertThat(expression.getTriviaKind()).isSameAs(TriviaKind.COMMENT);
  }

}
