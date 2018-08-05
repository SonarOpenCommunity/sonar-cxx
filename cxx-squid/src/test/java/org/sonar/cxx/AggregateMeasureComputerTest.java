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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.test.TestComponent;
import org.sonar.api.ce.measure.test.TestComponent.FileAttributesImpl;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinition.MeasureComputerDefinitionBuilderImpl;
import org.sonar.api.ce.measure.test.TestSettings;
import org.sonar.api.measures.Metric;

public class AggregateMeasureComputerTest {

  static final Map<CxxMetricsFactory.Key, Metric<?>> metrics = CxxMetricsFactory.generateMap("c++", "cxx");
  static final String key = metrics.get(CxxMetricsFactory.Key.PUBLIC_API_KEY).key();

  private static TestMeasureComputerContext createContext(AggregateMeasureComputer aggregator, Component component) {
    return new TestMeasureComputerContext(component, new TestSettings(),
        new MeasureComputerDefinitionBuilderImpl().setInputMetrics(aggregator.getAggregatedMetrics())
            .setOutputMetrics(aggregator.getAggregatedMetrics()).build());
  }

  @Test
  public void metricsNumber() {
    final AggregateMeasureComputer aggregator = new AggregateMeasureComputer("c++", "cxx");
    assertThat(aggregator.getAggregatedMetrics().length).isEqualTo(19);
  }

  @Test
  public void ignoreFiles() {

    final AggregateMeasureComputer aggregator = new AggregateMeasureComputer("c++", "cxx");

    TestComponent file = new TestComponent("file", Type.FILE, new FileAttributesImpl("c++", false));
    TestMeasureComputerContext context = createContext(aggregator, file);

    context.addChildrenMeasures(key, 4, 3, 2, 1);
    aggregator.compute(context);

    assertThat(context.getMeasure(key)).isNull();
  }

  @Test
  public void ignoreAlreadyAggregatedMetric() {
    final AggregateMeasureComputer aggregator = new AggregateMeasureComputer("c++", "cxx");

    TestComponent module = new TestComponent("module0", Type.MODULE, null);
    TestMeasureComputerContext context = createContext(aggregator, module);

    context.addMeasure(key, 42);
    context.addChildrenMeasures(key, 1, 2, 3, 4);
    aggregator.compute(context);

    assertThat(context.getMeasure(key).getIntValue()).isEqualTo(42);
  }

  @Test
  public void ignoreIfNothingToAggregate() {
    final AggregateMeasureComputer aggregator = new AggregateMeasureComputer("c++", "cxx");

    TestComponent module = new TestComponent("module0", Type.MODULE, null);
    TestMeasureComputerContext context = createContext(aggregator, module);

    aggregator.compute(context);

    assertThat(context.getMeasure(key)).isNull();
  }

  @Test
  public void aggregate() {
    final AggregateMeasureComputer aggregator = new AggregateMeasureComputer("c++", "cxx");

    TestComponent module = new TestComponent("module0", Type.MODULE, null);
    TestMeasureComputerContext context = createContext(aggregator, module);
    context.addChildrenMeasures(key, 1, 2, 3, 4);
    aggregator.compute(context);

    assertThat(context.getMeasure(key).getIntValue()).isEqualTo(10);
  }

}
