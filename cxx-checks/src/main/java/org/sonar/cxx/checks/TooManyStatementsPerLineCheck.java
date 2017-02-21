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
package org.sonar.cxx.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.AbstractOneStatementPerLineCheck;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.TokenType;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.tag.Tag;

@Rule(
  key = "TooManyStatementsPerLine",
  name = "Statements should be on separate lines",
  tags = {Tag.BRAIN_OVERLOAD},
  priority = Priority.MAJOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class TooManyStatementsPerLineCheck extends AbstractOneStatementPerLineCheck<Grammar> {

  private static final boolean DEFAULT_EXCLUDE_CASE_BREAK = false;

  @RuleProperty(
    key = "excludeCaseBreak",
    description = "Exclude 'break' statement if it is on the same line as the switch label (case: or default:)",
    defaultValue = "" + DEFAULT_EXCLUDE_CASE_BREAK)
  public boolean excludeCaseBreak = DEFAULT_EXCLUDE_CASE_BREAK;

  @Override
  public com.sonar.sslr.api.Rule getStatementRule() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.statement);
  }

  /**
   * Exclude subsequent generated nodes, if they are consecutive and on the same
   * line.
   */
  private boolean isGeneratedNodeExcluded(AstNode astNode) {
    AstNode prev = astNode.getPreviousAstNode();
    return prev != null
      && prev.getTokenLine() == astNode.getTokenLine()
      && prev.isCopyBookOrGeneratedNode();
  }

  /**
   * Exclude 'break' statement if it is on the same line as the switch label
   */
  private boolean isBreakStatementExcluded(AstNode astNode) {
    boolean exclude = false;
    if (excludeCaseBreak && astNode.getToken().getType() == CxxKeyword.BREAK) {
      for (AstNode statement = astNode.getFirstAncestor(CxxGrammarImpl.statement);
        statement != null;
        statement = statement.getPreviousSibling()) {
        if (astNode.getTokenLine() != statement.getTokenLine()) {
          break;
        }
        TokenType type = statement.getToken().getType();
        if (type == CxxKeyword.CASE || type == CxxKeyword.DEFAULT) {
          exclude = true;
          break;
        }
      }
    }
    return exclude;
  }

  /**
   * Exclude type alias definitions inside of blocks ( ... { using a = b; ... }
   * ... )
   */
  private boolean isTypeAlias(AstNode astNode) {
    return astNode.getFirstDescendant(CxxGrammarImpl.aliasDeclaration) != null;
  }

  /**
   * Exclude empty expression statement
   */
  private boolean isEmptyExpressionStatement(AstNode astNode) {
      if (astNode.is(CxxGrammarImpl.expressionStatement) && ";".equals(astNode.getToken().getValue()))
      {
        AstNode statement = astNode.getFirstAncestor(CxxGrammarImpl.selectionStatement);
        if (statement != null )
          return astNode.getTokenLine() == statement.getTokenLine();
      }
      return false;
  }

  @Override
  public boolean isExcluded(AstNode astNode) {
    AstNode statementNode = astNode.getFirstChild();
    return statementNode.is(CxxGrammarImpl.compoundStatement)
      || statementNode.is(CxxGrammarImpl.emptyStatement)
      || statementNode.is(CxxGrammarImpl.iterationStatement)
      || statementNode.is(CxxGrammarImpl.labeledStatement)
      || statementNode.is(CxxGrammarImpl.declaration)
      || isTypeAlias(statementNode)
      || (statementNode.isCopyBookOrGeneratedNode() && isGeneratedNodeExcluded(statementNode))
      || (statementNode.is(CxxGrammarImpl.jumpStatement) && isBreakStatementExcluded(statementNode))
      || isEmptyExpressionStatement(statementNode);
  }
}
