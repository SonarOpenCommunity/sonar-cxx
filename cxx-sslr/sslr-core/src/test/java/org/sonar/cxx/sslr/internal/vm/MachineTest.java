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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.internal.matchers.Matcher;

class MachineTest {

  @Test
  void subSequenceNotSupported() {
    var machine = new Machine("", new Instruction[0]);
    var thrown = catchThrowableOfType(UnsupportedOperationException.class,
      () -> machine.subSequence(0, 0)
    );
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void testInitialState() {
    var machine = new Machine("", new Instruction[2]);
    assertThat(machine.getAddress()).isZero();
    assertThat(machine.getIndex()).isZero();
    assertThat(machine.peek().isEmpty()).isTrue();
  }

  @Test
  void shouldJump() {
    var machine = new Machine("", new Instruction[2]);
    assertThat(machine.getAddress()).isZero();
    machine.jump(42);
    assertThat(machine.getAddress()).isEqualTo(42);
    machine.jump(13);
    assertThat(machine.getAddress()).isEqualTo(42 + 13);
  }

  @Test
  void shouldAdvanceIndex() {
    var machine = new Machine("foo bar", new Instruction[2]);
    assertThat(machine.getIndex()).isZero();
    assertThat(machine).hasSize(7);
    assertThat(machine.charAt(0)).isEqualTo('f');
    assertThat(machine.charAt(1)).isEqualTo('o');
    machine.advanceIndex(3);
    assertThat(machine.getIndex()).isEqualTo(3);
    assertThat(machine).hasSize(4);
    assertThat(machine.charAt(0)).isEqualTo(' ');
    assertThat(machine.charAt(1)).isEqualTo('b');
    machine.advanceIndex(1);
    assertThat(machine.getIndex()).isEqualTo(4);
    assertThat(machine).hasSize(3);
    assertThat(machine.charAt(0)).isEqualTo('b');
    assertThat(machine.charAt(1)).isEqualTo('a');
  }

  @Test
  void shouldPushReturn() {
    var machine = new Machine("foo", new Instruction[3]);
    var matcher = mock(Matcher.class);
    machine.advanceIndex(1);
    machine.jump(1);
    var previousStack = machine.peek();
    machine.pushReturn(2, matcher, 1);
    assertThat(machine.getAddress()).as("new address").isEqualTo(2);
    assertThat(machine.peek()).isNotSameAs(previousStack);
    assertThat(machine.peek().parent()).isSameAs(previousStack);
    assertThat(machine.peek().index()).as("current index").isEqualTo(1);
    assertThat(machine.peek().address()).as("return address").isEqualTo(1 + 2);
    assertThat(machine.peek().matcher()).isSameAs(matcher);
  }

  @Test
  void shouldDetectLeftRecursion() {
    var machine = new Machine("foo", new Instruction[2]);
    var matcher = mock(Matcher.class);

    machine.advanceIndex(1);
    machine.pushReturn(0, matcher, 1);
    assertThat(machine.peek().calledAddress()).isEqualTo(1);
    assertThat(machine.peek().leftRecursion()).isEqualTo(-1);

    // same rule, but another index of input sequence
    machine.advanceIndex(1);
    machine.pushReturn(0, matcher, 0);
    assertThat(machine.peek().calledAddress()).isEqualTo(1);
    assertThat(machine.peek().leftRecursion()).isEqualTo(1);

    // same rule and index of input sequence
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> machine.pushReturn(0, matcher, 0)
    );
    assertThat(thrown).hasMessage("Left recursion has been detected, involved rule: " + matcher.toString());
  }

  @Test
  void shouldPushBacktrack() {
    var machine = new Machine("foo", new Instruction[2]);
    machine.advanceIndex(1);
    machine.jump(42);
    var previousStack = machine.peek();
    machine.pushBacktrack(13);
    assertThat(machine.peek()).isNotSameAs(previousStack);
    assertThat(machine.peek().parent()).isSameAs(previousStack);
    assertThat(machine.peek().index()).as("current index").isEqualTo(1);
    assertThat(machine.peek().address()).as("backtrack address").isEqualTo(42 + 13);
    assertThat(machine.peek().matcher()).isNull();
  }

  @Test
  void shouldPop() {
    var machine = new Machine("", new Instruction[2]);
    var previousStack = machine.peek();
    machine.pushBacktrack(13);
    assertThat(machine.peek()).isNotSameAs(previousStack);
    machine.pop();
    assertThat(machine.peek()).isSameAs(previousStack);
  }

