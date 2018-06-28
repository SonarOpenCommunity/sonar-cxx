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
package org.sonar.cxx.api;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class CxxMetricTest {

  @Test
  public void test() {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(CxxMetric.values()).hasSize(10);

    for (CxxMetric metric : CxxMetric.values()) {
      softly.assertThat(metric.getName()).isEqualTo(metric.name());
      softly.assertThat(metric.isCalculatedMetric()).isFalse();
      softly.assertThat(metric.aggregateIfThereIsAlreadyAValue()).isTrue();
      softly.assertThat(metric.isThereAggregationFormula()).isTrue();
      softly.assertThat(metric.getCalculatedMetricFormula()).isNull();
    }
    softly.assertAll();
  }

}
