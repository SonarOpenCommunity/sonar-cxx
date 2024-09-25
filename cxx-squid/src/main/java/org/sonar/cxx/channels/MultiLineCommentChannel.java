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

import static com.sonar.cxx.sslr.api.GenericTokenType.COMMENT;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.Trivia;
import com.sonar.cxx.sslr.impl.Lexer;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

public class MultiLineCommentChannel extends Channel<Lexer> {

  private final StringBuilder sb = new StringBuilder(256);
  private final Token.Builder tokenBuilder = Token.builder();

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    // start of multi line comment?
    int next = isComment(code);
    if (next == 0) {
      return false;
    }

    int line = code.getLinePosition();
    int column = code.getColumnPosition();

    code.skip(next);
    sb.append('/');
    sb.append('*');

    read(code, sb); // search end of multi line comment

    var value = sb.toString();
    var token = tokenBuilder
      .setType(COMMENT)
      .setValueAndOriginalValue(value)
      .setURI(lexer.getURI())
      .setLine(line)
      .setColumn(column)
      .build();

    lexer.addTrivia(Trivia.createComment(token));
    sb.delete(0, sb.length());
    return true;
  }

  public static int isComment(CodeReader code) {
    int next = 0;

    // start of multi line comment?
    if (code.charAt(next) != '/') {
      return 0;
    }
    next += 1;
    next += ChannelUtils.handleLineSplicing(code, next);

    if (code.charAt(next) != '*') {
      return 0;
    }
    next += 1;
    return next;
  }

  public static boolean read(CodeReader code, StringBuilder sb) {
    boolean first = false;
    while (true) { // search end of multi line comment: */
      var end = ChannelUtils.handleLineSplicing(code, 0);
      code.skip(end); // remove line splicing

      var charAt = (char) code.pop();
      switch (charAt) {
        case '*':
          first = true;
          break;
        case '/':
          if (first) {
            sb.append('/');
            return true;
          }
          break;
        case ChannelUtils.EOF:
          return false;
        default:
          first = false;
          break;
      }

      sb.append(charAt);
    }

  }

}
