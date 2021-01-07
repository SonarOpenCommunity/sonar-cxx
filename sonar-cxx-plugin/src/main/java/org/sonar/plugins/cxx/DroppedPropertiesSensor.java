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
package org.sonar.plugins.cxx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@Phase(name = Phase.Name.PRE)
public class DroppedPropertiesSensor implements ProjectSensor {

  private static final Logger LOG = Loggers.get(DroppedPropertiesSensor.class);

  private static final String MSG_COMPILER = "Use 'sonar.cxx.vc' or 'sonar.cxx.gcc' instead."
                                               + " Use 'sonar.cxx.msbuild' to read includes and defines from MSBuild log file.";

  private static final Map<String, String> ALL_REMOVED_PROPERTIES = initRemovedProperties();
  private final AnalysisWarnings analysisWarnings;

  public DroppedPropertiesSensor(AnalysisWarnings analysisWarnings) {
    this.analysisWarnings = analysisWarnings;
  }

  private static Map<String, String> initRemovedProperties() {
    var map = new HashMap<String, String>();
    map.put("sonar.cxx.include_directories", "Use 'sonar.cxx.includeDirectories' instead."); // V0.9.1
    map.put("sonar.cxx.externalrules.reportPaths", "Use 'sonar.cxx.other.reportPaths' instead."); // V0.9.1
    map.put("sonar.cxx.cppncss.reportPaths", ""); // V0.9.1
    map.put("sonar.cxx.other.sqales", ""); // V0.9.6
    map.put("sonar.cxx.xunit.provideDetails", ""); // V0.9.7
    map.put("sonar.cxx.coverage.itReportPaths", ""); // V0.9.8
    map.put("sonar.cxx.coverage.overallReportPaths", ""); // V0.9.8
    map.put("sonar.cxx.forceZeroCoverage", ""); // V0.9.8
    map.put("sonar.cxx.scanOnlySpecifiedSources", ""); // V1.0.0
    map.put("sonar.cxx.compiler.parser", MSG_COMPILER); // V1.2.0
    map.put("sonar.cxx.compiler.reportPaths", MSG_COMPILER); // V1.2.0
    map.put("sonar.cxx.compiler.regex", MSG_COMPILER); // V1.2.0
    map.put("sonar.cxx.compiler.charset", MSG_COMPILER); // V1.2.0
    map.put("sonar.cxx.missingIncludeWarnings", "Turn debug info on to get the information."); // V1.2.0
    map.put("sonar.cxx.cFilesPatterns",
            "Define C++ keywords in an own header file and include it with 'sonar.cxx.forceIncludes' instead."); // V2.0.0
    map.put("sonar.cxx.suffixes.sources", "Use key 'sonar.cxx.file.suffixes' instead."); // V2.0.0
    map.put("sonar.cxx.suffixes.headers",
            "Use key 'sonar.cxx.file.suffixes' instead. For API detection use 'sonar.cxx.api.file.suffixes'."); // V2.0.0
    map.put("sonar.cxx.other.xslt.1.stylesheet", "Use 'sonar.cxx.xslt.1.stylesheet' instead."); // V2.0.0
    map.put("sonar.cxx.other.xslt.1.inputs", "Use 'sonar.cxx.xslt.1.inputs' instead."); // V2.0.0
    map.put("sonar.cxx.other.xslt.1.outputs", "Use 'sonar.cxx.xslt.1.outputs' instead."); // V2.0.0
    map.put("sonar.cxx.xunit.xsltURL", "Use 'sonar.cxx.xslt.xxx' instead."); // V2.0.0
    map.put("sonar.cxx.clangsa.reportPath", "Use 'sonar.cxx.clangsa.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.clangtidy.reportPath", "Use 'sonar.cxx.clangtidy.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.gcc.reportPath", "Use 'sonar.cxx.gcc.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.vc.reportPath", "Use 'sonar.cxx.vc.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.cppcheck.reportPath", "Use 'sonar.cxx.cppcheck.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.drmemory.reportPath", "Use 'sonar.cxx.drmemory.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.other.reportPath", "Use 'sonar.cxx.other.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.pclint.reportPath", "Use 'sonar.cxx.pclint.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.xunit.reportPath", "Use 'sonar.cxx.xunit.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.valgrind.reportPath", "Use 'sonar.cxx.valgrind.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.vera.reportPath", "Use 'sonar.cxx.vera.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.msbuild.reportPath", "Use 'sonar.cxx.msbuild.reportPaths' instead."); // V2.0.0
    map.put("sonar.cxx.coverage.reportPath", "Use 'sonar.cxx.bullseye.reportPaths'"
                                               + ", 'sonar.cxx.cobertura.reportPaths', 'sonar.cxx.vscoveragexml.reportPaths' or 'sonar.cxx.ctctxt.reportPaths'"
                                             + " instead."); // V2.0.0
    return Collections.unmodifiableMap(map);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage("cxx")
      .onlyWhenConfiguration(configuration -> ALL_REMOVED_PROPERTIES.keySet().stream().anyMatch(configuration::hasKey))
      .name("CXX verify analysis parameters");
  }

  @Override
  public void execute(SensorContext context) {
    ALL_REMOVED_PROPERTIES.forEach((key, info) -> {
      if (context.config().hasKey(key)) {
        String msg = "CXX property '" + key + "' is no longer supported.";
        if (!info.isEmpty()) {
          msg += " " + info;
        }
        analysisWarnings.addUnique(msg);
        LOG.warn(msg);
      }
    });
  }

}
