/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.TokenType;
import javax.annotation.Nullable;

/**
 * C++ Standard, Section 16 "Preprocessing directives"
 */
public enum PPKeyword implements TokenType {

  IF("#if"),
  IFDEF("#ifdef"),
  IFNDEF("#ifndef"),
  ELIF("#elif"),
  ELIFDEF("#elifdef"),
  ELIFNDEF("#elifndef"),
  ELSE("#else"),
  ENDIF("#endif"),
  INCLUDE("#include"),
  DEFINE("#define"),
  UNDEF("#undef"),
  LINE("#line"),
  ERROR("#error"),
  PRAGMA("#pragma"),
  // extensions
  WARNING("#warning"),
  INCLUDE_NEXT("#include_next");

  private final String value;

  PPKeyword(String value) {
    this.value = value;
  }

  public static String[] keywordValues() {
    PPKeyword[] keywordsEnum = PPKeyword.values();
    var keywords = new String[keywordsEnum.length];
    for (var i = 0; i < keywords.length; i++) {
      keywords[i] = keywordsEnum[i].getValue();
    }
    return keywords;
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public boolean hasToBeSkippedFromAst(@Nullable AstNode node) {
    return false;
  }

}
