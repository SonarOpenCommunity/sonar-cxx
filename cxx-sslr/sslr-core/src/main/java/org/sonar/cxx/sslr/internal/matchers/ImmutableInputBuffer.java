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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImmutableInputBuffer implements InputBuffer {

  private final char[] buffer;

  /**
   * Indices of lines in buffer.
   */
  private final int[] lines;

  public ImmutableInputBuffer(char[] buffer) {
    this.buffer = buffer;

    List<Integer> newlines = new ArrayList<>();
    int i = 0;
    newlines.add(0);
    while (i < buffer.length) {
      if (isEndOfLine(buffer, i)) {
        newlines.add(i + 1);
      }
      i++;
    }
    newlines.add(i);
    this.lines = new int[newlines.size()];
    for (i = 0; i < newlines.size(); i++) {
      this.lines[i] = newlines.get(i);
    }
  }

  @Override
  public int length() {
    return buffer.length;
  }

  @Override
  public char charAt(int i) {
    return buffer[i];
  }

  /**
   * A line is considered to be terminated by any one of
   * a line feed ({@code '\n'}), a carriage return ({@code '\r'}),
   * or a carriage return followed immediately by a line feed ({@code "\r\n"}).
   */
  private static boolean isEndOfLine(char[] buffer, int i) {
    return buffer[i] == TextUtils.LF ||
        (buffer[i] == TextUtils.CR && (((i + 1 < buffer.length) && buffer[i + 1] != TextUtils.LF) || i + 1 == buffer.length));
  }

  @Override
  public String extractLine(int lineNumber) {
    int start = lines[lineNumber - 1];
    int end = lines[lineNumber];
    return new String(buffer, start, end - start);
  }

  @Override
  public int getLineCount() {
    return lines.length - 1;
  }

  private int getLineNumber(int index) {
    int i = Arrays.binarySearch(lines, index);
    return Math.min(i >= 0 ? (i + 1) : -(i + 1), getLineCount());
  }

  @Override
  public Position getPosition(int index) {
    int line = getLineNumber(index);
    int column = index - lines[line - 1] + 1;
    return new Position(line, column);
  }

}
