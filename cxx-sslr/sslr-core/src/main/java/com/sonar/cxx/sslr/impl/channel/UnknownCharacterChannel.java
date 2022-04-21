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

import static com.sonar.cxx.sslr.api.GenericTokenType.UNKNOWN_CHAR;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

/**
 * Creates token with type {@link com.sonar.cxx.sslr.api.GenericTokenType#UNKNOWN_CHAR} for any character.
 * This channel, if present, should be the last one.
 *
 * @since 1.2
 */
public class UnknownCharacterChannel extends Channel<Lexer> {

  private final Token.Builder tokenBuilder = Token.builder();

  public UnknownCharacterChannel() {
  }

  /**
   * @deprecated logging removed in 1.20, use {@link #UnknownCharacterChannel()} or implement your own Channel with
   * logging
   */
  @Deprecated
  public UnknownCharacterChannel(boolean shouldLogWarning) {
  }

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    if (code.peek() != -1) {
      var unknownChar = (char) code.pop();

      var token = tokenBuilder
        .setType(UNKNOWN_CHAR)
        .setValueAndOriginalValue(String.valueOf(unknownChar))
        .setURI(lexer.getURI())
        .setLine(code.getLinePosition())
        .setColumn(code.getColumnPosition() - 1)
        .build();

      lexer.addToken(token);

      return true;
    }
    return false;
  }

}
