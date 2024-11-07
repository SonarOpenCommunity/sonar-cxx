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
package com.sonar.cxx.sslr.impl.token;

import static com.sonar.cxx.sslr.api.GenericTokenType.EOF;
import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.cxx.sslr.channel.CodeReader;

public final class TokenUtils {

  private TokenUtils() {
  }

  public static String merge(List<Token> tokens) {
    return merge(tokens, " ");
  }

  public static String merge(List<Token> tokens, String spacer) {
    tokens = removeLastTokenIfEof(tokens);
    var result = new StringBuilder();
    for (int i = 0; i < tokens.size(); i++) {
      var token = tokens.get(i);
      result.append(token.getValue());
      if (i < tokens.size() - 1) {
        result.append(spacer);
      }
    }
    return result.toString();
  }

  public static List<Token> removeLastTokenIfEof(List<Token> tokens) {
    if (!tokens.isEmpty()) {
      var lastToken = tokens.get(tokens.size() - 1);
      if ("EOF".equals(lastToken.getValue())) {
        return tokens.subList(0, tokens.size() - 1);
      }
    }

    return tokens;
  }

  public static List<Token> lex(String sourceCode) {
    List<Token> tokens = new ArrayList<>();
    var reader = new CodeReader(sourceCode);
    var matcher = Pattern.compile("[a-zA-Z_0-9\\+\\-\\*/]+").matcher("");

    while (reader.peek() != -1) {
      var nextStringToken = new StringBuilder();
      Token token;
      int linePosition = reader.getLinePosition();
      int columnPosition = reader.getColumnPosition();
      if (reader.popTo(matcher, nextStringToken) != -1) {
        if ("EOF".equals(nextStringToken.toString())) {
          token = tokenBuilder(EOF, nextStringToken.toString(), linePosition, columnPosition);
        } else {
          token = tokenBuilder(IDENTIFIER, nextStringToken.toString(), linePosition, columnPosition);
        }
      } else if (Character.isWhitespace(reader.peek())) {
        reader.pop();
        continue;
      } else {
        token = tokenBuilder(IDENTIFIER, Character.toString((char) reader.pop()), linePosition, columnPosition);
      }
      tokens.add(token);
    }
    return tokens;
  }

  @SuppressWarnings("java:S1075")
  public static Token tokenBuilder(TokenType type, String value, int line, int column) {
    try {
      return Token.builder()
        .setType(type)
        .setValueAndOriginalValue(value)
        .setURI(new URI("tests://unittest"))
        .setLine(line)
        .setColumn(column).
        build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
