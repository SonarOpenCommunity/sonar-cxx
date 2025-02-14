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
package org.sonar.cxx.sslr.internal.vm.lexerful;

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.internal.vm.CompilationHandler;
import org.sonar.cxx.sslr.internal.vm.Machine;

class TokensBridgeExpressionTest {

  private final TokenType fromType = mock(TokenType.class);
  private final TokenType toType = mock(TokenType.class);
  private final TokenType anotherType = mock(TokenType.class);
  private final TokensBridgeExpression expression = new TokensBridgeExpression(fromType, toType);
  private final Machine machine = mock(Machine.class);

  @Test
  void shouldCompile() {
    assertThat(expression.compile(new CompilationHandler())).containsOnly(expression);
    assertThat(expression).hasToString("Bridge[" + fromType + "," + toType + "]");
  }

  @Test
  void shouldMatch() {
    when(machine.length()).thenReturn(5);
    var token1 = token(fromType);
    var token2 = token(fromType);
    var token3 = token(anotherType);
    var token4 = token(toType);
    var token5 = token(toType);
    when(machine.tokenAt(0)).thenReturn(token1);
    when(machine.tokenAt(1)).thenReturn(token2);
    when(machine.tokenAt(2)).thenReturn(token3);
    when(machine.tokenAt(3)).thenReturn(token4);
    when(machine.tokenAt(4)).thenReturn(token5);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).length();
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).tokenAt(1);
    inOrder.verify(machine).tokenAt(2);
    inOrder.verify(machine).tokenAt(3);
    inOrder.verify(machine).tokenAt(4);
    // Number of created nodes must be equal to the number of consumed tokens (5):
    inOrder.verify(machine, times(5)).createLeafNode(expression, 1);
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
    when(machine.length()).thenReturn(2);
    var token1 = token(anotherType);
    when(machine.tokenAt(0)).thenReturn(token1);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).length();
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void shouldBacktrack3() {
    when(machine.length()).thenReturn(2);
    var token1 = token(fromType);
    var token2 = token(fromType);
    when(machine.tokenAt(0)).thenReturn(token1);
    when(machine.tokenAt(1)).thenReturn(token2);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).length();
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).tokenAt(1);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  private static Token token(TokenType tokenType) {
    var token = mock(Token.class);
    when(token.getType()).thenReturn(tokenType);
    return token;
  }

}
