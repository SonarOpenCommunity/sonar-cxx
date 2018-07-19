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
import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.test.TestComponent;
import org.sonar.api.ce.measure.test.TestComponent.FileAttributesImpl;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinition.MeasureComputerDefinitionBuilderImpl;
import org.sonar.api.ce.measure.test.TestSettings;
import org.sonar.api.measures.Metric;

public class DensityMeasureComputerTest {

  static final Map<CxxMetricsFactory.Key, Metric<?>> metrics = CxxMetricsFactory.generateMap("c++", "cxx");

  // percentKey0 = valueKey0 / totalKey0 * 100
  static final String valueKey0 = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_KEY).key();
  static final String totalKey0 = metrics.get(CxxMetricsFactory.Key.LOC_IN_FUNCTIONS_KEY).key();
  static final String percentKey0 = metrics.get(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_PERC_KEY).key();

  // percentKey1 = ( totalKey1 - valueKey1 ) / totalKey1 * 100
  static final String valueKey1 = metrics.get(CxxMetricsFactory.Key.PUBLIC_UNDOCUMENTED_API_KEY).key();
  static final String totalKey1 = metrics.get(CxxMetricsFactory.Key.PUBLIC_API_KEY).key();
  static final String percentKey1 = metrics.get(CxxMetricsFactory.Key.PUBLIC_DOCUMENTED_API_DENSITY_KEY).key();

  private static TestMeasureComputerContext createContext(DensityMeasureComputer computer) {
    final TestComponent component = new TestComponent("file", Type.FILE, new FileAttributesImpl("c++", false));
    return new TestMeasureComputerContext(component, new TestSettings(), new MeasureComputerDefinitionBuilderImpl()
        .setInputMetrics(computer.getInputMetrics()).setOutputMetrics(computer.getOutputMetrics()).build());
  }

  @Test
  public void metricsNumber() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    assertThat(computer.getInputMetrics().length).isEqualTo(8);
    assertThat(computer.getOutputMetrics().length).isEqualTo(5);
  }

  @Test
  public void ignoreMissingValue() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(totalKey0, 500);
    computer.compute(context);

    assertThat(context.getMeasure(percentKey0)).isNull();
  }

  @Test
  public void ignoreMissingTotal() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(valueKey0, 100);
    computer.compute(context);

    assertThat(context.getMeasure(percentKey0)).isNull();
  }

  @Test
  public void ignoreMissingBoth() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    TestMeasureComputerContext context = createContext(computer);

    computer.compute(context);

    assertThat(context.getMeasure(percentKey0)).isNull();
  }

  @Test
  public void ignoreAlreadyCalculated() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(valueKey0, 100);
    context.addInputMeasure(totalKey0, 500);
    context.addMeasure(percentKey0, 42.0);

    computer.compute(context);

    assertThat(context.getMeasure(percentKey0).getDoubleValue()).isEqualTo(42.0);
  }

  @Test
  public void calculatePercent() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(valueKey0, 100);
    context.addInputMeasure(totalKey0, 500);
    computer.compute(context);

    assertThat(context.getMeasure(percentKey0).getDoubleValue()).isEqualTo(100.0 / 500.0 * 100.0);
  }

  @Test
  public void calculateRemainingPercent() {
    final DensityMeasureComputer computer = new DensityMeasureComputer("c++", "cxx");
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(valueKey1, 100);
    context.addInputMeasure(totalKey1, 500);
    computer.compute(context);

    assertThat(context.getMeasure(percentKey1).getDoubleValue()).isEqualTo(400.0 / 500.0 * 100.0);
  }

}
