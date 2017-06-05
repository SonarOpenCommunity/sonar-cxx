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
package org.sonar.cxx;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.config.Settings;
import org.sonar.api.internal.apachecommons.lang.builder.HashCodeBuilder;

/**
 * {@inheritDoc}
 */
public abstract class CxxLanguage extends AbstractLanguage {
  public static final String ERROR_RECOVERY_KEY = "errorRecoveryEnabled";
  private final Settings settings;
  private final Map<String, Metric> MetricsCache;

  public CxxLanguage(String key, Settings settings) {
    super(key);
    this.settings = settings;
    this.MetricsCache = new HashMap<>();
  }
  
  public CxxLanguage(String key, String name, Settings settings) {
    super(key, name);
    this.settings = settings;
    this.MetricsCache = new HashMap<>();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
          appendSuper(super.hashCode()).
          append(getKey()).
          toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (this.getClass() == obj.getClass()) {
      return getKey().equals(((AbstractLanguage) obj).getKey()); 
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
//  @Override
//  public abstract String[] getFileSuffixes();

  public abstract String[] getSourceFileSuffixes();

  public abstract String[] getHeaderFileSuffixes();
  
  public abstract String getPropertiesKey();
  
  public abstract List<Class> getChecks();  
  public abstract String getRepositoryKey();

  public String getRepositorySuffix() {
    return ""; //NOSONAR
  }
  
  public String getPluginProperty(String key) {
    return "sonar." + getPropertiesKey() + "." + key;
  }

  public boolean getBooleanOption(String key) {
    return this.settings.getBoolean(getPluginProperty(key));
  }
  
  public String getStringOption(String key) {
    return this.settings.getString(getPluginProperty(key));
  }
  
  public String[] getStringArrayOption(String key) {
    return this.settings.getStringArray(getPluginProperty(key));
  }  
        
  public boolean IsRecoveryEnabled() {
    return this.settings.getBoolean(getPluginProperty(ERROR_RECOVERY_KEY));
  }
  
  public String[] getStringLinesOption(String key) {
    return this.settings.getStringLines(getPluginProperty(key));
  }    
    
  public boolean hasKey(String key) {
    return this.settings.hasKey(getPluginProperty(key));
  }
  
  public void SaveMetric(Metric metric, String key) {
    if (!MetricsCache.containsKey(key)) {
      MetricsCache.put(key, metric);
    }    
  }
  
  public Collection<Metric> getMetricsCache() {
    return this.MetricsCache.values();
  }

  public Metric getMetric(String metricKey) {
    return this.MetricsCache.get(metricKey);
  } 
}
