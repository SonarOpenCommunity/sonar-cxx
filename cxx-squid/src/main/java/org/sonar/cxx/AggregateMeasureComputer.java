/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * SonarQube supports hierarchical multi-module projects. It is not enough to calculate a metric M for the file and/or
 * for the corresponding module. The same metric has to be calculated/propagated/aggregated for all parent modules and
 * the root project.
 *
 * This {@link MeasureComputer} is executed on Compute Engine (server-side). For each metric M in the given set of
 * metrics the sum on the hierarchy level L is calculated. The sum is persisted as aggregated metric M on the level
 * (L-1).
 *
 * Some CXX sensors (see CxxReportSensor) can create issues on the whole module. Such sensors have to aggregate the
 * corresponding module Metric by themselves. {@link AggregateMeasureComputer} doesn't recalculate already aggregated
 * metrics.
 *
 */
public class AggregateMeasureComputer implements MeasureComputer {

  private static final Logger LOG = Loggers.get(AggregateMeasureComputer.class);

  private final String[] metricKeys;

  public AggregateMeasureComputer() {
    metricKeys = new String[]{
      // public API
      CxxMetrics.PUBLIC_API_KEY,
      CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY,
      // complexity
      CxxMetrics.COMPLEX_FUNCTIONS_KEY,
      CxxMetrics.COMPLEX_FUNCTIONS_LOC_KEY,
      CxxMetrics.BIG_FUNCTIONS_KEY,
      CxxMetrics.BIG_FUNCTIONS_LOC_KEY,
      CxxMetrics.LOC_IN_FUNCTIONS_KEY
    };
  }

  private static void compute(MeasureComputerContext context, String metricKey) {
    final Component component = context.getComponent();
    if (component.getType() == Component.Type.FILE) {
      // FILE doesn't required any aggregation. Relevant metrics should be provided by the sensor.
      return;
    }
    final Measure existingMeasure = context.getMeasure(metricKey);
    if (existingMeasure != null) {
      // For all other component types (e.g. PROJECT, MODULE, DIRECTORY) the
      // measurement <metricKey> should not be calculated manually (e.g. in the sensors).
      // Otherwise there is a chance, that your custom calculation won't work properly for
      // multi-module projects.
      LOG.debug("Component {}: measure {} already calculated, value = {}", component.getKey(), metricKey,
                existingMeasure.getIntValue());
      return;
    }
    Iterable<Measure> childrenMeasures = context.getChildrenMeasures(metricKey);
    if (childrenMeasures == null || !childrenMeasures.iterator().hasNext()) {
      // There is always a chance, that required metrics were not calculated for this particular component
      // (e.g. one of modules in a multi-module project doesn't contain any C/C++ data at all).
      // So don't complain about the missing data, but just ignore such components.
      return;
    }
    int aggregation = 0;
    for (var childMeasure : childrenMeasures) {
      if (childMeasure != null) {
        aggregation += childMeasure.getIntValue();
      }
    }
    LOG.debug("Component {}: add measure {}, value {}", component.getKey(), metricKey, aggregation);
    context.addMeasure(metricKey, aggregation);
  }

  public String[] getAggregatedMetrics() {
    return metricKeys.clone();
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(metricKeys).setOutputMetrics(metricKeys).build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    for (var metricKey : metricKeys) {
      compute(context, metricKey);
    }
  }

}
