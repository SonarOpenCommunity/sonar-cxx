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

import com.sonar.cxx.sslr.api.GenericTokenType;
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

class TillNewLineExpressionTest {

  private final TillNewLineExpression expression = TillNewLineExpression.INSTANCE;
  private final Machine machine = mock(Machine.class);

  @Test
  void should_compile() {
    assertThat(expression.compile(new CompilationHandler())).containsOnly(expression);
    assertThat(expression).hasToString("TillNewLine");
  }

  @Test
  void should_match() {
    var token1 = token(GenericTokenType.IDENTIFIER, 1);
    var token2 = token(GenericTokenType.IDENTIFIER, 1);
    var token3 = token(GenericTokenType.IDENTIFIER, 2);
    when(machine.tokenAt(0)).thenReturn(token1);
    when(machine.tokenAt(1)).thenReturn(token2);
    when(machine.tokenAt(2)).thenReturn(token3);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).tokenAt(1);
    inOrder.verify(machine).tokenAt(2);
    // Number of created nodes must be equal to the number of consumed tokens (2):
    inOrder.verify(machine, times(2)).createLeafNode(expression, 1);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void should_match2() {
    var token0 = token(GenericTokenType.IDENTIFIER, 1);
    var token1 = token(GenericTokenType.IDENTIFIER, 1);
    var token2 = token(GenericTokenType.IDENTIFIER, 1);
    var token3 = token(GenericTokenType.EOF, 1);
    when(machine.getIndex()).thenReturn(1);
    when(machine.tokenAt(-1)).thenReturn(token0);
    when(machine.tokenAt(0)).thenReturn(token1);
    when(machine.tokenAt(1)).thenReturn(token2);
    when(machine.tokenAt(2)).thenReturn(token3);
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine).tokenAt(-1);
    inOrder.verify(machine).tokenAt(0);
    inOrder.verify(machine).tokenAt(1);
    inOrder.verify(machine).tokenAt(2);
    // Number of created nodes must be equal to the number of consumed tokens (2):
    inOrder.verify(machine, times(2)).createLeafNode(expression, 1);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  private static Token token(TokenType tokenType, int line) {
    var token = mock(Token.class);
    when(token.getLine()).thenReturn(line);
    when(token.getType()).thenReturn(tokenType);
    return token;
  }

}
