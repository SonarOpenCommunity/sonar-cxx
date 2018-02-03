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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;

/**
 * {@inheritDoc}
 */
public abstract class CxxLanguage extends AbstractLanguage {

  public static final String ERROR_RECOVERY_KEY = "errorRecoveryEnabled";
  private final Configuration settings;
  private final Map<String, Metric> MetricsCache;

  public CxxLanguage(String key, Configuration settings) {
    super(key);
    this.settings = settings;
    this.MetricsCache = new HashMap<>();
  }

  public CxxLanguage(String key, String name, Configuration settings) {
    super(key, name);
    this.settings = settings;
    this.MetricsCache = new HashMap<>();
  }

  /**
   * {@inheritDoc}
   */
  public abstract String[] getSourceFileSuffixes();

  public abstract String[] getHeaderFileSuffixes();

  public abstract String getPropertiesKey();

  public abstract List<Class> getChecks();

  public abstract String getRepositoryKey();

  public String getRepositorySuffix() {
    return "";
  }

  public String getPluginProperty(String key) {
    return "sonar." + getPropertiesKey() + "." + key;
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
      return value.get().split("\r?\n|\r", -1);
    }
    return new String[0];
  }

  public boolean hasKey(String key) {
    return this.settings.hasKey(getPluginProperty(key));
  }

  public boolean SaveMetric(Metric metric, String key) {
    if (!MetricsCache.containsKey(key)) {
      MetricsCache.put(key, metric);
      return true;
    }
    return false;
  }

  public Collection<?> getMetricsCache() {
    return this.MetricsCache.values();
  }

  public Metric getMetric(String metricKey) {
    return this.MetricsCache.get(metricKey);
  }
}
