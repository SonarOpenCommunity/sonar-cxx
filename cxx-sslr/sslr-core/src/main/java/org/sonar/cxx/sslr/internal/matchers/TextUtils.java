/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package org.sonar.cxx.sslr.internal.matchers;

public final class TextUtils {

  private TextUtils() {
  }

  public static final char CR = '\r';
  public static final char LF = '\n';

  private static final char[] ESCAPE = {'\r', '\n', '\f', '\t', '"'};
  private static final String[] ESCAPED = {"\\r", "\\n", "\\f", "\\t", "\\\""};

  /**
   * Replaces carriage returns, line feeds, form feeds, tabs and double quotes with their respective escape sequences.
   */
  public static String escape(char ch) {
    for (int i = 0; i < ESCAPE.length; i++) {
      if (ESCAPE[i] == ch) {
        return ESCAPED[i];
      }
    }
    return String.valueOf(ch);
  }

  // TODO Godin: can be replaced by com.google.common.base.CharMatcher.anyOf("\n\r").trimTrailingFrom(string)
  public static String trimTrailingLineSeparatorFrom(String string) {
    int last;
    for (last = string.length() - 1; last >= 0; last--) {
      if (string.charAt(last) != LF && string.charAt(last) != CR) {
        break;
      }
    }
    return string.substring(0, last + 1);
  }

}
