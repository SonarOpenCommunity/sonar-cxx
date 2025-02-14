/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

final class PPGeneratedToken {

  private PPGeneratedToken() {

  }

  /**
   * Mark token "generated".
   */
  static Token build(Token token) {
    if (!token.isGeneratedCode()) {
      return Token.builder(token)
        .setGeneratedCode(true)
        .build();
    }
    return token;
  }

  /**
   * Copy token, set new type and value and mark it "generated".
   */
  static Token build(Token token, TokenType type, String valueAndOriginalValue) {
    return Token.builder()
      .setLine(token.getLine())
      .setColumn(token.getColumn())
      .setURI(token.getURI())
      .setValueAndOriginalValue(valueAndOriginalValue)
      .setType(type)
      .setGeneratedCode(true)
      .build();
  }

  /**
   * Copy token, set new position and mark it "generated".
   */
  static Token build(Token token, URI uri, int line, int column) {
    return Token.builder()
      .setLine(line)
      .setColumn(column)
      .setURI(uri)
      .setValueAndOriginalValue(token.getValue())
      .setType(token.getType())
      .setGeneratedCode(true)
      .build();
  }

  /**
   * Create new token and mark it "generated".
   */
  @SuppressWarnings({"java:S1075", "java:S112"})
  static Token build(TokenType type, String valueAndOriginalValue, int line, int column) {
    try {
      return Token.builder()
        .setLine(line)
        .setColumn(column)
        .setValueAndOriginalValue(valueAndOriginalValue)
        .setType(type)
        .setURI(new URI("tests://unittest"))
        .setGeneratedCode(true)
        .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Mark all tokens in the list as generated.
   */
  static List<Token> markAllAsGenerated(List<Token> tokens) {
    var result = new ArrayList<Token>(tokens.size());
    for (int i = 0; i < tokens.size(); i++) {
      result.add(PPGeneratedToken.build(tokens.get(i)));
    }
    return result;
  }

}
