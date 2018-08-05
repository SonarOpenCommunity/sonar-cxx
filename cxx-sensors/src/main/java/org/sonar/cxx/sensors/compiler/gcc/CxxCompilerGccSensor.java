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
package org.sonar.cxx.sensors.compiler.gcc;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;

public class CxxCompilerGccSensor extends CxxCompilerSensor {

  public static final String KEY = "GCC";
  public static final String REPORT_PATH_KEY = "gcc.reportPath";
  public static final String REPORT_REGEX_DEF = "gcc.regex";
  public static final String REPORT_CHARSET_DEF = "gcc.charset";
  public static final String DEFAULT_CHARSET_DEF = "UTF-8";
  public static final String DEFAULT_REGEX_DEF = "(?<file>.*):(?<line>[0-9]+):[0-9]+:\\x20warning:\\x20(?<message>.*)\\x20\\[(?<id>.*)\\]";

  public CxxCompilerGccSensor(CxxLanguage language) {
    super(language, REPORT_PATH_KEY, CxxCompilerGccRuleRepository.getRepositoryKey(language));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " CxxCompilerGccSensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepositories(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected String getCompilerKey() {
    return KEY;
  }

  @Override
  protected String getCharset(final SensorContext context) {
    return getContextStringProperty(context, getLanguage().getPluginProperty(REPORT_CHARSET_DEF), DEFAULT_CHARSET_DEF);
  }

  @Override
  protected String getRegex(final SensorContext context) {
    return getContextStringProperty(context, getLanguage().getPluginProperty(REPORT_REGEX_DEF), DEFAULT_REGEX_DEF);
  }

  @Override
  protected CxxMetricsFactory.Key getMetricKey() {
    return CxxMetricsFactory.Key.GCC_SENSOR_ISSUES_KEY;
  }

  @Override
  protected String alignId(String id) {
    return id.replaceAll("=$", "");
  }

}
