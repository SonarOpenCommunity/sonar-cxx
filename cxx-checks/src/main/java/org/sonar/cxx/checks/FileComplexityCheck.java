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
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squid.measures.MetricDef;
import com.sonar.sslr.api.Grammar;

import com.sonar.sslr.squid.checks.AbstractFileComplexityCheck;

@Rule(
    key = "FileCyclomaticComplexity",
    priority = Priority.MAJOR)
public class FileComplexityCheck extends AbstractFileComplexityCheck<Grammar> {
  private static final int DEFAULT_MAX = 200;

  @RuleProperty(defaultValue = "" + DEFAULT_MAX)
  private int max = DEFAULT_MAX;
  

  public void setMax(int max) {
    this.max = max;
  }


  @Override
  public int getMaximumFileComplexity() {
    return this.max;
  }


  @Override
  public MetricDef getComplexityMetric() {
    return CxxMetric.COMPLEXITY;
  }

}
