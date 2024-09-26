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
package org.sonar.cxx.sslr.internal.grammar;

import java.util.HashMap;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;

class MutableGrammarTest {

  @Test
  void test() {
    var ruleKey = mock(GrammarRuleKey.class);
    var rule = mock(CompilableGrammarRule.class);
    var rootRuleKey = mock(GrammarRuleKey.class);
    var rootRule = mock(CompilableGrammarRule.class);
    HashMap<GrammarRuleKey, CompilableGrammarRule> rules = new HashMap<>();
    rules.put(ruleKey, rule);
    rules.put(rootRuleKey, rootRule);
    var grammar = new MutableGrammar(rules, rootRuleKey);
    assertThat(grammar.rule(ruleKey)).isSameAs(rule);
    assertThat(grammar.getRootRule()).isSameAs(rootRule);
  }

}
