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
package org.sonar.cxx;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import java.util.Collection;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.cxx.visitors.CxxCognitiveComplexityVisitor;
import org.sonar.cxx.visitors.CxxFileVisitor;
import org.sonar.cxx.visitors.CxxLinesOfCodeInFunctionBodyVisitor;
import org.sonar.cxx.visitors.CxxLinesOfCodeVisitor;
import org.sonar.cxx.visitors.CxxParseErrorLoggerVisitor;
import org.sonar.cxx.visitors.CxxPublicApiVisitor;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.CommentAnalyser;
import org.sonar.squidbridge.SourceCodeBuilderCallback;
import org.sonar.squidbridge.SourceCodeBuilderVisitor;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.squidbridge.metrics.CommentsVisitor;
import org.sonar.squidbridge.metrics.ComplexityVisitor;
import org.sonar.squidbridge.metrics.CounterVisitor;
import org.sonar.squidbridge.metrics.LinesVisitor;

public final class CxxAstScanner {

  private CxxAstScanner() {
  }

  /**
   * Helper method for testing checks without having to deploy them on a Sonar instance.
   *
   * @param file is the file to be checked
   * @param sensorContext SQ API batch side context
   * @param visitors AST checks and visitors to use
   * @param language CxxLanguage to use
   * @return file checked with measures and issues
   */
  @SafeVarargs
  public static SourceFile scanSingleFile(InputFile file, SensorContext sensorContext, CxxLanguage language,
    SquidAstVisitor<Grammar>... visitors) {
    return scanSingleFileConfig(language, file, new CxxConfiguration(sensorContext.fileSystem().encoding()),
      visitors);
  }

  /**
   * Helper method for scanning a single file
   *
   * @param file is the file to be checked
   * @param cxxConfig the plugin configuration
   * @param visitors AST checks and visitors to use
   * @param language for sensor
   * @return file checked with measures and issues
   */
  public static SourceFile scanSingleFileConfig(CxxLanguage language, InputFile file, CxxConfiguration cxxConfig,
    SquidAstVisitor<Grammar>... visitors) {
    if (!file.isFile()) {
      throw new IllegalArgumentException("File '" + file + "' not found.");
    }
    AstScanner<Grammar> scanner = create(language, cxxConfig, visitors);
    scanner.scanFile(file.file()); //@todo: deprecated file.file()
    Collection<SourceCode> sources = scanner.getIndex().search(new QueryByType(SourceFile.class));
    if (sources.size() != 1) {
      throw new IllegalStateException("Only one SourceFile was expected whereas "
        + sources.size() + " has been returned.");
    }
    return (SourceFile) sources.iterator().next();
  }

