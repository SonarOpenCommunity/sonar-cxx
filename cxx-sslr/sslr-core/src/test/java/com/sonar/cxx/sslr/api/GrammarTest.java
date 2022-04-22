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
package com.sonar.cxx.sslr.api;

import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.parser.LexerlessGrammar;

class GrammarTest {

  @Test
  void testGetRuleFields() {
    var ruleFields = Grammar.getRuleFields(MyGrammar.class);
    assertThat(ruleFields).hasSize(1);
  }

  @Test
  void testGetAllRuleFields() {
    var ruleFields = Grammar.getAllRuleFields(MyGrammar.class);
    assertThat(ruleFields).hasSize(5);
  }

  @Test
  void method_rule_should_throw_exception_by_default() {
    var thrown = catchThrowableOfType(
      () -> new MyGrammar().rule(mock(GrammarRuleKey.class)),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void should_automatically_instanciate_lexerful_rules() throws IllegalAccessException {
    var ruleFields = Grammar.getAllRuleFields(MyGrammar.class);
    Grammar grammar = new MyGrammar();
    for (var ruleField : ruleFields) {
      ruleField.setAccessible(true);
      assertThat(ruleField.get(grammar)).as("Current rule name = " + ruleField.getName()).isNotNull().isInstanceOf(
        RuleDefinition.class);
    }
  }

  @Test
  void should_automatically_instanciate_lexerless_rules() throws IllegalAccessException {
    var ruleFields = Grammar.getAllRuleFields(MyLexerlessGrammar.class);
    LexerlessGrammar grammar = new MyLexerlessGrammar();
    for (var ruleField : ruleFields) {
      ruleField.setAccessible(true);
      assertThat(ruleField.get(grammar)).as("Current rule name = " + ruleField.getName()).isNotNull().isInstanceOf(
        MutableParsingRule.class);
    }
  }

  @Test
  void should_throw_exception() {
    var thrown = catchThrowableOfType(IllegalGrammar::new, GrammarException.class);
    assertThat(thrown).hasMessageStartingWith("Unable to instanciate the rule 'rootRule': ");
  }

  public static abstract class MyBaseGrammar extends Grammar {

    Rule basePackageRule;
    public Rule basePublicRule;
    @SuppressWarnings("unused")
    private Rule basePrivateRule;
    protected Rule baseProtectedRule;
  }

  public static class MyGrammar extends MyBaseGrammar {

    @SuppressWarnings("unused")
    private int junkIntField;
    public Object junkObjectField;
    public Rule rootRule;

    @Override
    public Rule getRootRule() {
      return rootRule;
    }
  }

  private static class MyLexerlessGrammar extends LexerlessGrammar {

    public Rule rootRule;

    @Override
    public Rule getRootRule() {
      return rootRule;
    }
  }

  private static class IllegalGrammar extends Grammar {

    private static final Rule rootRule = mock(Rule.class);

    @Override
    public Rule getRootRule() {
      return rootRule;
    }
  }

}
