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
package org.sonar.cxx.api;

import org.sonar.squidbridge.measures.CalculatedMetricFormula;
import org.sonar.squidbridge.measures.MetricDef;

public enum CxxMetric implements MetricDef {
  FILES,
  LINES,
  LINES_OF_CODE,
  STATEMENTS,
  FUNCTIONS,
  CLASSES,
  COMPLEXITY,
  COMMENT_LINES,
  COMMENT_BLANK_LINES,
  PUBLIC_API,
  PUBLIC_UNDOCUMENTED_API;

  public String getName() {
    return name();
  }

  public boolean isCalculatedMetric() {
    return false;
  }

  public boolean aggregateIfThereIsAlreadyAValue() {
    return true;
  }

  public boolean isThereAggregationFormula() {
    return true;
  }

  public CalculatedMetricFormula getCalculatedMetricFormula() {
    return null;
  }

}
