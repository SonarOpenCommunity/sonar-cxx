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
package org.sonar.cxx.sslr.internal.vm.lexerful;

import com.sonar.cxx.sslr.api.Token;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.internal.vm.CompilationHandler;
import org.sonar.cxx.sslr.internal.vm.Machine;

class AdjacentExpressionTest {

  private final AdjacentExpression expression = AdjacentExpression.INSTANCE;
  private final Machine machine = mock(Machine.class);

  @Test
  void should_compile() {
    assertThat(expression.compile(new CompilationHandler())).containsOnly(expression);
    assertThat(expression).hasToString("Adjacent");
  }

  @Test
  void should_match() {
    var previousToken = mock(Token.class);
    when(previousToken.getValue()).thenReturn("foo");
    when(previousToken.getLine()).thenReturn(42);
    when(previousToken.getColumn()).thenReturn(13);
    var nextToken = mock(Token.class);
    when(nextToken.getLine()).thenReturn(42);
    when(nextToken.getColumn()).thenReturn(13 + 3);
    when(machine.getIndex()).thenReturn(1);
    when(machine.tokenAt(-1)).thenReturn(previousToken);
    when(machine.tokenAt(0)).thenReturn(nextToken);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine).tokenAt(-1);
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void should_backtrack() {
    var previousToken = mock(Token.class);
    when(previousToken.getValue()).thenReturn("foo");
    when(previousToken.getLine()).thenReturn(42);
    when(previousToken.getColumn()).thenReturn(13);
    var nextToken = mock(Token.class);
    when(nextToken.getLine()).thenReturn(42 + 1);
    when(nextToken.getColumn()).thenReturn(13 + 3);
    when(machine.getIndex()).thenReturn(1);
    when(machine.tokenAt(-1)).thenReturn(previousToken);
    when(machine.tokenAt(0)).thenReturn(nextToken);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine).tokenAt(-1);
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void should_backtrack2() {
    var previousToken = mock(Token.class);
    when(previousToken.getValue()).thenReturn("foo");
    when(previousToken.getLine()).thenReturn(42);
    when(previousToken.getColumn()).thenReturn(13);
    var nextToken = mock(Token.class);
    when(nextToken.getLine()).thenReturn(13);
    when(nextToken.getColumn()).thenReturn(42);
    when(machine.getIndex()).thenReturn(1);
    when(machine.tokenAt(-1)).thenReturn(previousToken);
    when(machine.tokenAt(0)).thenReturn(nextToken);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine).tokenAt(-1);
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void should_backtrack3() {
    when(machine.getIndex()).thenReturn(0);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

}
