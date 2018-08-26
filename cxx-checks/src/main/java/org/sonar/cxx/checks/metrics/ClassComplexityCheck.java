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
package org.sonar.cxx.checks.metrics;

import java.util.Optional;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;

import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;

@Rule(
  key = "ClassComplexity",
  priority = Priority.MAJOR,
  name = "Classes should not be too complex",
  tags = {Tag.BRAIN_OVERLOAD})
@SqaleLinearWithOffsetRemediation(
  coeff = "1min",
  offset = "10min",
  effortToFixDescription = "per complexity point over the threshold")
public class ClassComplexityCheck extends CxxCyclomaticComplexityCheck<Grammar> {

  private static final int DEFAULT_MAXIMUM_CLASS_COMPLEXITY_THRESHOLD = 60;

  @RuleProperty(
    key = "maximumClassComplexityThreshold",
    description = "Max value of complexity allowed in a class",
    defaultValue = "" + DEFAULT_MAXIMUM_CLASS_COMPLEXITY_THRESHOLD)
  private int max = DEFAULT_MAXIMUM_CLASS_COMPLEXITY_THRESHOLD;

  @Override
  protected Optional<AstNodeType> getScopeType() {
    return Optional.of(CxxGrammarImpl.classSpecifier);
  }

  @Override
  protected String getScopeName() {
    return "class";
  }

  @Override
  protected int getMaxComplexity() {
    return max;
  }

  public void setMaxComplexity(int threshold) {
    this.max = threshold;
  }
}
