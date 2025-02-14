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
package com.sonar.cxx.sslr.test.lexer; // cxx: in use

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.util.List;
import org.assertj.core.api.Condition;

public final class LexerConditions {

  private LexerConditions() {
  }

  public static Condition<List<Token>> hasToken(String tokenValue, TokenType tokenType) {
    return new HasTokenCondition(tokenValue, tokenType);
  }

  public static Condition<List<Token>> hasToken(String tokenValue) {
    return new HasTokenValueCondition(tokenValue);
  }

  public static Condition<List<Token>> hasOriginalToken(String tokenValue) {
    return new HasTokenValueCondition(tokenValue, true);
  }

  public static Condition<List<Token>> hasToken(TokenType tokenType) {
    return new HasTokenTypeCondition(tokenType);
  }

  public static Condition<List<Token>> hasTokens(String... tokenValues) {
    return new HasTokensCondition(tokenValues);
  }

  public static Condition<List<Token>> hasLastToken(String tokenValue, TokenType tokenType) {
    return new HasLastTokenCondition(tokenValue, tokenType);
  }

  public static Condition<List<Token>> hasComment(String commentValue) {
    return new HasCommentCondition(commentValue);
  }

  public static Condition<List<Token>> hasComment(String commentValue, int commentLine) {
    return new HasCommentCondition(commentValue, commentLine);
  }

  public static Condition<List<Token>> hasOriginalComment(String commentValue) {
    return new HasCommentCondition(commentValue, true);
  }

  public static Condition<List<Token>> hasOriginalComment(String commentValue, int commentLine) {
    return new HasCommentCondition(commentValue, commentLine, true);
  }

}
