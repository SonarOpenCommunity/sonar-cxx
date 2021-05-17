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
package org.sonar.cxx.squidbridge;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.ast.AstWalker;
import java.io.File;
import java.io.InterruptedIOException;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.squidbridge.api.AnalysisException;
import org.sonar.cxx.squidbridge.api.SourceCodeSearchEngine;
import org.sonar.cxx.squidbridge.api.SourceCodeTreeDecorator;
import org.sonar.cxx.squidbridge.api.SourceProject;
import org.sonar.cxx.squidbridge.indexer.SquidIndex;
import org.sonar.cxx.squidbridge.measures.MetricDef;

public class AstScanner<G extends Grammar> {

  private static final Logger LOG = Loggers.get(AstScanner.class);

  private final List<SquidAstVisitor<G>> visitors;
  private final Parser<G> parser;
  private final SquidAstVisitorContextImpl<G> context;

  private final SquidIndex indexer = new SquidIndex();
  private final MetricDef[] metrics;
  private final MetricDef filesMetric;

  protected AstScanner(Builder<G> builder) {
    this.visitors = Lists.newArrayList(builder.visitors);
    this.parser = builder.baseParser;
    this.context = builder.context;

    this.context.setGrammar(parser.getGrammar());
    this.context.getProject().setSourceCodeIndexer(indexer);
    this.context.setCommentAnalyser(builder.commentAnalyser);
    this.metrics = builder.metrics;
    this.filesMetric = builder.filesMetric;
    indexer.index(context.getProject());
  }

  public SourceCodeSearchEngine getIndex() {
    return indexer;
  }

  public void scanFile(File file) {
    scanFiles(java.util.List.of(file));
  }

  public void scanInputFile(InputFile inputFile) {
    scanInputFiles(java.util.List.of(inputFile));
  }

  public void scanFiles(Collection<File> files) {
    initVisitors();

    var astWalker = new AstWalker(visitors);

    for (var file : files) {
      checkCancel();
      context.setFile(file, filesMetric);

      Exception parseException = null;
      AstNode ast = null;
      try {
        try {
          ast = parser.parse(file);
        } catch (Exception e) {
          parseException = handleParseException(file, e);
        }
        walkAndVisit(astWalker, ast, parseException);
      } catch (Throwable e) {
        throw new AnalysisException("Unable to parse file: " + file.getAbsolutePath(), e);
      }
    }

    destroyVisitors();
    decorateSquidTree();
  }

  public void scanInputFiles(Iterable<InputFile> inputFiles) {
    initVisitors();

    var astWalker = new AstWalker(visitors);

    for (var inputFile : inputFiles) {
      var file = new File(inputFile.uri().getPath());
      checkCancel();
      context.setInputFile(inputFile, filesMetric);

      Exception parseException = null;
      AstNode ast = null;
      try {
        try {
          ast = parser.parse(inputFile.contents());
        } catch (Exception e) {
          parseException = handleParseException(file, e);
        }
        walkAndVisit(astWalker, ast, parseException);
      } catch (Throwable e) {
        throw new AnalysisException("Unable to parse file: " + file.getAbsolutePath(), e);
      }
    }

    destroyVisitors();
    decorateSquidTree();
  }

  private static Exception handleParseException(File file, Exception e) {
    checkInterrupted(e);
    if (e instanceof RecognitionException) {
      LOG.error("Unable to parse file: " + file.getAbsolutePath());
      LOG.error(e.getMessage());
    } else {
      LOG.error("Unable to parse file: " + file.getAbsolutePath(), e);
    }
    return e;
  }

  private void walkAndVisit(AstWalker astWalker, AstNode ast, @Nullable Exception parseException) throws Throwable {
    if (parseException == null) {
      astWalker.walkAndVisit(ast);
    } else {
      // process parse error
      for (var visitor : visitors) {
        visitor.visitFile(ast);
      }
      for (var visitor : visitors) {
        if (visitor instanceof AstScannerExceptionHandler) {
          if (parseException instanceof RecognitionException) {
            ((AstScannerExceptionHandler) visitor)
              .processRecognitionException((RecognitionException) parseException);
          } else {
            ((AstScannerExceptionHandler) visitor).processException(parseException);
          }
        }
      }
      for (var visitor : visitors) {
        visitor.leaveFile(ast);
      }
    }
    context.popTillSourceProject();
  }

  private void initVisitors() {
    for (var visitor : visitors) {
      visitor.init();
    }
  }

  private void destroyVisitors() {
    for (var visitor : visitors) {
      visitor.destroy();
    }
  }

  /**
   * Checks if the root cause of the thread is related to an interrupt.
   * Note that when such an exception is thrown, the interrupt flag is reset.
   */
  private static void checkInterrupted(Exception e) {
    Throwable cause = Throwables.getRootCause(e);
    if (cause instanceof InterruptedException || cause instanceof InterruptedIOException) {
      throw new AnalysisException("Analysis cancelled", e);
    }
  }

  private static void checkCancel() {
    if (Thread.interrupted()) {
      throw new AnalysisException("Analysis cancelled");
    }
  }

  protected void decorateSquidTree() {
    if (metrics != null && metrics.length > 0) {
      SourceProject project = context.getProject();
      var decorator = new SourceCodeTreeDecorator(project);
      decorator.decorateWith(metrics);
    }
  }

  public static <G extends Grammar> Builder<G> builder(SquidAstVisitorContextImpl<G> context) {
    return new Builder<>(context);
  }

  public static class Builder<G extends Grammar> {

    private Parser<G> baseParser;
    private final List<SquidAstVisitor<G>> visitors = Lists.newArrayList();
    private final SquidAstVisitorContextImpl<G> context;
    private CommentAnalyser commentAnalyser;
    private MetricDef[] metrics;
    private MetricDef filesMetric;

    public Builder(SquidAstVisitorContextImpl<G> context) {
      checkNotNull(context, "context cannot be null");
      this.context = context;
    }

    public Builder<G> setBaseParser(Parser<G> baseParser) {
      checkNotNull(baseParser, "baseParser cannot be null");
      this.baseParser = baseParser;
      return this;
    }

    public Builder<G> setCommentAnalyser(CommentAnalyser commentAnalyser) {
      checkNotNull(commentAnalyser, "commentAnalyser cannot be null");
      this.commentAnalyser = commentAnalyser;
      return this;
    }

    public Builder<G> withSquidAstVisitor(SquidAstVisitor<G> visitor) {
      checkNotNull(visitor, "visitor cannot be null");
      visitor.setContext(context);
      visitors.add(visitor);
      return this;
    }

    public Builder<G> withMetrics(MetricDef... metrics) {
      for (var metric : metrics) {
        checkNotNull(metric, "metrics cannot be null");
      }
      this.metrics = metrics.clone();
      return this;
    }

    public Builder<G> setFilesMetric(MetricDef filesMetric) {
      checkNotNull(filesMetric, "filesMetric cannot be null");
      this.filesMetric = filesMetric;
      return this;
    }

    public AstScanner<G> build() {
      checkState(baseParser != null, "baseParser must be set");
      checkState(commentAnalyser != null, "commentAnalyser must be set");
      checkState(filesMetric != null, "filesMetric must be set");
      return new AstScanner<>(this);
    }
  }

}
