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
package com.sonar.cxx.sslr.impl.typed;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.vm.CompilationHandler;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;

class DelayedRuleInvocationExpressionTest {

  @Test
  void should_compile_rule_keys() {
    var b = spy(LexerlessGrammarBuilder.create());
    var ruleKey = mock(GrammarRuleKey.class);

    var expression = new DelayedRuleInvocationExpression(b, ruleKey);

    var compiler = mock(CompilationHandler.class);
    expression.compile(compiler);

    verify(b).rule(ruleKey);

    var ruleExpression = ArgumentCaptor.forClass(ParsingExpression.class);
    verify(compiler).compile(ruleExpression.capture());
    assertThat(ruleExpression.getAllValues()).hasSize(1);
    assertThat(((MutableParsingRule) ruleExpression.getValue()).getRuleKey()).isSameAs(ruleKey);
  }

  @Test
  void should_compile_methods() throws Exception {
    var b = spy(LexerlessGrammarBuilder.create());
    var ruleKey = mock(GrammarRuleKey.class);
    var method = DelayedRuleInvocationExpressionTest.class.getDeclaredMethod("FOO");
    var grammarBuilderInterceptor = mock(GrammarBuilderInterceptor.class);
    when(grammarBuilderInterceptor.ruleKeyForMethod(method)).thenReturn(ruleKey);

    var expression = new DelayedRuleInvocationExpression(b, grammarBuilderInterceptor,
      method);

    var compiler = mock(CompilationHandler.class);
    expression.compile(compiler);

    verify(b).rule(ruleKey);

    var ruleExpression = ArgumentCaptor.forClass(ParsingExpression.class);
    verify(compiler).compile(ruleExpression.capture());
    assertThat(ruleExpression.getAllValues()).hasSize(1);
    assertThat(((MutableParsingRule) ruleExpression.getValue()).getRuleKey()).isSameAs(ruleKey);
  }

  @Test
  void should_fail_when_method_is_not_mapped() throws Exception {
    var method = DelayedRuleInvocationExpressionTest.class.getDeclaredMethod("FOO");

    var thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      new DelayedRuleInvocationExpression(LexerlessGrammarBuilder.create(), mock(GrammarBuilderInterceptor.class), method).compile(mock(CompilationHandler.class));
    });
    assertThat(thrown).hasMessage("Cannot find the rule key corresponding to the invoked method: FOO()");
  }

  @Test
  void test_toString() throws Exception {
    var ruleKey = mock(GrammarRuleKey.class);
    when(ruleKey.toString()).thenReturn("foo");
    assertThat(new DelayedRuleInvocationExpression(mock(LexerlessGrammarBuilder.class), ruleKey)).hasToString(
      "foo");

    var method = DelayedRuleInvocationExpressionTest.class.getDeclaredMethod("FOO");
    assertThat(new DelayedRuleInvocationExpression(mock(LexerlessGrammarBuilder.class), mock(
      GrammarBuilderInterceptor.class), method)).hasToString(
      "FOO()");
  }

  // Called by reflection
  public void FOO() {
  }

}
