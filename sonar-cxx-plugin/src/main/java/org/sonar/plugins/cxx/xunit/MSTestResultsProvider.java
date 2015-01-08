/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.xunit;

import org.sonar.api.config.Settings;
import org.sonar.plugins.dotnet.tests.UnitTestConfiguration;
import org.sonar.plugins.dotnet.tests.UnitTestResultsAggregator;
import org.sonar.plugins.dotnet.tests.UnitTestResultsImportSensor;

public class MSTestResultsProvider {

  public static final String VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY = "sonar.cxx.vstest.reportsPaths";
  private static final UnitTestConfiguration UNIT_TEST_CONF = new UnitTestConfiguration(VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY);
  
  private MSTestResultsProvider() {
  }

  public static class MSTestResultsAggregator extends UnitTestResultsAggregator {

    public MSTestResultsAggregator(Settings settings) {
      super(UNIT_TEST_CONF, settings);
    }

  }

  public static class MSTestResultsImportSensor extends UnitTestResultsImportSensor {
    
    public MSTestResultsImportSensor(MSTestResultsAggregator unitTestResultsAggregator) {
      super(unitTestResultsAggregator);

    }
  }

}
