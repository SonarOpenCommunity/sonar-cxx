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

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import java.util.ArrayList;
import java.util.List;
import org.sonar.cxx.parser.CxxLexerPool;

final class TokenList {

  private static Lexer lexer = null;

  private TokenList() {

  }

  /**
   * Adjust token positions to new position.
   */
  static List<Token> adjustPosition(List<Token> tokens, Token newPos) {
    var result = new ArrayList<Token>(tokens.size());
    int column = newPos.getColumn();
    int line = newPos.getLine();
    var uri = newPos.getURI();
    for (var token : tokens) {
      result.add(PPGeneratedToken.build(token, uri, line, column));
      column += token.getValue().length() + 1;
    }

    return result;
  }

  /**
   * Map preprocessor tokens to corresponding cxx tokens.
   */
  static List<Token> transformToCxx(List<Token> ppTokens, Token newPos) {
    List<Token> result = new ArrayList<>(ppTokens.size());

    if (lexer == null) { // lazy initialization
      lexer = CxxLexerPool.create().getLexer();
    }

    for (var ppToken : ppTokens) {
      String value = ppToken.getValue();
      if (!"EOF".equals(value) && !value.isBlank()) {

        // call CXX lexer to create a CXX newPos
        List<Token> cxxTokens = lexer.lex(value);

        var cxxToken = Token.builder()
          .setLine(newPos.getLine() + ppToken.getLine() - 1)
          .setColumn(newPos.getColumn() + ppToken.getColumn())
          .setURI(ppToken.getURI())
          .setValueAndOriginalValue(ppToken.getValue())
          .setType(cxxTokens.get(0).getType())
          .build();

        result.add(cxxToken);
      }
    }

    return result;
  }

}
