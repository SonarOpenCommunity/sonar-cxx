/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.sslr.channel.Channel;
import org.sonar.sslr.channel.CodeReader;

/**
 * StringLiteralsChannel
 */
public class StringLiteralsChannel extends Channel<Lexer> {

  private static final char EOF = (char) -1;

  private final StringBuilder csb = new StringBuilder(256);
  private int index = 0;
  private char ch = ' ';
  private boolean isRawString = false;

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    int line = code.getLinePosition();
    int column = code.getColumnPosition();
    if (!read(code, csb)) {
      return false;
    }
    output.addToken(Token.builder()
      .setLine(line)
      .setColumn(column)
      .setURI(output.getURI())
      .setValueAndOriginalValue(csb.toString())
      .setType(CxxTokenType.STRING)
      .build());
    csb.delete(0, csb.length());
    return true;
  }

  public boolean read(CodeReader code, StringBuilder sb) {
    index = 0;
    readStringPrefix(code);
    if (ch != '\"') {
      return false;
    }
    if (isRawString) {
      if (!readRawString(code, sb)) {
        return false;
      }
    } else {
      if (!readString(code)) {
        return false;
      }
    }
    readUdSuffix(code);
    for (var i = 0; i < index; i++) {
      sb.append((char) code.pop());
    }
    return true;
  }

  private void readStringPrefix(CodeReader code) {
    ch = code.charAt(index);
    isRawString = false;
    if ((ch == 'u') || (ch == 'U') || ch == 'L') {
      index++;
      if (ch == 'u' && code.charAt(index) == '8') {
        index++;
      }
      if (code.charAt(index) == ' ') {
        index++;
      }
      ch = code.charAt(index);
    }
    if (ch == 'R') {
      index++;
      isRawString = true;
      ch = code.charAt(index);
    }
  }

  private boolean readRawString(CodeReader code, StringBuilder sb) {
    // "delimiter( raw_character* )delimiter"
    char charAt;
    index++;
    while ((charAt = code.charAt(index)) != '(') { // delimiter in front of (
      if (charAt == EOF) {
        return false;
      }
      sb.append(charAt);
      index++;
    }
    String delimiter = sb.toString();
    sb.delete(0, sb.length());
    do {
      index -= sb.length();
      sb.delete(0, sb.length());
      while ((charAt = code.charAt(index)) != ')') { // raw_character*
        if (charAt == EOF) {
          return false;
        }
        index++;
      }
      index++;
      while ((charAt = code.charAt(index)) != '"') { // delimiter after )
        if (charAt == EOF) {
          return false;
        }
        sb.append(charAt);
        index++;

        if (sb.length() > delimiter.length()) {
          break;
        }
      }
    } while (!sb.toString().equals(delimiter));
    sb.delete(0, sb.length());
    index++;
    return true;
  }

  private boolean readString(CodeReader code) {
    index++;
    char charAt;
    while ((charAt = code.charAt(index)) != ch) {
      if (charAt == EOF) {
        return false;
      }
      if (charAt == '\\') {
        // escape
        index++;
      }
      index++;
    }
    index++;
    return true;
  }

  private void readUdSuffix(CodeReader code) {
    for (int start_index = index, len = 0;; index++) {
      char charAt = code.charAt(index);
      if (charAt == EOF) {
        return;
      }
      if (Character.isLowerCase(charAt) || Character.isUpperCase(charAt) || (charAt == '_')) {
        len++;
      } else {
        if (Character.isDigit(charAt)) {
          if (len > 0) {
            len++;
          } else {
            index = start_index;
            return;
          }
        } else {
          return;
        }
      }
    }
  }

}
