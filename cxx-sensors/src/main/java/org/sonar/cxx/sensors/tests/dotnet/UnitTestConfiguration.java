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

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com

import org.sonar.cxx.CxxLanguage;

public class UnitTestConfiguration {

  private final CxxLanguage language;
  public static final String VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY = "vstest.reportsPaths";
  public static final String XUNIT_TEST_RESULTS_PROPERTY_KEY = "xunit.reportsPaths";
  public static final String NUNIT_TEST_RESULTS_PROPERTY_KEY = "nunit.reportsPaths";

  public UnitTestConfiguration(CxxLanguage language) {
    this.language = language;
  }

  String visualStudioTestResultsFilePropertyKey() {
    return language.getPluginProperty(VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY);
  }

  String xunitTestResultsFilePropertyKey() {
    return language.getPluginProperty(XUNIT_TEST_RESULTS_PROPERTY_KEY);
  }

  String nunitTestResultsFilePropertyKey() {
    return language.getPluginProperty(NUNIT_TEST_RESULTS_PROPERTY_KEY);
  }
}
