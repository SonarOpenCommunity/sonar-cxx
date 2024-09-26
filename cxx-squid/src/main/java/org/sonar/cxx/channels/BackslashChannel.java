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

import com.sonar.cxx.sslr.impl.Lexer;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

public class BackslashChannel extends Channel<Lexer> {

  private final StringBuilder sb = new StringBuilder(256);

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    if (code.charAt(0) != '\\') {
      return false;
    }

    var lineSplicing = read(code, sb);
    sb.delete(0, sb.length());
    return lineSplicing != 0;
  }

  public static int read(CodeReader code, StringBuilder sb) {
    var end = ChannelUtils.handleLineSplicing(code, 0);
    code.skip(end); // remove line splicing
    return end;
  }

}