  @Test
  void shouldFail() {
    var machine = new Machine("", new Instruction[3]);
    var matcher = mock(Matcher.class);
    machine.pushReturn(13, matcher, 0);
    machine.pushReturn(13, matcher, 1);
    machine.backtrack();
    assertThat(machine.getAddress()).isEqualTo(-1);
    // TODO matched=false
  }

  @Test
  void shouldBacktrack() {
    var machine = new Machine("", new Instruction[4]);
    var matcher = mock(Matcher.class);
    var previousStack = machine.peek();
    machine.pushBacktrack(42);
    machine.pushReturn(13, matcher, 0);
    machine.pushReturn(13, matcher, 1);
    machine.backtrack();
    assertThat(machine.peek()).isSameAs(previousStack);
    assertThat(machine.getAddress()).isEqualTo(42);
  }

  @Test
  void shouldCreateLeafNode() {
    var machine = new Machine("", new Instruction[2]);
    var matcher = mock(Matcher.class);
    machine.advanceIndex(42);
    machine.createLeafNode(matcher, 13);
    var node = machine.peek().subNodes().get(0);
    assertThat(node.getMatcher()).isSameAs(matcher);
    assertThat(node.getStartIndex()).isEqualTo(42);
    assertThat(node.getEndIndex()).isEqualTo(42 + 13);
    assertThat(node.getChildren()).isEmpty();
  }

  @Test
  void shouldCreateNode() {
    var machine = new Machine(" ", new Instruction[2]);
    var matcher = mock(Matcher.class);
    machine.advanceIndex(1);
    // remember startIndex and matcher
    machine.pushReturn(0, matcher, 0);
    var subMatcher = mock(Matcher.class);
    machine.createLeafNode(subMatcher, 2);
    machine.createLeafNode(subMatcher, 3);
    machine.createNode();
    var node = machine.peek().parent().subNodes().get(0);
    assertThat(node.getMatcher()).isSameAs(matcher);
    assertThat(node.getStartIndex()).isEqualTo(1);
    assertThat(node.getEndIndex()).isEqualTo(1 + 2 + 3);
    assertThat(node.getChildren()).hasSize(2);
  }

  @Test
  void shouldUseMemo() {
    var machine = new Machine("foo", new Instruction[3]);
    var matcher = mock(MemoParsingExpression.class);
    when(matcher.shouldMemoize()).thenReturn(true);
    machine.pushBacktrack(0);
    machine.pushReturn(1, matcher, 2);
    machine.advanceIndex(3);
    machine.createNode();
    var memo = machine.peek().parent().subNodes().get(0);
    machine.backtrack();
    machine.pushReturn(2, matcher, 1);
    assertThat(machine.getAddress()).isEqualTo(2);
    assertThat(machine.getIndex()).isEqualTo(3);
    assertThat(machine.peek().subNodes()).containsOnly(memo);
  }

  @Test
  void shouldNotMemorize() {
    var machine = new Machine("foo", new Instruction[3]);
    var matcher = mock(MemoParsingExpression.class);
    when(matcher.shouldMemoize()).thenReturn(false);
    machine.pushBacktrack(0);
    machine.pushReturn(1, matcher, 2);
    machine.advanceIndex(3);
    machine.createNode();
    machine.backtrack();
    machine.pushReturn(2, matcher, 1);
    assertThat(machine.getAddress()).isEqualTo(1);
    assertThat(machine.getIndex()).isZero();
    assertThat(machine.peek().subNodes()).isEmpty();
  }

  @Test
  void shouldNotUseMemo() {
    var machine = new Machine("foo", new Instruction[3]);
    var matcher = mock(MemoParsingExpression.class);
    when(matcher.shouldMemoize()).thenReturn(true);
    machine.pushBacktrack(0);
    machine.pushReturn(2, matcher, 1);
    machine.advanceIndex(3);
    machine.createNode();
    machine.backtrack();
    var anotherMatcher = mock(Matcher.class);
    machine.pushReturn(2, anotherMatcher, 1);
    assertThat(machine.getAddress()).isEqualTo(1);
    assertThat(machine.getIndex()).isZero();
    assertThat(machine.peek().subNodes()).isEmpty();
  }

}
