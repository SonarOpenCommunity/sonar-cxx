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

import com.sonar.cxx.sslr.api.Token;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.parser.CxxTokenType;

/**
 * A ## operator between any two successive identifiers in the replacement-list runs parameter replacement on the two
 * identifiers (which are not macro-expanded first) and then concatenates the result. This operation is called
 * "concatenation" or "token pasting". Only tokens that form a valid token together may be pasted: identifiers that form
 * a longer identifier, digits that form a number, or operators + and = that form a +=. A comment cannot be created by
 * pasting / and * because comments are removed from text before macro substitution is considered.
 */
class PPConcatenation {

  private static final Logger LOG = Loggers.get(PPConcatenation.class);

  private PPConcatenation() {

  }

  static List<Token> concatenate(List<Token> tokens) {
    var newTokens = new ArrayList<Token>();

    Iterator<Token> it = tokens.iterator();
    while (it.hasNext()) {
      var curr = it.next();
      if ("##".equals(curr.getValue())) {
        var pred = predConcatToken(newTokens);
        var succ = succConcatToken(it);
        if (pred != null && succ != null) {
          newTokens.add(PPGeneratedToken.build(pred, pred.getType(), pred.getValue() + succ.getValue()));
        } else {
          LOG.error("Missing data : succ ='{}' or pred = '{}'", succ, pred);
        }
      } else {
        newTokens.add(curr);
      }
    }

    return newTokens;
  }

  @CheckForNull
  private static Token predConcatToken(List<Token> tokens) {
    while (!tokens.isEmpty()) {
      var last = tokens.remove(tokens.size() - 1);
      if (!last.getType().equals(CxxTokenType.WS)) {
        if (!tokens.isEmpty()) {
          var pred = tokens.get(tokens.size() - 1);
          if (!pred.getType().equals(CxxTokenType.WS) && !pred.hasTrivia()) {
            // Needed to paste tokens 0 and x back together after #define N(hex) 0x ## hex
            tokens.remove(tokens.size() - 1);
            last = PPGeneratedToken.build(pred, pred.getType(), pred.getValue() + last.getValue());
          }
        }
        return last;
      }
    }
    return null;
  }

  @CheckForNull
  private static Token succConcatToken(Iterator<Token> it) {
    Token succ = null;
    while (it.hasNext()) {
      succ = it.next();
      if (!"##".equals(succ.getValue()) && !succ.getType().equals(CxxTokenType.WS)) {
        break;
      }
    }
    return succ;
  }

}
