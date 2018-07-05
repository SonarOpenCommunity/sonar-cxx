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
package org.sonar.cxx.visitors;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.checks.ChecksHelper;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;

public class CxxFunctionComplexityVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private static final Logger LOG = Loggers.get(CxxFunctionComplexityVisitor.class);

  public static final String FUNCTION_COMPLEXITY_THRESHOLD_KEY = "funccomplexity.threshold";

  private int cyclomaticComplexityThreshold;

  private int complexFunctions;
  private int complexFunctionsLoc;

  public CxxFunctionComplexityVisitor(CxxLanguage language) {
    this.cyclomaticComplexityThreshold = language.getIntegerOption(FUNCTION_COMPLEXITY_THRESHOLD_KEY).orElse(10);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Cyclomatic complexity threshold: " + this.cyclomaticComplexityThreshold);
    }
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

    SourceFile sourceFile = (SourceFile) getContext().peekSourceCode();
    sourceFile.setMeasure(CxxMetric.COMPLEX_FUNCTIONS, complexFunctions);
    sourceFile.setMeasure(CxxMetric.COMPLEX_FUNCTIONS_LOC, complexFunctionsLoc);
  }

  @Override
  public void leaveNode(AstNode node) {
    SourceFunction sourceFunction = (SourceFunction) getContext().peekSourceCode();

    final int complexity = ChecksHelper.getRecursiveMeasureInt(sourceFunction, CxxMetric.COMPLEXITY);
    final int lineCount = sourceFunction.getInt(CxxMetric.LINES_OF_CODE_IN_FUNCTION_BODY);

    if (complexity > this.cyclomaticComplexityThreshold) {
      complexFunctions++;
      complexFunctionsLoc += lineCount;
    }
  }

}
