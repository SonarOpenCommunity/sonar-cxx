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
package com.sonar.cxx.sslr.impl.channel;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import static com.sonar.cxx.sslr.test.lexer.LexerConditions.*;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.channel.CodeReader;
import static org.sonar.cxx.sslr.test.channel.ChannelMatchers.consume;

class IdentifierAndKeywordChannelTest {

  private IdentifierAndKeywordChannel channel;
  private final Lexer lexer = Lexer.builder().build();

  @Test
  void testConsumeWord() {
    channel = new IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.values());
    AssertionsForClassTypes.assertThat(channel).has(consume("word", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("word", GenericTokenType.IDENTIFIER));
  }

  @Test
  void testConsumeCaseSensitiveKeywords() {
    channel = new IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.values());
    AssertionsForClassTypes.assertThat(channel).has(consume("KEYWORD1", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("KEYWORD1", MyKeywords.KEYWORD1));

    AssertionsForClassTypes.assertThat(channel).has(consume("KeyWord2", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("KeyWord2", MyKeywords.KeyWord2));

    AssertionsForClassTypes.assertThat(channel).has(consume("KEYWORD2", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("KEYWORD2", GenericTokenType.IDENTIFIER));
  }

  @Test
  void testConsumeNotCaseSensitiveKeywords() {
    channel = new IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.values());
    AssertionsForClassTypes.assertThat(channel).has(consume("keyword1", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("KEYWORD1", MyKeywords.KEYWORD1));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("KEYWORD1"));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasOriginalToken("keyword1"));

    AssertionsForClassTypes.assertThat(channel).has(consume("keyword2", lexer));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasToken("KEYWORD2", MyKeywords.KeyWord2));
  }

  @Test
  void testColumnAndLineNumbers() {
    channel = new IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.values());
    var reader = new CodeReader("\n\n  keyword1");
    reader.pop();
    reader.pop();
    reader.pop();
    reader.pop();
    AssertionsForClassTypes.assertThat(channel).has(consume(reader, lexer));
    var keyword = lexer.getTokens().get(0);
    assertThat(keyword.getColumn()).isEqualTo(2);
    assertThat(keyword.getLine()).isEqualTo(3);
  }

  @Test
  void testNotConsumNumber() {
    channel = new IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.values());
    AssertionsForClassTypes.assertThat(channel).isNot(consume("1234", lexer));
  }

  private enum MyKeywords implements TokenType {
    KEYWORD1, KeyWord2;

    @Override
    public String getName() {
      return name();
    }

    @Override
    public String getValue() {
      return name();
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

  }

}
