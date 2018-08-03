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
import org.sonar.api.measures.CoreMetrics;
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
 * a pair of existing metrics VALUE_METRIC_KEY and TOTAL_METRIC_KEY it
 * calculates the PERCENT_OF_VALUE_IN_TOTAL_METRIC. This calculation takes place
 * on each hierarchy level of SonarQube project.
 *
 * REQUIREMENT: input metrics VALUE_METRIC_KEY and TOTAL_METRIC_KEY must be
 * already calculated and propagated/aggregated on each level.
 * AggregateMeasureComputer must have already run.
 *
 * See also {@link AggregateMeasureComputer}
 */
public class DensityMeasureComputer implements MeasureComputer {

  private static final Logger LOG = Loggers.get(DensityMeasureComputer.class);

  private final String publicAPIKey;
  private final String publicUndocumentedAPIKey;
  private final String publicDocumentedAPIDensityKey;

  private final String complexFunctionsKey;
  private final String complexFunctionsPercKey;
  private final String complexFunctionsLocKey;
  private final String complexFunctionsLocPercKey;

  private final String bigFunctionsKey;
  private final String bigFunctionsPercKey;
  private final String bigFunctionsLocKey;
  private final String bigFunctionsLocPercKey;

  private final String locInFunctionsKey;

  private final String[] inputMetrics;
  private final String[] outputMetrics;

  public DensityMeasureComputer(String languageKey, String languagePropsKey) {
    final Map<CxxMetricsFactory.Key, Metric<?>> metrics = CxxMetricsFactory.generateMap(languageKey, languagePropsKey);

    publicAPIKey = metrics.get(CxxMetricsFactory.Key.PUBLIC_API_KEY).key();
    publicUndocumentedAPIKey = metrics.get(CxxMetricsFactory.Key.PUBLIC_UNDOCUMENTED_API_KEY).key();
    publicDocumentedAPIDensityKey = metrics.get(CxxMetricsFactory.Key.PUBLIC_DOCUMENTED_API_DENSITY_KEY).key();

    complexFunctionsKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_KEY).key();
    complexFunctionsPercKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_PERC_KEY).key();
    complexFunctionsLocKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_KEY).key();
    complexFunctionsLocPercKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_PERC_KEY).key();

    bigFunctionsKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_KEY).key();
    bigFunctionsPercKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_PERC_KEY).key();
    bigFunctionsLocKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_KEY).key();
    bigFunctionsLocPercKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_PERC_KEY).key();

    locInFunctionsKey = metrics.get(CxxMetricsFactory.Key.LOC_IN_FUNCTIONS_KEY).key();

    inputMetrics = new String[] { publicAPIKey, publicUndocumentedAPIKey, CoreMetrics.FUNCTIONS_KEY, locInFunctionsKey,
        complexFunctionsKey, complexFunctionsLocKey, bigFunctionsKey, bigFunctionsLocKey };
    outputMetrics = new String[] { publicDocumentedAPIDensityKey, complexFunctionsPercKey, complexFunctionsLocPercKey,
        bigFunctionsPercKey, bigFunctionsLocPercKey };
  }

  public String[] getInputMetrics() {
    return inputMetrics.clone();
  }

  public String[] getOutputMetrics() {
    return outputMetrics.clone();
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(inputMetrics).setOutputMetrics(outputMetrics).build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    compute(context, publicUndocumentedAPIKey, publicAPIKey, publicDocumentedAPIDensityKey, true);
    compute(context, complexFunctionsKey, CoreMetrics.FUNCTIONS_KEY, complexFunctionsPercKey, false);
    compute(context, complexFunctionsLocKey, locInFunctionsKey, complexFunctionsLocPercKey, false);
    compute(context, bigFunctionsKey, CoreMetrics.FUNCTIONS_KEY, bigFunctionsPercKey, false);
    compute(context, bigFunctionsLocKey, locInFunctionsKey, bigFunctionsLocPercKey, false);
  }

  private static void compute(MeasureComputerContext context, String valueKey, String totalKey, String densityKey,
      boolean calculateReminingPercent) {
    final Component component = context.getComponent();

    final Measure valueMeasure = context.getMeasure(valueKey);
    final Measure totalMeasure = context.getMeasure(totalKey);
    if (valueMeasure == null || totalMeasure == null) {
      LOG.error("Component {}: not enough data to calcualte measure {}", context.getComponent().getKey(), densityKey);
      return;
    }
    final Measure existingMeasure = context.getMeasure(densityKey);
    if (existingMeasure != null) {
      LOG.error("Component {}: measure {} already calculated, value = {}", component.getKey(), densityKey,
          existingMeasure.getDoubleValue());
      return;
    }

    int value = valueMeasure.getIntValue();
    final int total = totalMeasure.getIntValue();
    if (calculateReminingPercent) {
      value = Integer.max(total - value, 0);
    }

    double density = 0.0;
    if (total >= value && total != 0) {
      density = (double) value / (double) total * 100.0;
    }

    LOG.info("Component {}: add measure {}, value {}", component.getKey(), densityKey, density);
    context.addMeasure(densityKey, density);
  }

}
