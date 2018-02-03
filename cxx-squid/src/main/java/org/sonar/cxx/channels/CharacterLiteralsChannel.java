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
package org.sonar.cxx.channels;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.sslr.channel.Channel;
import org.sonar.sslr.channel.CodeReader;

/**
 * CharacterLiteralsChannel
 */
public class CharacterLiteralsChannel extends Channel<Lexer> {

  private static final char EOF = (char) -1;

  private final StringBuilder sb = new StringBuilder();

  private int index;
  private char ch = ' ';

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    int line = code.getLinePosition();
    int column = code.getColumnPosition();
    index = 0;
    readPrefix(code);
    if (ch != '\'') {
      return false;
    }
    if (!read(code)) {
      return false;
    }
    readUdSuffix(code);
    for (int i = 0; i < index; i++) {
      sb.append((char) code.pop());
    }
    output.addToken(Token.builder()
      .setLine(line)
      .setColumn(column)
      .setURI(output.getURI())
      .setValueAndOriginalValue(sb.toString())
      .setType(CxxTokenType.CHARACTER)
      .build());
    sb.setLength(0);
    return true;
  }

  private boolean read(CodeReader code) {
    index++;
    while (code.charAt(index) != ch) {
      if (code.charAt(index) == EOF) {
        return false;
      }
      if (code.charAt(index) == '\\') {
        // escape
        index++;
      }
      index++;
    }
    index++;
    return true;
  }

  private void readPrefix(CodeReader code) {
    ch = code.charAt(index);
    if ((ch == 'u') || (ch == 'U') || ch == 'L') {
      index++;
      ch = code.charAt(index);
    }
  }

  private void readUdSuffix(CodeReader code) {
    for (int start_index = index, len = 0;; index++) {
      char c = code.charAt(index);
      if (c == EOF) {
        return;
      }
      if ((c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || (c == '_')) {
        len++;
      } else {
        if (c >= '0' && c <= '9') {
          if (len > 0) {
            len++;
          } else {
            index = start_index;
            return;
          }
        } else {
          return;
        }
      }
    }
  }
}
