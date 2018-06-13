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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "TooManyParameters",
  priority = Priority.MAJOR,
  name = "Functions, methods and lambdas should not have too many parameters",
  tags = {Tag.BRAIN_OVERLOAD}
)
@SqaleConstantRemediation("20min")
@ActivatedByDefault
public class TooManyParametersCheck extends SquidCheck<Grammar> {

  private static final int DEFAULT_MAX = 7;

  @RuleProperty(
    key = "max",
    description = "Maximum parameters allowed",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.parameterDeclarationClause,
      CxxGrammarImpl.lambdaDeclarator,
      CxxGrammarImpl.cliParameterArray);
  }

  @Override
  public void visitNode(AstNode node) {
    int nbParameters = 0;
    AstNode parameterList = node.getFirstChild(CxxGrammarImpl.parameterDeclarationList);
    if (parameterList != null) {
      nbParameters = parameterList.getChildren(CxxGrammarImpl.parameterDeclaration).size();
      if (nbParameters > max) {
        String message = "parameter list has {0} parameters, which is greater than the {1} authorized.";
        getContext().createLineViolation(this, message, node, nbParameters, max);
      }
    }
  }

  public void setMax(int max) {
    this.max = max;
  }
}
