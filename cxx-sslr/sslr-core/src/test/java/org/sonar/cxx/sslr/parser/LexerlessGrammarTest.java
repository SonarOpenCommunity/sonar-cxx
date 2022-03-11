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
package org.sonar.cxx.sslr.parser;

import com.sonar.cxx.sslr.api.Rule;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;

public class LexerlessGrammarTest {

  @Test
  public void should_instanciate_rule_fields() {
    var grammar = new TestGrammar();
    assertThat(grammar.getRootRule()).isInstanceOf(MutableParsingRule.class);
    assertThat(((MutableParsingRule) grammar.getRootRule()).getName()).isEqualTo("rootRule");
  }

  @Test
  public void should_throw_exception() {
    var thrown = catchThrowableOfType(IllegalGrammar::new, GrammarException.class);
    assertThat(thrown).hasMessageStartingWith("Unable to instanciate the rule 'rootRule': ");
  }

  private static class TestGrammar extends LexerlessGrammar {

    private Rule rootRule;

    @Override
    public Rule getRootRule() {
      return rootRule;
    }
  }

  private static class IllegalGrammar extends LexerlessGrammar {

    private static final Rule rootRule = mock(Rule.class);

    @Override
    public Rule getRootRule() {
      return rootRule;
    }
  }

}
