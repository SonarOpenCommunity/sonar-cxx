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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import javax.annotation.Nullable;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CxxKeyword;
import static org.sonar.cxx.checks.utils.CheckUtils.isIfStatement;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "CollapsibleIfCandidate",
  name = "Collapsible 'if' statements should be merged",
  priority = Priority.MAJOR,
  tags = {Tag.BRAIN_OVERLOAD})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class CollapsibleIfCandidateCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.selectionStatement);
  }

  @Override
  public void visitNode(AstNode node) {
    if (!hasElseClause(node) && !hasDeclaration(node)) {
      AstNode enclosingIfStatement = getEnclosingIfStatement(node);
      if (enclosingIfStatement != null && !hasElseClause(enclosingIfStatement)
        && hasSingleTrueStatement(enclosingIfStatement) && !hasDeclaration(enclosingIfStatement)) {
        getContext().createLineViolation(this, "Merge this if statement with the enclosing one.", node);
      }
    }
  }

  private static boolean hasElseClause(AstNode node) {
    return node.hasDirectChildren(CxxKeyword.ELSE);
  }

  /**
   * Verify if the ifStatement's condition is actually a variable declaration. This is the case if the condition is not
   * an expression. This prevents collapse, since multiple definitions and expressions cannot be combined.
   */
  private static boolean hasDeclaration(AstNode node) {
    AstNode condition = node.getFirstChild(CxxGrammarImpl.condition);
    return !(condition.getNumberOfChildren() == 1 && condition.getFirstChild().is(CxxGrammarImpl.expression));
  }

  @Nullable
  private static AstNode getEnclosingIfStatement(AstNode node) {
    AstNode grandParent = node.getParent().getParent();
    if (isIfStatement(grandParent)) {
      return grandParent;
    } else if (!grandParent.is(CxxGrammarImpl.statementSeq)) {
      return null;
    }

    AstNode statement = grandParent.getFirstAncestor(CxxGrammarImpl.compoundStatement).getParent();
    if (!statement.is(CxxGrammarImpl.statement)) {
      return null;
    }

    AstNode enclosingStatement = statement.getParent();
    return isIfStatement(enclosingStatement) ? enclosingStatement : null;
  }

  private static boolean hasSingleTrueStatement(AstNode node) {
    AstNode statement = node.getFirstChild(CxxGrammarImpl.statement);
    return statement.hasDirectChildren(CxxGrammarImpl.compoundStatement)
      ? statement.getFirstChild(CxxGrammarImpl.compoundStatement).getFirstChild(CxxGrammarImpl.statementSeq)
        .getChildren().size() == 1 : true;
  }

}
