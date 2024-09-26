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
package com.sonar.cxx.sslr.test.lexer;

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.util.List;
import org.assertj.core.api.Condition;

class HasLastTokenCondition extends Condition<List<Token>> {

  private final String tokenValue;
  private final TokenType tokenType;

  HasLastTokenCondition(String tokenValue, TokenType tokenType) {
    this.tokenType = tokenType;
    this.tokenValue = tokenValue;
    as(describe());
  }

  @Override
  public boolean matches(List<Token> tokens) {
    if (tokens.isEmpty()) {
      throw new IllegalArgumentException("There must be at least one lexed token.");
    }
    var lastToken = tokens.get(tokens.size() - 1);
    return lastToken.getValue().equals(tokenValue) && lastToken.getType() == tokenType;
  }

  private String describe() {
    return "Token('" + tokenValue + "'," + tokenType + ")";
  }

}
