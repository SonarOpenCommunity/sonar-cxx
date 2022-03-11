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
package org.sonar.cxx.sslr.internal.grammar;

import com.sonar.cxx.sslr.api.AstNode;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;

public class MutableParsingRuleTest {

  @Test
  public void should_not_allow_redefinition() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = new MutableParsingRule(ruleKey);
    rule.is(mock(ParsingExpression.class));
    var thrown = catchThrowableOfType(
      () -> rule.is(mock(ParsingExpression.class)),
      GrammarException.class);
    assertThat(thrown).hasMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
  }

  @Test
  public void should_override() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = new MutableParsingRule(ruleKey);
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);
    rule.is(e1, e2);
    rule.override(e2);
    assertThat(rule.getExpression()).isSameAs(e2);
    rule.override(e1, e2);
    assertThat(rule.getExpression()).isInstanceOf(SequenceExpression.class);
  }

  @Test
  public void should_not_skip_from_AST() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = new MutableParsingRule(ruleKey);
    var astNode = mock(AstNode.class);
    assertThat(rule.hasToBeSkippedFromAst(astNode)).isFalse();
  }

  @Test
  public void should_skip_from_AST() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = new MutableParsingRule(ruleKey);
    rule.skip();
    var astNode = mock(AstNode.class);
    assertThat(rule.hasToBeSkippedFromAst(astNode)).isTrue();
  }

  @Test
  public void should_skip_from_AST_if_one_child() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = new MutableParsingRule(ruleKey);
    rule.skipIfOneChild();
    var astNode = mock(AstNode.class);
    when(astNode.getNumberOfChildren()).thenReturn(1);
    assertThat(rule.hasToBeSkippedFromAst(astNode)).isTrue();
  }

  @Test
  public void should_return_real_AstNodeType() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = new MutableParsingRule(ruleKey);
    assertThat(rule.getRealAstNodeType()).isSameAs(ruleKey);
  }

}
