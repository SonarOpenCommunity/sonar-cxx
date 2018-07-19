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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;

import java.util.Collections;

/**
 * {@inheritDoc}
 */
public abstract class CxxLanguage extends AbstractLanguage {

  public static final String ERROR_RECOVERY_KEY = "errorRecoveryEnabled";
  private final String propertiesKey;
  private final Configuration settings;
  private final Map<CxxMetricsFactory.Key, Metric<?>> langSpecificMetrics;
  public static final Pattern EOLPattern = Pattern.compile("\\R");

  public CxxLanguage(String key, String propertiesKey, Configuration settings) {
    super(key);
    this.propertiesKey = propertiesKey;
    this.settings = settings;
    this.langSpecificMetrics = Collections.unmodifiableMap(CxxMetricsFactory.generateMap(key, propertiesKey));
  }

  public CxxLanguage(String key, String name, String propertiesKey, Configuration settings) {
    super(key, name);
    this.propertiesKey = propertiesKey;
    this.settings = settings;
    this.langSpecificMetrics = Collections.unmodifiableMap(CxxMetricsFactory.generateMap(key, propertiesKey));
  }

  public String getPropertiesKey() {
    return propertiesKey;
  }

  /**
   * {@inheritDoc}
   */
  public abstract String[] getSourceFileSuffixes();

  public abstract String[] getHeaderFileSuffixes();

  public abstract List<Class> getChecks();

  public abstract String getRepositoryKey();

  public String getRepositorySuffix() {
    return "";
  }

  public String getPluginProperty(String key) {
    return "sonar." + getPropertiesKey() + "." + key;
  }

  public Optional<Integer> getIntegerOption(String key) {
    return this.settings.getInt(getPluginProperty(key));
  }

  public Optional<Boolean> getBooleanOption(String key) {
    return this.settings.getBoolean(getPluginProperty(key));
  }

  public Optional<String> getStringOption(String key) {
    return this.settings.get(getPluginProperty(key));
  }

  public String[] getStringArrayOption(String key) {
    return this.settings.getStringArray(getPluginProperty(key));
  }

  public Optional<Boolean> IsRecoveryEnabled() {
    return this.settings.getBoolean(getPluginProperty(ERROR_RECOVERY_KEY));
  }

  public String[] getStringLinesOption(String key) {
    Optional<String> value = this.settings.get(getPluginProperty(key));
    if (value.isPresent()) {
      return EOLPattern.split(value.get(), -1);
    }
    return new String[0];
  }

  public boolean hasKey(String key) {
    return this.settings.hasKey(getPluginProperty(key));
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
}
