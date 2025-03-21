/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.api;

import org.sonar.cxx.squidbridge.measures.MetricDef;

/**
 * Decorator to add a metric definition to SourceCode tree nodes.
 */
public class SourceCodeTreeDecorator {

  private final SourceProject project;

  /**
   * Create a new SourceCodeTreeDecorator with the given SourceProject as root.
   *
   * @param project project node (root) of the SourceCode tree
   *
   * @see SourceCode
   */
  public SourceCodeTreeDecorator(SourceProject project) {
    this.project = project;
  }

  /**
   * Add the given metric definition to all descendants of the SourceProject node in the SourceCode tree.
   *
   * @param metrics metric definition to add
   */
  public void decorateWith(MetricDef... metrics) {
    decorateWith(project, metrics);
  }

  private static void decorateWith(SourceCode sourceCode, MetricDef... metrics) {
    if (sourceCode.hasChildren()) {
      for (var child : sourceCode.getChildren()) {
        decorateWith(child, metrics);
      }
    }
    for (var metric : metrics) {
      if (!metric.aggregateIfThereIsAlreadyAValue() && Double.doubleToRawLongBits(sourceCode.getDouble(metric)) != 0) {
        continue;
      }
      if (sourceCode.hasChildren()) {
        for (var child : sourceCode.getChildren()) {
          if (!metric.isCalculatedMetric() && metric.isThereAggregationFormula()) {
            sourceCode.add(metric, child);
          }
        }
      }
    }
  }
}
