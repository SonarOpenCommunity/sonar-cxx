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
package org.sonar.cxx.sensors.tests.dotnet;

import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com

import org.sonar.cxx.CxxLanguage;

public class UnitTestConfiguration {

  private static final String EXIST_CONFIGURATION_PARAMETER = "Exist configuration parameter: '{}':'{}'";
  private static final Logger LOG = Loggers.get(UnitTestConfiguration.class);

  public static final String VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY = "vstest.reportsPaths";
  public static final String XUNIT_TEST_RESULTS_PROPERTY_KEY = "xunit.reportsPaths";
  public static final String NUNIT_TEST_RESULTS_PROPERTY_KEY = "nunit.reportsPaths";

  private final Configuration config;
  private final String vsKeyEffective;
  private final String xUnitKeyEffective;
  private final String nUnitKeyEffective;

  public UnitTestConfiguration(CxxLanguage language, Configuration config) {
    this.config = config;
    vsKeyEffective = language.getPluginProperty(VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY);
    xUnitKeyEffective = language.getPluginProperty(XUNIT_TEST_RESULTS_PROPERTY_KEY);
    nUnitKeyEffective = language.getPluginProperty(NUNIT_TEST_RESULTS_PROPERTY_KEY);
  }

  boolean hasVisualStudioTestResultsFile() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(EXIST_CONFIGURATION_PARAMETER, vsKeyEffective, config.hasKey(vsKeyEffective));
    }
    return config.hasKey(vsKeyEffective);
  }

  boolean hasXUnitTestResultsFile() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(EXIST_CONFIGURATION_PARAMETER, xUnitKeyEffective, config.hasKey(xUnitKeyEffective));
    }
    return config.hasKey(xUnitKeyEffective);
  }

  boolean hasNUnitTestResultsFile() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(EXIST_CONFIGURATION_PARAMETER, nUnitKeyEffective, config.hasKey(nUnitKeyEffective));
    }
    return config.hasKey(nUnitKeyEffective);
  }

  boolean hasUnitTestResultsProperty() {
    return hasVisualStudioTestResultsFile() || hasXUnitTestResultsFile() || hasNUnitTestResultsFile();
  }

  String[] getVisualStudioTestResultsFiles() {
    return config.getStringArray(vsKeyEffective);
  }

  String[] getXUnitTestResultsFiles() {
    return config.getStringArray(xUnitKeyEffective);
  }

  String[] getNUnitTestResultsFiles() {
    return config.getStringArray(nUnitKeyEffective);
  }

}
