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
package org.sonar.cxx.sslr.grammar;

import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import java.util.regex.PatternSyntaxException;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.internal.grammar.MutableGrammar;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.cxx.sslr.internal.vm.EndOfInputExpression;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
import org.sonar.cxx.sslr.internal.vm.NothingExpression;
import org.sonar.cxx.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.OptionalExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.PatternExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.StringExpression;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;
import org.sonar.cxx.sslr.internal.vm.TriviaExpression;
import org.sonar.cxx.sslr.internal.vm.ZeroOrMoreExpression;

class LexerlessGrammarBuilderTest {

  @Test
  void shouldCreateExpressions() {
    var b = LexerlessGrammarBuilder.create();
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    var e3 = mock(ParsingExpression.class);

    assertThat(b.convertToExpression(e1)).isSameAs(e1);
    assertThat(b.convertToExpression("")).isInstanceOf(StringExpression.class);
    assertThat(b.convertToExpression('c')).isInstanceOf(StringExpression.class);

    var ruleKey = mock(GrammarRuleKey.class);
    assertThat(b.convertToExpression(ruleKey)).isInstanceOf(MutableParsingRule.class);
    assertThat(b.convertToExpression(ruleKey)).isSameAs(b.convertToExpression(ruleKey));

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

    assertThat(b.regexp("")).isInstanceOf(PatternExpression.class);

    assertThat(b.endOfInput()).as("singleton").isSameAs(EndOfInputExpression.INSTANCE);
  }

  @Test
  void testToken() {
    var tokenType = mock(TokenType.class);
    var e = mock(ParsingExpression.class);
    var result = LexerlessGrammarBuilder.create().token(tokenType, e);
    assertThat(result).isInstanceOf(TokenExpression.class);
    assertThat(((TokenExpression) result).getTokenType()).isSameAs(tokenType);
  }

  @Test
  void testCommentTrivia() {
    var e = mock(ParsingExpression.class);
    var result = LexerlessGrammarBuilder.create().commentTrivia(e);
    assertThat(result).isInstanceOf(TriviaExpression.class);
    assertThat(((TriviaExpression) result).getTriviaKind()).isEqualTo(TriviaKind.COMMENT);
  }

  @Test
  void testSkippedTrivia() {
    var e = mock(ParsingExpression.class);
    var result = LexerlessGrammarBuilder.create().skippedTrivia(e);
    assertThat(result).isInstanceOf(TriviaExpression.class);
    assertThat(((TriviaExpression) result).getTriviaKind()).isEqualTo(TriviaKind.SKIPPED_TEXT);
  }

  @Test
  void shouldSetRootRule() {
    var b = LexerlessGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is(b.nothing());
    b.setRootRule(ruleKey);
    var grammar = (MutableGrammar) b.build();
    assertThat(((CompilableGrammarRule) grammar.getRootRule()).getRuleKey()).isSameAs(ruleKey);
  }

  @Test
  void testUndefinedRootRule() {
    var b = LexerlessGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.setRootRule(ruleKey);
    var thrown = catchThrowableOfType(GrammarException.class, b::build);
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' hasn't been defined.");
  }

  @Test
  void testUndefinedRule() {
    var b = LexerlessGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey);
    var thrown = catchThrowableOfType(GrammarException.class, b::build);
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' hasn't been defined.");
  }

  @Test
  void testUsedUndefinedRule() {
    var b = LexerlessGrammarBuilder.create();
    var ruleKey1 = mock(GrammarRuleKey.class);
    var ruleKey2 = mock(GrammarRuleKey.class);
    b.rule(ruleKey1).is(ruleKey2);
    var thrown = catchThrowableOfType(GrammarException.class, b::build);
    assertThat(thrown).hasMessage("The rule '" + ruleKey2 + "' hasn't been defined.");
  }

  @Test
  void testWrongRegexp() {
    var b = LexerlessGrammarBuilder.create();
    var thrown = catchThrowableOfType(PatternSyntaxException.class, () -> b.regexp("["));
    assertThat(thrown).isExactlyInstanceOf(PatternSyntaxException.class);
  }

  @Test
  void testIncorrectTypeOfParsingExpression() {
    var thrown = catchThrowableOfType(IllegalArgumentException.class,
      () -> LexerlessGrammarBuilder.create().convertToExpression(new Object())
    );
    assertThat(thrown).hasMessage("Incorrect type of parsing expression: class java.lang.Object");
  }

  @Test
  void testNullParsingExpression() {
    var thrown = catchThrowableOfType(NullPointerException.class,
      () -> LexerlessGrammarBuilder.create().convertToExpression(null)
    );
    assertThat(thrown).hasMessage("Parsing expression can't be null");
  }

  @Test
  void shouldFailToRedefine() {
    var b = LexerlessGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is("foo");
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> b.rule(ruleKey).is("foo")
    );
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
  }

  @Test
  void shouldFailToRedefine2() {
    var b = LexerlessGrammarBuilder.create();
    var ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey).is("foo", "bar");
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> b.rule(ruleKey).is("foo", "bar")
    );
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
  }

}
