/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
  
  public static String GetKey(String key, CxxLanguage language) {
    return language.getPropertiesKey().toUpperCase(Locale.ENGLISH) + "-" + key.toUpperCase(Locale.ENGLISH);
  }
  
  public CxxMetrics(CxxLanguage language) {
    this.language = language;
    
    this.BuildMetric(CxxCompilerSensor.COMPILER_KEY, "Compiler issues", language);
    this.BuildMetric(CxxCppCheckSensor.KEY, "CppCheck issues", language);
    this.BuildMetric(CxxOtherSensor.KEY, "Other tools issues", language);
    this.BuildMetric(CxxPCLintSensor.KEY, "PC-Lint issues", language);
    this.BuildMetric(CxxRatsSensor.KEY, "Rats issues", language);    
    this.BuildMetric(CxxSquidSensor.KEY, "Squid issues", language);      
    this.BuildMetric(CxxValgrindSensor.KEY, "Valgrind issues", language);    
    this.BuildMetric(CxxVeraxxSensor.KEY, "Vera issues", language);    
    this.BuildMetric(CxxDrMemorySensor.KEY, "DrMemory issues", language);  
  }
  
  @Override
  public List<Metric> getMetrics() {
    return new ArrayList(this.language.getMetricsCache());
  }

  private void BuildMetric(String key, String description, CxxLanguage language) {
    String effectiveKey = CxxMetrics.GetKey(key, language);
    Metric metric = new Metric.Builder(effectiveKey, description, Metric.ValueType.INT)
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(Boolean.TRUE)
    .setDomain(language.getKey().toUpperCase(Locale.ENGLISH))
    .create();
    
    language.SaveMetric(metric, key);    
  }
}
