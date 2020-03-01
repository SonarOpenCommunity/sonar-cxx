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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;

/**
 * {@inheritDoc}
 */
public class CxxLanguage extends AbstractLanguage {

  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  public static final Pattern EOL_PATTERN = Pattern.compile("\\R");
  private final Map<CxxMetricsFactory.Key, Metric<?>> langSpecificMetrics;

  private final String[] sourceSuffixes;
  private final String[] headerSuffixes;
  private final String[] fileSuffixes;

  /**
   * cxx key
   */
  public static final String KEY = "c++";

  /**
   * cxx name
   */
  public static final String NAME = "C++ (Community)";

  /**
   * Default cxx source files suffixes
   */
  public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
  public static final String DEFAULT_C_FILES = "*.c,*.C";

  /**
   * Default cxx header files suffixes
   */
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  public CxxLanguage(Configuration settings) {
    super(KEY);
    this.langSpecificMetrics = Collections.unmodifiableMap(CxxMetricsFactory.generateMap());

    sourceSuffixes = createStringArray(settings.getStringArray("sonar.cxx.suffixes.sources"),
      DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(settings.getStringArray("sonar.cxx.suffixes.headers"),
      DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    return fileSuffixes.clone();
  }

  public String[] getSourceFileSuffixes() {
    return sourceSuffixes.clone();
  }

  public String[] getHeaderFileSuffixes() {
    return headerSuffixes.clone();
  }

  /**
   * Get language specific metric
   *
   * @throws IllegalStateException if metric was not registered
   */
  public <G extends Serializable> Metric<G> getMetric(CxxMetricsFactory.Key metricKey) {
    Metric<G> metric = (Metric<G>) this.langSpecificMetrics.get(metricKey);
    if (metric == null) {
      throw new IllegalStateException("Requested metric " + metricKey + " couldn't be found");
    }
    return metric;
  }

  public static String[] createStringArray(String[] values, String defaultValues) {
    if (values.length == 0) {
      return defaultValues.split(",");
    }
    return values;
  }

  private String[] mergeArrays(String[] array1, String[] array2) {
    String[] result = new String[array1.length + array2.length];
    System.arraycopy(sourceSuffixes, 0, result, 0, array1.length);
    System.arraycopy(headerSuffixes, 0, result, array1.length, array2.length);
    return result;
  }

}
