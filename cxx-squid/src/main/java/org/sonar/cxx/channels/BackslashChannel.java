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

import com.sonar.cxx.sslr.impl.Lexer;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

public class BackslashChannel extends Channel<Lexer> {

  private static boolean isNewLine(char ch) {
    return (ch == '\n') || (ch == '\r');
  }

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    var ch = (char) code.peek();

    if ((ch == '\\') && isNewLine(code.charAt(1))) {
      // just throw away the backslash
      code.pop();
      return true;
    }

    return false;
  }

}
