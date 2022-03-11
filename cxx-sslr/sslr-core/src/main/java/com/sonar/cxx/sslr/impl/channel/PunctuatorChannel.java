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
package com.sonar.cxx.sslr.impl.channel; // cxx: in use

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import java.util.Arrays;
import java.util.Comparator;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

public class PunctuatorChannel extends Channel<Lexer> {

  private final int lookahead;
  private final TokenType[] sortedPunctuators;
  private final char[][] sortedPunctuatorsChars;
  private final Token.Builder tokenBuilder = Token.builder();

  private static class PunctuatorComparator implements Comparator<TokenType> {

    @Override
    public int compare(TokenType a, TokenType b) {
      if (a.getValue().length() == b.getValue().length()) {
        return 0;
      }
      return a.getValue().length() > b.getValue().length() ? -1 : 1;
    }

  }

  public PunctuatorChannel(TokenType... punctuators) {
    sortedPunctuators = punctuators;
    Arrays.sort(sortedPunctuators, new PunctuatorComparator());

    sortedPunctuatorsChars = new char[sortedPunctuators.length][];

    int maxLength = 0;
    for (int i = 0; i < sortedPunctuators.length; i++) {
      sortedPunctuatorsChars[i] = sortedPunctuators[i].getValue().toCharArray();
      maxLength = Math.max(maxLength, sortedPunctuatorsChars[i].length);
    }
    this.lookahead = maxLength;
  }

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    var next = code.peek(lookahead);
    for (int i = 0; i < sortedPunctuators.length; i++) {
      if (arraysEquals(next, sortedPunctuatorsChars[i])) {
        var token = tokenBuilder
          .setType(sortedPunctuators[i])
          .setValueAndOriginalValue(sortedPunctuators[i].getValue())
          .setURI(lexer.getURI())
          .setLine(code.getLinePosition())
          .setColumn(code.getColumnPosition())
          .build();

        lexer.addToken(token);

        /* Advance the CodeReader stream by the length of the punctuator */
        for (int j = 0; j < sortedPunctuatorsChars[i].length; j++) {
          code.pop();
        }

        return true;
      }
    }

    return false;
  }

  /**
   * Expected that length of second array can be less than length of first.
   */
  private static boolean arraysEquals(char[] a, char[] a2) {
    int length = a2.length;
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

}
