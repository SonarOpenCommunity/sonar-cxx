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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ## operator between any two successive identifiers in the replacement-list runs parameter replacement on the two
 * identifiers (which are not macro-expanded first) and then concatenates the result. This operation is called
 * "concatenation" or "token pasting". Only tokens that form a valid token together may be pasted: identifiers that form
 * a longer identifier, digits that form a number, or operators + and = that form a +=. A comment cannot be created by
 * pasting / and * because comments are removed from text before macro substitution is considered.
 */
final class PPConcatenation {

  private static final Logger LOG = LoggerFactory.getLogger(PPConcatenation.class);

  private PPConcatenation() {

  }

  static List<Token> concatenate(List<Token> tokens) {
    int firstIndex = 0;
    while (true) {
      int operatorPos = searchOperatorPos(firstIndex, tokens); // ... ## ...

      if (operatorPos == -1) {
        // this function is called for every parameter replacement, the case without concatenation should be fast:
        // return list without any changes
        return tokens;
      }

      var result = new ArrayList<Token>(tokens.size());
      int leftPos = searchLeftPos(operatorPos, tokens); // token ## ...
      int rightPos = searchRightPos(operatorPos, tokens); // ... ## token

      if (leftPos != -1 && rightPos != -1) {
        var left = tokens.subList(0, leftPos);
        var right = tokens.subList(rightPos + 1, tokens.size());

        var leftToken = tokens.get(leftPos);
        var rightToken = tokens.get(rightPos);
        var concatenated = PPGeneratedToken.build( // a ## b ==> ab
          leftToken, leftToken.getType(), leftToken.getValue() + rightToken.getValue()
        );

        result.addAll(left);
        result.add(concatenated);
        result.addAll(right);

        firstIndex = result.size() - right.size();

      } else { // error, remove ## and continue
        var token = tokens.get(operatorPos);
        LOG.warn("concatenation error, ignoring ## at {}:{}:{}",
          token.getURI(), token.getLine(), token.getColumn());

        var left = tokens.subList(0, operatorPos);
        var right = tokens.subList(operatorPos + 1, tokens.size());

        result.addAll(left);
        result.addAll(right);

        firstIndex = result.size() - right.size();
      }

      tokens = result;
    }
  }

  /**
   * search for ## operator
   */
  private static int searchOperatorPos(int firstIndex, List<Token> tokens) {
    int operatorPos = -1;
    for (int i = firstIndex; i < tokens.size(); i++) {
      if (PPPunctuator.HASHHASH.equals(tokens.get(i).getType())) {
        operatorPos = i;
        break;
      }
    }
    return operatorPos;
  }

  /**
   * search left operand (skipping blanks)
   */
  private static int searchLeftPos(int firstIndex, List<Token> tokens) {
    int leftPos = -1;
    for (int i = firstIndex - 1; i >= 0; i--) {
      if (isToken(tokens.get(i))) {
        leftPos = i;
        break;
      }
    }
    return leftPos;
  }

  /**
   * search right operand (skipping blanks)
   */
  private static int searchRightPos(int firstIndex, List<Token> tokens) {
    int rightPos = -1;
    for (int i = firstIndex + 1; i < tokens.size(); i++) {
      if (isToken(tokens.get(i))) {
        rightPos = i;
        break;
      }
    }
    return rightPos;
  }

  private static boolean isToken(Token token) {
    TokenType type = token.getType();
    return !(GenericTokenType.EOF.equals(type) || PPPunctuator.HASHHASH.equals(type)); // a ## ## ## b => ab
  }

}