  /**
   * Create scanner for language
   *
   * @param language for sensor
   * @param conf settings for sensor
   * @param sensorContext for sensor
   * @param visitors visitors AST checks and visitors to use
   * @return scanner for the given parameters
   */
  @SafeVarargs
  public static AstScanner<Grammar> create(CxxLanguage language, CxxConfiguration conf,
    SquidAstVisitor<Grammar>... visitors) {
    final SquidAstVisitorContextImpl<Grammar> context
      = new SquidAstVisitorContextImpl<>(new SourceProject("Cxx Project"));
    final Parser<Grammar> parser = CxxParser.create(context, conf, language);

    AstScanner.Builder<Grammar> builder = AstScanner.<Grammar>builder(context).setBaseParser(parser);

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
        return "/*".equals(comment.substring(0, 2))
          ? comment.substring(2, comment.length() - 2)
          : comment.substring(2);
      }
    });

    /* Functions */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<>(new SourceCodeBuilderCallback() {
      @Override
      public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
        StringBuilder sb = new StringBuilder();
        for (Token token : astNode.getFirstDescendant(CxxGrammarImpl.declaratorId).getTokens()) {
          sb.append(token.getValue());
        }
        String functionName = sb.toString();
        sb.setLength(0);
        // todo: check if working with nested-namespace-definition
        AstNode namespace = astNode.getFirstAncestor(CxxGrammarImpl.namedNamespaceDefinition);
        while (namespace != null) {
          if (sb.length() > 0) {
            sb.insert(0, "::");
          }
          sb.insert(0, namespace.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue());
          // todo: check if working with nested-namespace-definition
          namespace = namespace.getFirstAncestor(CxxGrammarImpl.namedNamespaceDefinition);
        }
        String namespaceName = sb.length() > 0 ? sb.toString() + "::" : "";
        SourceFunction function = new SourceFunction(intersectingConcatenate(namespaceName, functionName)
          + ":" + astNode.getToken().getLine());
        function.setStartAtLine(astNode.getTokenLine());
        return function;
      }
    }, CxxGrammarImpl.functionDefinition));

    builder.withSquidAstVisitor(CounterVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.FUNCTIONS)
      .subscribeTo(CxxGrammarImpl.functionDefinition)
      .build());

    /* Classes */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<>(new SourceCodeBuilderCallback() {
      @Override
      public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
        AstNode classNameAst = astNode.getFirstDescendant(CxxGrammarImpl.className);
        String className = classNameAst == null ? "" : classNameAst.getFirstChild().getTokenValue();
        SourceClass cls = new SourceClass(className + ":" + astNode.getToken().getLine(), className);
        cls.setStartAtLine(astNode.getTokenLine());
        return cls;
      }
    }, CxxGrammarImpl.classSpecifier));

    builder.withSquidAstVisitor(CounterVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.CLASSES)
      .subscribeTo(CxxGrammarImpl.classSpecifier)
      .build());

    /* Metrics */
    builder.withSquidAstVisitor(new LinesVisitor<>(CxxMetric.LINES));
    builder.withSquidAstVisitor(new CxxLinesOfCodeVisitor<>(CxxMetric.LINES_OF_CODE));
    builder.withSquidAstVisitor(new CxxLinesOfCodeInFunctionBodyVisitor<>());
    builder.withSquidAstVisitor(new CxxPublicApiVisitor<>(CxxMetric.PUBLIC_API,
      CxxMetric.PUBLIC_UNDOCUMENTED_API)
      .withHeaderFileSuffixes(conf.getHeaderFileSuffixes()));

    builder.withSquidAstVisitor(CommentsVisitor.<Grammar>builder().withCommentMetric(CxxMetric.COMMENT_LINES)
      .withNoSonar(true)
      .withIgnoreHeaderComment(conf.getIgnoreHeaderComments())
      .build());

    /* Statements */
    builder.withSquidAstVisitor(CounterVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.STATEMENTS)
      .subscribeTo(CxxGrammarImpl.statement)
      .build());

    AstNodeType[] complexityAstNodeType = new AstNodeType[]{
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

    builder.withSquidAstVisitor(ComplexityVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.COMPLEXITY)
      .subscribeTo(complexityAstNodeType)
      .build());

    builder.withSquidAstVisitor(CxxCognitiveComplexityVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.COGNITIVE_COMPLEXITY)
      .subscribeTo(CxxGrammarImpl.functionDefinition)
      .build());

    // to emit a 'new file' event to the internals of the plugin
    builder.withSquidAstVisitor(new CxxFileVisitor<>(context));

    // log syntax errors
    builder.withSquidAstVisitor(new CxxParseErrorLoggerVisitor<>(context));

    /* External visitors (typically Check ones) */
    for (SquidAstVisitor<Grammar> visitor : visitors) {
      if (visitor instanceof CxxCharsetAwareVisitor) {
        ((CxxCharsetAwareVisitor) visitor).setCharset(conf.getCharset());
      }
      builder.withSquidAstVisitor(visitor);
    }

    return builder.build();
  }

  // Concatenate two strings, but if there is overlap at the intersection,
  // include the intersection/overlap only once.
  public static String intersectingConcatenate(String a, String b) {

    // find length of maximum possible match
    int len_a = a.length();
    int len_b = b.length();
    int max_match = (len_a > len_b) ? len_b : len_a;

    // search down from maximum match size, to get longest possible intersection
    for (int size = max_match; size > 0; size--) {
      if (a.regionMatches(len_a - size, b, 0, size)) {
        return a + b.substring(size, len_b);
      }
    }

    // Didn't find any intersection. Fall back to straight concatenation.
    return a + b;
  }
}
