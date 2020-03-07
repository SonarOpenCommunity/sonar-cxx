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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Qualifiers;

/**
 * {@inheritDoc}
 */
public class CxxLanguage extends AbstractLanguage {

  public static final Pattern EOL_PATTERN = Pattern.compile("\\R");
  private final Map<CxxMetricsFactory.Key, Metric<?>> langSpecificMetrics;

  private final String[] sourceSuffixes;
  private final String[] headerSuffixes;
  private final String[] fileSuffixes;

  /**
   * cxx language key
   */
  public static final String KEY = "c++";

  /**
   * cxx language name
   */
  public static final String NAME = "C++ (Community)";

  /**
   * Key of the file suffix parameter
   */
  public static final String SOURCE_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.sources";
  public static final String HEADER_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.headers";

  /**
   * Default cxx source files knows suffixes
   */
  public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";

  /**
   * Default cxx header files knows suffixes
   */
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  public CxxLanguage(Configuration settings) {
    super(KEY);
    this.langSpecificMetrics = Collections.unmodifiableMap(CxxMetricsFactory.generateMap());

    sourceSuffixes = createStringArray(settings.getStringArray(SOURCE_FILE_SUFFIXES_KEY),
      DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(settings.getStringArray(HEADER_FILE_SUFFIXES_KEY),
      DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  public static List<PropertyDefinition> properties() {
    String subcateg = "Cxx Suffixes";
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(SOURCE_FILE_SUFFIXES_KEY)
        .multiValues(true)
        .defaultValue(DEFAULT_SOURCE_SUFFIXES)
        .name("Source files suffixes")
        .description("Comma-separated list of suffixes for source files to analyze. Leave empty to use the default.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(HEADER_FILE_SUFFIXES_KEY)
        .multiValues(true)
        .defaultValue(DEFAULT_HEADER_SUFFIXES)
        .name("Header files suffixes")
        .description("Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.")
        .subCategory(subcateg)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
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
