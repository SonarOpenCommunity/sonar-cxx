/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.squidbridge.checks.SquidCheck;
import org.sonar.cxx.tag.Tag;

/**
 * TooLongLineCheck - similar Vera++ rule L004 "Line too long"
 *
 */
@Rule(
  key = "TooLongLine",
  name = "Lines of code should not be too long",
  tags = {Tag.BRAIN_OVERLOAD},
  priority = Priority.MINOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class TooLongLineCheck extends SquidCheck<Grammar> {

  private static final int DEFAULT_MAXIMUM_LINE_LENHGTH = 160;
  private static final int DEFAULT_TAB_WIDTH = 8;

  /**
   * maximumLineLength
   */
  @RuleProperty(
    key = "maximumLineLength",
    description = "The maximum authorized line length",
    defaultValue = "" + DEFAULT_MAXIMUM_LINE_LENHGTH)
  public long maximumLineLength = DEFAULT_MAXIMUM_LINE_LENHGTH;

  /**
   * tabWidth
   */
  @RuleProperty(
    key = "tabWidth",
    description = "Number of spaces in a 'tab' character",
    defaultValue = "" + DEFAULT_TAB_WIDTH)
  public int tabWidth = DEFAULT_TAB_WIDTH;

  @Override
  public void visitFile(AstNode astNode) {
    var nr = 0;
    for (var line : getContext().getInputFileLines()) {
      ++nr;
      long length = line.chars().filter(c -> c == '\t').count();
      length = line.length() + length * (tabWidth - 1);
      if (length > maximumLineLength) {
        getContext().createLineViolation(
          this,
          "Split this {0} characters long line (which is greater than {1} authorized).",
          nr, length, maximumLineLength);
      }
    }
  }

}
