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

public class PreprocessorChannel extends Channel<Lexer> {

  private static final char EOF = (char) -1;

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    int line = code.getLinePosition();
    int column = code.getColumnPosition();

    char ch = code.charAt(0);
    if ((ch != '#')) {
      return false;
    }

    String tokenValue = read(code);
    output.addToken(Token.builder()
      .setLine(line)
      .setColumn(column)
      .setURI(output.getURI())
      .setValueAndOriginalValue(tokenValue)
      .setType(CxxTokenType.PREPROCESSOR)
      .build());

    return true;
  }

  private static String read(CodeReader code) {
    StringBuilder sb = new StringBuilder();
    char ch;

    while (true) {
      ch = (char) code.pop();
      if (isNewline(ch) || ch == EOF) {
        break;
      }
      if (ch == '/' && code.charAt(0) == '*') {
        consumeComment(code);
      } else if (ch == '\\' && isNewline((char) code.peek())) {
        // the newline is escaped: we have a the multi line preprocessor directive
        // consume both the backslash and the newline, insert a space instead
        consumeNewline(code);
        sb.append(' ');
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  private static void consumeNewline(CodeReader code) {
    if ((code.charAt(0) == '\r') && (code.charAt(1) == '\n')) {
      // \r\n
      code.pop();
      code.pop();
    } else {
      // \r or \n
      code.pop();
    }
  }

  private static void consumeComment(CodeReader code) {
    char ch;

    code.pop(); // initial '*'
    while (true) {
      ch = (char) code.pop();
      if (ch == EOF) {
        break;
      }
      if (ch == '*' && code.charAt(0) == '/') {
        code.pop();
        break;
      }
    }
  }

  private static boolean isNewline(char ch) {
    return (ch == '\n') || (ch == '\r');
  }
}
