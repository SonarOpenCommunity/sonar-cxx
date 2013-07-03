/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.CommentAnalyser;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.AstScanner;
import com.sonar.sslr.squid.SourceCodeBuilderCallback;
import com.sonar.sslr.squid.SourceCodeBuilderVisitor;
import com.sonar.sslr.squid.SquidAstVisitor;
import com.sonar.sslr.squid.SquidAstVisitorContextImpl;
import com.sonar.sslr.squid.metrics.CommentsVisitor;
import com.sonar.sslr.squid.metrics.ComplexityVisitor;
import com.sonar.sslr.squid.metrics.CounterVisitor;
import com.sonar.sslr.squid.metrics.LinesVisitor;
import org.sonar.cxx.api.CxxGrammar;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.squid.api.SourceClass;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourceFunction;
import org.sonar.squid.api.SourceProject;
import org.sonar.squid.indexer.QueryByType;

import java.io.File;
import java.util.Collection;

public final class CxxAstScanner {

  private CxxAstScanner() {
  }

  /**
   * Helper method for testing checks without having to deploy them on a Sonar instance.
   */
  public static SourceFile scanSingleFile(File file, SquidAstVisitor<CxxGrammar>... visitors) {
    if (!file.isFile()) {
      throw new IllegalArgumentException("File '" + file + "' not found.");
    }
    AstScanner<CxxGrammar> scanner = create(new CxxConfiguration(), visitors);
    scanner.scanFile(file);
    Collection<SourceCode> sources = scanner.getIndex().search(new QueryByType(SourceFile.class));
    if (sources.size() != 1) {
      throw new IllegalStateException("Only one SourceFile was expected whereas " + sources.size() + " has been returned.");
    }
    return (SourceFile) sources.iterator().next();
  }

  public static AstScanner<CxxGrammar> create(CxxConfiguration conf, SquidAstVisitor<CxxGrammar>... visitors) {
    final SquidAstVisitorContextImpl<CxxGrammar> context = new SquidAstVisitorContextImpl<CxxGrammar>(new SourceProject("Cxx Project"));
    final Parser<CxxGrammar> parser = CxxParser.create(context, conf);

    AstScanner.Builder<CxxGrammar> builder = AstScanner.<CxxGrammar> builder(context).setBaseParser(parser);

    /* Metrics */
    builder.withMetrics(CxxMetric.values());

    /* Files */
    builder.setFilesMetric(CxxMetric.FILES);

    /* Comments */
    builder.setCommentAnalyser(
        new CommentAnalyser() {
          @Override
          public boolean isBlank(String line) {
            for (int i = 0; i < line.length(); i++) {
              if (Character.isLetterOrDigit(line.charAt(i))) {
                return false;
              }
            }
            return true;
          }

          @Override
          public String getContents(String comment) {
            return comment.substring(0, 2).equals("/*")
                ? comment.substring(2, comment.length() - 2)
                : comment.substring(2);
          }
        });

    /* Functions */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<CxxGrammar>(new SourceCodeBuilderCallback() {
      public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
        StringBuilder sb = new StringBuilder();
        for (Token token : astNode.findFirstChild(parser.getGrammar().declaratorId).getTokens()) {
          sb.append(token.getValue());
        }
        String functionName = sb.toString();
        SourceFunction function = new SourceFunction(functionName + ":" + astNode.getToken().getLine());
        function.setStartAtLine(astNode.getTokenLine());
        return function;
      }
    }, parser.getGrammar().functionDefinition));

    builder.withSquidAstVisitor(CounterVisitor.<CxxGrammar> builder()
        .setMetricDef(CxxMetric.FUNCTIONS)
        .subscribeTo(parser.getGrammar().functionDefinition)
        .build());

    /* Classes */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<CxxGrammar>(new SourceCodeBuilderCallback() {
      public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
        AstNode classNameAst = astNode.findFirstChild(parser.getGrammar().className);
        String className = classNameAst == null ? "" : classNameAst.getChild(0).getTokenValue();
        SourceClass cls = new SourceClass(className + ":" + astNode.getToken().getLine());
        cls.setStartAtLine(astNode.getTokenLine());
        return cls;
      }
    }, parser.getGrammar().classSpecifier));

    builder.withSquidAstVisitor(CounterVisitor.<CxxGrammar> builder()
        .setMetricDef(CxxMetric.CLASSES)
        .subscribeTo(parser.getGrammar().classSpecifier)
        .build());

    /* Metrics */
    builder.withSquidAstVisitor(new LinesVisitor<CxxGrammar>(CxxMetric.LINES));
    builder.withSquidAstVisitor(new CxxLinesOfCodeVisitor<CxxGrammar>(CxxMetric.LINES_OF_CODE));

    builder.withSquidAstVisitor(CommentsVisitor.<CxxGrammar> builder().withCommentMetric(CxxMetric.COMMENT_LINES)
        .withBlankCommentMetric(CxxMetric.COMMENT_BLANK_LINES)
        .withNoSonar(true)
        .withIgnoreHeaderComment(conf.getIgnoreHeaderComments())
        .build());

    builder.withSquidAstVisitor(CounterVisitor.<CxxGrammar> builder()
        .setMetricDef(CxxMetric.STATEMENTS)
        .subscribeTo(parser.getGrammar().statement)
        .build());

    AstNodeType[] complexityAstNodeType = new AstNodeType[] {
      // Entry points
      parser.getGrammar().functionDefinition,

      CxxKeyword.IF,
      CxxKeyword.FOR,
      CxxKeyword.WHILE,
      CxxKeyword.CATCH,
      CxxKeyword.CASE,
      CxxKeyword.DEFAULT,

      CxxPunctuator.AND,
      CxxPunctuator.OR,
      CxxPunctuator.QUEST
    };
    builder.withSquidAstVisitor(ComplexityVisitor.<CxxGrammar> builder()
        .setMetricDef(CxxMetric.COMPLEXITY)
        .subscribeTo(complexityAstNodeType)
        .build());

    /* External visitors (typically Check ones) */
    for (SquidAstVisitor<CxxGrammar> visitor : visitors) {
      builder.withSquidAstVisitor(visitor);
    }

    return builder.build();
  }

}
