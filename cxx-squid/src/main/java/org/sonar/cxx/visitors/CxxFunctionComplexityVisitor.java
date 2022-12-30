/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.api.SourceFunction;
import org.sonar.cxx.squidbridge.checks.ChecksHelper;

public class CxxFunctionComplexityVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private static final Logger LOG = Loggers.get(CxxFunctionComplexityVisitor.class);

  private final int cyclomaticComplexityThreshold;

  private int complexFunctions;
  private int complexFunctionsLoc;

  public CxxFunctionComplexityVisitor(CxxSquidConfiguration squidConfig) {
    this.cyclomaticComplexityThreshold = squidConfig.getInt(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES,
                                                            CxxSquidConfiguration.FUNCTION_COMPLEXITY_THRESHOLD)
      .orElse(10);
    LOG.debug("'Complex Functions' metric threshold (cyclomatic complexity): " + this.cyclomaticComplexityThreshold);
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionBody);
  }

  @Override
  public void visitFile(AstNode astNode) {
    complexFunctions = 0;
    complexFunctionsLoc = 0;

    super.visitFile(astNode);
  }

  @Override
  public void leaveFile(AstNode astNode) {
    super.leaveFile(astNode);

    var sourceFile = (SourceFile) getContext().peekSourceCode();
    sourceFile.setMeasure(CxxMetric.COMPLEX_FUNCTIONS, complexFunctions);
    sourceFile.setMeasure(CxxMetric.COMPLEX_FUNCTIONS_LOC, complexFunctionsLoc);
  }

  @Override
  public void leaveNode(AstNode node) {
    var sourceFunction = (SourceFunction) getContext().peekSourceCode();

    var complexity = ChecksHelper.getRecursiveMeasureInt(sourceFunction, CxxMetric.COMPLEXITY);
    var lineCount = sourceFunction.getInt(CxxMetric.LINES_OF_CODE_IN_FUNCTION_BODY);

    if (complexity > this.cyclomaticComplexityThreshold) {
      complexFunctions++;
      complexFunctionsLoc += lineCount;
    }
  }

}
