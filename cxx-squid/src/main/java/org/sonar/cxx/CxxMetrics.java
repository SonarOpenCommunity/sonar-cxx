/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;

public final class CxxMetrics {

  // Introduce own documentation metrics, after they has been removed from SQ core
  // see https://jira.sonarsource.com/browse/SONAR-8328
  public static final String PUBLIC_API_KEY = "CXX-public_api";
  public static final Metric<Integer> PUBLIC_API
                                        = new Metric.Builder(PUBLIC_API_KEY, "Public API", Metric.ValueType.INT)
      .setDescription("Public API")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.FALSE)
      .setDomain("C++")
      .create();

  public static final String PUBLIC_DOCUMENTED_API_DENSITY_KEY = "CXX-public_documented_api_density";
  public static final Metric<Double> PUBLIC_DOCUMENTED_API_DENSITY
                                       = new Metric.Builder(PUBLIC_DOCUMENTED_API_DENSITY_KEY,
                                                            "Public Documented API (%)",
                                                            Metric.ValueType.PERCENT)
      .setDescription("Public documented classes and functions balanced by ncloc")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(Boolean.TRUE)
      .setDomain("C++")
      .setWorstValue(0.0)
      .setBestValue(100.0)
      .setOptimizedBestValue(Boolean.TRUE)
      .create();

  public static final String PUBLIC_UNDOCUMENTED_API_KEY = "CXX-public_undocumented_api";
  public static final Metric<Integer> PUBLIC_UNDOCUMENTED_API = new Metric.Builder(PUBLIC_UNDOCUMENTED_API_KEY,
                                                                                   "Public Undocumented API",
                                                                                   Metric.ValueType.INT)
    .setDescription("Public undocumented classes, functions and variables")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.TRUE)
    .setDomain("C++")
    .setBestValue(0.0)
    .setOptimizedBestValue(Boolean.TRUE)
    .create();

  // Introduce additional complexity metrics
  public static final String COMPLEX_FUNCTIONS_KEY = "CXX-complex_functions";
  public static final Metric<Integer> COMPLEX_FUNCTIONS = new Metric.Builder(COMPLEX_FUNCTIONS_KEY,
                                                                             "Complex Functions",
                                                                             Metric.ValueType.INT)
    .setDescription("Number of functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final String COMPLEX_FUNCTIONS_PERC_KEY = "CXX-perc_complex_functions";
  public static final Metric<Double> COMPLEX_FUNCTIONS_PERC = new Metric.Builder(COMPLEX_FUNCTIONS_PERC_KEY,
                                                                                 "Complex Functions (%)",
                                                                                 Metric.ValueType.PERCENT)
    .setDescription("% of functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final String COMPLEX_FUNCTIONS_LOC_KEY = "CXX-loc_in_complex_functions";
  public static final Metric<Integer> COMPLEX_FUNCTIONS_LOC = new Metric.Builder(COMPLEX_FUNCTIONS_LOC_KEY,
                                                                                 "Complex Functions Lines of Code",
                                                                                 Metric.ValueType.INT)
    .setDescription("Number of lines of code in functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final String COMPLEX_FUNCTIONS_LOC_PERC_KEY = "CXX-perc_loc_in_complex_functions";
  public static final Metric<Double> COMPLEX_FUNCTIONS_LOC_PERC = new Metric.Builder(
    COMPLEX_FUNCTIONS_LOC_PERC_KEY,
    "Complex Functions Lines of Code (%)",
    Metric.ValueType.PERCENT)
    .setDescription("% of lines of code in functions with high cyclomatic complexity")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_COMPLEXITY)
    .create();

  public static final String BIG_FUNCTIONS_KEY = "CXX-big_functions";
  public static final Metric<Integer> BIG_FUNCTIONS = new Metric.Builder(BIG_FUNCTIONS_KEY,
                                                                         "Big Functions",
                                                                         Metric.ValueType.INT)
    .setDescription("Number of functions with too many lines")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_SIZE)
    .create();

  public static final String BIG_FUNCTIONS_LOC_KEY = "CXX-loc_in_big_functions";
  public static final Metric<Integer> BIG_FUNCTIONS_LOC = new Metric.Builder(BIG_FUNCTIONS_LOC_KEY,
                                                                             "Big Functions Lines of Code",
                                                                             Metric.ValueType.INT)
    .setDescription("Number of lines of code in functions with too many lines")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_SIZE)
    .create();

  public static final String BIG_FUNCTIONS_PERC_KEY = "CXX-perc_big_functions";
  public static final Metric<Double> BIG_FUNCTIONS_PERC = new Metric.Builder(BIG_FUNCTIONS_PERC_KEY,
                                                                             "Big Functions (%)",
                                                                             Metric.ValueType.PERCENT)
    .setDescription("% of functions with too many lines")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_SIZE)
    .create();

  public static final String BIG_FUNCTIONS_LOC_PERC_KEY = "CXX-perc_loc_in_big_functions";
  public static final Metric<Double> BIG_FUNCTIONS_LOC_PERC = new Metric.Builder(BIG_FUNCTIONS_LOC_PERC_KEY,
                                                                                 "Big Functions Lines of Code (%)",
                                                                                 Metric.ValueType.PERCENT)
    .setDescription("% of lines of code in functions with too many lines")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_SIZE)
    .create();

  public static final String LOC_IN_FUNCTIONS_KEY = "CXX-loc_in_functions";
  public static final Metric<Integer> LOC_IN_FUNCTIONS = new Metric.Builder(LOC_IN_FUNCTIONS_KEY,
                                                                            "Lines of Code in Functions",
                                                                            Metric.ValueType.INT)
    .setDescription("Number of lines of code in function bodies")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.FALSE)
    .setDomain(CoreMetrics.DOMAIN_SIZE)
    .create();

  private static final List<Metric> METRICS;

  static {
    METRICS = new LinkedList<>();
    for (var field : CxxMetrics.class.getFields()) {
      if (!Modifier.isTransient(field.getModifiers()) && Metric.class.isAssignableFrom(field.getType())) {
        try {
          Metric metric = (Metric) field.get(null);
          METRICS.add(metric);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException("can not introspect " + CxxMetrics.class + " to get metrics", e);
        }
      }
    }
  }

  private CxxMetrics() {
    // only static stuff
  }

  public static List<Metric> getMetrics() {
    return new ArrayList<>(METRICS);
  }

  public static Metric getMetric(final String key) {
    return METRICS.stream().filter(metric -> metric != null && metric.getKey().equals(key)).findFirst().orElseThrow(
      NoSuchElementException::new);
  }
}
