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

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Helper class to evaluate preprocessor numbers.
 */
final class PPNumber {

  private static HashMap<String, BigInteger> numberCache = new HashMap<>();

  private PPNumber() {

  }

  static BigInteger decode(String number) {

    // search first in already cached results
    BigInteger result = numberCache.get(number);
    if (result != null) {
      return result;
    }

    // This part is only responsible for providing a string and a radix to BigInteger.
    // The lexer ensures that the number has a valid format.
    var radix = 10;
    var begin = 0;
    if ((number.length() > 2) && (number.charAt(0) == '0')) {
      switch (number.charAt(1)) {
        case 'x':
        case 'X':
          radix = 16; // 0x...
          begin = 2;
          break;
        case 'b':
        case 'B':
          radix = 2; // 0b...
          begin = 2;
          break;
        default:
          radix = 8; // 0...
          break;
      }
    }

    var sb = new StringBuilder(number.length());
    var suffix = false;
    for (var index = begin; index < number.length() && !suffix; index++) {
      var c = number.charAt(index);
      switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':

        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':

        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
          sb.append(c);
          break;

        case '\'': // ignore digit separator
          break;

        default: // suffix
          suffix = true;
          break;
      }
    }

    // use ctor to convert to BigInteger (expensive)
    result = new BigInteger(sb.toString(), radix);

    // cache result
    numberCache.put(number, result);
    return result;
  }

}
