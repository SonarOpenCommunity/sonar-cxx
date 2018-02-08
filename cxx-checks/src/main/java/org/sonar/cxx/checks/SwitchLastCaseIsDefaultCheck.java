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
import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import static org.sonar.cxx.checks.utils.CheckUtils.isSwitchStatement;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "SwitchLastCaseIsDefault",
  name = "Switch statements should end with a default case",
  priority = Priority.MAJOR,
  tags = {Tag.BAD_PRACTICE, Tag.PITFALL})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class SwitchLastCaseIsDefaultCheck extends SquidCheck<Grammar> {

  private static final AstNodeType[] CHECKED_TYPES = new AstNodeType[]{
    CxxGrammarImpl.selectionStatement
  };

  private static final String MISSING_DEFAULT_CASE_MESSAGE = "Add a default case to this switch.";
  private static final String DEFAULT_CASE_IS_NOT_LAST_MESSAGE = "Move this default to the end of the switch.";
  private static final String DEFAULT_CASE_TOKENVALUE = "default";

  @Override
  public void init() {
    subscribeTo(CHECKED_TYPES);
  }

  @Override
  public void visitNode(AstNode node) {
    if (isSwitchStatement(node)) {
      List<AstNode> switchCases = getSwitchCases(node);
      AstNode defaultCase = null;

      for (AstNode switchCase : switchCases) {
        if (switchCase.getTokenValue().equals(DEFAULT_CASE_TOKENVALUE)) {
          defaultCase = switchCase;
          break;
        }
      }

      if (defaultCase == null) {
        getContext().createLineViolation(this, MISSING_DEFAULT_CASE_MESSAGE, node);
      } else if (!defaultCase.equals(switchCases.get(switchCases.size() - 1))) {
        getContext().createLineViolation(this, DEFAULT_CASE_IS_NOT_LAST_MESSAGE, defaultCase);
      }
    }
  }

  private List<AstNode> getSwitchCases(AstNode node) {
    List<AstNode> cases = new ArrayList<>();
    AstNode seq = node.getFirstDescendant(CxxGrammarImpl.statementSeq);

    if (seq != null) {
      getSwitchCases(cases, seq);
    }

    return cases;
  }

  private static void getSwitchCases(List<AstNode> result, AstNode node) {
    if (isSwitchStatement(node)) {
      return;
    }
    if (node.is(CxxGrammarImpl.labeledStatement)) {
      result.add(node);
    }
    if (node.hasChildren()) {
      for (AstNode child : node.getChildren()) {
        getSwitchCases(result, child);
      }
    }
  }

}
