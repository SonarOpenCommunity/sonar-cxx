/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * sonarqube@googlegroups.com
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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.api.SourceFunction;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.squidbridge.checks.ChecksHelper;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(key = "TooManyLinesOfCodeInFunction",
      name = "Avoid too many code lines in a single function",
      priority = Priority.MAJOR,
      tags = {Tag.BRAIN_OVERLOAD})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1h")
public class TooManyLinesOfCodeInFunctionCheck extends SquidCheck<Grammar> {
	  private static final int DEFAULT_MAXIMUM = 50;

	  @RuleProperty(
	      key = "max",
	      description = "Maximum code lines allowed",
	      defaultValue = "" + DEFAULT_MAXIMUM)

	  private int max = DEFAULT_MAXIMUM;

	  @Override
	  public void init() {
	    subscribeTo(CxxGrammarImpl.functionDefinition);
	  }
	  
	  @Override
	  public void leaveNode(AstNode node) {
	    SourceFunction sourceFunction = (SourceFunction) getContext().peekSourceCode();
	    int lineCount = ChecksHelper.getRecursiveMeasureInt(sourceFunction, CxxMetric.LINES_OF_CODE);
	    // LINES_OF_CODE not correct == 7 and should be only 5
	    if (lineCount > max) {
	      getContext().createLineViolation(this,
	          "The number of code lines in this function is {0,number,integer} which is greater than {1,number,integer} authorized.",
	          node,
	          lineCount,
	          max);
	    }
	  }

	  public void setMax(int max) {
	    this.max = max;
	  }

}

