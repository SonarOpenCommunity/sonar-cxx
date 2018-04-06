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

import org.assertj.core.api.AbstractAssert;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;

public class LexerAssert extends AbstractAssert<LexerAssert, Token> {

  public LexerAssert(Token actual) {
    super(actual, LexerAssert.class);

  }

  public static LexerAssert assertThat(Token actual) {
    return new LexerAssert(actual);
  }

  public LexerAssert hasType(TokenType type) {
    isNotNull();
    if (actual.getType() != type) {
      failWithMessage("Expected the Token type to be <%s> but was <%s>", type, actual.getType());
    }
    return this;
  }

  public LexerAssert isValue(String value) {
    isNotNull();
    String tokenValue = actual.getValue();
    if (!tokenValue.contentEquals(value)) {
      failWithMessage("Expected the Token value to be <%s> but was <%s>", value, tokenValue);
    }
    return this;
  }

  public LexerAssert hasTrivia() {
    isNotNull();
    boolean exists = actual.hasTrivia();
    if (!exists) {
      failWithMessage("Expected the Token hasTrivia but was <%s>", exists);
    }
    return this;
  }

  public LexerAssert isTrivia(String trivia) {
    isNotNull();
    boolean exists = actual.hasTrivia();
    if (exists) {
      String value = actual.getTrivia().get(0).getToken().getValue();
      if (!value.contentEquals(trivia)) {
        failWithMessage("Expected the Trivia to be <%s> but was <%s>", trivia, value);
      }
    } else {
      failWithMessage("Expected the Token hasTrivia but was <%s>", exists);
    }
    return this;
  }

  public LexerAssert isTriviaLine(int line) {
    isNotNull();
    boolean exists = actual.hasTrivia();
    if (exists) {
      int value = actual.getTrivia().get(0).getToken().getLine();
      if (value != line) {
        failWithMessage("Expected the Trivia line to be <%s> but was <%s>", line, value);
      }
    } else {
      failWithMessage("Expected the Token hasTrivia but was <%s>", exists);
    }
    return this;
  }

    public LexerAssert isComment() {
      isNotNull();
      Boolean exists = actual.getTrivia().get(0).isComment();
      if (!exists) {
         failWithMessage("Expected the Token isComment but was <%s>", exists);
      }
    return this;
  }

}
