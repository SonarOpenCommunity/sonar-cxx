/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

// Detects preprocessor directives:
// This channel detects source code lines which should be handled by the preprocessor.
// If a line is not marked CxxTokenType.PREPROCESSOR it is not handled by CppLexer and CppGrammar.
//
public class PreprocessorChannel extends Channel<Lexer> {

  private static final char EOF = (char) -1;
  private final StringLiteralsChannel stringLiteralsChannel = new StringLiteralsChannel();
  private final StringBuilder sb = new StringBuilder(256);
  private final Matcher matcher;

  public PreprocessorChannel(TokenType[]... keywordSets) {
    var regexp = new StringBuilder(256);
    regexp.append("#");
    for (var keywords : keywordSets) {
      for (var keyword : keywords) {
        regexp.append("|");
        regexp.append(keyword.getValue());
        regexp.append("\\s++");
      }
    }
    matcher = Pattern.compile(regexp.toString()).matcher("");
  }

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    int line = code.getLinePosition();
    int column = code.getColumnPosition();

    if (code.popTo(matcher, sb) <= 0) {
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
    sb.delete(0, sb.length());
    return true;
  }

  private void read(CodeReader code) {
    while (true) {
      var ch = code.charAt(0);
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
      var charAt = code.charAt(0);
      if (isNewline(charAt) || charAt == EOF) {
        break;
      }
      code.pop();
    }
  }

  private static void consumeMultiLineComment(CodeReader code) {
    code.pop(); // initial '*'
    while (true) {
      var ch = (char) code.pop();
      if (ch == EOF) {
        return;
      }
      if (ch == '*' && code.charAt(0) == '/') {
        code.pop();
        return;
      }
    }
  }

  private static boolean isNewline(char ch) {
    return (ch == '\n') || (ch == '\r');
  }

}
