/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.test.TestComponent;
import org.sonar.api.ce.measure.test.TestComponent.FileAttributesImpl;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinition.MeasureComputerDefinitionBuilderImpl;
import org.sonar.api.ce.measure.test.TestSettings;

public class DensityMeasureComputerTest {

  private static TestMeasureComputerContext createContext(DensityMeasureComputer computer) {
    var component = new TestComponent("file", Type.FILE, new FileAttributesImpl("cxx", false));
    return new TestMeasureComputerContext(component, new TestSettings(), new MeasureComputerDefinitionBuilderImpl()
                                          .setInputMetrics(computer.getInputMetrics()).setOutputMetrics(computer
                                          .getOutputMetrics()).build());
  }

  @Test
  public void metricsNumber() {
    var computer = new DensityMeasureComputer();
    assertThat(computer.getInputMetrics()).hasSize(8);
    assertThat(computer.getOutputMetrics()).hasSize(5);
  }

  @Test
  public void ignoreMissingValue() {
    var computer = new DensityMeasureComputer();
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(CxxMetrics.LOC_IN_FUNCTIONS_KEY, 500);
    computer.compute(context);

    assertThat(context.getMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY)).isNull();
  }

  @Test
  public void ignoreMissingTotal() {
    var computer = new DensityMeasureComputer();
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_KEY, 100);
    computer.compute(context);

    assertThat(context.getMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY)).isNull();
  }

  @Test
  public void ignoreMissingBoth() {
    var computer = new DensityMeasureComputer();
    TestMeasureComputerContext context = createContext(computer);

    computer.compute(context);

    assertThat(context.getMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY)).isNull();
  }

  @Test
  public void ignoreAlreadyCalculated() {
    var computer = new DensityMeasureComputer();
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_KEY, 100);
    context.addInputMeasure(CxxMetrics.LOC_IN_FUNCTIONS_KEY, 500);
    context.addMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY, 42.0);

    computer.compute(context);

    assertThat(context.getMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY).getDoubleValue()).isEqualTo(42.0);
  }

  @Test
  public void calculatePercent() {
    var computer = new DensityMeasureComputer();
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_KEY, 100);
    context.addInputMeasure(CxxMetrics.LOC_IN_FUNCTIONS_KEY, 500);
    computer.compute(context);

    assertThat(context.getMeasure(CxxMetrics.BIG_FUNCTIONS_LOC_PERC_KEY).getDoubleValue()).isEqualTo(100.0 / 500.0
                                                                                                       * 100.0);
  }

  @Test
  public void calculateRemainingPercent() {
    var computer = new DensityMeasureComputer();
    TestMeasureComputerContext context = createContext(computer);

    context.addInputMeasure(CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY, 100);
    context.addInputMeasure(CxxMetrics.PUBLIC_API_KEY, 500);
    computer.compute(context);

    assertThat(context.getMeasure(CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY).getDoubleValue()).isEqualTo(400.0
                                                                                                              / 500.0
                                                                                                              * 100.0);
  }

}
