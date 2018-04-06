/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.lexer;

import org.sonar.cxx.api.CxxTokenType;

import com.sonar.sslr.api.TokenType;


public final class LiteralValuesBuilder {
  String lexerValue;
  String tokenValue;
  TokenType tokenType;

  LiteralValuesBuilder (Builder builder) {
    this.lexerValue = builder.lexerValue;
    this.tokenValue = builder.tokenValue;
    this.tokenType = builder .tokenType;
  }

  public static Builder builder(String key) {
    return new Builder(key);
  }

  public static class Builder {
    private final String lexerValue;
    private String tokenValue ="";
    private TokenType tokenType = CxxTokenType.NUMBER;

    private Builder(String lexerValue) {
      this.lexerValue = lexerValue;
    }

    public Builder tokenValue(String tokenValue) {
      this.tokenValue = tokenValue;
      return this;
    }

    public Builder tokenType(TokenType tokenType) {
      this.tokenType = tokenType;
      return this;
    }

    public LiteralValuesBuilder build() {
      return new LiteralValuesBuilder(this);
    }
  }

}
