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
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.checks.ChecksHelper;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "ClassComplexity",
  priority = Priority.MAJOR,
  name = "Classes should not be too complex",
  tags = {Tag.BRAIN_OVERLOAD})
@SqaleLinearWithOffsetRemediation(
  coeff = "1min",
  offset = "10min",
  effortToFixDescription = "per complexity point over the threshold")
public class ClassComplexityCheck extends SquidCheck<Grammar> {

  private static final int DEFAULT_MAXIMUM_CLASS_COMPLEXITY_THRESHOLD = 60;

  @RuleProperty(
    key = "maximumClassComplexityThreshold",
    description = "Max value of complexity allowed in a class",
    defaultValue = "" + DEFAULT_MAXIMUM_CLASS_COMPLEXITY_THRESHOLD)
  private int maximumClassComplexityThreshold = DEFAULT_MAXIMUM_CLASS_COMPLEXITY_THRESHOLD;

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.classSpecifier);
  }

  @Override
  public void leaveNode(AstNode node) {
    SourceClass sourceClass = (SourceClass) getContext().peekSourceCode();
    int complexity = ChecksHelper.getRecursiveMeasureInt(sourceClass, CxxMetric.COMPLEXITY);
    if (complexity > maximumClassComplexityThreshold) {
      getContext().createLineViolation(this,
        "Class has a complexity of {0,number,integer} which is greater than {1,number,integer} authorized.",
        node,
        complexity,
        maximumClassComplexityThreshold);
    }
  }

  public void setMaximumClassComplexityThreshold(int threshold) {
    this.maximumClassComplexityThreshold = threshold;
  }

}
