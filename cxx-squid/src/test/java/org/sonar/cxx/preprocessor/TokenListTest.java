/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;

class TokenListTest {

  private Lexer lexer;

  @BeforeEach
  void setUp() {
    lexer = PPLexer.create();
  }

  @Test
  void testAdjustPosition() {
    List<Token> tokens = lexer.lex("A");
    Token newPos = TokenUtils.tokenBuilder(IDENTIFIER, "NewPos", 10, 20);
    List<Token> result = TokenList.adjustPosition(tokens, newPos);
    assertThat(result)
      .hasSize(2)
      .matches(t -> "A".equals(t.get(0).getValue()))
      .matches(t -> 10 == t.get(0).getLine())
      .matches(t -> 20 == t.get(0).getColumn());
  }

  @Test
  void testTransformToCxx() {
    List<Token> ppTokens = lexer.lex("break ;"); // CxxKeyword CxxPunctuator
    Token newPos = TokenUtils.tokenBuilder(IDENTIFIER, "NewPos", 10, 20);
    List<Token> result = TokenList.transformToCxx(ppTokens, newPos);
    assertThat(result)
      .hasSize(2) // no WS and EOF
      .matches(t -> CxxKeyword.BREAK.equals(t.get(0).getType()))
      .matches(t -> CxxPunctuator.SEMICOLON.equals(t.get(1).getType()));
  }

}
