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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;

class GrammarFunctionsTest {

  @Test
  void test() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);

    assertThat(GrammarFunctions.Standard.firstOf(e1)).isSameAs(e1);
    assertThat(GrammarFunctions.Standard.firstOf(e1, e2)).isInstanceOf(FirstOfExpression.class);
  }

  @Test
  void firstOf_requires_at_least_one_argument() {
    var thrown = catchThrowableOfType(GrammarFunctions.Standard::firstOf,
                                  IllegalArgumentException.class);
    assertThat(thrown).hasMessage("You must define at least one matcher.");
  }

  @Test
  void private_constructors() throws Exception {
    assertThat(hasPrivateConstructor(GrammarFunctions.class)).isTrue();
    assertThat(hasPrivateConstructor(GrammarFunctions.Standard.class)).isTrue();
  }

  private static final boolean hasPrivateConstructor(Class cls) throws Exception {
    var constructor = cls.getDeclaredConstructor();
    var result = !constructor.isAccessible();
    constructor.setAccessible(true);
    constructor.newInstance();
    return result;
  }

}
