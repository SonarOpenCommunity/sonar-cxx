/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.channels;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.sonar.channel.Channel;
import org.sonar.channel.CodeReader;
import org.sonar.cxx.api.CxxTokenType;

/**
  */
public class StringLiteralsChannel extends Channel<Lexer> {

  private static final char EOF = (char) -1;

  private final StringBuilder sb = new StringBuilder();

  private int index;
  private char ch;
  private boolean rawMode = false;

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    rawMode = false;
    int line = code.getLinePosition();
    int column = code.getColumnPosition();
    index = 0;
    readStringPrefix(code);
    if ((ch != '\"')) {
      return false;
    }
    if (!read(code)) {
      return false;
    }
    for (int i = 0; i < index; i++) {
      sb.append((char) code.pop());
    }
    output.addToken(Token.builder()
        .setLine(line)
        .setColumn(column)
        .setURI(output.getURI())
        .setValueAndOriginalValue(sb.toString())
        .setType(CxxTokenType.STRING)
        .build());
    sb.setLength(0);
    return true;
  }

  private boolean read(CodeReader code) {
    // TODO: proper reading raw strings.

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

  private void readStringPrefix(CodeReader code) {
    ch = code.charAt(index);
    if ((ch == 'u') || (ch == 'U') || ch == 'L') {
      index++;
      if (ch == 'u' && code.charAt(index) == '8') {
        index++;
      }
      ch = code.charAt(index);
    }
    if (ch == 'R') {
      rawMode = true;
      index++;
      ch = code.charAt(index);
    }
  }
}
