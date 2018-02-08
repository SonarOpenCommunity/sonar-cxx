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
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.Metric;
import org.sonar.cxx.CxxLanguage;

public class CxxMetricsTest {

  private MapSettings settings;
  private CxxLanguage language;
  private CxxMetrics metrics;

  public class CxxLanguageImpl extends CxxLanguage {

    public CxxLanguageImpl(Configuration settings) {
      super("c++", "c++", settings);
    }

    @Override
    public String[] getFileSuffixes() {
      return new String[]{};
    }

    @Override
    public String[] getSourceFileSuffixes() {
      return new String[]{};
    }

    @Override
    public String[] getHeaderFileSuffixes() {
      return new String[]{};
    }

    @Override
    public String getPropertiesKey() {
      return "cxx";
    }

    @Override
    public List<Class> getChecks() {
      return new ArrayList<>();
    }

    @Override
    public String getRepositoryKey() {
      return "cxx";
    }

  }

  @Before
  public void setUp() {
    settings = new MapSettings();
    language = new CxxLanguageImpl(settings.asConfig());
    metrics = new CxxMetrics(language);
  }

  @Test
  public void getMetricsTest() {
    List<?> list = metrics.getMetrics();
    assertThat(list.size()).isEqualTo(14);
  }

  @Test
  public void getMetricTest() {
    Metric<?> metric = language.getMetric(CxxMetrics.PUBLIC_API_KEY);
    assertThat(metric).isNotNull();

    metric = language.getMetric(CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY);
    assertThat(metric).isNotNull();

    metric = language.getMetric(CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY);
    assertThat(metric).isNotNull();
  }
}
