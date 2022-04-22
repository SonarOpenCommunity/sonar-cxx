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
package com.sonar.cxx.sslr.impl.matcher;

import static com.sonar.cxx.sslr.impl.MockTokenType.WORD;
import static com.sonar.cxx.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
import static com.sonar.cxx.sslr.impl.matcher.GrammarFunctions.Standard.opt;
import static com.sonar.cxx.sslr.impl.matcher.GrammarFunctions.Standard.or;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleMatcherTest {

  private RuleDefinition javaClassDefinition;
  private Matcher opMatcher;

  @BeforeEach
  public void init() {
    javaClassDefinition = new RuleDefinition("JavaClassDefinition");
    opMatcher = opt("implements", WORD, o2n(",", WORD));
    javaClassDefinition.is("public", or("class", "interface"), opMatcher);
  }

  @Test
  void getName() {
    assertThat(javaClassDefinition.getName()).isEqualTo("JavaClassDefinition");
  }

  @Test
  void getToString() {
    assertThat(javaClassDefinition.getName()).isEqualTo("JavaClassDefinition");
  }

}
