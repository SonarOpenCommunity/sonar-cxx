/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.PreprocessorAction;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JoinStringsPreprocessorTest {

  private JoinStringsPreprocessor pp;
  private Lexer lexer;

  @BeforeEach
  void setUp() {
    pp = new JoinStringsPreprocessor();
    lexer = PPLexer.create();
  }

  @Test
  void testProcess() {
    List<Token> tokens = lexer.lex("\"A\"\"B\"");
    PreprocessorAction result = pp.process(tokens);
    assertThat(result.getNumberOfConsumedTokens()).isEqualTo(2);
    assertThat(result.getTokensToInject())
      .hasSize(1)
      .matches(t -> "\"AB\"".equals(t.get(0).getValue()));
    assertThat(result.getTriviaToInject()).hasSize(1);
    assertThat(result.getTriviaToInject().get(0).getTokens())
      .hasSize(2)
      .matches(t -> "\"A\"".equals(t.get(0).getValue()))
      .matches(t -> "\"B\"".equals(t.get(1).getValue()));
  }

}
