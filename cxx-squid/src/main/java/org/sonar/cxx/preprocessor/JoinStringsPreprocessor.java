/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonar.cxx.api.CxxTokenType;

//@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.api.PreprocessorAction;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;

// @todo deprecated PreprocessorAction
public class JoinStringsPreprocessor extends Preprocessor {

  @Override
  // @todo deprecated PreprocessorAction
  public PreprocessorAction process(List<Token> tokens) {

    int nrOfAdjacentStringLiterals = 0;
    boolean isGenerated = false;
    for (Token t : tokens) {
      if (!CxxTokenType.STRING.equals(t.getType())) {
        break;
      }
      nrOfAdjacentStringLiterals++;
      isGenerated |= t.isGeneratedCode();
    }

    if (nrOfAdjacentStringLiterals < 2) {
      // @todo deprecated PreprocessorAction
      return PreprocessorAction.NO_OPERATION;
    }

    // Concatenate adjacent string literals
    // (C++ Standard, "2.2 Phases of translation, Phase 6")
    List<Token> concatenatedTokens = new ArrayList<>(tokens.subList(0, nrOfAdjacentStringLiterals));
    String concatenatedLiteral = concatenateStringLiterals(concatenatedTokens);
    Trivia trivia = Trivia.createSkippedText(concatenatedTokens);
    Token firstToken = tokens.get(0);
    Token tokenToInject = Token.builder().setLine(firstToken.getLine()).setColumn(firstToken.getColumn())
        .setURI(firstToken.getURI()).setType(CxxTokenType.STRING).setValueAndOriginalValue(concatenatedLiteral)
        .setGeneratedCode(isGenerated).build();

    // @todo deprecated PreprocessorAction
    return new PreprocessorAction(nrOfAdjacentStringLiterals, Collections.singletonList(trivia),
        Collections.singletonList(tokenToInject));
  }

  private static String stripQuotes(String str) {
    return str.substring(str.indexOf('"') + 1, str.lastIndexOf('"'));
  }

  private static String concatenateStringLiterals(List<Token> concatenatedTokens) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    for (Token t : concatenatedTokens) {
      sb.append(stripQuotes(t.getValue()));
    }
    sb.append("\"");
    return sb.toString();
  }
}
