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
package com.sonar.cxx.sslr.impl.channel;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import static com.sonar.cxx.sslr.test.lexer.LexerConditions.*;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.channel.CodeReader;
import static org.sonar.cxx.sslr.test.channel.ChannelMatchers.consume;

public class PunctuatorChannelTest {

  private final PunctuatorChannel channel = new PunctuatorChannel(MyPunctuatorAndOperator.values());
  private final Lexer lexer = Lexer.builder().build();

  @Test
  public void testConsumeSpecialCharacters() {
    AssertionsForClassTypes.assertThat(channel).has(consume("**=", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("*", MyPunctuatorAndOperator.STAR));

    AssertionsForClassTypes.assertThat(channel).has(consume(",=", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken(",", MyPunctuatorAndOperator.COLON));

    AssertionsForClassTypes.assertThat(channel).has(consume("=*", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("=", MyPunctuatorAndOperator.EQUAL));

    AssertionsForClassTypes.assertThat(channel).has(consume("==,", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("==", MyPunctuatorAndOperator.EQUAL_OP));

    AssertionsForClassTypes.assertThat(channel).has(consume("*=,", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("*=", MyPunctuatorAndOperator.MUL_ASSIGN));

    assertThat(channel.consume(new CodeReader("!"), lexer)).isFalse();
  }

  @Test
  public void testNotConsumeWord() {
    assertThat(channel.consume(new CodeReader("word"), lexer)).isFalse();
  }

  private enum MyPunctuatorAndOperator implements TokenType {
    STAR("*"), COLON(","), EQUAL("="), EQUAL_OP("=="), MUL_ASSIGN("*="), NOT_EQUAL("!=");

    private final String value;

    private MyPunctuatorAndOperator(String value) {
      this.value = value;
    }

    @Override
    public String getName() {
      return name();
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

  }
}
