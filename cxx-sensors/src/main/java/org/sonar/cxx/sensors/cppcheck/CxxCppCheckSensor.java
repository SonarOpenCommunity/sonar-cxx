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
package org.sonar.cxx.sensors.cppcheck;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * Sensor for cppcheck (static code analyzer).
 *
 * @author fbonin
 * @author vhardion
 */
public class CxxCppCheckSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxCppCheckSensor.class);
  public static final String REPORT_PATH_KEY = "cppcheck.reportPath";
  public static final String KEY = "CppCheck";

  private final List<CppcheckParser> parsers = new LinkedList<>();

  /**
   * CxxCppCheckSensor for CppCheck Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxCppCheckSensor(CxxLanguage language) {
    super(language);
    parsers.add(new CppcheckParserV2(this));
    parsers.add(new CppcheckParserV1(this));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " CppCheckSensor")
      .onlyOnLanguage(this.language.getKey())
      .createIssuesForRuleRepository(CxxCppCheckRuleRepository.KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    boolean parsed = false;

    for (CppcheckParser parser : parsers) {
      try {
        parser.processReport(context, report);
        LOG.info("Added report '{}' (parsed by: {})", report, parser);
        parsed = true;
        break;
      } catch (XMLStreamException e) {
        LOG.trace("Report {} cannot be parsed by {}", report, parser, e);
      }
    }

    if (!parsed) {
      LOG.error("Report {} cannot be parsed", report);
    }
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }

  @Override
  protected Optional<CxxMetricsFactory.Key> getMetricKey() {
    return Optional.of(CxxMetricsFactory.Key.CPPCHECK_SENSOR_ISSUES_KEY);
  }
}
