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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.test.TestComponent;
import org.sonar.api.ce.measure.test.TestComponent.FileAttributesImpl;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinition.MeasureComputerDefinitionBuilderImpl;
import org.sonar.api.ce.measure.test.TestSettings;

public class AggregateMeasureComputerTest {

  private static TestMeasureComputerContext createContext(AggregateMeasureComputer aggregator, Component component) {
    return new TestMeasureComputerContext(component, new TestSettings(),
                                          new MeasureComputerDefinitionBuilderImpl().setInputMetrics(aggregator
                                            .getAggregatedMetrics())
                                            .setOutputMetrics(aggregator.getAggregatedMetrics()).build());
  }

  @Test
  public void metricsNumber() {
    var aggregator = new AggregateMeasureComputer();
    assertThat(aggregator.getAggregatedMetrics().length).isEqualTo(7);
  }

  @Test
  public void ignoreFiles() {
    var aggregator = new AggregateMeasureComputer();

    var file = new TestComponent("file", Type.FILE, new FileAttributesImpl("cxx", false));
    TestMeasureComputerContext context = createContext(aggregator, file);

    context.addChildrenMeasures(CxxMetrics.PUBLIC_API_KEY, 4, 3, 2, 1);
    aggregator.compute(context);

    assertThat(context.getMeasure(CxxMetrics.PUBLIC_API_KEY)).isNull();
  }

  @Test
  public void ignoreAlreadyAggregatedMetric() {
    var aggregator = new AggregateMeasureComputer();

    var module = new TestComponent("module0", Type.MODULE, null);
    TestMeasureComputerContext context = createContext(aggregator, module);

    context.addMeasure(CxxMetrics.PUBLIC_API_KEY, 42);
    context.addChildrenMeasures(CxxMetrics.PUBLIC_API_KEY, 1, 2, 3, 4);
    aggregator.compute(context);

    assertThat(context.getMeasure(CxxMetrics.PUBLIC_API_KEY).getIntValue()).isEqualTo(42);
  }

  @Test
  public void ignoreIfNothingToAggregate() {
    var aggregator = new AggregateMeasureComputer();

    var module = new TestComponent("module0", Type.MODULE, null);
    TestMeasureComputerContext context = createContext(aggregator, module);

    aggregator.compute(context);

    assertThat(context.getMeasure(CxxMetrics.PUBLIC_API_KEY)).isNull();
  }

  @Test
  public void aggregate() {
    var aggregator = new AggregateMeasureComputer();

    var module = new TestComponent("module0", Type.MODULE, null);
    TestMeasureComputerContext context = createContext(aggregator, module);
    context.addChildrenMeasures(CxxMetrics.PUBLIC_API_KEY, 1, 2, 3, 4);
    aggregator.compute(context);

    assertThat(context.getMeasure(CxxMetrics.PUBLIC_API_KEY).getIntValue()).isEqualTo(10);
  }

}
