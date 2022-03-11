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

import com.sonar.cxx.sslr.api.Preprocessor;
import com.sonar.cxx.sslr.api.PreprocessorAction;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.Trivia;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sonar.cxx.parser.CxxTokenType;

public class JoinStringsPreprocessor extends Preprocessor {

  private static String stripQuotes(String str) {
    return str.substring(str.indexOf('"') + 1, str.lastIndexOf('"'));
  }

  private static String concatenateStringLiterals(List<Token> concatenatedTokens) {
    var sb = new StringBuilder(256);
    sb.append("\"");
    for (var t : concatenatedTokens) {
      sb.append(stripQuotes(t.getValue()));
    }
    sb.append("\"");
    return sb.toString();
  }

  @Override
  public PreprocessorAction process(List<Token> tokens) {

    var nrOfAdjacentStringLiterals = 0;
    var isGenerated = false;
    for (var t : tokens) {
      if (!CxxTokenType.STRING.equals(t.getType())) {
        break;
      }
      nrOfAdjacentStringLiterals++;
      isGenerated |= t.isGeneratedCode();
    }

    if (nrOfAdjacentStringLiterals < 2) {
      return PreprocessorAction.NO_OPERATION;
    }

    // Concatenate adjacent string literals
    // (C++ Standard, "2.2 Phases of translation, Phase 6")
    var concatenatedTokens = new ArrayList<Token>(tokens.subList(0, nrOfAdjacentStringLiterals));
    String concatenatedLiteral = concatenateStringLiterals(concatenatedTokens);
    Trivia trivia = Trivia.createSkippedText(concatenatedTokens);
    var firstToken = tokens.get(0);
    var tokenToInject = Token.builder()
      .setLine(firstToken.getLine())
      .setColumn(firstToken.getColumn())
      .setURI(firstToken.getURI())
      .setType(CxxTokenType.STRING)
      .setValueAndOriginalValue(concatenatedLiteral)
      .setGeneratedCode(isGenerated).build();

    return new PreprocessorAction(nrOfAdjacentStringLiterals, Collections.singletonList(trivia),
                                  Collections.singletonList(tokenToInject));
  }

}
