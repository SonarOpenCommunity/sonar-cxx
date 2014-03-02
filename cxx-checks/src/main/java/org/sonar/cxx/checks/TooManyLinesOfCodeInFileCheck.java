/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import com.sonar.sslr.api.Grammar;
import org.sonar.cxx.api.CxxMetric;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.squid.checks.ChecksHelper;
import com.sonar.sslr.squid.checks.SquidCheck;

@Rule(
    key = "TooManyLinesOfCodeInFile",
    priority = Priority.MAJOR)

//similar Vera++ rule L006 "Source file is too long"
public class TooManyLinesOfCodeInFileCheck extends SquidCheck<Grammar> {
        
  private static final int DEFAULT_MAXIMUM = 2000;

  @RuleProperty(key = "maximumFileLocThreshold",
          defaultValue = "" + DEFAULT_MAXIMUM)
  private int max = DEFAULT_MAXIMUM;
  

  public void SetMax(int max) {
    this.max = max;
  }

  @Override
  public void leaveFile(AstNode astNode) {
    int linesOfCode = ChecksHelper.getRecursiveMeasureInt(getContext().peekSourceCode(), CxxMetric.LINES_OF_CODE);
    if (linesOfCode > max) {
      getContext().createFileViolation(this, "This file has {0} lines of code, which is greater than {1} authorized. Split it into smaller files.", linesOfCode, max);
    }
  }
}

