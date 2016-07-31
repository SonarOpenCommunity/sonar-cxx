/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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
package org.sonar.cxx;

import com.sonar.sslr.api.Token;

public class TokenLocation {

  private final int startLine;
  private final int startLineOffset;
  private final int endLine;
  private final int endLineOffset;

  public TokenLocation(Token token) {
    this.startLine = token.getLine();
    this.startLineOffset = token.getColumn();

    String value = token.getValue();
    String[] lines = value.split("\r\n|\n|\r", -1);

    if (lines.length > 1) {
      endLine = token.getLine() + lines.length - 1;
      endLineOffset = lines[lines.length - 1].length();

    } else {
      this.endLine = this.startLine;
      this.endLineOffset = this.startLineOffset + token.getValue().length();
    }
  }

  public int startLine() {
    return startLine;
  }

  public int startLineOffset() {
    return startLineOffset;
  }

  public int endLine() {
    return endLine;
  }

  public int endLineOffset() {
    return endLineOffset;
  }
}
