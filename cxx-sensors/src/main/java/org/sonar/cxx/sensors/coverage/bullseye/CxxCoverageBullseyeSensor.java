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
package org.sonar.cxx.sensors.coverage.bullseye;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.coverage.CoverageSensor;

public class CxxCoverageBullseyeSensor extends CoverageSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.bullseye.reportPaths";

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("BullseyeCoverage Report(s)")
        .description(
          "Comma-separated list of paths pointing to coverage reports (absolute or relative to the project"
            + " base directory). Ant patterns are accepted for relative path. The reports have to conform to the"
            + " `BullseyeCoverage XML format`."
        )
        .category("CXX External Analyzers")
        .subCategory("BullseyeCoverage")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    ));
  }

  public CxxCoverageBullseyeSensor() {
    super(REPORT_PATH_KEY, new BullseyeParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Bullseye coverage report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .onlyWhenConfiguration(conf -> conf.hasKey(REPORT_PATH_KEY));
  }

}
