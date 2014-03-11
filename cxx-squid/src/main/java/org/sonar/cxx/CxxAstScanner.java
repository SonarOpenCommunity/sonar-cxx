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
import com.sonar.sslr.api.Grammar;

import com.sonar.sslr.squid.AstScanner;
import com.sonar.sslr.squid.SourceCodeBuilderCallback;
import com.sonar.sslr.squid.SourceCodeBuilderVisitor;
import com.sonar.sslr.squid.SquidAstVisitor;
import com.sonar.sslr.squid.SquidAstVisitorContextImpl;
import com.sonar.sslr.squid.metrics.CommentsVisitor;
import com.sonar.sslr.squid.metrics.ComplexityVisitor;
import com.sonar.sslr.squid.metrics.CounterVisitor;
import com.sonar.sslr.squid.metrics.LinesVisitor;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.cxx.visitors.CxxFileVisitor;
import org.sonar.cxx.visitors.CxxLinesOfCodeVisitor;
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
  public static SourceFile scanSingleFile(File file, SquidAstVisitor<Grammar>... visitors) {
    return scanSingleFileConfig(file, new CxxConfiguration(), visitors);
  }

  /**
   * Helper method for scanning a single file
   */
  public static SourceFile scanSingleFileConfig(File file, CxxConfiguration cxxConfig, SquidAstVisitor<Grammar>... visitors) {
    if (!file.isFile()) {
      throw new IllegalArgumentException("File '" + file + "' not found.");
    }
    AstScanner<Grammar> scanner = create(cxxConfig, visitors);
    scanner.scanFile(file);
    Collection<SourceCode> sources = scanner.getIndex().search(new QueryByType(SourceFile.class));
    if (sources.size() != 1) {
      throw new IllegalStateException("Only one SourceFile was expected whereas " + sources.size() + " has been returned.");
    }
    return (SourceFile) sources.iterator().next();
  }
  
  public static AstScanner<Grammar> create(CxxConfiguration conf, SquidAstVisitor<Grammar>... visitors) {
    final SquidAstVisitorContextImpl<Grammar> context = new SquidAstVisitorContextImpl<Grammar>(new SourceProject("Cxx Project"));
    final Parser<Grammar> parser = CxxParser.create(context, conf);

    AstScanner.Builder<Grammar> builder = AstScanner.<Grammar> builder(context).setBaseParser(parser);

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
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<Grammar>(new SourceCodeBuilderCallback() {
      public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
        StringBuilder sb = new StringBuilder();
        for (Token token : astNode.getFirstDescendant(CxxGrammarImpl.declaratorId).getTokens()) {
          sb.append(token.getValue());
        }
        String functionName = sb.toString();
        SourceFunction function = new SourceFunction(functionName + ":" + astNode.getToken().getLine());
        function.setStartAtLine(astNode.getTokenLine());
        return function;
      }
    }, CxxGrammarImpl.functionDefinition));

    builder.withSquidAstVisitor(CounterVisitor.<Grammar> builder()
        .setMetricDef(CxxMetric.FUNCTIONS)
        .subscribeTo(CxxGrammarImpl.functionDefinition)
        .build());

    /* Classes */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<Grammar>(new SourceCodeBuilderCallback() {
      public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
        AstNode classNameAst = astNode.getFirstDescendant(CxxGrammarImpl.className);
        String className = classNameAst == null ? "" : classNameAst.getFirstChild().getTokenValue();
        SourceClass cls = new SourceClass(className + ":" + astNode.getToken().getLine(), className);
        cls.setStartAtLine(astNode.getTokenLine());
        return cls;
      }
    }, CxxGrammarImpl.classSpecifier));

    builder.withSquidAstVisitor(CounterVisitor.<Grammar> builder()
        .setMetricDef(CxxMetric.CLASSES)
        .subscribeTo(CxxGrammarImpl.classSpecifier)
        .build());

    /* Metrics */
    builder.withSquidAstVisitor(new LinesVisitor<Grammar>(CxxMetric.LINES));
    builder.withSquidAstVisitor(new CxxLinesOfCodeVisitor<Grammar>(CxxMetric.LINES_OF_CODE));

    builder.withSquidAstVisitor(CommentsVisitor.<Grammar> builder().withCommentMetric(CxxMetric.COMMENT_LINES)
        .withNoSonar(true)
        .withIgnoreHeaderComment(conf.getIgnoreHeaderComments())
        .build());

    /* Statements */
    builder.withSquidAstVisitor(CounterVisitor.<Grammar> builder()
        .setMetricDef(CxxMetric.STATEMENTS)
        .subscribeTo(CxxGrammarImpl.statement)
        .subscribeTo(CxxGrammarImpl.switchBlockStatementGroups)
        .subscribeTo(CxxGrammarImpl.switchBlockStatementGroup)
        .build());

    AstNodeType[] complexityAstNodeType = new AstNodeType[] {
      // Entry points
      CxxGrammarImpl.functionDefinition,

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
    
    builder.withSquidAstVisitor(ComplexityVisitor.<Grammar> builder()
        .setMetricDef(CxxMetric.COMPLEXITY)
        .subscribeTo(complexityAstNodeType)
        .build());

    // to emit a 'new file' event to the internals of the plugin
    builder.withSquidAstVisitor(new CxxFileVisitor(context));
    
    /* External visitors (typically Check ones) */
    for (SquidAstVisitor<Grammar> visitor : visitors) {
        if (visitor instanceof CxxCharsetAwareVisitor) {
            ((CxxCharsetAwareVisitor) visitor).setCharset(conf.getCharset());
          }      
        builder.withSquidAstVisitor(visitor);
    }

    return builder.build();
  }

}
