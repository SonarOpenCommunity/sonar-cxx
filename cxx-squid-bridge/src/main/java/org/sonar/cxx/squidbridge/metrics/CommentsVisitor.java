/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.metrics;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.util.HashSet;
import java.util.Set;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.measures.MetricDef;

/**
 * Visitor that computes the number of lines of comments and the number of empty lines of comments.
 */
public final class CommentsVisitor<G extends Grammar> extends SquidAstVisitor<G> implements AstAndTokenVisitor {

  private Set<Integer> noSonar;
  private Set<Integer> comments;
  private boolean seenFirstToken;

  private final boolean enableNoSonar;
  private final MetricDef commentMetric;
  private final boolean ignoreHeaderComments;

  private CommentsVisitor(CommentsVisitorBuilder<G> builder) {
    this.enableNoSonar = builder.enableNoSonar;
    this.commentMetric = builder.commentMetric;
    this.ignoreHeaderComments = builder.ignoreHeaderComments;
  }

  private void addNoSonar(int line) {
    comments.remove(line);
    noSonar.add(line);
  }

  private void addCommentLine(int line) {
    if (!noSonar.contains(line)) {
      comments.add(line);
    }
  }

  @Override
  public void visitFile(AstNode astNode) {
    noSonar = new HashSet<Integer>();
    comments = new HashSet<Integer>();
    seenFirstToken = false;
  }

  @Override
  public void visitToken(Token token) {
    if (!ignoreHeaderComments || seenFirstToken) {
      for (var trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          String[] commentLines = getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue())
            .split("(\r)?\n|\r", -1);
          int line = trivia.getToken().getLine();

          for (var commentLine : commentLines) {
            if (enableNoSonar && commentLine.contains("NOSONAR")) {
              addNoSonar(line);
            } else if (commentMetric != null && !getContext().getCommentAnalyser().isBlank(commentLine)) {
              addCommentLine(line);
            }

            line++;
          }
        }
      }
    }

    seenFirstToken = true;
  }

  @Override
  public void leaveFile(AstNode astNode) {
    if (enableNoSonar) {
      ((SourceFile) getContext().peekSourceCode()).addNoSonarTagLines(noSonar);
    }
    if (commentMetric != null) {
      getContext().peekSourceCode().add(commentMetric, comments.size());
    }
  }

  public static <G extends Grammar> CommentsVisitorBuilder<G> builder() {
    return new CommentsVisitorBuilder<G>();
  }

  public static final class CommentsVisitorBuilder<G extends Grammar> {

    private boolean enableNoSonar = false;
    private MetricDef commentMetric;
    private boolean ignoreHeaderComments = false;

    private CommentsVisitorBuilder() {
    }

    public CommentsVisitor<G> build() {
      return new CommentsVisitor<G>(this);
    }

    public CommentsVisitorBuilder<G> withNoSonar(boolean enableNoSonar) {
      this.enableNoSonar = enableNoSonar;
      return this;
    }

    public CommentsVisitorBuilder<G> withCommentMetric(MetricDef commentMetric) {
      this.commentMetric = commentMetric;
      return this;
    }

    public CommentsVisitorBuilder<G> withIgnoreHeaderComment(boolean ignoreHeaderComments) {
      this.ignoreHeaderComments = ignoreHeaderComments;
      return this;
    }

  }

}
