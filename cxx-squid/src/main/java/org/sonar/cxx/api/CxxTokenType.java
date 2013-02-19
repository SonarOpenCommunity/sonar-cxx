/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.api;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;

public enum CxxTokenType implements TokenType {
  NUMBER,
  STRING,
  CHARACTER,
  PREPROCESSOR,
  PREPROCESSOR_DEFINE,
  PREPROCESSOR_INCLUDE,
  PREPROCESSOR_IFDEF,
  PREPROCESSOR_IFNDEF,
  PREPROCESSOR_IF,
  PREPROCESSOR_ELSE,
  PREPROCESSOR_ENDIF,
  WS; //whitespace

  public String getName() {
    return name();
  }

  public String getValue() {
    return name();
  }

  public boolean hasToBeSkippedFromAst(AstNode node) {
    return this == WS;
  }

}
