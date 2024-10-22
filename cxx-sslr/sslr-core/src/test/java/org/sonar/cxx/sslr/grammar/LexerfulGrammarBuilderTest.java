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
package org.sonar.cxx.sslr.grammar;

import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.internal.grammar.MutableGrammar;
import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
import org.sonar.cxx.sslr.internal.vm.NothingExpression;
import org.sonar.cxx.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.OptionalExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.ZeroOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.AnyTokenExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TillNewLineExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeClassExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypesExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenValueExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokensBridgeExpression;

class LexerfulGrammarBuilderTest {

  @Test
  void shouldCreateExpressions() {
    var b = LexerfulGrammarBuilder.create();

    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    var e3 = mock(ParsingExpression.class);

    assertThat(b.convertToExpression(e1)).isSameAs(e1);
    assertThat(b.convertToExpression("")).isInstanceOf(TokenValueExpression.class);
    assertThat(b.convertToExpression(mock(TokenType.class))).isInstanceOf(TokenTypeExpression.class);
    assertThat(b.convertToExpression(Object.class)).isInstanceOf(TokenTypeClassExpression.class);

    assertThat(b.sequence(e1, e2)).isInstanceOf(SequenceExpression.class);
    assertThat(b.sequence(e1, e2, e3)).isInstanceOf(SequenceExpression.class);

    assertThat(b.firstOf(e1, e2)).isInstanceOf(FirstOfExpression.class);
    assertThat(b.firstOf(e1, e2, e3)).isInstanceOf(FirstOfExpression.class);

    assertThat(b.optional(e1)).isInstanceOf(OptionalExpression.class);
    assertThat(b.optional(e1, e2)).isInstanceOf(OptionalExpression.class);

    assertThat(b.oneOrMore(e1)).isInstanceOf(OneOrMoreExpression.class);
    assertThat(b.oneOrMore(e1, e2)).isInstanceOf(OneOrMoreExpression.class);

    assertThat(b.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression.class);
    assertThat(b.zeroOrMore(e1, e2)).isInstanceOf(ZeroOrMoreExpression.class);

    assertThat(b.next(e1)).isInstanceOf(NextExpression.class);
    assertThat(b.next(e1, e2)).isInstanceOf(NextExpression.class);

    assertThat(b.nextNot(e1)).isInstanceOf(NextNotExpression.class);
    assertThat(b.nextNot(e1, e2)).isInstanceOf(NextNotExpression.class);

    assertThat(b.nothing()).as("singleton").isSameAs(NothingExpression.INSTANCE);

    assertThat(b.isOneOfThem(mock(TokenType.class), mock(TokenType.class))).isInstanceOf(TokenTypesExpression.class);
    assertThat(b.bridge(mock(TokenType.class), mock(TokenType.class))).isInstanceOf(TokensBridgeExpression.class);

    assertThat(b.adjacent(e1)).hasToString("Sequence[Adjacent, " + e1 + "]");

    assertThat(b.anyTokenButNot(e1)).hasToString("Sequence[NextNot[" + e1 + "], AnyToken]");

    assertThat(b.till(e1)).hasToString("Sequence[ZeroOrMore[Sequence[NextNot[" + e1 + "], AnyToken]], " + e1
      + "]");

    assertThat(b.exclusiveTill(e1)).hasToString("ZeroOrMore[Sequence[NextNot[" + e1 + "], AnyToken]]");
    assertThat(b.exclusiveTill(e1, e2)).hasToString("ZeroOrMore[Sequence[NextNot[FirstOf[" + e1 + ", " + e2
      + "]], AnyToken]]");

    assertThat(b.anyToken()).as("singleton").isSameAs(AnyTokenExpression.INSTANCE);
    assertThat(b.tillNewLine()).as("singleton").isSameAs(TillNewLineExpression.INSTANCE);
  }

  @Test
  void shouldSetRootRule() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is(b.nothing());
    b.setRootRule(ruleKey);
    var grammar = (MutableGrammar) b.build();
    assertThat(((CompilableGrammarRule) grammar.getRootRule()).getRuleKey()).isSameAs(ruleKey);
  }

  @Test
  void shouldBuildWithMemoization() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is("foo");
    var grammar = b.buildWithMemoizationOfMatchesForAllRules();
    assertThat(((RuleDefinition) grammar.rule(ruleKey)).shouldMemoize()).isTrue();
  }

  @Test
  void testUndefinedRootRule() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.setRootRule(ruleKey);
    var thrown = catchThrowableOfType(GrammarException.class, b::build);
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' hasn't been defined.");
  }

  @Test
  void testUndefinedRule() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey);
    var thrown = catchThrowableOfType(GrammarException.class, b::build);
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' hasn't been defined.");
  }

  @Test
  void testUsedUndefinedRule() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey1 = mock(GrammarRuleKey.class);
    var ruleKey2 = mock(GrammarRuleKey.class);
    b.rule(ruleKey1).is(ruleKey2);
    var thrown = catchThrowableOfType(GrammarException.class, b::build);
    assertThat(thrown).hasMessage("The rule '" + ruleKey2 + "' hasn't been defined.");
  }

  @Test
  void testIncorrectTypeOfParsingExpression() {
    var thrown = catchThrowableOfType(IllegalArgumentException.class,
      () -> LexerfulGrammarBuilder.create().convertToExpression(new Object())
    );
    assertThat(thrown).hasMessage("Incorrect type of parsing expression: class java.lang.Object");
  }

  @Test
  void testNullParsingExpression() {
    var thrown = catchThrowableOfType(NullPointerException.class,
      () -> LexerfulGrammarBuilder.create().convertToExpression(null)
    );
    assertThat(thrown).hasMessage("Parsing expression can't be null");
  }

  @Test
  void shouldFailToRedefine() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is("foo");
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> b.rule(ruleKey).is("foo")
    );
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
  }

  @Test
  void shouldFailToRedefine2() {
    var b = LexerfulGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is("foo", "bar");
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> b.rule(ruleKey).is("foo")
    );
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
  }

}
