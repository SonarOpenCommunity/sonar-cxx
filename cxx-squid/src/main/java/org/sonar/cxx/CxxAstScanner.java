/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import static java.lang.Math.min;
import java.util.Collection;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.cxx.squidbridge.AstScanner;
import org.sonar.cxx.squidbridge.CommentAnalyser;
import org.sonar.cxx.squidbridge.SourceCodeBuilderVisitor;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.cxx.squidbridge.api.SourceClass;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.api.SourceFunction;
import org.sonar.cxx.squidbridge.api.SourceProject;
import org.sonar.cxx.squidbridge.indexer.QueryByType;
import org.sonar.cxx.squidbridge.metrics.CommentsVisitor;
import org.sonar.cxx.squidbridge.metrics.ComplexityVisitor;
import org.sonar.cxx.squidbridge.metrics.CounterVisitor;
import org.sonar.cxx.squidbridge.metrics.LinesVisitor;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.cxx.visitors.CxxCognitiveComplexityVisitor;
import org.sonar.cxx.visitors.CxxCpdVisitor;
import org.sonar.cxx.visitors.CxxCyclomaticComplexityVisitor;
import org.sonar.cxx.visitors.CxxFileLinesVisitor;
import org.sonar.cxx.visitors.CxxFileVisitor;
import org.sonar.cxx.visitors.CxxFunctionComplexityVisitor;
import org.sonar.cxx.visitors.CxxFunctionSizeVisitor;
import org.sonar.cxx.visitors.CxxHighlighterVisitor;
import org.sonar.cxx.visitors.CxxLinesOfCodeInFunctionBodyVisitor;
import org.sonar.cxx.visitors.CxxLinesOfCodeVisitor;
import org.sonar.cxx.visitors.CxxParseErrorLoggerVisitor;
import org.sonar.cxx.visitors.CxxPublicApiVisitor;

public final class CxxAstScanner {

  private CxxAstScanner() {
  }

  /**
   * Helper method for testing checks without having to deploy them on a Sonar instance.
   *
   * @param inputFile is the file to be checked
   * @param visitors AST checks and visitors to use
   * @return file checked with measures and issues
   */
  @SafeVarargs
  public static SourceFile scanSingleInputFile(InputFile inputFile, SquidAstVisitor<Grammar>... visitors) {
    return scanSingleInputFileConfig(inputFile, new CxxSquidConfiguration(), visitors);
  }

  /**
   * Helper method for scanning a single file
   *
   * @param inputFile is the file to be checked
   * @param squidConfig the Squid configuration
   * @param visitors AST checks and visitors to use
   * @return file checked with measures and issues
   */
  public static SourceFile scanSingleInputFileConfig(InputFile inputFile, CxxSquidConfiguration squidConfig,
                                                     SquidAstVisitor<Grammar>... visitors) {
    if (!inputFile.isFile()) {
      throw new IllegalArgumentException("File '" + inputFile.toString() + "' not found.");
    }
    AstScanner<Grammar> scanner = create(squidConfig, visitors);
    scanner.scanInputFile(inputFile);
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
   * @param squidConfig the Squid configuration
   * @param visitors visitors AST checks and visitors to use
   * @return scanner for the given parameters
   */
  @SafeVarargs
  public static AstScanner<Grammar> create(CxxSquidConfiguration squidConfig, SquidAstVisitor<Grammar>... visitors) {
    var context = new SquidAstVisitorContextImpl<>(new SourceProject("Cxx Project"));
    var parser = CxxParser.create(context, squidConfig);
    var builder = AstScanner.<Grammar>builder(context).setBaseParser(parser);

    /* Metrics */
    builder.withMetrics(CxxMetric.values());

    /* Files */
    builder.setFilesMetric(CxxMetric.FILES);

    /* Comments */
    builder.setCommentAnalyser(
      new CommentAnalyser() {
      @Override
      public boolean isBlank(String line) {
        for (var i = 0; i < line.length(); i++) {
          if (Character.isLetterOrDigit(line.charAt(i))) {
            return false;
          }
        }
        return true;
      }

      @Override
      public String getContents(String comment) {
        var HEADER_LEN = 2;
        return "/*".equals(comment.substring(0, HEADER_LEN))
                 ? comment.substring(HEADER_LEN, comment.length() - HEADER_LEN)
                 : comment.substring(HEADER_LEN);
      }
    });

    /* Functions */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<>((SourceCode parentSourceCode, AstNode astNode) -> {
      var sb = new StringBuilder(512);
      for (var token : astNode.getFirstDescendant(CxxGrammarImpl.declaratorId).getTokens()) {
        sb.append(token.getValue());
      }
      var functionName = sb.toString();
      sb.setLength(0);
      // todo: check if working with nested-namespace-definition
      var namespace = astNode.getFirstAncestor(CxxGrammarImpl.namedNamespaceDefinition);
      while (namespace != null) {
        if (sb.length() > 0) {
          sb.insert(0, "::");
        }
        sb.insert(0, namespace.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue());
        // todo: check if working with nested-namespace-definition
        namespace = namespace.getFirstAncestor(CxxGrammarImpl.namedNamespaceDefinition);
      }
      var namespaceName = sb.length() > 0 ? sb.toString() + "::" : "";
      var function = new SourceFunction(intersectingConcatenate(namespaceName, functionName)
                                      + ":" + astNode.getToken().getLine());
      function.setStartAtLine(astNode.getTokenLine());
      return function;
    }, CxxGrammarImpl.functionDefinition));

    builder.withSquidAstVisitor(CounterVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.FUNCTIONS)
      .subscribeTo(CxxGrammarImpl.functionDefinition)
      .build());

