/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.checks.metrics;

import com.sonar.cxx.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.tag.Tag;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.visitors.CxxCognitiveComplexityVisitor;
import org.sonar.cxx.visitors.CxxComplexityScope;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.SqaleLinearWithOffsetRemediation;

@Rule(
  key = "FunctionCognitiveComplexity",
  name = "Cognitive Complexity of methods/functions should not be too high",
  tags = {Tag.BRAIN_OVERLOAD},
  priority = Priority.CRITICAL)
@ActivatedByDefault
@SqaleLinearWithOffsetRemediation(
  coeff = "1min",
  offset = "5min",
  effortToFixDescription = "per complexity point above the threshold")
public class FunctionCognitiveComplexityCheck extends CxxCognitiveComplexityVisitor<Grammar> {

  private static final int DEFAULT_MAX = 15;

  @RuleProperty(
    key = "max",
    description = "Maximum complexity allowed",
    defaultValue = "" + DEFAULT_MAX)
  private int max = DEFAULT_MAX;

  public void setMaxComplexity(int max) {
    this.max = max;
  }

  @Override
  protected void analyzeComplexity(CxxComplexityScope scope) {
    if (scope.getComplexity() > max) {
      var msg = new StringBuilder(256);
      msg.append("The Cognitive Complexity of this function is ").append(scope.getComplexity())
        .append(" which is greater than ").append(max).append(" authorized.");

      var issue = new CxxReportIssue(getRuleKey(), null, scope.getStartingLine(), null, msg.toString());
      for (var source : scope.getSources()) {
        issue.addLocation(null, source.getLine(), null, source.getExplanation());
      }
      createMultiLocationViolation(issue);
    }
  }

}
