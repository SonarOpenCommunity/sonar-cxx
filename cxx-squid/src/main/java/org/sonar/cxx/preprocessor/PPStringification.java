/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

/**
 * In function-like macros, a # operator before an identifier in the replacement-list runs the identifier through
 * parameter replacement and encloses the result in quotes, effectively creating a string literal. In addition, the
 * preprocessor adds backslashes to escape the quotes surrounding embedded string literals, if any, and doubles the
 * backslashes within the string as necessary. All leading and trailing whitespace is removed, and any sequence of
 * whitespace in the middle of the text (but not inside embedded string literals) is collapsed to a single space. This
 * operation is called "stringification". If the result of stringification is not a valid string literal, the behavior
 * is undefined.
 */
class PPStringification {

  private PPStringification() {

  }

  static String stringify(String str) {
    var result = new StringBuilder(2 * str.length());
    var addBlank = false;
    var ignoreNextBlank = false;
    result.append("\"");
    for (var i = 0; i < str.length(); i++) {
      var c = str.charAt(i);
      if (Character.isLowerCase(c) || Character.isUpperCase(c) || Character.isDigit(c) || c == '_') { // token
        if (addBlank) {
          result.append(' ');
          addBlank = false;
        }
        result.append(c);
      } else { // special characters
        switch (c) {
          case ' ':
            if (ignoreNextBlank) {
              ignoreNextBlank = false;
            } else {
              addBlank = true;
            }
            break;
          case '\"':
            if (addBlank) {
              result.append(' ');
              addBlank = false;
            }
            result.append("\\\"");
            break;
          case '\\':
            result.append("\\\\");
            addBlank = false;
            ignoreNextBlank = true;
            break;
          default: // operator
            result.append(c);
            addBlank = false;
            ignoreNextBlank = true;
            break;
        }
      }
    }
    result.append("\"");
    return result.toString();
  }

}
