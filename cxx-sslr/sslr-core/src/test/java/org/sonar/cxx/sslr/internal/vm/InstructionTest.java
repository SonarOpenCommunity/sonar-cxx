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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.vm.Instruction.BackCommitInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.BacktrackInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.CallInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.ChoiceInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.CommitInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.CommitVerifyInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.EndInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.FailTwiceInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.IgnoreErrorsInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.JumpInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.PredicateChoiceInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.RetInstruction;

class InstructionTest {

  private final Machine machine = mock(Machine.class);

  @Test
  void jump() {
    var instruction = Instruction.jump(42);
    assertThat(instruction)
      .isInstanceOf(JumpInstruction.class)
      .hasToString("Jump 42")
      .isEqualTo(Instruction.jump(42))
      .isNotEqualTo(Instruction.jump(13))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void call() {
    var matcher = mock(Matcher.class);
    var instruction = Instruction.call(42, matcher);
    assertThat(instruction)
      .isInstanceOf(CallInstruction.class)
      .hasToString("Call 42")
      .isEqualTo(Instruction.call(42, matcher))
      .isNotEqualTo(Instruction.call(42, mock(Matcher.class)))
      .isNotEqualTo(Instruction.call(13, matcher))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).pushReturn(1, matcher, 42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void choice() {
    var instruction = Instruction.choice(42);
    assertThat(instruction)
      .isInstanceOf(ChoiceInstruction.class)
      .hasToString("Choice 42")
      .isEqualTo(Instruction.choice(42))
      .isNotEqualTo(Instruction.choice(13))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).pushBacktrack(42);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void predicateChoice() {
    var instruction = Instruction.predicateChoice(42);
    assertThat(instruction)
      .isInstanceOf(PredicateChoiceInstruction.class)
      .hasToString("PredicateChoice 42")
      .isEqualTo(Instruction.predicateChoice(42))
      .isNotEqualTo(Instruction.predicateChoice(13))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).pushBacktrack(42);
    inOrder.verify(machine).setIgnoreErrors(true);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void commit() {
    var instruction = Instruction.commit(42);
    assertThat(instruction)
      .isInstanceOf(CommitInstruction.class)
      .hasToString("Commit " + 42)
      .isEqualTo(Instruction.commit(42))
      .isNotEqualTo(Instruction.commit(13))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    var stack = new MachineStack().getOrCreateChild();
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine, times(2)).peek();
    inOrder.verify(machine).pop();
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void commitVerify() {
    var instruction = Instruction.commitVerify(42);
    assertThat(instruction)
      .isInstanceOf(CommitVerifyInstruction.class)
      .hasToString("CommitVerify " + 42)
      .isEqualTo(Instruction.commitVerify(42))
      .isNotEqualTo(Instruction.commitVerify(13))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    var stack = new MachineStack().getOrCreateChild();
    when(machine.peek()).thenReturn(stack);
    when(machine.getIndex()).thenReturn(13);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine, times(3)).peek();
    inOrder.verify(machine).pop();
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void commitVerify_should_throw_exception() {
    var instruction = Instruction.commitVerify(42);
    var stack = new MachineStack().getOrCreateChild();
    stack.setIndex(13);
    when(machine.peek()).thenReturn(stack);
    when(machine.getIndex()).thenReturn(13);
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> instruction.execute(machine)
    );
    assertThat(thrown).hasMessage("The inner part of ZeroOrMore and OneOrMore must not allow empty matches");
  }

  @Test
  void ret() {
    var instruction = Instruction.ret();
    assertThat(instruction)
      .isInstanceOf(RetInstruction.class)
      .hasToString("Ret")
      .as("singleton").isSameAs(Instruction.ret());

    var stack = mock(MachineStack.class);
    when(stack.address()).thenReturn(42);
    when(stack.isIgnoreErrors()).thenReturn(true);
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).createNode();
    inOrder.verify(machine).peek();
    inOrder.verify(machine).setIgnoreErrors(true);
    inOrder.verify(machine).setAddress(42);
    inOrder.verify(machine).popReturn();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void backtrack() {
    var instruction = Instruction.backtrack();
    assertThat(instruction)
      .isInstanceOf(BacktrackInstruction.class)
      .hasToString("Backtrack")
      .as("singleton").isSameAs(Instruction.backtrack());

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void end() {
    var instruction = Instruction.end();
    assertThat(instruction)
      .isInstanceOf(EndInstruction.class)
      .hasToString("End")
      .as("singleton").isSameAs(Instruction.end());

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).setAddress(-1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void failTwice() {
    var instruction = Instruction.failTwice();
    assertThat(instruction)
      .isInstanceOf(FailTwiceInstruction.class)
      .hasToString("FailTwice")
      .as("singleton").isSameAs(Instruction.failTwice());

    var stack = mock(MachineStack.class);
    when(stack.index()).thenReturn(13);
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).peek();
    inOrder.verify(machine).setIndex(13);
    inOrder.verify(machine).pop();
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  void backCommit() {
    var instruction = Instruction.backCommit(42);
    assertThat(instruction)
      .isInstanceOf(BackCommitInstruction.class)
      .hasToString("BackCommit 42")
      .isEqualTo(Instruction.backCommit(42))
      .isNotEqualTo(Instruction.backCommit(13))
      .isNotEqualTo(new Object());
    assertThat(instruction.hashCode()).isEqualTo(42);

    var stack = mock(MachineStack.class);
    when(stack.index()).thenReturn(13);
    when(stack.isIgnoreErrors()).thenReturn(true);
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).peek();
    inOrder.verify(machine).setIndex(13);
    inOrder.verify(machine).setIgnoreErrors(true);
    inOrder.verify(machine).pop();
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  void ignoreErrors() {
    var instruction = Instruction.ignoreErrors();
    assertThat(instruction)
      .isInstanceOf(IgnoreErrorsInstruction.class)
      .hasToString("IgnoreErrors")
      .as("singleton").isSameAs(Instruction.ignoreErrors());

    instruction.execute(machine);
    verify(machine).setIgnoreErrors(true);
    verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

}
