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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.xunit.MSTestResultsProvider;
import org.sonar.plugins.cxx.xunit.MSTestResultsProvider.MSTestResultsAggregator;
import org.sonar.plugins.cxx.xunit.MSTestResultsProvider.MSTestResultsImportSensor;
import org.sonar.plugins.dotnet.tests.UnitTestConfiguration;

public class MSTestResultsProviderTest {
  private Project project;
//  private SensorContext context;
  private Settings settings;
  private MSTestResultsAggregator unitTestResultsAggregator;
//  private DefaultFileSystem fs;
//  String pathPrefix = "/org/sonar/plugins/cxx/reports-project/vs-test-report/";
  private final String visualStudioTestResultsFilePropertyKey = "valid.trx";
  private static final UnitTestConfiguration UNIT_TEST_CONF = new UnitTestConfiguration(MSTestResultsProvider.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY);

  @Before
  public void setUp() {
    settings = new Settings();
//    context = mock(SensorContext.class);

  }

  @Test
  public void testMSTestResult() {
//  	fs = new DefaultFileSystem();
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/reports-project/vs-test-report");
    project = TestUtils.mockProject(baseDir);
    unitTestResultsAggregator = new MSTestResultsAggregator(settings);

//    when(unitTestResultsAggregator.hasUnitTestResultsProperty()).thenReturn(true);
//    assertThat(new MSTestResultsImportSensor(unitTestResultsAggregator).shouldExecuteOnProject(project)).isTrue();
//
//    when(unitTestResultsAggregator.hasUnitTestResultsProperty()).thenReturn(false);
//    assertThat(new MSTestResultsImportSensor(unitTestResultsAggregator).shouldExecuteOnProject(project)).isFalse();
  }

}
