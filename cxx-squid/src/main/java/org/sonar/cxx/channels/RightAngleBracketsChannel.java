/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

/**
 * Solving the problem amounts to decreeing that under some circumstances a >> token is treated as
 * two right angle brackets instead of a right shift operator.
 *
 * According to Document number: N1757 05-0017, Right Angle Brackets (Revision 2)
 *
 * Decree that if a left angle bracket is active (i.e. not yet matched by a right angle bracket)
 * the >> token is treated as two right angle brackets instead of a shift operator, except within
 * - parentheses or
 * - brackets that are themselves within the angle brackets.
 *
 * A<(X>Y)> a; // The first > token appears within parentheses and
 *             // therefore is not a right angle bracket. The second one
 *             // is a right angle bracket because a left angle bracket
 *             // is active and no parentheses are more recently active.
 */
public class RightAngleBracketsChannel extends Channel<Lexer> {

  private int angleBracketLevel = 0; // angle brackets <  >
  private int parentheseLevel = 0;   // parentheses / round brackets ( )

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    var consumed = false;

    switch (code.charAt(0)) {
      case '(':
        if (angleBracketLevel > 0) {
          parentheseLevel++;
        }
        break;
      case ')':
        if (parentheseLevel > 0) {
          parentheseLevel--;
        }
        break;
      case ';': // end of expression => reset
        angleBracketLevel = 0;
        parentheseLevel = 0;
        break;

      case '<':
        if (parentheseLevel == 0) {
          var next = code.charAt(1);
          if ((next != '<') && (next != '=')) { // not <<, <=, <<=, <=>
            angleBracketLevel++;
          }
        }
        break;

      case '>':
        if (angleBracketLevel > 0) {
          var consume = parentheseLevel == 0;
          var next = code.charAt(1);
          consume = consume && (next != '='); // not >=
          consume = consume && !((next == '>') && (angleBracketLevel == 1)); // not dangling >>

          if (consume) {
            output.addToken(Token.builder()
              .setLine(code.getLinePosition())
              .setColumn(code.getColumnPosition())
              .setURI(output.getURI())
              .setValueAndOriginalValue(">")
              .setType(CxxPunctuator.GT)
              .build());
            code.pop();

            angleBracketLevel--;
            consumed = true;
          }
        }
        break;

      default:
        break;
    }

    return consumed;
  }

}
