/*
 * C++ Community Plugin (cxx plugin)
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
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * SonarQube supports hierarchical multi-module projects. It is not enough to calculate a metric M for the file and/or
 * for the corresponding module. The same metric has to be calculated/propagated/aggregated for all parent modules and
 * the root project.
 *
 * This {@link MeasureComputer} is executed on Compute Engine (server-side). For a pair of existing metrics
 * VALUE_METRIC_KEY and TOTAL_METRIC_KEY it calculates the PERCENT_OF_VALUE_IN_TOTAL_METRIC. This calculation takes
 * place on each hierarchy level of SonarQube project.
 *
 * REQUIREMENT: input metrics VALUE_METRIC_KEY and TOTAL_METRIC_KEY must be already calculated and propagated/aggregated
 * on each level. AggregateMeasureComputer must have already run.
 *
 * See also {@link AggregateMeasureComputer}
 */
public class DensityMeasureComputer implements MeasureComputer {

  private static final Logger LOG = Loggers.get(DensityMeasureComputer.class);

  private final String[] inputMetrics;
  private final String[] outputMetrics;

  public DensityMeasureComputer() {
    inputMetrics = new String[]{
      CxxMetrics.PUBLIC_API_KEY,
      CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY,
      CoreMetrics.FUNCTIONS_KEY,
      CxxMetrics.LOC_IN_FUNCTIONS_KEY,
      CxxMetrics.COMPLEX_FUNCTIONS_KEY,
      CxxMetrics.COMPLEX_FUNCTIONS_LOC_KEY,
      CxxMetrics.BIG_FUNCTIONS_KEY,
      CxxMetrics.BIG_FUNCTIONS_LOC_KEY
    };
    outputMetrics = new String[]{
      CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY,
      CxxMetrics.COMPLEX_FUNCTIONS_PERC_KEY,
      CxxMetrics.COMPLEX_FUNCTIONS_LOC_PERC_KEY,
      CxxMetrics.BIG_FUNCTIONS_PERC_KEY,
      CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY
    };
  }

  private static void compute(MeasureComputerContext context, String valueKey, String totalKey, String densityKey,
                              boolean calculateReminingPercent) {
    final Component component = context.getComponent();

    final Measure valueMeasure = context.getMeasure(valueKey);
    final Measure totalMeasure = context.getMeasure(totalKey);
    if (valueMeasure == null || totalMeasure == null) {
      // There is always a chance, that required metrics were not calculated for this particular component
      // (e.g. one of modules in a multi-module project doesn't contain any C/C++ data at all).
      // So don't complain about the missing data, but just ignore such components.
      return;
    }
    final Measure existingMeasure = context.getMeasure(densityKey);
    if (existingMeasure != null) {
      // Measurement <densityKey> should not be calculated manually (e.g. in the sensors).
      // Otherwise there is a chance, that your custom calculation won't work properly for
      // multi-module projects.
      LOG.debug("Component {}: measure {} already calculated, value = {}", component.getKey(), densityKey,
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

    LOG.debug("Component {}: add measure {}, value {}", component.getKey(), densityKey, density);
    context.addMeasure(densityKey, density);
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
    compute(context,
            CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY,
            CxxMetrics.PUBLIC_API_KEY,
            CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY,
            true);
    compute(context,
            CxxMetrics.COMPLEX_FUNCTIONS_KEY,
            CoreMetrics.FUNCTIONS_KEY,
            CxxMetrics.COMPLEX_FUNCTIONS_PERC_KEY,
            false);
    compute(context,
            CxxMetrics.COMPLEX_FUNCTIONS_LOC_KEY,
            CxxMetrics.LOC_IN_FUNCTIONS_KEY,
            CxxMetrics.COMPLEX_FUNCTIONS_LOC_PERC_KEY,
            false);
    compute(context,
            CxxMetrics.BIG_FUNCTIONS_KEY,
            CoreMetrics.FUNCTIONS_KEY,
            CxxMetrics.BIG_FUNCTIONS_PERC_KEY,
            false);
    compute(context,
            CxxMetrics.BIG_FUNCTIONS_LOC_KEY,
            CxxMetrics.LOC_IN_FUNCTIONS_KEY,
            CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY,
            false);
  }

}
