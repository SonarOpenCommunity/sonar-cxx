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

import static com.sonar.cxx.sslr.api.GenericTokenType.UNKNOWN_CHAR;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import java.io.StringReader;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

class UnknownCharacterChannelTest {

  private final UnknownCharacterChannel channel = new UnknownCharacterChannel();

  @Test
  void shouldConsumeAnyCharacter() {
    check("'", channel, UNKNOWN_CHAR, "'", Lexer.builder().build());
    check("a", channel, UNKNOWN_CHAR, "a", Lexer.builder().build());
  }

  @Test
  void shouldConsumeEofCharacter() {
    assertThat(channel.consume(new CodeReader(""), null)).isFalse();
  }

  private void check(String input, Channel<Lexer> channel, TokenType expectedTokenType, String expectedTokenValue,
                     Lexer lexer) {
    var code = new CodeReader(new StringReader(input));

    assertThat(channel.consume(code, lexer)).isTrue();
    assertThat(lexer.getTokens()).hasSize(1);
    assertThat(lexer.getTokens().get(0).getType()).isEqualTo(expectedTokenType);
    assertThat(lexer.getTokens().get(0).getValue()).isEqualTo(expectedTokenValue);
  }

}
