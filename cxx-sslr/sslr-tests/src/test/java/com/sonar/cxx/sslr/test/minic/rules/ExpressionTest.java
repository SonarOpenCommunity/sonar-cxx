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
package com.sonar.cxx.sslr.test.minic.rules;

import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.sslr.tests.Assertions.assertThat;

public class ExpressionTest extends RuleTest {

  @Override
  @BeforeEach
  public void init() {
    p.setRootRule(g.rule(MiniCGrammar.EXPRESSION));
  }

  @Test
  public void reallife() {
    assertThat(p)
      .matches("1")
      .matches("1 + 1")
      .matches("1 + 1 * 1")
      .matches("(1)")
      .matches("myVariable")
      .matches("myVariable = 0")
      .notMatches("myVariable = myVariable2 = 0")
      .matches("myFunction()")
      .matches("myFunction(arg1, arg2, 1*3)")
      .matches("myVariable++")
      .matches("++myVariable")
      .notMatches("++++myVariable")
      .matches("myVariable = i++")
      .matches("myVariable = myFunction(1, 3)*2")
      .matches("++((myVariable))");
  }

}
