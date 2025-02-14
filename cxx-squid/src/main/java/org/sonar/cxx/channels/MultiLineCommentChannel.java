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

import org.sonar.cxx.sslr.channel.CodeReader;

public class MultiLineCommentChannel extends CommentChannel {

  public MultiLineCommentChannel() {
    super('/', '*');
  }

  @Override
  public boolean read(CodeReader code, StringBuilder sb) {
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
