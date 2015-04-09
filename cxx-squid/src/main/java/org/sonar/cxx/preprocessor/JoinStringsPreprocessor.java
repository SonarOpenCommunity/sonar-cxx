/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx.preprocessor;

import java.util.Collections;
import java.util.List;

import org.sonar.cxx.api.CxxTokenType;

import com.google.common.collect.Lists;
import com.sonar.sslr.api.Preprocessor; //@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import com.sonar.sslr.api.PreprocessorAction; //@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import com.sonar.sslr.api.Token;

public class JoinStringsPreprocessor extends Preprocessor {
  @Override
  public PreprocessorAction process(List<Token> tokens) {
    Token token = tokens.get(0);

    if (token.getType() == CxxTokenType.STRING) {

      // Joining string literals (C++ Standard, "2.2 Phases of translation, Phase 6")

      int numberOfStrings = 1;
      StringBuilder sb = new StringBuilder();

      for (;;) {
        Token nextToken = tokens.get(numberOfStrings);
        if (nextToken.getType() != CxxTokenType.STRING) {
          break;
        }
        sb.append(stripQuotes(nextToken.getValue()));
        numberOfStrings++;
      }

      if (numberOfStrings > 1) {
        List<Token> tokensToInject = Lists.newArrayList();
        tokensToInject.add(
            Token.builder()
                .setLine(token.getLine())
                .setColumn(token.getColumn())
                .setURI(token.getURI())
                .setType(CxxTokenType.STRING)
                .setValueAndOriginalValue("\"" + stripQuotes(token.getValue()) + sb.toString() + "\"")
                .build()
            );
        return new PreprocessorAction(numberOfStrings, Collections.EMPTY_LIST, tokensToInject);
      }

      return PreprocessorAction.NO_OPERATION;
    }
    return PreprocessorAction.NO_OPERATION;
  }

  private String stripQuotes(String str) {
    return str.substring(1, str.length() - 1);
  }
}
