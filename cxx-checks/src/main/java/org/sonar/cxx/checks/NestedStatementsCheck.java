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
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.api.CxxKeyword;
import static org.sonar.cxx.checks.utils.CheckUtils.isIfStatement;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * NestedStatementsCheck
 *
 */
@Rule(
  key = "NestedStatements",
  name = "Control flow statements \"if\", \"switch\", \"try\" and iterators should not be nested too deeply",
  tags = {Tag.BRAIN_OVERLOAD},
  priority = Priority.MAJOR
)
@ActivatedByDefault
@SqaleConstantRemediation("10min")
public class NestedStatementsCheck extends SquidCheck<Grammar> {

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[]{
    CxxGrammarImpl.selectionStatement,
    CxxGrammarImpl.tryBlock,
    CxxGrammarImpl.iterationStatement
  };

  private static final int DEFAULT_MAX = 3;

  /**
   * max
   */
  @RuleProperty(
    key = "max",
    description = "Maximum allowed control flow statement nesting depth.",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  private final Set<AstNode> checkedNodes = new HashSet<>();
  private int nestingLevel;

  @Override
  public void init() {
    subscribeTo(CHECKED_TYPES);
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.isCopyBookOrGeneratedNode() || checkedNodes.contains(node)) {
      return;
    }

    List<AstNode> watchedDescendants = node.getDescendants(CHECKED_TYPES);

    // In the AST 'else if' blocks are technically nested, but should not increase the nesting level as they are
    // actually flat in terms of 'spaghetti code'. This bypasses the nesting increment/decrement for such blocks.
    if (isElseIf(node)) {
      visitChildren(watchedDescendants);
    } else {
      nestingLevel++;

      // If the max level is reached, stop descending the tree and create a violation
      if (nestingLevel == max + 1) {
        getContext().createLineViolation(
          this,
          "Refactor this code to not nest more than " + max + " if/switch/try/for/while/do statements.",
          node);
      } else {
        visitChildren(watchedDescendants);
      }

      nestingLevel--;
    }

    // Prevent re-checking of descendant nodes
    checkedNodes.addAll(watchedDescendants);
  }

  private void visitChildren(List<AstNode> watchedDescendants) {
    for (AstNode descendant : watchedDescendants) {
      visitNode(descendant);
    }
  }

  /**
   * @return True if the given node is the 'if' in an 'else if' construct.
   */
  private static boolean isElseIf(AstNode node) {
    return isIfStatement(node) && node.getParent().getPreviousAstNode().getType().equals(CxxKeyword.ELSE);
  }
}
