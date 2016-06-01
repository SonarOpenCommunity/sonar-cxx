/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.tests.dotnet;

import org.sonar.api.config.Settings;
import org.sonar.plugins.dotnet.tests.UnitTestConfiguration;
import org.sonar.plugins.dotnet.tests.UnitTestResultsAggregator;
import org.sonar.plugins.dotnet.tests.UnitTestResultsImportSensor;

public class CxxUnitTestResultsProvider {

  public static final String VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY = "sonar.cxx.vstest.reportsPaths";
  public static final String NUNIT_TEST_RESULTS_PROPERTY_KEY = "sonar.cxx.nunit.reportsPaths";
  public static final String XUNIT_TEST_RESULTS_PROPERTY_KEY = "sonar.cxx.xunit.reportsPaths";
  
  private static final UnitTestConfiguration UNIT_TEST_CONF = new UnitTestConfiguration(
          VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY,
          NUNIT_TEST_RESULTS_PROPERTY_KEY,
          XUNIT_TEST_RESULTS_PROPERTY_KEY);
  
  private CxxUnitTestResultsProvider() {
  }

  public static class CxxUnitTestResultsAggregator extends UnitTestResultsAggregator {

    public CxxUnitTestResultsAggregator(Settings settings) {
      super(UNIT_TEST_CONF, settings);
    }

  }

  public static class CxxUnitTestResultsImportSensor extends UnitTestResultsImportSensor {
    
    public CxxUnitTestResultsImportSensor(CxxUnitTestResultsAggregator unitTestResultsAggregator) {
      super(unitTestResultsAggregator);
    }

  }
}
