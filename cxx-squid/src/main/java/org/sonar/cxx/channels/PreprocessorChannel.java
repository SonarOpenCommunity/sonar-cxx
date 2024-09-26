/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

  private final StringLiteralsChannel stringLiteralsChannel = new StringLiteralsChannel();
  private final StringBuilder sb = new StringBuilder(256);
  private final StringBuilder dummy = new StringBuilder(256);
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

    // if there was already a token in the line it's not a preprocessor command
    var previousTokens = output.getTokens();
    if (!previousTokens.isEmpty()) {
      if (previousTokens.get(previousTokens.size() - 1).getLine() == line) {
        return false;
      }
    }

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
      var charAt = code.charAt(0);
      if (ChannelUtils.isNewLine(charAt) || charAt == ChannelUtils.EOF) {
        code.pop();
        break;
      } else if (stringLiteralsChannel.read(code, sb)) { // string literal
        continue;
      }

      var len = 0;
      switch (charAt) {
        case '/': // comment?
          len = SingleLineCommentChannel.isComment(code);
          if (len != 0) {
            // single line comment
            code.skip(len);
            SingleLineCommentChannel.read(code, dummy);
            dummy.delete(0, dummy.length());
          } else {
            len = MultiLineCommentChannel.isComment(code);
            if (len != 0) {
              // multi line comment
              code.skip(len);
              MultiLineCommentChannel.read(code, dummy);
              dummy.delete(0, dummy.length());
            }
          }
          break;
        case '\\':
          len = BackslashChannel.read(code, dummy);
          if (len != 0) {
            // consume backslash and the newline
            dummy.delete(0, dummy.length());
          }
          break;
      }

      if (len == 0) {
        sb.append((char) code.pop());
      }
    }
  }

}
