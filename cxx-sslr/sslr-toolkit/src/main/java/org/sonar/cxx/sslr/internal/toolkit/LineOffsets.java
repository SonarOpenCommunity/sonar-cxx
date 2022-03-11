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
package org.sonar.cxx.sslr.internal.toolkit;

import com.sonar.cxx.sslr.api.Token;
import java.util.HashMap;
import java.util.Map;

public class LineOffsets {

  private static final String NEWLINE_REGEX = "(\r)?\n|\r";

  private final Map<Integer, Integer> lineOffsets = new HashMap<>();
  private final int endOffset;

  public LineOffsets(String code) {
    int currentOffset = 0;

    var lines = code.split(NEWLINE_REGEX, -1);
    for (int line = 1; line <= lines.length; line++) {
      lineOffsets.put(line, currentOffset);
      currentOffset += lines[line - 1].length() + 1;
    }

    endOffset = currentOffset - 1;
  }

  public int getStartOffset(Token token) {
    return getOffset(token.getLine(), token.getColumn());
  }

  public int getEndOffset(Token token) {
    var tokenLines = token.getOriginalValue().split(NEWLINE_REGEX, -1);

    int tokenLastLine = token.getLine() + tokenLines.length - 1;
    int tokenLastLineColumn = (tokenLines.length > 1 ? 0 : token.getColumn()) + tokenLines[tokenLines.length - 1]
      .length();

    return getOffset(tokenLastLine, tokenLastLineColumn);
  }

  public int getOffset(int line, int column) {
    if (line < 1) {
      throw new IllegalArgumentException();
    }
    if (column < 0) {
      throw new IllegalArgumentException();
    }

    if (lineOffsets.containsKey(line)) {
      return Math.min(lineOffsets.get(line) + column, endOffset);
    } else {
      return endOffset;
    }
  }

}
