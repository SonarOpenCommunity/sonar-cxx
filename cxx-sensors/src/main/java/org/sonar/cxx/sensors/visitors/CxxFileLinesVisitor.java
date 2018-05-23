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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.squidbridge.SquidAstVisitor;

/**
 * Visitor that computes {@link CoreMetrics#NCLOC_DATA_KEY} and {@link CoreMetrics#COMMENT_LINES_DATA_KEY} metrics used
 * by the DevCockpit.
 */
public class CxxFileLinesVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private static final Logger LOG = Loggers.get(CxxFileLinesVisitor.class);

  private final CxxLanguage language;
  private final FileLinesContextFactory fileLinesContextFactory;
  private static final Set<Integer> linesOfCode = Sets.newHashSet();
  private static final Set<Integer> linesOfComments = Sets.newHashSet();
  private static final Set<Integer> executableLines = Sets.newHashSet();
  private final FileSystem fileSystem;
  private static int isWithinFunctionDefinition;
  private static final Set<String> ignoreToken = Sets.newHashSet(";", "{", "}", "(", ")", "[", "]");
  private static final AstNodeType[] nodesToVisit = {
    CxxGrammarImpl.labeledStatement,
    CxxGrammarImpl.expressionStatement,
    CxxGrammarImpl.iterationStatement,
    CxxGrammarImpl.jumpStatement,
    CxxGrammarImpl.assignmentExpression,
    CxxGrammarImpl.lambdaExpression};

  /**
   * CxxFileLinesVisitor generates sets for linesOfCode, linesOfComments, executableLines
   *
   * @param context for coverage analysis
   * @param fileLinesContextFactory container for linesOfCode, linesOfComments, executableLines
   * @param language properties
   */
  public CxxFileLinesVisitor(CxxLanguage language, FileLinesContextFactory fileLinesContextFactory,
      SensorContext context) {
    this.language = language;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.fileSystem = context.fileSystem();
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionDefinition);
    for (AstNodeType nodeType : nodesToVisit) {
      subscribeTo(nodeType);
    }
  }

  @Override
  public void visitToken(Token token) {
    if (token.getType().equals(GenericTokenType.EOF)) {
      return;
    }

    if ((isWithinFunctionDefinition != 0) && !ignoreToken.contains(token.getType().getValue())) {
      linesOfCode.add(token.getLine());
    }

    List<Trivia> trivias = token.getTrivia();
    for (Trivia trivia : trivias) {
      if (trivia.isComment()) {
        linesOfComments.add(trivia.getToken().getLine());
      }
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    switch ((CxxGrammarImpl) astNode.getType()) {
      case functionDefinition:
        if (!isDefaultOrDeleteFunctionBody(astNode)) {
          increaseFunctionDefinition();
        }
        break;
      case labeledStatement:
      case expressionStatement:
      case iterationStatement:
      case jumpStatement:
      case assignmentExpression:
      case lambdaExpression:
        visitStatement(astNode);
        break;
      default:
      // Do nothing particular
    }
  }


  /**
   * @param astNode
   */
  private static void visitStatement(AstNode astNode) {
    if (astNode.hasDirectChildren(CxxGrammarImpl.declarationStatement)
      && !astNode.hasDescendant(CxxGrammarImpl.initializer)) {
      return;
    }
    String value = astNode.getTokenValue();
    if (value != null && !"{".equals(value) && !"default".equals(value) && !"case".equals(value)) {
      executableLines.add(astNode.getTokenLine());
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (!isDefaultOrDeleteFunctionBody(astNode)) {
      decreaseFunctionDefinitions();
    }
  }

  /**
   *
   */
  private static void increaseFunctionDefinition() {
    isWithinFunctionDefinition++;
  }

  /**
   *
   */
  private static void decreaseFunctionDefinitions() {
    isWithinFunctionDefinition--;
  }

  private static boolean isDefaultOrDeleteFunctionBody(AstNode astNode) {
    AstNode node = astNode.getFirstChild(CxxGrammarImpl.functionBody);
    if ((node != null)) {
      List<AstNode> functionBody = node.getChildren();

      // look for exact sub AST
      if ((functionBody.size() == 3) && functionBody.get(0).is(CxxPunctuator.ASSIGN)
        && functionBody.get(2).is(CxxPunctuator.SEMICOLON)) {
        AstNode bodyType = functionBody.get(1);
        if (bodyType.is(CxxKeyword.DELETE)
          || bodyType.is(CxxKeyword.DEFAULT)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void visitFile(AstNode astNode) {
    linesOfCode.clear();
    linesOfComments.clear();
    executableLines.clear();
  }

  @Override
  public void leaveFile(AstNode astNode) {
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().is(getContext().getFile()));
    if (inputFile == null) {
      throw new IllegalStateException("InputFile is null, but it should not be.");
    }
    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);

    try {
      linesOfCode.stream().forEach(
        line -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1)
      );
    } catch (IllegalArgumentException e) {
      LOG.error("NCLOC_DATA_KEY metric error: {}", e.getMessage());
      CxxUtils.validateRecovery(e, language);
    }
    try {
      linesOfComments.stream().forEach(
        line -> fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, 1)
      );
    } catch (IllegalArgumentException e) {
      LOG.error("COMMENT_LINES_DATA_KEY metric error: {}", e.getMessage());
      CxxUtils.validateRecovery(e, language);
    }
    try {
      executableLines.stream().forEach(
        line -> fileLinesContext.setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, line, 1)
      );
    } catch (IllegalArgumentException e) {
      LOG.error("EXECUTABLE_LINES_DATA_KEY metric error: {}", e.getMessage());
      CxxUtils.validateRecovery(e, language);
    }
    fileLinesContext.save();

    if (LOG.isDebugEnabled()) {
      LOG.debug("CxxFileLinesVisitor: '{}'", inputFile.uri().getPath());
      LOG.debug("   lines:           '{}'", inputFile.lines());
      LOG.debug("   executableLines: '{}'", executableLines);
      LOG.debug("   linesOfCode:     '{}'", linesOfCode);
      LOG.debug("   linesOfComments: '{}'", linesOfComments);
    }
  }

  public Set<Integer> getLinesOfCode() {
    return ImmutableSet.copyOf(linesOfCode);
  }

  public Set<Integer> getLinesOfComments() {
    return ImmutableSet.copyOf(linesOfComments);
  }

  public Set<Integer> getExecutableLines() {
    return ImmutableSet.copyOf(executableLines);
  }

}
