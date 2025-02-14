/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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

import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;

public class ParseTreePrinter {

  public static String leafsToString(ParseNode node, char[] input) {
    var result = new StringBuilder();
    printLeafs(node, input, result);
    return result.toString();
  }

  private static void printLeafs(ParseNode node, char[] input, StringBuilder result) {
    if (node.getChildren().isEmpty()) {
      for (int i = node.getStartIndex(); i < Math.min(node.getEndIndex(), input.length); i++) {
        result.append(input[i]);
      }
    } else {
      for (var child : node.getChildren()) {
        printLeafs(child, input, result);
      }
    }
  }

  public static void print(ParseNode node, char[] input) {
    print(node, 0, input);
  }

  private static void print(ParseNode node, int level, char[] input) {
    for (int i = 0; i < level; i++) {
      System.out.print("  ");
    }
    var sb = new StringBuilder();
    for (int i = node.getStartIndex(); i < Math.min(node.getEndIndex(), input.length); i++) {
      sb.append(input[i]);
    }
    System.out.println(matcherToString(node.getMatcher())
      + " (start=" + node.getStartIndex()
      + ", end=" + node.getEndIndex()
      + ", matches=" + sb.toString()
      + ")");
    for (var child : node.getChildren()) {
      print(child, level + 1, input);
    }
  }

  private static String matcherToString(Matcher matcher) {
    if (matcher instanceof MutableParsingRule mutableParsingRule) {
      return mutableParsingRule.getName();
    } else if (matcher instanceof TokenExpression tokenExpression) {
      return tokenExpression.getTokenType().getName();
    } else {
      return matcher.toString();
    }
  }

}
