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
package com.sonar.cxx.sslr.impl.channel; // cxx: in use

import com.sonar.cxx.sslr.api.TokenType;

public final class RegexpChannelBuilder {

  public static final String DIGIT = "\\d";
  public static final String ANY_CHAR = "[\\s\\S]";
  public static final String OCTAL_DIGIT = "[0-7]";
  public static final String HEXA_DIGIT = "[a-fA-F0-9]";

  private RegexpChannelBuilder() {
  }

  public static RegexpChannel regexp(TokenType type, String... regexpPiece) {
    return new RegexpChannel(type, merge(regexpPiece));
  }

  public static CommentRegexpChannel commentRegexp(String... regexpPiece) {
    return new CommentRegexpChannel(merge(regexpPiece));
  }

  public static String opt(String regexpPiece) {
    return regexpPiece + "?+";
  }

  public static String and(String... regexpPieces) {
    var result = new StringBuilder();
    for (var rexpPiece : regexpPieces) {
      result.append(rexpPiece);
    }
    return result.toString();
  }

  public static String one2n(String regexpPiece) {
    return regexpPiece + "++";
  }

  public static String o2n(String regexpPiece) {
    return regexpPiece + "*+";
  }

  public static String anyButNot(String... character) {
    var result = new StringBuilder();
    result.append("[^");
    for (var character1 : character) {
      result.append(character1);
    }
    result.append("]");
    return result.toString();
  }

  public static String g(String... regexpPiece) {
    var result = new StringBuilder();
    result.append("(");
    for (String element : regexpPiece) {
      result.append(element);
    }
    result.append(")");
    return result.toString();
  }

  public static String or(String... regexpPiece) {
    var result = new StringBuilder();
    result.append("(");
    for (int i = 0; i < regexpPiece.length; i++) {
      result.append(regexpPiece[i]);
      if (i != regexpPiece.length - 1) {
        result.append("|");
      }
    }
    result.append(")");
    return result.toString();
  }

  private static String merge(String... piece) {
    var result = new StringBuilder();
    for (var element : piece) {
      result.append(element);
    }
    return result.toString();
  }
}
