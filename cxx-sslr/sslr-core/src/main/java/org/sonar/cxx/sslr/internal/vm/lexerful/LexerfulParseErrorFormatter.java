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
package org.sonar.cxx.sslr.internal.vm.lexerful;

import com.sonar.cxx.sslr.api.Token;
import java.util.List;

public class LexerfulParseErrorFormatter {

  /**
   * Number of tokens in snippet before and after token with error.
   */
  private static final int SNIPPET_SIZE = 30;

  public String format(List<Token> tokens, int errorIndex) {
    var sb = new StringBuilder();
    var errorPos = errorIndex < tokens.size()
                 ? getTokenStart(tokens.get(errorIndex))
                 : getTokenEnd(tokens.get(tokens.size() - 1));
    sb.append("Parse error at line ").append(errorPos.line)
      .append(" column ").append(errorPos.column)
      .append(":\n\n");
    appendSnippet(sb, tokens, errorIndex, errorPos.line);
    return sb.toString();
  }

  private static class Pos {

    int line;
    int column;

    @Override
    public String toString() {
      return "(" + line + ", " + column + ")";
    }
  }

  private static Pos getTokenStart(Token token) {
    var pos = new Pos();
    pos.line = token.getLine();
    pos.column = token.getColumn();
    return pos;
  }

  private static Pos getTokenEnd(Token token) {
    var pos = new Pos();
    pos.line = token.getLine();
    pos.column = token.getColumn();
    var tokenLines = token.getOriginalValue().split("(\r)?\n|\r", -1);
    if (tokenLines.length == 1) {
      pos.column += tokenLines[0].length();
    } else {
      pos.line += tokenLines.length - 1;
      pos.column = tokenLines[tokenLines.length - 1].length();
    }
    return pos;
  }

  private static void appendSnippet(StringBuilder sb, List<Token> tokens, int errorIndex, int errorLine) {
    int startToken = Math.max(errorIndex - SNIPPET_SIZE, 0);
    int endToken = Math.min(errorIndex + SNIPPET_SIZE, tokens.size());
    tokens = tokens.subList(startToken, endToken);

    int line = tokens.get(0).getLine();
    int column = tokens.get(0).getColumn();
    sb.append(formatLineNumber(line, errorLine));
    for (var token : tokens) {
      while (line < token.getLine()) {
        line++;
        column = 0;
        sb.append('\n').append(formatLineNumber(line, errorLine));
      }
      while (column < token.getColumn()) {
        sb.append(' ');
        column++;
      }
      var tokenLines = token.getOriginalValue().split("(\r)?\n|\r", -1);
      sb.append(tokenLines[0]);
      column += tokenLines[0].length();
      for (int j = 1; j < tokenLines.length; j++) {
        line++;
        sb.append('\n').append(formatLineNumber(line, errorLine)).append(tokenLines[j]);
        column = tokenLines[j].length();
      }
    }
    sb.append('\n');
  }

  private static String formatLineNumber(int line, int errorLine) {
    return line == errorLine
             ? String.format("%1$5s  ", "-->")
             : String.format("%1$5d: ", line);
  }

}
