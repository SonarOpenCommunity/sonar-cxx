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
package org.sonar.cxx.sensors.tests.dotnet;

import org.sonar.api.config.Configuration;

public class UnitTestConfiguration {

  public static final String VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY = "sonar.cxx.vstest.reportPaths";
  public static final String NUNIT_TEST_RESULTS_PROPERTY_KEY = "sonar.cxx.nunit.reportPaths";

  private final Configuration config;

  public UnitTestConfiguration(Configuration config) {
    this.config = config;
  }

  boolean hasVisualStudioTestResultsFile() {
    return config.hasKey(VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY);
  }

  boolean hasNUnitTestResultsFile() {
    return config.hasKey(NUNIT_TEST_RESULTS_PROPERTY_KEY);
  }

  boolean hasUnitTestResultsProperty() {
    return hasVisualStudioTestResultsFile() || hasNUnitTestResultsFile();
  }

  String[] getVisualStudioTestResultsFiles() {
    return config.getStringArray(VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY);
  }

  String[] getNUnitTestResultsFiles() {
    return config.getStringArray(NUNIT_TEST_RESULTS_PROPERTY_KEY);
  }

}
