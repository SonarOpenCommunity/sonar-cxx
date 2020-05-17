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
package org.sonar.cxx.sensors.infer;

import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Sensor for Infer - A static analyzer for Java, C, C++, and Objective-C
 *
 * @author begarco
 */
public class CxxInferSensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.infer.reportPath";
  private static final Logger LOG = Loggers.get(CxxInferSensor.class);

  public static List<PropertyDefinition> properties() {
    return List.of(PropertyDefinition.builder(REPORT_PATH_KEY)
            .name("Infer JSON report(s)")
            .description(
                    "Path to a <a href='https://fbinfer.com/>Infer</a> JSON report, relative to"
                            + " projects root. Only JSON format is supported. If necessary, <a href='https://"
                            + "ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> are at your service."
            )
            .category("CXX External Analyzers")
            .subCategory("Infer")
            .onQualifiers(Qualifiers.PROJECT)
            .multiValues(true)
            .build());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Infer report import")
      .onlyOnLanguage("cxx")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected void processReport(File report) {
    InferParser parser = new InferParser(this);
    try {
      parser.processReport(report);
      LOG.info("Added report '{}' (parsed by: {})", report, parser);
    } catch (FileNotFoundException e) {
      LOG.error("Report {} cannot be found", report);
    }
  }

  @Override
  protected String getReportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxInferRuleRepository.KEY;
  }

}
