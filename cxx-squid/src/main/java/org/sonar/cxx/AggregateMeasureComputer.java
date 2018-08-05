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
package org.sonar.cxx;

import java.util.Map;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * SonarQube supports hierarchical multi-module projects. It is not enough to
 * calculate a metric M for the file and/or for the corresponding module. The
 * same metric has to be calculated/propagated/aggregated for all parent modules
 * and the root project.
 *
 * This {@link MeasureComputer} is executed on Compute Engine (server-side). For
 * each metric M in the given set of metrics the sum on the hierarchy level L is
 * calculated. The sum is persisted as aggregated metric M on the level (L-1).
 *
 * Some CXX sensors (see CxxReportSensor) can create issues on the whole module.
 * Such sensors have to aggregate the corresponding module Metric by themselves.
 * {@link AggregateMeasureComputer} doesn't recalculate already aggregated
 * metrics.
 *
 */
public class AggregateMeasureComputer implements MeasureComputer {

  private static final Logger LOG = Loggers.get(AggregateMeasureComputer.class);

  private final String[] metricKeys;

  public AggregateMeasureComputer(String languageKey, String languagePropsKey) {
    final Map<CxxMetricsFactory.Key, Metric<?>> metrics = CxxMetricsFactory.generateMap(languageKey, languagePropsKey);

    metricKeys = new String[] {
        // public API
        metrics.get(CxxMetricsFactory.Key.PUBLIC_API_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.PUBLIC_UNDOCUMENTED_API_KEY).key(),

        // sensors
        metrics.get(CxxMetricsFactory.Key.CLANG_SA_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.CLANG_TIDY_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.VC_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.GCC_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.CPPCHECK_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.DRMEMORY_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.OTHER_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.PCLINT_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.RATS_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.SQUID_SENSOR_ISSUES_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.VALGRIND_SENSOR_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.VERAXX_SENSOR_KEY).key(),

        // complexity
        metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_KEY).key(),

        metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_KEY).key(),
        metrics.get(CxxMetricsFactory.Key.LOC_IN_FUNCTIONS_KEY).key(), };
  }

  public String[] getAggregatedMetrics() {
    return metricKeys.clone();
  }

  private static void compute(MeasureComputerContext context, String metricKey) {
    final Component component = context.getComponent();
    if (component.getType() == Component.Type.FILE) {
      LOG.debug("Component {}: FILE doesn't required an aggregation", component.getKey());
      return;
    }
    final Measure existingMeasure = context.getMeasure(metricKey);
    if (existingMeasure != null) {
      LOG.debug("Component {}: measure {} already calculated, value = {}", component.getKey(), metricKey,
          existingMeasure.getIntValue());
      return;
    }
    Iterable<Measure> childrenMeasures = context.getChildrenMeasures(metricKey);
    if (childrenMeasures == null || !childrenMeasures.iterator().hasNext()) {
      LOG.debug("Component {}: measure {} is not set for children", component.getKey(), metricKey);
      return;
    }
    int aggregation = 0;
    for (Measure childMeasure : childrenMeasures) {
      if (childMeasure != null) {
        aggregation += childMeasure.getIntValue();
      }
    }
    LOG.info("Component {}: add measure {}, value {}", component.getKey(), metricKey, aggregation);
    context.addMeasure(metricKey, aggregation);
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(metricKeys).setOutputMetrics(metricKeys).build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    for (final String metricKey : metricKeys) {
      compute(context, metricKey);
    }
  }
}
