/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
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
package org.sonar.plugins.cxx.highlighter;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.Lexer;
import org.sonar.api.source.Highlightable;
import org.sonar.cxx.CxxConfiguration;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.lexer.CxxLexer;

/* for CSS see https://github.com/SonarSource/sonarqube/blob/master/sonar-colorizer/src/main/resources/sonar-colorizer.css */
public class CxxHighlighter {

  private Lexer lexer;
  private Charset charset;

  public CxxHighlighter(CxxConfiguration conf) {
    this.lexer = CxxLexer.create(conf);
    this.charset = conf.getCharset();
  }

  public void highlight(Highlightable highlightable, File file) {
    SourceFileOffsets offsets = new SourceFileOffsets(file, charset);
    List<Token> tokens = lexer.lex(file);
    doHighlight(highlightable, tokens, offsets);
  }

  public void highlight(Highlightable highlightable, String string) {
    SourceFileOffsets offsets = new SourceFileOffsets(string);
    List<Token> tokens = lexer.lex(string);
    doHighlight(highlightable, tokens, offsets);
  }

  private void doHighlight(Highlightable highlightable, List<Token> tokens, SourceFileOffsets offsets) {
    Highlightable.HighlightingBuilder highlighting = highlightable.newHighlighting();
    highlightNonComments(highlighting, tokens, offsets);
    highlightComments(highlighting, tokens, offsets);
    highlighting.done();
  }

  private void highlightComments(Highlightable.HighlightingBuilder highlighting, List<Token> tokens, SourceFileOffsets offsets) {
    String code;
    for (Token token : tokens) {
      if (!token.getTrivia().isEmpty()) {
        for (Trivia trivia : token.getTrivia()) {
          if (trivia.getToken().getValue().startsWith("/**")) {
            code = "j"; // javadoc
          } else {
            code = "cd"; // classic comment
          }
          highlight(highlighting, offsets.startOffset(trivia.getToken()), offsets.endOffset(trivia.getToken()), code);
        }
      }
    }
  }

  private void highlightNonComments(Highlightable.HighlightingBuilder highlighting, List<Token> tokens, SourceFileOffsets offsets) {
    for (Token token : tokens) {
      if (CxxTokenType.STRING.equals(token.getType())) {
        highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "s"); // string
      }
      if (isConstant(token.getType())) {
        highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "c"); // constants
      }
      if (isPreprocessingDirective(token.getType())) {
        highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "p"); // preprocessing directive
      }
      if (isKeyword(token.getType())) {
        highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "k"); // keyword
      }
    }
  }

  private static void highlight(Highlightable.HighlightingBuilder highlighting, int startOffset, int endOffset, String code) {
    if (endOffset > startOffset) {
      highlighting.highlight(startOffset, endOffset, code);
    }
  }

  private boolean isConstant(TokenType type) {
    return CxxTokenType.NUMBER.equals(type)
      || CxxTokenType.CHARACTER.equals(type);
  }
  
  private boolean isPreprocessingDirective(TokenType type) {
    return CxxTokenType.PREPROCESSOR.equals(type)
      || CxxTokenType.PREPROCESSOR_DEFINE.equals(type)
      || CxxTokenType.PREPROCESSOR_INCLUDE.equals(type)
      || CxxTokenType.PREPROCESSOR_IFDEF.equals(type)
      || CxxTokenType.PREPROCESSOR_IFNDEF.equals(type)
      || CxxTokenType.PREPROCESSOR_IF.equals(type)
      || CxxTokenType.PREPROCESSOR_ELSE.equals(type)
      || CxxTokenType.PREPROCESSOR_ENDIF.equals(type);
  }

  private boolean isKeyword(TokenType type) {
    for (TokenType keywordType : CxxKeyword.values()) {
      if (keywordType.equals(type)) {
        return true;
      }
    }
    return false;
  }
}
