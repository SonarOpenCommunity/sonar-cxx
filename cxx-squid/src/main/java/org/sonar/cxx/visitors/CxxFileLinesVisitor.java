/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.squidbridge.SquidAstVisitor;

/**
 * Visitor that computes {@link CoreMetrics#NCLOC_DATA_KEY} and {@link CoreMetrics#EXECUTABLE_LINES_DATA} metrics used
 * by the DevCockpit.
 */
public class CxxFileLinesVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private List<Integer> linesOfCode;
  private List<Integer> executableLines;
  private int isWithinFunctionDefinition;

  private static boolean isDefaultOrDeleteFunctionBody(AstNode astNode) {
    var node = astNode.getFirstChild(CxxGrammarImpl.functionBody);
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

  static boolean isCodeToken(Token token) {
    var type = token.getType();
    if (!(type instanceof CxxPunctuator)) {
      return true;
    }

    switch ((CxxPunctuator) type) {
      case SEMICOLON:
      case BR_LEFT:
      case BR_RIGHT:
      case CURLBR_LEFT:
      case CURLBR_RIGHT:
      case SQBR_LEFT:
      case SQBR_RIGHT:
        return false;

      default:
        return true;
    }
  }

  static boolean isExecutableToken(Token token) {
    var type = token.getType();
    return !CxxPunctuator.CURLBR_LEFT.equals(type) && !CxxKeyword.DEFAULT.equals(type) && !CxxKeyword.CASE.equals(type);
  }

  static void addLineNumber(List<Integer> collection, int lineNr) {
    // use the fact, that we iterate over tokens from top to bottom.
    // if the line was already visited its index was put at the end of
    // collection.
    //
    // don't use Set, because Set would sort elements on each insert
    // since we potentially insert line number for each token it would create
    // large run-time overhead
    //
    // we sort/filter duplicates only once - on leaveFile(AstNode)
    //
    if (collection.isEmpty() || collection.get(collection.size() - 1) != lineNr) {
      collection.add(lineNr);
    }
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionDefinition,
                CxxGrammarImpl.labeledStatement,
                CxxGrammarImpl.expressionStatement,
                CxxGrammarImpl.iterationStatement,
                CxxGrammarImpl.jumpStatement,
                CxxGrammarImpl.assignmentExpression,
                CxxGrammarImpl.lambdaExpression);
  }

  @Override
  public void visitToken(Token token) {
    if (token.getType().equals(GenericTokenType.EOF)) {
      return;
    }

    if ((isWithinFunctionDefinition != 0) && isCodeToken(token)) {
      addLineNumber(linesOfCode, token.getLine());
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

  @Override
  public void leaveNode(AstNode astNode) {
    if (!isDefaultOrDeleteFunctionBody(astNode)) {
      decreaseFunctionDefinitions();
    }
  }

  @Override
  public void visitFile(AstNode astNode) {
    linesOfCode = new ArrayList<>();
    executableLines = new ArrayList<>();
  }

  @Override
  public void leaveFile(AstNode astNode) {
    getContext().peekSourceCode().addData(CxxMetric.NCLOC_DATA, linesOfCode);
    linesOfCode = null;
    getContext().peekSourceCode().addData(CxxMetric.EXECUTABLE_LINES_DATA, executableLines);
    executableLines = null;
  }

  /**
   * @param astNode
   */
  private void visitStatement(AstNode astNode) {
    if (astNode.hasDirectChildren(CxxGrammarImpl.declarationStatement)
          && !astNode.hasDescendant(CxxGrammarImpl.initializer)) {
      return;
    }
    if (isExecutableToken(astNode.getToken())) {
      addLineNumber(executableLines, astNode.getTokenLine());
    }
  }

  private void increaseFunctionDefinition() {
    isWithinFunctionDefinition++;
  }

  private void decreaseFunctionDefinitions() {
    isWithinFunctionDefinition--;
  }

}
