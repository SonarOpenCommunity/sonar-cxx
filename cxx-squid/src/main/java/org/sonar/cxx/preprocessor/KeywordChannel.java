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

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.impl.Lexer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.sonar.cxx.api.CppPunctuator.HASH;
import org.sonar.sslr.channel.Channel;
import org.sonar.sslr.channel.CodeReader;

public class KeywordChannel extends Channel<Lexer> {

  private final Map<String, TokenType> keywordsMap = new HashMap<>();
  private final StringBuilder tmpBuilder = new StringBuilder();
  private final Matcher matcher;
  private final Token.Builder tokenBuilder = Token.builder();

  public KeywordChannel(String regexp, TokenType[]... keywordSets) {
    for (TokenType[] keywords : keywordSets) {
      for (TokenType keyword : keywords) {
        keywordsMap.put(keyword.getValue(), keyword);
      }
    }
    matcher = Pattern.compile(regexp).matcher("");
  }

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    if (code.popTo(matcher, tmpBuilder) > 0) {
      String word = tmpBuilder.toString();
      tmpBuilder.delete(0, tmpBuilder.length());

      // do this work to strip potential whitespace between the hash and the directive
      String identifier = word.substring(1, word.length()).trim();
      String potentialKeyword = HASH.getValue() + identifier;

      TokenType keywordType = keywordsMap.get(potentialKeyword);
      if (keywordType != null) {
        Token token = tokenBuilder
          .setType(keywordType)
          .setValueAndOriginalValue(potentialKeyword)
          .setURI(lexer.getURI())
          .setLine(code.getPreviousCursor().getLine())
          .setColumn(code.getPreviousCursor().getColumn())
          .build();

        lexer.addToken(token);
      } else {
        // if its not a keyword, then it is a sequence of a hash followed by an identifier
        lexer.addToken(tokenBuilder
          .setType(HASH)
          .setValueAndOriginalValue(HASH.getValue())
          .setURI(lexer.getURI())
          .setLine(code.getPreviousCursor().getLine())
          .setColumn(code.getPreviousCursor().getColumn())
          .build());
        lexer.addToken(tokenBuilder
          .setType(IDENTIFIER)
          .setValueAndOriginalValue(identifier)
          .setURI(lexer.getURI())
          .setLine(code.getPreviousCursor().getLine())
          .setColumn(code.getPreviousCursor().getColumn())
          .build());
      }

      return true;
    }
    return false;
  }
}
