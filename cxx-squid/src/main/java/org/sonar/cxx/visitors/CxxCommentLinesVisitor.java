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
package org.sonar.cxx.visitors;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import java.util.HashSet;
import java.util.Set;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.SourceCode;

/**
 * CxxCommentLinesVisitor
 *
 * @param <GRAMMAR>
 *
 */
public class CxxCommentLinesVisitor<GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR>
  implements AstAndTokenVisitor {

  private final Set<Integer> comments = new HashSet<>();
  private boolean seenFirstToken;

  @Override
  public void init() {
    subscribeTo(CxxPunctuator.CURLBR_RIGHT);
  }

  @Override
  public void visitFile(AstNode astNode) {
    comments.clear();
    seenFirstToken = false;
  }

  @Override
  public void visitToken(Token token) {
    for (Trivia trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        if (seenFirstToken) {
          String[] commentLines = getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue())
            .split("(\r)?\n|\r", -1);
          int line = trivia.getToken().getLine();
          for (String commentLine : commentLines) {
            if (!commentLine.contains("NOSONAR") && !getContext().getCommentAnalyser().isBlank(commentLine)) {
              comments.add(line);
            }
            line++;
          }
        } else {
          seenFirstToken = true;
        }
      }
    }
    seenFirstToken = true;
  }

  @Override
  public void leaveNode(AstNode astNode) {
    SourceCode sourceCode = getContext().peekSourceCode();
    int commentlines = 0;
    for (int line = sourceCode.getStartAtLine(); line <= sourceCode.getEndAtLine(); line++) {
      if (comments.contains(line)) {
        commentlines++;
      }
    }
    sourceCode.setMeasure(CxxMetric.COMMENT_LINES, commentlines);
  }

  @Override
  public void leaveFile(AstNode ast) {
    getContext().peekSourceCode().setMeasure(CxxMetric.COMMENT_LINES, comments.size());
    comments.clear();
  }

}
