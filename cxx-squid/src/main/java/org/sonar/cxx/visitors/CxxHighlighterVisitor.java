/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstAndTokenVisitor;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.Trivia;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.cxx.squidbridge.SquidAstVisitor;

public class CxxHighlighterVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private static final Pattern EOL_PATTERN = Pattern.compile("\\R");
  private List<Highlight> highlighting = null;

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    highlighting = new ArrayList<>();
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    getContext().peekSourceCode().addData(CxxMetric.HIGHLIGTHING_DATA, highlighting);
    highlighting = null;
  }

  @Override
  public void visitToken(Token token) {
    if (!token.isGeneratedCode()) {
      TokenLocation last = null;
      if (token.getType().equals(CxxTokenType.NUMBER)) {
        last = highlight(last, new TokenLocation(token), "c");
      } else if (token.getType() instanceof CxxKeyword) {
        last = highlight(last, new TokenLocation(token), "k");
      } else if (token.getType().equals(CxxTokenType.CHARACTER)) {
        last = highlight(last, new TokenLocation(token), "s");
      } else if (token.getType().equals(CxxTokenType.STRING)) {
        Optional<Trivia> triviaWithConcatenatedLiterals = getTriviaWithConcatenatedLiterals(token);
        if (!triviaWithConcatenatedLiterals.isPresent()) {
          last = highlight(last, new StringLocation(token), "s");
        } else {
          for (var concatenatedLiterals : triviaWithConcatenatedLiterals.get().getTokens()) {
            last = highlight(last, new StringLocation(concatenatedLiterals), "s");
          }
        }
      }

      for (var trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          highlight(last, new CommentLocation(trivia.getToken()), "cd");
        } else if (trivia.isSkippedText() && trivia.getToken().getType().equals(CxxTokenType.PREPROCESSOR)) {
          highlight(last, new PreprocessorDirectiveLocation(trivia.getToken()), "p");
        }
      }
    }
  }

  private Optional<Trivia> getTriviaWithConcatenatedLiterals(Token stringToken) {
    return stringToken.getTrivia().stream()
      .filter(t -> t.isSkippedText() && CxxTokenType.STRING.equals(t.getToken().getType())).findFirst();
  }

  private TokenLocation highlight(@Nullable TokenLocation last, TokenLocation current, String typeOfText) {
    if (!current.overlaps(last)) {
      highlighting.add(new Highlight(current.startLine(), current.startLineOffset(),
                                     current.endLine(), current.endLineOffset(), typeOfText));
    }

    return current;
  }

  public static class Highlight {

    public final int startLine;
    public final int startLineOffset;
    public final int endLine;
    public final int endLineOffset;
    public final String typeOfText;

    Highlight(int startLine, int startLineOffset, int endLine, int endLineOffset, String typeOfText) {
      this.startLine = startLine;
      this.startLineOffset = startLineOffset;
      this.endLine = endLine;
      this.endLineOffset = endLineOffset;
      this.typeOfText = typeOfText;
    }
  };

  private static class TokenLocation {

    protected int startLine;
    protected int startLineOffset;
    protected int endLine;
    protected int endLineOffset;

    public TokenLocation(Token token) {
      startLine = token.getLine();
      startLineOffset = token.getColumn();
      endLine = this.startLine;
      endLineOffset = startLineOffset + token.getValue().length();
    }

    public int startLine() {
      return startLine;
    }

    public int startLineOffset() {
      return startLineOffset;
    }

    public int endLine() {
      return endLine;
    }

    public int endLineOffset() {
      return endLineOffset;
    }

    public boolean overlaps(@Nullable TokenLocation other) {
      if (other != null) {
        return !(startLineOffset() > other.endLineOffset()
                 || other.startLineOffset() > endLineOffset()
                 || startLine() > other.endLine()
                 || other.startLine() > endLine());
      }
      return false;
    }

  }

  private static class StringLocation extends TokenLocation {

    public StringLocation(Token token) {
      super(token);
      String value = token.getValue();
      if (value.startsWith("R")) { // Raw String?
        String[] lines = EOL_PATTERN.split(value, -1);

        if (lines.length > 1) {
          endLine = token.getLine() + lines.length - 1;
          endLineOffset = lines[lines.length - 1].length();
        }
      }
    }
  }

  private static class CommentLocation extends TokenLocation {

    public CommentLocation(Token token) {
      super(token);
      String value = token.getValue();
      String[] lines = EOL_PATTERN.split(value, -1);

      if (lines.length > 1) {
        endLine = token.getLine() + lines.length - 1;
        endLineOffset = lines[lines.length - 1].length();
      }
    }
  }

  private static class PreprocessorDirectiveLocation extends TokenLocation {

    public static final Pattern PREPROCESSOR_PATTERN = Pattern.compile("^[ \t]*#[ \t]*\\w+");

    PreprocessorDirectiveLocation(Token token) {
      super(token);
      var m = PREPROCESSOR_PATTERN.matcher(token.getValue());
      if (m.find()) {
        endLineOffset = startLineOffset + (m.end() - m.start());
      } else {
        endLineOffset = startLineOffset;
      }
    }
  }

}
