/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.Plugin;
import org.sonar.cxx.AggregateMeasureComputer;
import org.sonar.cxx.DensityMeasureComputer;
import org.sonar.cxx.postjobs.FinalReport;
import org.sonar.cxx.prejobs.XlstSensor;
import org.sonar.cxx.sensors.clangsa.CxxClangSARuleRepository;
import org.sonar.cxx.sensors.clangsa.CxxClangSASensor;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidyRuleRepository;
import org.sonar.cxx.sensors.clangtidy.CxxClangTidySensor;
import org.sonar.cxx.sensors.compiler.gcc.CxxCompilerGccRuleRepository;
import org.sonar.cxx.sensors.compiler.gcc.CxxCompilerGccSensor;
import org.sonar.cxx.sensors.compiler.vc.CxxCompilerVcRuleRepository;
import org.sonar.cxx.sensors.compiler.vc.CxxCompilerVcSensor;
import org.sonar.cxx.sensors.coverage.bullseye.CxxCoverageBullseyeSensor;
import org.sonar.cxx.sensors.coverage.cobertura.CxxCoverageCoberturaSensor;
import org.sonar.cxx.sensors.coverage.ctc.CxxCoverageTestwellCtcTxtSensor;
import org.sonar.cxx.sensors.coverage.vs.CxxCoverageVisualStudioSensor;
import org.sonar.cxx.sensors.cppcheck.CxxCppCheckRuleRepository;
import org.sonar.cxx.sensors.cppcheck.CxxCppCheckSensor;
import org.sonar.cxx.sensors.drmemory.CxxDrMemoryRuleRepository;
import org.sonar.cxx.sensors.drmemory.CxxDrMemorySensor;
import org.sonar.cxx.sensors.infer.CxxInferRuleRepository;
import org.sonar.cxx.sensors.infer.CxxInferSensor;
import org.sonar.cxx.sensors.other.CxxOtherRepository;
import org.sonar.cxx.sensors.other.CxxOtherSensor;
import org.sonar.cxx.sensors.pclint.CxxPCLintRuleRepository;
import org.sonar.cxx.sensors.pclint.CxxPCLintSensor;
import org.sonar.cxx.sensors.rats.CxxRatsRuleRepository;
import org.sonar.cxx.sensors.rats.CxxRatsSensor;
import org.sonar.cxx.sensors.tests.dotnet.CxxUnitTestResultsAggregator;
import org.sonar.cxx.sensors.tests.dotnet.CxxUnitTestResultsImportSensor;
import org.sonar.cxx.sensors.tests.xunit.CxxXunitSensor;
import org.sonar.cxx.sensors.utils.RulesDefinitionXmlLoader;
import org.sonar.cxx.sensors.valgrind.CxxValgrindRuleRepository;
import org.sonar.cxx.sensors.valgrind.CxxValgrindSensor;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxRuleRepository;
import org.sonar.cxx.sensors.veraxx.CxxVeraxxSensor;

/**
 * {@inheritDoc}
 */
public final class CxxPlugin implements Plugin {

  /**
   * {@inheritDoc}
   */
  @Override
  public void define(Context context) {
    var l = new ArrayList<Object>();

    // plugin elements
    l.add(CxxLanguage.class);
    l.add(CxxSonarWayProfile.class);
    l.add(CxxRuleRepository.class);

    // reusable elements
    l.addAll(getSensorsImpl());

    // properties elements
    l.addAll(CxxLanguage.properties());
    l.addAll(CxxSquidSensor.properties());
    l.addAll(CxxCppCheckSensor.properties());
    l.addAll(CxxValgrindSensor.properties());
    l.addAll(CxxDrMemorySensor.properties());
    l.addAll(CxxPCLintSensor.properties());
    l.addAll(CxxRatsSensor.properties());
    l.addAll(CxxVeraxxSensor.properties());
    l.addAll(CxxOtherSensor.properties());
    l.addAll(CxxClangTidySensor.properties());
    l.addAll(CxxClangSASensor.properties());
    l.addAll(CxxCoverageBullseyeSensor.properties());
    l.addAll(CxxCoverageCoberturaSensor.properties());
    l.addAll(CxxCoverageTestwellCtcTxtSensor.properties());
    l.addAll(CxxCoverageVisualStudioSensor.properties());
    l.addAll(CxxXunitSensor.properties());
    l.addAll(CxxUnitTestResultsImportSensor.properties());
    l.addAll(CxxCompilerVcSensor.properties());
    l.addAll(CxxCompilerGccSensor.properties());

    context.addExtensions(l);
  }

  static private List<Object> getSensorsImpl() {
    var l = new ArrayList<Object>();

    // utility classes
    l.add(CxxUnitTestResultsAggregator.class);
    l.add(RulesDefinitionXmlLoader.class);

    // metrics
    l.add(CxxMetricDefinition.class);
    // ComputeEngine: propagate metrics through all levels (FILE -> MODULE -> PROJECT)
    l.add(AggregateMeasureComputer.class);
    // ComputeEngine: calculate new metrics from existing ones
    l.add(DensityMeasureComputer.class);

    // pre jobs
    l.add(DroppedPropertiesSensor.class);
    l.add(XlstSensor.class);

    // issue sensors
    l.add(CxxSquidSensor.class);
    l.add(CxxRatsSensor.class);
    l.add(CxxCppCheckSensor.class);
    l.add(CxxInferSensor.class);
    l.add(CxxPCLintSensor.class);
    l.add(CxxDrMemorySensor.class);
    l.add(CxxCompilerGccSensor.class);
    l.add(CxxCompilerVcSensor.class);
    l.add(CxxVeraxxSensor.class);
    l.add(CxxValgrindSensor.class);
    l.add(CxxClangTidySensor.class);
    l.add(CxxClangSASensor.class);
    l.add(CxxOtherSensor.class);

    // test sensors
    l.add(CxxXunitSensor.class);
    l.add(CxxUnitTestResultsImportSensor.class);

    // coverage sensors
    l.add(CxxCoverageBullseyeSensor.class);
    l.add(CxxCoverageCoberturaSensor.class);
    l.add(CxxCoverageTestwellCtcTxtSensor.class);
    l.add(CxxCoverageVisualStudioSensor.class);

    // rule provides
    l.add(CxxRatsRuleRepository.class);
    l.add(CxxCppCheckRuleRepository.class);
    l.add(CxxInferRuleRepository.class);
    l.add(CxxPCLintRuleRepository.class);
    l.add(CxxDrMemoryRuleRepository.class);
    l.add(CxxCompilerVcRuleRepository.class);
    l.add(CxxCompilerGccRuleRepository.class);
    l.add(CxxVeraxxRuleRepository.class);
    l.add(CxxValgrindRuleRepository.class);
    l.add(CxxOtherRepository.class);
    l.add(CxxClangTidyRuleRepository.class);
    l.add(CxxClangSARuleRepository.class);

    // post jobs
    l.add(FinalReport.class);

    return l;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
