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
package org.sonar.cxx.sensors.functioncomplexity;

import static java.util.Arrays.asList;
import java.util.List;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

public class FunctionComplexityMetrics implements Metrics {

  public static final Metric<Integer> COMPLEX_FUNCTIONS = new Metric.Builder("complex_functions", "Complex Functions", Metric.ValueType.INT)
    .setDescription("Number of functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final Metric<Double> COMPLEX_FUNCTIONS_PERC = new Metric.Builder("perc_complex_functions", "Complex Functions (%)", Metric.ValueType.PERCENT)
    .setDescription("% of functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final Metric<Integer> COMPLEX_FUNCTIONS_LOC = new Metric.Builder("loc_in_complex_functions", "Complex Functions Lines of Code", Metric.ValueType.INT)
    .setDescription("Number of lines of code in functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final Metric<Double> COMPLEX_FUNCTIONS_LOC_PERC = new Metric.Builder("perc_loc_in_complex_functions", "Complex Functions Lines of Code (%)", Metric.ValueType.PERCENT)
    .setDescription("% of lines of code in functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  @Override
  public List<Metric> getMetrics() {
    return asList(COMPLEX_FUNCTIONS, COMPLEX_FUNCTIONS_PERC, COMPLEX_FUNCTIONS_LOC, COMPLEX_FUNCTIONS_LOC_PERC);
  }

}
