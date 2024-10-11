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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstAndTokenVisitor;
import com.sonar.cxx.sslr.api.AstNode;
import static com.sonar.cxx.sslr.api.GenericTokenType.EOF;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.Trivia;
import java.util.regex.Pattern;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceFile;

/**
 * Visitor that computes the number of lines of code of a file.
 *
 * @param <GRAMMAR>
 */
public class CxxLinesOfCodeVisitor<GRAMMAR extends Grammar>
  extends SquidAstVisitor<GRAMMAR> implements AstAndTokenVisitor {

  public static final Pattern EOL_PATTERN = Pattern.compile("\\R");

  private int lastTokenLine;

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitFile(AstNode node) {
    lastTokenLine = -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitToken(Token token) {
    if (token.getType().equals(EOF)) {
      return;
    }

    // handle all the lines of the token
    String[] tokenLines = EOL_PATTERN.split(token.getValue(), -1);

    int firstLineAlreadyCounted = lastTokenLine == token.getLine() ? 1 : 0;
    getContext().peekSourceCode().add(CxxMetric.LINES_OF_CODE, (double) tokenLines.length - firstLineAlreadyCounted);

    lastTokenLine = token.getLine() + tokenLines.length - 1;

    // handle comments
    for (var trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        visitComment(trivia);
      }
    }
  }

  /**
   * Search in comments for NOSONAR
   */
  public void visitComment(Trivia trivia) {
    String[] commentLines = EOL_PATTERN
      .split(getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue()), -1);
    int line = trivia.getToken().getLine();

    for (var commentLine : commentLines) {
      if (commentLine.contains("NOSONAR")) {
        SourceCode sourceCode = getContext().peekSourceCode();
        if (sourceCode instanceof SourceFile sourceFile) {
          sourceFile.hasNoSonarTagAtLine(line);
        }
      }
      line++;
    }
  }

}
