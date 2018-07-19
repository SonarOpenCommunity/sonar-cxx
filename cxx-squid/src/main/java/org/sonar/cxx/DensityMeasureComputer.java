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

  private final String PublicAPIKey;
  private final String PublicUndocumentedAPIKey;
  private final String PublicDocumentedAPIDensityKey;

  private final String ComplexFunctionsKey;
  private final String ComplexFunctionsPercKey;
  private final String ComplexFunctionsLocKey;
  private final String ComplexFunctionsLocPercKey;

  private final String BigFunctionsKey;
  private final String BigFunctionsPercKey;
  private final String BigFunctionsLocKey;
  private final String BigFunctionsLocPercKey;

  private final String LocInFunctionsKey;

  private final String[] inputMetrics;
  private final String[] outputMetrics;

  public DensityMeasureComputer(String languageKey, String languagePropsKey) {
    final Map<CxxMetricsFactory.Key, Metric<?>> metrics = CxxMetricsFactory.generateMap(languageKey, languagePropsKey);

    PublicAPIKey = metrics.get(CxxMetricsFactory.Key.PUBLIC_API_KEY).key();
    PublicUndocumentedAPIKey = metrics.get(CxxMetricsFactory.Key.PUBLIC_UNDOCUMENTED_API_KEY).key();
    PublicDocumentedAPIDensityKey = metrics.get(CxxMetricsFactory.Key.PUBLIC_DOCUMENTED_API_DENSITY_KEY).key();

    ComplexFunctionsKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_KEY).key();
    ComplexFunctionsPercKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_PERC_KEY).key();
    ComplexFunctionsLocKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_KEY).key();
    ComplexFunctionsLocPercKey = metrics.get(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_PERC_KEY).key();

    BigFunctionsKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_KEY).key();
    BigFunctionsPercKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_PERC_KEY).key();
    BigFunctionsLocKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_KEY).key();
    BigFunctionsLocPercKey = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_PERC_KEY).key();

    LocInFunctionsKey = metrics.get(CxxMetricsFactory.Key.LOC_IN_FUNCTIONS_KEY).key();

    inputMetrics = new String[] { PublicAPIKey, PublicUndocumentedAPIKey, CoreMetrics.FUNCTIONS_KEY, LocInFunctionsKey,
        ComplexFunctionsKey, ComplexFunctionsLocKey, BigFunctionsKey, BigFunctionsLocKey };
    outputMetrics = new String[] { PublicDocumentedAPIDensityKey, ComplexFunctionsPercKey, ComplexFunctionsLocPercKey,
        BigFunctionsPercKey, BigFunctionsLocPercKey };
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
    compute(context, PublicUndocumentedAPIKey, PublicAPIKey, PublicDocumentedAPIDensityKey, true);
    compute(context, ComplexFunctionsKey, CoreMetrics.FUNCTIONS_KEY, ComplexFunctionsPercKey, false);
    compute(context, ComplexFunctionsLocKey, LocInFunctionsKey, ComplexFunctionsLocPercKey, false);
    compute(context, BigFunctionsKey, CoreMetrics.FUNCTIONS_KEY, BigFunctionsPercKey, false);
    compute(context, BigFunctionsLocKey, LocInFunctionsKey, BigFunctionsLocPercKey, false);
  }

  private void compute(MeasureComputerContext context, String valueKey, String totalKey, String densityKey,
      boolean calculateReminingPercent) {
    final Component component = context.getComponent();

    final Measure valueMeasure = context.getMeasure(valueKey);
    final Measure totalMeasure = context.getMeasure(totalKey);
    if (valueMeasure == null || totalMeasure == null) {
      LOG.error("Component {}: not enough data to calcualte measure {}", context.getComponent().getKey(), densityKey);
      return;
    }
    if (context.getMeasure(densityKey) != null) {
      LOG.error("Component {}: measure {} already calculated, value = {}", component.getKey(), densityKey,
          context.getMeasure(densityKey).getDoubleValue());
      return;
    }

    int value = valueMeasure.getIntValue();
    final int total = totalMeasure.getIntValue();
    if (calculateReminingPercent) {
      value = Integer.max(total - value, 0);
    }

    final double density = (total >= value && total != 0) ? (double) value / (double) total * 100.0 : 0.0;

    LOG.info("Component {}: add measure {}, value {}", component.getKey(), densityKey, density);
    context.addMeasure(densityKey, density);
  }

}