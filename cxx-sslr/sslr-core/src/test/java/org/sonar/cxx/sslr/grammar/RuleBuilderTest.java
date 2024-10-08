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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarBuilder.RuleBuilder;
import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;

class RuleBuilderTest {

  private final GrammarBuilder b = mock(GrammarBuilder.class);
  private final CompilableGrammarRule delegate = mock(CompilableGrammarRule.class);
  private final RuleBuilder ruleBuilder = new RuleBuilder(b, delegate);

  @Test
  void test_is() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    when(b.convertToExpression(e1)).thenReturn(e2);
    ruleBuilder.is(e1);
    verify(delegate).setExpression(e2);
  }

  @Test
  void test_is2() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    var e3 = mock(ParsingExpression.class);
    when(b.convertToExpression(Mockito.any(SequenceExpression.class))).thenReturn(e3);
    ruleBuilder.is(e1, e2);
    verify(delegate).setExpression(e3);
  }

  @Test
  void should_fail_to_redefine() {
    var e = mock(ParsingExpression.class);
    when(delegate.getExpression()).thenReturn(e);
    var ruleKey = mock(GrammarRuleKey.class);
    when(delegate.getRuleKey()).thenReturn(ruleKey);
    var thrown = catchThrowableOfType(GrammarException.class,
      () -> ruleBuilder.is(e)
    );
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
  }

  @Test
  void test_override() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    var e3 = mock(ParsingExpression.class);
    when(b.convertToExpression(e1)).thenReturn(e1);
    when(b.convertToExpression(e2)).thenReturn(e3);
    ruleBuilder.is(e1);
    ruleBuilder.override(e2);
    var inOrder = Mockito.inOrder(delegate);
    inOrder.verify(delegate).setExpression(e1);
    inOrder.verify(delegate).setExpression(e3);
  }

  @Test
  void test_override2() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    var e3 = mock(ParsingExpression.class);
    when(b.convertToExpression(e1)).thenReturn(e1);
    ruleBuilder.is(e1);
    verify(delegate).setExpression(e1);
    when(b.convertToExpression(Mockito.any(SequenceExpression.class))).thenReturn(e3);
    ruleBuilder.override(e1, e2);
    verify(delegate).setExpression(e3);
  }

  @Test
  void test_skip() {
    ruleBuilder.skip();
    verify(delegate).skip();
  }

  @Test
  void test_skipIfOneChild() {
    ruleBuilder.skipIfOneChild();
    verify(delegate).skipIfOneChild();
  }

}
