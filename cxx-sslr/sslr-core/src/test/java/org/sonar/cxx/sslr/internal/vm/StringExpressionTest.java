/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StringExpressionTest {

  private final StringExpression expression = new StringExpression("foo");
  private final Machine machine = mock(Machine.class);

  @Test
  void shouldCompile() {
    assertThat(expression.compile(new CompilationHandler())).containsOnly(expression);
    assertThat(expression).hasToString("String foo");
  }

  @Test
  void shouldMatch() {
    when(machine.length()).thenReturn(3);
    when(machine.charAt(0)).thenReturn('f');
    when(machine.charAt(1)).thenReturn('o');
    when(machine.charAt(2)).thenReturn('o');
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).length();
    inOrder.verify(machine).charAt(0);
    inOrder.verify(machine).charAt(1);
    inOrder.verify(machine).charAt(2);
    inOrder.verify(machine).createLeafNode(expression, 3);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void shouldBacktrack() {
    when(machine.length()).thenReturn(0);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).length();
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void shouldBacktrack2() {
    when(machine.length()).thenReturn(3);
    when(machine.charAt(0)).thenReturn('b');
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).length();
    inOrder.verify(machine).charAt(0);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

}
