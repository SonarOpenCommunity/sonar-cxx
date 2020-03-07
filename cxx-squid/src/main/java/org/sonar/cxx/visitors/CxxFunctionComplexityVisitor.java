/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.checks.ChecksHelper;

public class CxxFunctionComplexityVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  public static final String FUNCTION_COMPLEXITY_THRESHOLD_KEY = "funccomplexity.threshold";
  private static final Logger LOG = Loggers.get(CxxFunctionComplexityVisitor.class);

  private final int cyclomaticComplexityThreshold;

  private int complexFunctions;
  private int complexFunctionsLoc;

  public CxxFunctionComplexityVisitor(Configuration settings) {
    this.cyclomaticComplexityThreshold = settings.getInt(FUNCTION_COMPLEXITY_THRESHOLD_KEY).orElse(10);
    LOG.debug("Cyclomatic complexity threshold: " + this.cyclomaticComplexityThreshold);
  }

  public static List<PropertyDefinition> properties() {
    String subcateg = "Metrics";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(FUNCTION_COMPLEXITY_THRESHOLD_KEY)
        .defaultValue("10")
        .name("Cyclomatic complexity threshold")
        .description("Cyclomatic complexity threshold used to classify a function as complex")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build()
    ));
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
