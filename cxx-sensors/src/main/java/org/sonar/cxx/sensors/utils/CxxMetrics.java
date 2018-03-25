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
package org.sonar.cxx.sensors.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.clangsa.CxxClangSASensor;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidySensor;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;
import org.sonar.cxx.sensors.cppcheck.CxxCppCheckSensor;
import org.sonar.cxx.sensors.drmemory.CxxDrMemorySensor;
import org.sonar.cxx.sensors.other.CxxOtherSensor;
import org.sonar.cxx.sensors.pclint.CxxPCLintSensor;
import org.sonar.cxx.sensors.rats.CxxRatsSensor;
import org.sonar.cxx.sensors.squid.CxxSquidSensor;
import org.sonar.cxx.sensors.valgrind.CxxValgrindSensor;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxSensor;

/**
 * {@inheritDoc}
 */
public class CxxMetrics implements Metrics {

  private final CxxLanguage language;
  private final String domain;

  // Introduce own documentation metrics, after they has been removed from SQ core
  // see https://jira.sonarsource.com/browse/SONAR-8328
  public static final String PUBLIC_API_KEY = "public_api";
  public static final String PUBLIC_UNDOCUMENTED_API_KEY = "public_undocumented_api";
  public static final String PUBLIC_DOCUMENTED_API_DENSITY_KEY = "public_documented_api_density";

  /**
   * CxxMetrics
   *
   * @param language for metrics
   *
   */
  public CxxMetrics(CxxLanguage language) {
    this.language = language;
    this.domain = language.getKey().toUpperCase(Locale.ENGLISH);

    Metric<?> metric = new Metric.Builder(getKey(PUBLIC_API_KEY, language), "Public API", Metric.ValueType.INT)
      .setDescription("Public API")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.FALSE)
      .setDomain(this.domain)
      .create();
    saveMetric(PUBLIC_API_KEY, metric);

    metric = new Metric.Builder(getKey(PUBLIC_DOCUMENTED_API_DENSITY_KEY, language), "Public Documented API (%)", Metric.ValueType.PERCENT)
      .setDescription("Public documented classes and functions balanced by ncloc")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(Boolean.TRUE)
      .setDomain(this.domain)
      .setWorstValue(0.0)
      .setBestValue(100.0)
      .setOptimizedBestValue(true)
      .create();
    saveMetric(PUBLIC_DOCUMENTED_API_DENSITY_KEY, metric);

    metric = new Metric.Builder(getKey(PUBLIC_UNDOCUMENTED_API_KEY, language), "Public Undocumented API", Metric.ValueType.INT)
      .setDescription("Public undocumented classes, functions and variables")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(Boolean.TRUE)
      .setDomain(this.domain)
      .setBestValue(0.0)
      .setDirection(Metric.DIRECTION_WORST)
      .setOptimizedBestValue(true)
      .create();
    saveMetric(PUBLIC_UNDOCUMENTED_API_KEY, metric);

    saveMetric(CxxCompilerSensor.KEY, buildReportMetric(CxxCompilerSensor.KEY, "Compiler issues"));
    saveMetric(CxxCppCheckSensor.KEY, buildReportMetric(CxxCppCheckSensor.KEY, "CppCheck issues"));
    saveMetric(CxxOtherSensor.KEY, buildReportMetric(CxxOtherSensor.KEY, "Other tools issues"));
    saveMetric(CxxPCLintSensor.KEY, buildReportMetric(CxxPCLintSensor.KEY, "PC-Lint issues"));
    saveMetric(CxxRatsSensor.KEY, buildReportMetric(CxxRatsSensor.KEY, "Rats issues"));
    saveMetric(CxxSquidSensor.KEY, buildReportMetric(CxxSquidSensor.KEY, "Squid issues"));
    saveMetric(CxxValgrindSensor.KEY, buildReportMetric(CxxValgrindSensor.KEY, "Valgrind issues"));
    saveMetric(CxxVeraxxSensor.KEY, buildReportMetric(CxxVeraxxSensor.KEY, "Vera issues"));
    saveMetric(CxxDrMemorySensor.KEY, buildReportMetric(CxxDrMemorySensor.KEY, "DrMemory issues"));
    saveMetric(CxxClangSASensor.KEY, buildReportMetric(CxxClangSASensor.KEY, "ClangSA issues"));
    saveMetric(CxxClangTidySensor.KEY, buildReportMetric(CxxClangTidySensor.KEY, "ClangTidy issues"));
  }

  private Metric<?> buildReportMetric(String key, String description) {
    String effectiveKey = CxxMetrics.getKey(key, this.language);
    return new Metric.Builder(effectiveKey, description, Metric.ValueType.INT)
        .setDirection(Metric.DIRECTION_WORST)
        .setQualitative(Boolean.TRUE)
        .setDomain(this.domain)
        .create();
  }

  private Boolean saveMetric(String key, Metric<?> metric) {
    return this.language.SaveMetric(metric, key);
  }

  /**
   * getKey
   *
   * @param key for language
   * @param language for metrics
   * @return effective key
   *
   */
  public static String getKey(String key, CxxLanguage language) {
    return language.getPropertiesKey().toUpperCase(Locale.ENGLISH) + "-" + key.toUpperCase(Locale.ENGLISH);
  }

  @Override
  public List<Metric> getMetrics() {
    return new ArrayList(this.language.getMetricsCache());
  }
}
