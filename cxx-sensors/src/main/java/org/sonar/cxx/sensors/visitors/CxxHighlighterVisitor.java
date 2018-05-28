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
package org.sonar.cxx.sensors.visitors;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.squidbridge.SquidAstVisitor;

public class CxxHighlighterVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private static final Logger LOG = Loggers.get(CxxHighlighterVisitor.class);

  private NewHighlighting newHighlighting;
  private final SensorContext context;

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

  private static class CommentLocation extends TokenLocation {

    public CommentLocation(Token token) {
      super(token);
      String value = token.getValue();
      String[] lines = CxxUtils.EOLPattern.split(value, -1);

      if (lines.length > 1) {
        endLine = token.getLine() + lines.length - 1;
        endLineOffset = lines[lines.length - 1].length();
      }
    }
  }

  private static class PreprocessorDirectiveLocation extends TokenLocation {

    public static final Pattern preprocessorPattern = Pattern.compile("^[ \t]*#[ \t]*\\w+");

    PreprocessorDirectiveLocation(Token token) {
      super(token);
      Matcher m = preprocessorPattern.matcher(token.getValue());
      if (m.find()) {
        endLineOffset = startLineOffset + (m.end() - m.start());
      } else {
        endLineOffset = startLineOffset;
      }
    }
  }

  public CxxHighlighterVisitor(SensorContext context) {
    this.context = context;
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    newHighlighting = context.newHighlighting();
    InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates()
      .is(getContext().getFile().getAbsoluteFile()));
    if (inputFile != null) {
      newHighlighting.onFile(inputFile);
    }
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    try {
      newHighlighting.save();
    } catch (IllegalStateException e) {
      // ignore highlight errors: parsing errors could lead to wrong location data
      LOG.debug("Highligthing error in file: {}, error: {}", getContext().getFile().getAbsoluteFile(), e);
    }
  }


  private Optional<Trivia> getTriviaWithConcatenatedLiterals(Token stringToken) {
    return stringToken.getTrivia().stream()
        .filter(t -> t.isSkippedText() && CxxTokenType.STRING.equals(t.getToken().getType())).findFirst();
  }

  @Override
  public void visitToken(Token token) {
    if (!token.isGeneratedCode()) {
      TokenLocation last = null;
      if (token.getType().equals(CxxTokenType.NUMBER)) {
        last = highlight(last, new TokenLocation(token), TypeOfText.CONSTANT);
      } else if (token.getType() instanceof CxxKeyword) {
        last = highlight(last, new TokenLocation(token), TypeOfText.KEYWORD);
      } else if (token.getType().equals(CxxTokenType.CHARACTER)) {
        last = highlight(last, new TokenLocation(token), TypeOfText.STRING);
      } else if (token.getType().equals(CxxTokenType.STRING)) {
        Optional<Trivia> triviaWithConcatenatedLiterals = getTriviaWithConcatenatedLiterals(token);
        if (!triviaWithConcatenatedLiterals.isPresent()) {
          last = highlight(last, new TokenLocation(token), TypeOfText.STRING);
        } else {
          for (Token concatenatedLiterals : triviaWithConcatenatedLiterals.get().getTokens()) {
            last = highlight(last, new TokenLocation(concatenatedLiterals), TypeOfText.STRING);
          }
        }
      }

      for (Trivia trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          highlight(last, new CommentLocation(trivia.getToken()), TypeOfText.COMMENT);
        } else if (trivia.isSkippedText()
          && trivia.getToken().getType().equals(CxxTokenType.PREPROCESSOR)) {
          highlight(last, new PreprocessorDirectiveLocation(trivia.getToken()), TypeOfText.PREPROCESS_DIRECTIVE);
        }
      }
    }
  }

  private TokenLocation highlight(TokenLocation last, TokenLocation current, TypeOfText typeOfText) {
    try {
      if (!current.overlaps(last)) {
        newHighlighting.highlight(current.startLine(), current.startLineOffset(),
          current.endLine(), current.endLineOffset(), typeOfText);
      }
    } catch (IllegalArgumentException ex) {
      // ignore highlight errors: parsing errors could lead to wrong location data
      LOG.warn("Highligthing error in file '{}' at line:{}, column:{}", getContext().getFile().getAbsoluteFile(),
        current.startLine(), current.startLineOffset());
      if (LOG.isDebugEnabled()) {
        LOG.debug("highlighing exception {}", ex);
      }
    }
    return current;
  }

}
