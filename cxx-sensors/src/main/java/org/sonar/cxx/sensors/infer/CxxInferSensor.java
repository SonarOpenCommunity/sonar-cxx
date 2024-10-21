/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.infer;

import java.io.File;
import java.util.List;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * Sensor for Infer - A static analyzer for Java, C, C++, and Objective-C
 *
 * @author begarco
 */
public class CxxInferSensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.infer.reportPaths";

  public static List<PropertyDefinition> properties() {
    return List.of(PropertyDefinition.builder(REPORT_PATH_KEY)
      .name("Infer Report(s)")
      .description("""
        Comma-separated paths (absolute or relative to the project base directory) to `*.json` files with \
        `Infer` issues. Ant patterns are accepted for relative paths.""")
      .category(CxxReportSensor.CATEGORY)
      .subCategory("Infer")
      .onQualifiers(Qualifiers.PROJECT)
      .multiValues(true)
      .build());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Infer report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathsKey()));
  }

  @Override
  protected void processReport(File report) {
    var parser = new InferParser(this);
    parser.parse(report);
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxInferRuleRepository.KEY;
  }

}
