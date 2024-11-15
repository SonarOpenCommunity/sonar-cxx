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

import org.sonar.cxx.sslr.channel.CodeReader;

public final class ChannelUtils {

  public static final char LF = '\n';
  public static final char CR = '\r';
  public static final char EOF = (char) -1;

  private ChannelUtils() {
    // empty
  }

  public static boolean isNewLine(char ch) {
    return (ch == LF) || (ch == CR);
  }

  public static boolean isWhitespace(char ch) {
    return (ch == ' ') || (ch == '\t');
  }

  public static boolean isSuffix(char c) {
    return Character.isLowerCase(c) || Character.isUpperCase(c) || (c == '_');
  }

  /**
   * Handle line splicing. - lines terminated by a \ are spliced together with the next line - P2178R0 making trailing
   * whitespaces non-significant
   *
   * line endings: - Linux/Unix, Mac from OS X a.k.a macOS: LF - Windows/DOS: CR LF - Classic Mac OS: CR
   *
   * @return numbers of sign to remove to splice the lines
   */
  public static int handleLineSplicing(CodeReader code, int start) {
    int next = start;
    if (code.charAt(next) != '\\') {
      return 0;
    }

    boolean newline = false;
    next++;
    while (true) {
      var charAt = code.charAt(next);
      if (charAt == LF) {
        newline = true;
        break;
      }
      if (charAt == CR) {
        if (code.charAt(next + 1) == LF) {
          next++;
        }
        newline = true;
        break;
      }
      if (!isWhitespace(charAt)) {
        break;
      }
      next++;
    }

    return newline ? (next - start + 1) : 0;
  }

}
