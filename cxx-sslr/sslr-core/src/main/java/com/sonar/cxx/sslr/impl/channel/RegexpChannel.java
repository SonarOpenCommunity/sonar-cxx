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
package com.sonar.cxx.sslr.impl.channel;

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.LexerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

/**
 * Creates token of specified type from characters, which match given regular expression.
 *
 * @see RegexpChannelBuilder
 */
public class RegexpChannel extends Channel<Lexer> {

  private final StringBuilder tmpBuilder = new StringBuilder();
  private final TokenType type;
  private final Matcher matcher;
  private final String regexp;
  private final Token.Builder tokenBuilder = Token.builder();

  /**
   * @throws java.util.regex.PatternSyntaxException if the expression's syntax is invalid
   */
  public RegexpChannel(TokenType type, String regexp) {
    matcher = Pattern.compile(regexp).matcher("");
    this.type = type;
    this.regexp = regexp;
  }

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    try {
      if (code.popTo(matcher, tmpBuilder) > 0) {
        var value = tmpBuilder.toString();

        var token = tokenBuilder
          .setType(type)
          .setValueAndOriginalValue(value)
          .setURI(lexer.getURI())
          .setLine(code.getPreviousCursor().getLine())
          .setColumn(code.getPreviousCursor().getColumn())
          .build();

        lexer.addToken(token);

        tmpBuilder.delete(0, tmpBuilder.length());
        return true;
      }
      return false;
    } catch (StackOverflowError e) {
      throw new LexerException(
        "The regular expression "
          + regexp
          + " has led to a stack overflow error. "
          + "This error is certainly due to an inefficient use of alternations. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050507",
        e);
    }
  }
}
