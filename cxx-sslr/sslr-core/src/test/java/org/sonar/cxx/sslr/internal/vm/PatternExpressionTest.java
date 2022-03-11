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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarException;

public class PatternExpressionTest {

  private final PatternExpression expression = new PatternExpression("foo|bar");
  private final Machine machine = mock(Machine.class);

  @Test
  public void should_compile() {
    assertThat(expression.compile(new CompilationHandler())).containsOnly(expression);
    assertThat(expression.toString()).isEqualTo("Pattern foo|bar");
  }

  @Test
  public void should_match() {
    when(machine.length()).thenReturn(3);
    when(machine.charAt(0)).thenReturn('f');
    when(machine.charAt(1)).thenReturn('o');
    when(machine.charAt(2)).thenReturn('o');
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine, atLeast(1)).length();
    inOrder.verify(machine, atLeast(1)).charAt(0);
    inOrder.verify(machine, atLeast(1)).charAt(1);
    inOrder.verify(machine, atLeast(1)).charAt(2);
    inOrder.verify(machine).createLeafNode(expression, 3);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);

    // Should reset matcher with empty string:
    try {
      expression.getMatcher().find(1);
      fail("exception expected");
    } catch (IndexOutOfBoundsException e) {
      assertThat(e.getMessage()).isEqualTo("Illegal start index");
    }
  }

  @Test
  public void should_backtrack() {
    when(machine.length()).thenReturn(1);
    when(machine.charAt(0)).thenReturn('z');
    expression.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine, atLeast(1)).length();
    inOrder.verify(machine, atLeast(1)).charAt(0);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);

    // Should reset matcher with empty string:
    try {
      expression.getMatcher().find(1);
      fail("exception expected");
    } catch (IndexOutOfBoundsException e) {
      assertThat(e.getMessage()).isEqualTo("Illegal start index");
    }
  }

  @Test
  public void should_catch_StackOverflowError() {
    when(machine.length()).thenReturn(1);
    when(machine.charAt(0)).thenThrow(StackOverflowError.class);
    var thrown = catchThrowableOfType(
      () -> expression.execute(machine),
      GrammarException.class);
    assertThat(thrown)
      .hasMessage("The regular expression 'foo|bar' has led to a stack overflow error."
                    + " This error is certainly due to an inefficient use of alternations. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050507");
  }

}
