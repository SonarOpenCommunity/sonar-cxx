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

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import static com.sonar.cxx.sslr.test.lexer.LexerConditions.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.sslr.test.channel.ChannelMatchers.*;

public class CommentChannelTest {

  private CommentRegexpChannel channel;
  private final Lexer lexer = Lexer.builder().build();

  @Test
  public void testCommentRegexp() {
    channel = new CommentRegexpChannel("//.*");
    AssertionsForClassTypes.assertThat(channel).isNot(consume("This is not a comment", lexer));
    AssertionsForClassTypes.assertThat(channel).is(consume("//My Comment\n second line", lexer));
    var token = tokenBuilder(GenericTokenType.EOF, "EOF");
    lexer.addToken(token);
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasComment("//My Comment"));
    AssertionsForClassTypes.assertThat(lexer.getTokens()).has(hasOriginalComment("//My Comment"));
  }

  private static Token tokenBuilder(TokenType type, String value) {
    try {
      return Token.builder()
        .setType(type)
        .setValueAndOriginalValue(value)
        .setURI(new URI("tests://unittest"))
        .setLine(1)
        .setColumn(1)
        .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
