/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
import static com.sonar.sslr.api.GenericTokenType.EOL;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;

/**
 * Visitor that computes {@link CoreMetrics#NCLOC_DATA_KEY} and
 * {@link CoreMetrics#COMMENT_LINES_DATA_KEY} metrics used by the DevCockpit.
 */
/**
 * @author bertk
 *
 */
public class CxxFileLinesVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {
  
  private static final Logger LOG = Loggers.get(CxxFileLinesVisitor.class);
  private final FileLinesContextFactory fileLinesContextFactory;
  private static final Version SQ_6_2 = Version.create(6, 2);
  private boolean isSQ62orNewer;
  private Set<Integer> linesOfCode = Sets.newHashSet();
  private Set<Integer> linesOfComments = Sets.newHashSet();
  private Set<Integer> executableLines = Sets.newHashSet();
  private final FileSystem fileSystem;
  private final Map<InputFile, Set<Integer>> allLinesOfCode;
  private int isWithinFunctionDefinition = 0;
  private Set<CxxGrammarImpl> nodesToVisit = ImmutableSet.of(
      CxxGrammarImpl.functionBody,
      CxxGrammarImpl.compoundStatement,
      CxxGrammarImpl.labeledStatement,
      CxxGrammarImpl.statement,
      CxxGrammarImpl.expressionStatement,
      CxxGrammarImpl.selectionStatement,
      CxxGrammarImpl.iterationStatement,
      CxxGrammarImpl.jumpStatement,
      CxxGrammarImpl.tryBlock,
      CxxGrammarImpl.assignmentExpression,
      CxxGrammarImpl.lambdaExpression);


  /**
   * CxxFileLinesVisitor generates sets for linesOfCode, linesOfComments, executableLines
   * @param context for coverage analysis
   * @param fileLinesContextFactory container for linesOfCode, linesOfComments, executableLines
   * @param allLinesOfCode set of lines for a source file
   */
  public CxxFileLinesVisitor(FileLinesContextFactory fileLinesContextFactory, SensorContext context,
                             Map<InputFile, Set<Integer>> allLinesOfCode) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.fileSystem = context.fileSystem();
    this.allLinesOfCode = allLinesOfCode;
    if (context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_2)) {
      LOG.info("SonarQube 6.2 or newer environment");
      isSQ62orNewer = true;
      }
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionDefinition);
    if (isSQ62orNewer) {
      for (AstNodeType nodeType : nodesToVisit) {
        subscribeTo(nodeType);
      }
    }
  }

  @Override
  public void visitToken(Token token) {
    if (token.getType().equals(GenericTokenType.EOF)) {
      return;
    }

    // ignore functional macros
    if (!token.getType().equals(EOL) && !token.isGeneratedCode()) {
      // Handle all the lines of the token
      String[] tokenLines = token.getValue().split("\n", -1);
      for (int line = token.getLine(); line < token.getLine() + tokenLines.length; line++) {
        linesOfCode.add(line);
      }
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
    if (LOG.isDebugEnabled()) {
      LOG.debug("***** add executable lines for Node(s) : " + astNode);
    }
    switch ((CxxGrammarImpl) astNode.getType()) {
      case functionDefinition:
        if (!isDefaultOrDeleteFunctionBody(astNode)) {
          isWithinFunctionDefinition++;
        }
        break;
      case functionBody:
      case tryBlock:
        executableLines.add(astNode.getTokenLine());
        AstNode endblock = astNode.getFirstChild().getLastChild(CxxPunctuator.CURLBR_RIGHT);
        if (endblock != null) {
          executableLines.add(endblock.getTokenLine());
        }
        break;
      default:
        executableLines.add(astNode.getTokenLine());
    }
  }

  @Override
  public void leaveNode(AstNode node) {
    if (!isDefaultOrDeleteFunctionBody(node)) {
      isWithinFunctionDefinition--;
    }
  }

  private boolean isDefaultOrDeleteFunctionBody(AstNode functionDef) {
    AstNode functionBodyNode = functionDef
      .getFirstChild(CxxGrammarImpl.functionBody);
    boolean defaultOrDelete = false;

    if (functionBodyNode != null) {
      List<AstNode> functionBody = functionBodyNode.getChildren();

      // look for exact sub AST
      if (functionBody.size() == 3) {
        if (functionBody.get(0).is(CxxPunctuator.ASSIGN)
          && functionBody.get(2).is(CxxPunctuator.SEMICOLON)) {

          AstNode bodyType = functionBody.get(1);

          if (bodyType.is(CxxKeyword.DELETE)
            || bodyType.is(CxxKeyword.DEFAULT)) {
            defaultOrDelete = true;
          }
        }
      }
    }

    return defaultOrDelete;
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

    int fileLength = getContext().peekSourceCode().getInt(CxxMetric.LINES);
    for (int line = 1; line <= fileLength; line++) {
      fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, linesOfCode.contains(line) ? 1 : 0);
      fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, linesOfComments.contains(line) ? 1 : 0);
      if(isSQ62orNewer) {
        fileLinesContext.setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, line, 
            executableLines.contains(line) ? 1 : 0);
      }
    }
    fileLinesContext.save();
    this.allLinesOfCode.put(inputFile, linesOfCode);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("executableLines: '{}'", executableLines);
      LOG.debug("linesOfCode:     '{}'", linesOfCode);
      LOG.debug("linesOfComments: '{}'", linesOfComments);

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
  