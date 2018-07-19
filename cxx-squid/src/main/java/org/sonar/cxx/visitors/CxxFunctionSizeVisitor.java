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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;

public class CxxFunctionSizeVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private static final Logger LOG = Loggers.get(CxxFunctionSizeVisitor.class);

  public static final String FUNCTION_SIZE_THRESHOLD_KEY = "funcsize.threshold";

  private int sizeThreshold = 0;

  private int bigFunctions;
  private int bigFunctionsLoc;
  private int totalLoc;

  public CxxFunctionSizeVisitor(CxxLanguage language) {
    this.sizeThreshold = language.getIntegerOption(FUNCTION_SIZE_THRESHOLD_KEY).orElse(20);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Function size threshold: " + this.sizeThreshold);
    }
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionBody);
  }

  @Override
  public void leaveNode(AstNode node) {
    SourceFunction sourceFunction = (SourceFunction) getContext().peekSourceCode();

    final int lineCount = sourceFunction.getInt(CxxMetric.LINES_OF_CODE_IN_FUNCTION_BODY);

    if (lineCount > this.sizeThreshold) {
      bigFunctions++;
      bigFunctionsLoc += lineCount;
    }
    totalLoc += lineCount;
  }

  @Override
  public void visitFile(AstNode astNode) {
    bigFunctions = 0;
    bigFunctionsLoc = 0;
    totalLoc = 0;

    super.visitFile(astNode);
  }

  @Override
  public void leaveFile(AstNode astNode) {
    super.leaveFile(astNode);

    SourceFile sourceFile = (SourceFile) getContext().peekSourceCode();
    sourceFile.setMeasure(CxxMetric.BIG_FUNCTIONS, bigFunctions);
    sourceFile.setMeasure(CxxMetric.BIG_FUNCTIONS_LOC, bigFunctionsLoc);
    sourceFile.setMeasure(CxxMetric.LOC_IN_FUNCTIONS, totalLoc);
  }

}
