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

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.sonar.cxx.sslr.grammar.GrammarException;

// TODO this test should also check state of machine after execution
class MachineIntegrationTest {

  @Test
  @Timeout(5000)
  public void pattern() {
    var instructions = new PatternExpression("foo|bar").compile(new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isTrue();
    assertThat(Machine.execute("bar", instructions)).isTrue();
    assertThat(Machine.execute("baz", instructions)).isFalse();
  }

  @Test
  @Timeout(5000)
  public void string() {
    var instructions = new StringExpression("foo").compile(new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isTrue();
    assertThat(Machine.execute("bar", instructions)).isFalse();
  }

  @Test
  @Timeout(5000)
  public void sequence() {
    var instructions = new SequenceExpression(
      new StringExpression("foo"), new StringExpression("bar")).compile(new CompilationHandler());
    assertThat(Machine.execute("foobar", instructions)).isTrue();
    assertThat(Machine.execute("baz", instructions)).isFalse();
  }

  @Test
  @Timeout(5000)
  public void firstOf() {
    var instructions = new FirstOfExpression(
      new StringExpression("foo"),
      new StringExpression("bar"),
      new StringExpression("baz")).compile(new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isTrue();
    assertThat(Machine.execute("bar", instructions)).isTrue();
    assertThat(Machine.execute("baz", instructions)).isTrue();
    assertThat(Machine.execute("qux", instructions)).isFalse();
  }

  @Test
  @Timeout(5000)
  public void optional() {
    var instructions = new OptionalExpression(new StringExpression("a")).compile(new CompilationHandler());
    assertThat(Machine.execute("", instructions)).isTrue();
    assertThat(Machine.execute("a", instructions)).isTrue();
  }

  @Test
  @Timeout(5000)
  public void next() {
    var instructions = new NextExpression(new StringExpression("foo")).compile(new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isTrue();
    assertThat(Machine.execute("bar", instructions)).isFalse();
  }

  @Test
  @Timeout(5000)
  public void nextNot() {
    var instructions = new NextNotExpression(new StringExpression("foo")).compile(new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isFalse();
    assertThat(Machine.execute("bar", instructions)).isTrue();
  }

  @Test
  @Timeout(5000)
  public void zeroOrMore() {
    var instructions = new ZeroOrMoreExpression(new StringExpression("a")).compile(new CompilationHandler());
    assertThat(Machine.execute("", instructions)).isTrue();
    assertThat(Machine.execute("a", instructions)).isTrue();
    assertThat(Machine.execute("aa", instructions)).isTrue();
  }

  @Test
  @Timeout(5000)
  public void zeroOrMore_should_not_cause_infinite_loop() {
    var instructions = new ZeroOrMoreExpression(
      new FirstOfExpression(
        new StringExpression("foo"),
        new StringExpression(""))).compile(new CompilationHandler());
    var thrown = catchThrowableOfType(() -> Machine.execute("foo", instructions), GrammarException.class);
    assertThat(thrown).hasMessage("The inner part of ZeroOrMore and OneOrMore must not allow empty matches");
  }

  @Test
  @Timeout(5000)
  public void oneOrMore() {
    var instructions = new OneOrMoreExpression(new StringExpression("a")).compile(new CompilationHandler());
    assertThat(Machine.execute("", instructions)).isFalse();
    assertThat(Machine.execute("a", instructions)).isTrue();
    assertThat(Machine.execute("aa", instructions)).isTrue();
  }

  @Test
  @Timeout(5000)
  public void oneOrMore_should_not_cause_infinite_loop() {
    var instructions = new OneOrMoreExpression(
      new FirstOfExpression(
        new StringExpression("foo"),
        new StringExpression(""))).compile(new CompilationHandler());
    var thrown = catchThrowableOfType(() -> Machine.execute("foo", instructions), GrammarException.class);
    assertThat(thrown).hasMessage("The inner part of ZeroOrMore and OneOrMore must not allow empty matches");
  }

  @Test
  @Timeout(5000)
  public void token() {
    var instructions = new TokenExpression(GenericTokenType.IDENTIFIER, new StringExpression("foo")).compile(
      new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isTrue();
    assertThat(Machine.execute("bar", instructions)).isFalse();
  }

  @Test
  @Timeout(5000)
  public void trivia() {
    var instructions = new TriviaExpression(TriviaKind.COMMENT, new StringExpression("foo")).compile(
      new CompilationHandler());
    assertThat(Machine.execute("foo", instructions)).isTrue();
    assertThat(Machine.execute("bar", instructions)).isFalse();
  }

}
