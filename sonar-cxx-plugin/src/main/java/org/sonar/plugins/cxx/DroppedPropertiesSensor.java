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
package org.sonar.plugins.cxx;

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

  private static final Map<String, String> ALL_REMOVED_PROPERTIES = new HashMap<String, String>() {
    {
      put("sonar.cxx.include_directories", "Use 'sonar.cxx.includeDirectories' instead."); // V0.9.1
      put("sonar.cxx.externalrules.reportPath", "Use 'sonar.cxx.other.reportPath' instead."); // V0.9.1
      put("sonar.cxx.cppncss.reportPath", ""); // V0.9.1
      put("sonar.cxx.other.sqales", ""); // V0.9.6
      put("sonar.cxx.xunit.provideDetails", ""); // V0.9.7
      put("sonar.cxx.coverage.itReportPath", ""); // V0.9.8
      put("sonar.cxx.coverage.overallReportPath", ""); // V0.9.8
      put("sonar.cxx.forceZeroCoverage", ""); // V0.9.8
      put("sonar.cxx.scanOnlySpecifiedSources", ""); // V1.0.0
      put("sonar.cxx.compiler.parser", MSG_COMPILER); // V1.2.0
      put("sonar.cxx.compiler.reportPath", MSG_COMPILER); // V1.2.0
      put("sonar.cxx.compiler.regex", MSG_COMPILER); // V1.2.0
      put("sonar.cxx.compiler.charset", MSG_COMPILER); // V1.2.0
      put("sonar.cxx.missingIncludeWarnings", "Turn debug info on to get the information."); // V1.2.0
      put("sonar.cxx.cFilesPatterns",
          "Define C++ keywords in an own header file and include it with 'sonar.cxx.forceIncludes' instead."); // V2.0.0
      put("sonar.cxx.suffixes.sources", "Use key 'sonar.cxx.file.suffixes' instead."); // V2.0.0
      put("sonar.cxx.suffixes.headers",
          "Use key 'sonar.cxx.file.suffixes' instead. For API detection use 'sonar.cxx.api.file.suffixes'."); // V2.0.0
      put("sonar.cxx.other.xslt.1.stylesheet", "Use 'sonar.cxx.xslt.1.stylesheet' instead."); // V2.0.0
      put("sonar.cxx.other.xslt.1.inputs", "Use 'sonar.cxx.xslt.1.inputs' instead."); // V2.0.0
      put("sonar.cxx.other.xslt.1.outputs", "Use 'sonar.cxx.xslt.1.outputs' instead."); // V2.0.0
    }
  };

  private final AnalysisWarnings analysisWarnings;

  public DroppedPropertiesSensor(AnalysisWarnings analysisWarnings) {
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage("c++")
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
