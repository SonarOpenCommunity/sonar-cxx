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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

public class LocatedText implements CharSequence {

  private static final int[] EMPTY_INT_ARRAY = new int[0];

  private final File file;
  private final URI uri;
  private final char[] chars;

  /**
   * Indices of lines.
   * Number of elements equal to number of line terminators.
   */
  private final int[] lines;

  public LocatedText(@Nullable File file, char[] chars) {
    this.file = file;
    this.uri = file == null ? null : file.toURI();
    this.chars = chars;
    this.lines = computeLines(chars);
  }

  @Override
  public int length() {
    return chars.length;
  }

  public char[] toChars() {
    var chars = new char[length()];
    System.arraycopy(this.chars, 0, chars, 0, chars.length);
    return chars;
  }

  @Override
  public char charAt(int index) {
    return chars[index];
  }

  @Override
  public CharSequence subSequence(int from, int to) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return new String(toChars());
  }

  TextLocation getLocation(int index) {
    if (index < 0 || index > length()) {
      throw new IndexOutOfBoundsException();
    }
    int line = getLineNumber(index);
    int column = index - getLineStart(line) + 1;
    return new TextLocation(file, uri, line, column);
  }

  private int getLineNumber(int index) {
    int i = Arrays.binarySearch(lines, index);
    return i >= 0 ? (i + 2) : -i;
  }

  private int getLineStart(int line) {
    return line == 1 ? 0 : lines[line - 2];
  }

  private static int[] computeLines(char[] chars) {
    List<Integer> newlines = new ArrayList<>();
    int i = 0;
    while (i < chars.length) {
      if (isEndOfLine(chars, i)) {
        newlines.add(i + 1);
      }
      i++;
    }
    if (newlines.isEmpty()) {
      return EMPTY_INT_ARRAY;
    }
    var lines = new int[newlines.size()];
    for (i = 0; i < newlines.size(); i++) {
      lines[i] = newlines.get(i);
    }
    return lines;
  }

  /**
   * A line is considered to be terminated by any one of
   * a line feed ({@code '\n'}), a carriage return ({@code '\r'}),
   * or a carriage return followed immediately by a line feed ({@code "\r\n"}).
   */
  private static boolean isEndOfLine(char[] buffer, int i) {
    return buffer[i] == '\n' || buffer[i] == '\r' && (i + 1 < buffer.length && buffer[i + 1] != '\n' || i + 1
                                                                                                        == buffer.length);
  }

}
