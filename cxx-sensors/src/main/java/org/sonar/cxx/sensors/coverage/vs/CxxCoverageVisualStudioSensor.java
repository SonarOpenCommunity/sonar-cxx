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
package org.sonar.cxx.sensors.coverage.vs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.coverage.CoverageSensor;

public class CxxCoverageVisualStudioSensor extends CoverageSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.vscoveragexml.reportPaths";

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Visual C++ Coverage Report(s)")
        .description(
          "Comma-separated list of paths pointing to coverage reports (absolute or relative to the project base directory)."
          + " Ant patterns are accepted for relative path. The reports have to conform to the `Visual Studio Coverage XML format`."
        )
        .category("CXX External Analyzers")
        .subCategory("Visual C++")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    ));
  }

  public CxxCoverageVisualStudioSensor() {
    super(REPORT_PATH_KEY, new VisualStudioParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Visual Studio XML coverage report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .onlyWhenConfiguration(conf -> conf.hasKey(REPORT_PATH_KEY));
  }

}
