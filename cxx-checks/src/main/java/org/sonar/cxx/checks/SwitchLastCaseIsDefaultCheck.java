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

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.SquidCheck;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "SwitchLastCaseIsDefault",
  name = "Switch statements should end with a default case",
  priority = Priority.MAJOR)
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class SwitchLastCaseIsDefaultCheck extends SquidCheck<Grammar> {

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[] {
      CxxGrammarImpl.switchStatement
  };

  private static final String MISSING_DEFAULT_CASE_MESSAGE = "Add a default case to this switch.";
  private static final String DEFAULT_CASE_IS_NOT_LAST_MESSAGE = "Move this default to the end of the switch.";
  private static final String DEFAULT_CASE_TOKENVALUE = "default";

  private static final Predicate<AstNode> DEFAULT_CASE_NODE_FILTER = new Predicate<AstNode>() {
    public boolean apply(AstNode childNode) {
      return childNode.getTokenValue().equals(DEFAULT_CASE_TOKENVALUE);
    }
  };

  @Override
  public void init() {
    subscribeTo(CHECKED_TYPES);
  }

  @Override
  public void visitNode(AstNode node) {
    List<AstNode> switchCases = getSwitchCases(node);
    int defaultCaseIndex = Iterables.indexOf(switchCases, DEFAULT_CASE_NODE_FILTER);

    if (defaultCaseIndex == -1) {
      getContext().createLineViolation(this, MISSING_DEFAULT_CASE_MESSAGE, node);
    } else {
      AstNode defaultCase = Iterables.get(switchCases, defaultCaseIndex);

      if (!defaultCase.equals(Iterables.getLast(switchCases))) {
        getContext().createLineViolation(this, DEFAULT_CASE_IS_NOT_LAST_MESSAGE, defaultCase);
      }
    }
  }

  private List<AstNode> getSwitchCases(AstNode node) {
    List<AstNode> cases = Lists.newArrayList();

    for (AstNode stmtGroups : node.getChildren(CxxGrammarImpl.switchBlockStatementGroups)) {
       for (AstNode stmtGroup: stmtGroups.getChildren(CxxGrammarImpl.switchBlockStatementGroup)) {
         for (AstNode label : stmtGroup.getChildren(CxxGrammarImpl.switchLabelStatement)) {
           cases.add(label);
         }
       }
    }

    return cases;
  }

}