    /* Classes */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<>((SourceCode parentSourceCode, AstNode astNode) -> {
      var classNameAst = astNode.getFirstDescendant(CxxGrammarImpl.className);
      var className = classNameAst == null ? "" : classNameAst.getFirstChild().getTokenValue();
      var cls = new SourceClass(className + ":" + astNode.getToken().getLine(), className);
      cls.setStartAtLine(astNode.getTokenLine());
      return cls;
    }, CxxGrammarImpl.classSpecifier));

    builder.withSquidAstVisitor(CounterVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.CLASSES)
      .subscribeTo(CxxGrammarImpl.classSpecifier)
      .build());

    /* Metrics */
    builder.withSquidAstVisitor(new LinesVisitor<>(CxxMetric.LINES));
    builder.withSquidAstVisitor(new CxxLinesOfCodeVisitor<>());
    builder.withSquidAstVisitor(new CxxLinesOfCodeInFunctionBodyVisitor<>());
    builder.withSquidAstVisitor(new CxxPublicApiVisitor<>(squidConfig));
    builder.withSquidAstVisitor(CommentsVisitor.<Grammar>builder().withCommentMetric(CxxMetric.COMMENT_LINES)
      .withNoSonar(true)
      .withIgnoreHeaderComment(false)
      .build()
    );

    /* Statements */
    builder.withSquidAstVisitor(CounterVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.STATEMENTS)
      .subscribeTo(CxxGrammarImpl.statement)
      .build());

    builder.withSquidAstVisitor(new CxxCyclomaticComplexityVisitor<>(ComplexityVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.COMPLEXITY)
      .subscribeTo(CxxComplexityConstants.getCyclomaticComplexityTypes())
      .build()));

    builder.withSquidAstVisitor(new CxxCognitiveComplexityVisitor<>());
    builder.withSquidAstVisitor(new CxxFunctionComplexityVisitor<>(squidConfig));
    builder.withSquidAstVisitor(new CxxFunctionSizeVisitor<>(squidConfig));

    // to emit a 'new file' event to the internals of the plugin
    builder.withSquidAstVisitor(new CxxFileVisitor<>());

    // log syntax errors
    builder.withSquidAstVisitor(new CxxParseErrorLoggerVisitor<>());

    /* Highlighter */
    builder.withSquidAstVisitor(new CxxHighlighterVisitor());

    /* CPD */
    builder.withSquidAstVisitor(new CxxCpdVisitor(squidConfig));

    /* NCLOC & EXECUTABLE_LINES */
    builder.withSquidAstVisitor(new CxxFileLinesVisitor());

    /* External visitors (typically Check ones) */
    for (var visitor : visitors) {
      if (visitor instanceof CxxCharsetAwareVisitor) {
        ((CxxCharsetAwareVisitor) visitor).setCharset(squidConfig.getCharset());
      }
      builder.withSquidAstVisitor(visitor);
    }

    return builder.build();
  }

  // Concatenate two strings, but if there is overlap at the intersection,
  // include the intersection/overlap only once.
  public static String intersectingConcatenate(String a, String b) {

    // find length of maximum possible match
    var lenOfA = a.length();
    var lenOfB = b.length();
    var minIntersectionLen = min(lenOfB, lenOfA);

    // search down from maximum match size, to get longest possible intersection
    for (var size = minIntersectionLen; size > 0; size--) {
      if (a.regionMatches(lenOfA - size, b, 0, size)) {
        return a + b.substring(size, lenOfB);
      }
    }

    // Didn't find any intersection. Fall back to straight concatenation.
    return a + b;
  }

}
