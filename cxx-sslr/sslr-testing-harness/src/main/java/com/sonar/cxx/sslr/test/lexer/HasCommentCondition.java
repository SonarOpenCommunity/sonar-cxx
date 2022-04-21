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
package com.sonar.cxx.sslr.test.lexer;

import com.sonar.cxx.sslr.api.Token;
import java.util.List;
import org.assertj.core.api.Condition;

class HasCommentCondition extends Condition<List<Token>> {

  private final String commentValue;
  private final int commentLine;
  private final boolean originalValue;

  HasCommentCondition(String commentValue) {
    this(commentValue, -1);
  }

  HasCommentCondition(String commentValue, boolean originalValue) {
    this(commentValue, -1, originalValue);
  }

  public HasCommentCondition(String commentValue, int commentLine) {
    this(commentValue, commentLine, false);
  }

  public HasCommentCondition(String commentValue, int commentLine, boolean originalValue) {
    this.commentValue = commentValue;
    this.commentLine = commentLine;
    this.originalValue = originalValue;
    as(describe());
  }

  @Override
  public boolean matches(List<Token> tokens) {
    for (var token : tokens) {
      for (var trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          var value = originalValue ? trivia.getToken().getOriginalValue() : trivia.getToken().getValue();
          if (value.equals(commentValue)) {
            if (commentLine > -1 && trivia.getToken().getLine() != commentLine) {
              continue;
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  private String describe() {
    if (originalValue) {
      return "Comment('" + commentValue + "')";
    } else {
      return "OriginalComment('" + commentValue + "')";
    }
  }
}
