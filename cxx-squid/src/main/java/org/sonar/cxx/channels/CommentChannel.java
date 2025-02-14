/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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

public abstract class CommentChannel extends Channel<Lexer> {

  private final Character startCommentChar1;
  private final Character startCommentChar2;

  private final StringBuilder sb = new StringBuilder(256);
  private final Token.Builder tokenBuilder = Token.builder();

  protected CommentChannel(Character startCommentChar1, Character startCommentChar2) {
    this.startCommentChar1 = startCommentChar1;
    this.startCommentChar2 = startCommentChar2;
  }

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    // start of comment?
    int next = isComment(code);
    if (next == 0) {
      return false;
    }

    int line = code.getLinePosition();
    int column = code.getColumnPosition();

    code.skip(next);
    sb.append(startCommentChar1);
    sb.append(startCommentChar2);

    // search end of comment
    read(code, sb);

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

  public int isComment(CodeReader code) {
    int next = 0;

    // start of comment?
    if (code.charAt(next) != startCommentChar1) {
      return 0;
    }
    next += 1;
    next += ChannelUtils.handleLineSplicing(code, next);

    if (code.charAt(next) != startCommentChar2) {
      return 0;
    }
    next += 1;
    return next;
  }

  public abstract boolean read(CodeReader code, StringBuilder sb);

}
