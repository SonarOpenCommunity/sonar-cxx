/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.api.measures.Metric;
import org.sonar.cxx.CxxMetrics;

public class CxxMetricsTest {

  @Test
  public void getMetricsTest() {
    List<Metric> list = CxxMetrics.getMetrics();
    assertThat(list).hasSize(12);
  }

  @Test
  public void getMetricTest() {
    Metric<Integer> metric0 = CxxMetrics.getMetric(CxxMetrics.PUBLIC_API_KEY);
    assertThat(metric0).isNotNull();

    Metric<Integer> metric1 = CxxMetrics.getMetric(CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY);
    assertThat(metric1).isNotNull();

    Metric<Double> metric2 = CxxMetrics.getMetric(CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY);
    assertThat(metric2).isNotNull();
  }

}
