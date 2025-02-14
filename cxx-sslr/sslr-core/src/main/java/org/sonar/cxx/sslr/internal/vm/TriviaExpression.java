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
package org.sonar.cxx.sslr.internal.vm; // cxx: in use

import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import org.sonar.cxx.sslr.internal.matchers.Matcher;

public class TriviaExpression implements Matcher, ParsingExpression {

  private final TriviaKind triviaKind;
  private final ParsingExpression subExpression;

  public TriviaExpression(TriviaKind triviaKind, ParsingExpression subExpression) {
    this.triviaKind = triviaKind;
    this.subExpression = subExpression;
  }

  public TriviaKind getTriviaKind() {
    return triviaKind;
  }

  /**
   * Compiles this expression into a sequence of instructions:
   * <pre>
   * Call L1
   * Jump L2
   * L1: subExpression
   * Return
   * L2: ...
   * </pre>
   */
  @Override
  public Instruction[] compile(CompilationHandler compiler) {
    return TokenExpression.compile(compiler, this, subExpression);
  }

  @Override
  public String toString() {
    return "Trivia " + triviaKind + "[" + subExpression + "]";
  }

}
