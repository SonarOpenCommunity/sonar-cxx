/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.cxx.CxxSourceCode;

@Rule(
  key = "MaximumMethodComplexity",
name = "Maximum Method Complexity",
priority = Priority.MAJOR,
description = "<p>This rule ensures that the number of test cases in a file is correclty reported in Sonar. "
+ ".</p>")
public class CxxMaximumComplexity extends CxxAbstractCheck {

  public static org.sonar.api.rules.Rule getMyself() {

    List<org.sonar.api.rules.Rule> rule = new AnnotationRuleParser()
            .parse(CxxChecksRepository.REPOSITORY_KEY, Arrays.<Class>asList(
            CxxMaximumComplexity.class));

    return rule.get(0);
  }
  private static final int DEFAULT_MAX_COMPLEXTIY = 10;
  @RuleProperty(description = "Maximum complexity.", defaultValue = "" + DEFAULT_MAX_COMPLEXTIY)
  private int maximumMethodComplextity = DEFAULT_MAX_COMPLEXTIY;
  private int complexity;
  private int line;
  private String funcName = null;

  private void createNewViolation(org.sonar.api.rules.Rule rule, CxxSourceCode cxxSourceCode, int complexity, String functionName, int lineId) {

    int timeToReachThreshold = complexity - maximumMethodComplextity;
    String message = functionName + " took " + Integer.toString(complexity)
            + " maximum threshold of: "
            + Integer.toString(maximumMethodComplextity);

    createViolation(rule, cxxSourceCode, lineId, message, (double) timeToReachThreshold);
  }

  public void setMaximumMethodComplextity(int threshold) {
    this.maximumMethodComplextity = threshold;
  }

  @Override
  public void validate(org.sonar.api.rules.Rule rule, CxxSourceCode cxxSourceCode) {

    if (funcName != null && complexity > maximumMethodComplextity) {
          createNewViolation(rule,
                  cxxSourceCode,
                  complexity,
                  funcName,
                  line);
    }
  }  

  public void setFunctionComplexity(int methodComplexity) {
    this.complexity = methodComplexity;
  }

  public void setFunctionName(String funcName) {
    this.funcName = funcName;    
  }
  
  public void setLine(int line) {
    this.line = line;    
  }  
}
