/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
  private final StringLiteralsChannel stringLiteralsChannel = new StringLiteralsChannel();
  private final StringBuilder sb = new StringBuilder(256);

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    int line = code.getLinePosition();
    int column = code.getColumnPosition();

    char charAt = code.charAt(0);
    if ((charAt != '#')) {
      return false;
    }
    read(code);

    output.addToken(Token.builder()
      .setLine(line)
      .setColumn(column)
      .setURI(output.getURI())
      .setValueAndOriginalValue(sb.toString())
      .setType(CxxTokenType.PREPROCESSOR)
      .build());
    sb.setLength(0);
    return true;
  }

  private void read(CodeReader code) {
    while (true) {
      char ch = code.charAt(0);
      if (isNewline(ch) || ch == EOF) {
        code.pop();
        break;
      } else if (stringLiteralsChannel.read(code, sb)) {
        continue;
      }
      ch = (char) code.pop();
      if (ch == '/' && code.charAt(0) == '/') {
        consumeSingleLineComment(code);
      } else if (ch == '/' && code.charAt(0) == '*') {
        consumeMultiLineComment(code);
      } else if (ch == '\\' && isNewline((char) code.peek())) {
        // the newline is escaped: we have a the multi line preprocessor directive
        // consume both the backslash and the newline, insert a space instead
        consumeNewline(code);
        sb.append(' ');
      } else {
        sb.append(ch);
      }
    }
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

  private static void consumeSingleLineComment(CodeReader code) {
    code.pop(); // initial '/'
    while (true) {
      char charAt = code.charAt(0);
      if (isNewline(charAt) || charAt == EOF) {
        break;
      }
      code.pop();
    }
  }

  private static void consumeMultiLineComment(CodeReader code) {
    code.pop(); // initial '*'
    while (true) {
      char ch = (char) code.pop();
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
