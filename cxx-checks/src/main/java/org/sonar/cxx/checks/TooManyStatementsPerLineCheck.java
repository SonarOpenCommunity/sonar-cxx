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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.AbstractOneStatementPerLineCheck;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxGrammarImpl;

@Rule(
  key = "TooManyStatementsPerLine",
  description = "Only one statement per line is allowed. Split this line.",
  priority = Priority.MAJOR)

public class TooManyStatementsPerLineCheck extends AbstractOneStatementPerLineCheck<Grammar> {

  private static final boolean DEFAULT_EXCLUDE_CASE_BREAK = false;

  @RuleProperty(
      key = "excludeCaseBreak",
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

  /** Exclude subsequent generated nodes, if they are consecutive and on the same line.
   */
  private boolean isGeneratedNodeExcluded(AstNode astNode)
  {
    AstNode prev = astNode.getPreviousAstNode();
    return prev != null &&
           prev.getTokenLine() == astNode.getTokenLine() &&
           prev.getTokenLine() == astNode.getTokenLine() &&
           prev.isCopyBookOrGeneratedNode();
  }

  /** Exclude 'break' statement if it is on the same line as the switch label (case: or default:).
   * i.e. the break statement is on the same line as it's "switchBlockStatementGroup" ancestor.
   */
  private boolean isBreakStatementExcluded(AstNode astNode)
  {
    if (!excludeCaseBreak || astNode.getToken().getType() != CxxKeyword.BREAK)
      return false;

    AstNode switchGroup = astNode.getFirstAncestor(CxxGrammarImpl.switchBlockStatementGroup);
    return switchGroup != null
        && switchGroup.getTokenLine() == astNode.getTokenLine();
  }

  @Override
  public boolean isExcluded(AstNode astNode) {
    AstNode statementNode = astNode.getFirstChild();
    return statementNode.is(CxxGrammarImpl.compoundStatement)
      || statementNode.is(CxxGrammarImpl.emptyStatement)
      || statementNode.is(CxxGrammarImpl.iterationStatement)
      || statementNode.is(CxxGrammarImpl.labeledStatement)
      || statementNode.is(CxxGrammarImpl.declaration)
      || (statementNode.isCopyBookOrGeneratedNode() && isGeneratedNodeExcluded(statementNode))
      || (statementNode.is(CxxGrammarImpl.jumpStatement) && isBreakStatementExcluded(statementNode));
 }
}
