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

import java.io.File;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxUnitTestResultsAggregatorTest {

  private CxxLanguage language;
  private final String key1 = "sonar.cxx." + UnitTestConfiguration.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY;
  private final String key2 = "sonar.cxx." + UnitTestConfiguration.XUNIT_TEST_RESULTS_PROPERTY_KEY;
  private final String key3 = "sonar.cxx." + UnitTestConfiguration.NUNIT_TEST_RESULTS_PROPERTY_KEY;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(UnitTestConfiguration.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY))
      .thenReturn(key1);
    when(language.getPluginProperty(UnitTestConfiguration.XUNIT_TEST_RESULTS_PROPERTY_KEY))
      .thenReturn(key2);
    when(language.getPluginProperty(UnitTestConfiguration.NUNIT_TEST_RESULTS_PROPERTY_KEY))
      .thenReturn(key3);
  }

  @Test
  public void hasUnitTestResultsProperty() {

    Configuration settings = mock(Configuration.class);
    when(settings.hasKey(key1)).thenReturn(false);
    when(settings.hasKey(key2)).thenReturn(false);
    when(settings.hasKey(key3)).thenReturn(false);
    assertThat(new CxxUnitTestResultsAggregator(language, settings).hasUnitTestResultsProperty()).isFalse();

    when(settings.hasKey(key1)).thenReturn(true);
    when(settings.hasKey(key2)).thenReturn(false);
    when(settings.hasKey(key3)).thenReturn(false);
    assertThat(new CxxUnitTestResultsAggregator(language, settings).hasUnitTestResultsProperty()).isTrue();

    when(settings.hasKey(key1)).thenReturn(false);
    when(settings.hasKey(key2)).thenReturn(true);
    when(settings.hasKey(key3)).thenReturn(false);
    assertThat(new CxxUnitTestResultsAggregator(language, settings).hasUnitTestResultsProperty()).isTrue();

    when(settings.hasKey(key1)).thenReturn(false);
    when(settings.hasKey(key2)).thenReturn(false);
    when(settings.hasKey(key3)).thenReturn(true);
    assertThat(new CxxUnitTestResultsAggregator(language, settings).hasUnitTestResultsProperty()).isTrue();

    when(settings.hasKey(key1)).thenReturn(true);
    when(settings.hasKey(key2)).thenReturn(true);
    when(settings.hasKey(key3)).thenReturn(true);
    assertThat(new CxxUnitTestResultsAggregator(language, settings).hasUnitTestResultsProperty()).isTrue();
  }

  @Test
  public void aggregate() {
    WildcardPatternFileProvider wildcardPatternFileProvider = mock(WildcardPatternFileProvider.class);
    MapSettings settings = new MapSettings();
    UnitTestConfiguration unitTestConf = new UnitTestConfiguration(language);

    // Visual Studio test results only
    settings.setProperty(key1, "foo.trx");
    when(wildcardPatternFileProvider.listFiles("foo.trx")).thenReturn(new HashSet<>(Collections.singletonList(new File("foo.trx"))));
    VisualStudioTestResultsFileParser visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    XUnitTestResultsFileParser xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    NUnitTestResultsFileParser nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    UnitTestResults results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(unitTestConf, settings.asConfig(),
      visualStudioTestResultsFileParser, xunitTestResultsFileParser, nunitTestResultsFileParser)
      .aggregate(wildcardPatternFileProvider, results);
    verify(visualStudioTestResultsFileParser).accept(new File("foo.trx"), results);

    // XUnit test results only
    settings.clear();
    settings.setProperty(key2, "foo.xml");
    when(wildcardPatternFileProvider.listFiles("foo.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File("foo.xml"))));
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(unitTestConf, settings.asConfig(),
      visualStudioTestResultsFileParser, xunitTestResultsFileParser, nunitTestResultsFileParser)
      .aggregate(wildcardPatternFileProvider, results);
    verify(visualStudioTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
    verify(xunitTestResultsFileParser).accept(new File("foo.xml"), results);
    verify(nunitTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(UnitTestResults.class));

    // All configured
    settings.clear();
    settings.setProperty(key1, "foo.trx");
    when(wildcardPatternFileProvider.listFiles("foo.trx")).thenReturn(new HashSet<>(asList(new File("foo.trx"))));
    settings.setProperty(key2, "foo.xml");
    when(wildcardPatternFileProvider.listFiles("foo.xml")).thenReturn(new HashSet<>(asList(new File("foo.xml"))));
    settings.setProperty(key3, "foo1.xml");
    when(wildcardPatternFileProvider.listFiles("foo1.xml")).thenReturn(new HashSet<>(asList(new File("foo1.xml"))));
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(unitTestConf, settings.asConfig(),
      visualStudioTestResultsFileParser, xunitTestResultsFileParser, nunitTestResultsFileParser)
      .aggregate(wildcardPatternFileProvider, results);
    verify(visualStudioTestResultsFileParser).accept(new File("foo.trx"), results);
    verify(xunitTestResultsFileParser).accept(new File("foo.xml"), results);
    verify(nunitTestResultsFileParser).accept(new File("foo1.xml"), results);

    // None configured
    settings.clear();
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(unitTestConf, settings.asConfig(),
      visualStudioTestResultsFileParser, xunitTestResultsFileParser, nunitTestResultsFileParser)
      .aggregate(wildcardPatternFileProvider, results);
    verify(visualStudioTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
    verify(xunitTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
    verify(nunitTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
    
    // Multiple files configured
    Mockito.reset(wildcardPatternFileProvider);
    settings.clear();
    settings.setProperty(key1, ",*.trx  ,bar.trx");
    when(wildcardPatternFileProvider.listFiles("*.trx")).thenReturn(new HashSet<>(Collections.singletonList(new File("foo.trx"))));
    when(wildcardPatternFileProvider.listFiles("bar.trx")).thenReturn(new HashSet<>(Collections.singletonList(new File("bar.trx"))));
    settings.setProperty(key2, ",foo2.xml  ,bar2.xml");
    when(wildcardPatternFileProvider.listFiles("foo2.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File("foo2.xml"))));
    when(wildcardPatternFileProvider.listFiles("bar2.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File("bar2.xml"))));
    settings.setProperty(key3, ",foo3.xml  ,bar3.xml");
    when(wildcardPatternFileProvider.listFiles("foo3.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File("foo3.xml"))));
    when(wildcardPatternFileProvider.listFiles("bar3.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File("bar3.xml"))));
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);

    new CxxUnitTestResultsAggregator(unitTestConf, settings.asConfig(),
      visualStudioTestResultsFileParser, xunitTestResultsFileParser, nunitTestResultsFileParser)
      .aggregate(wildcardPatternFileProvider, results);

    verify(wildcardPatternFileProvider).listFiles("*.trx");
    verify(wildcardPatternFileProvider).listFiles("bar.trx");

    verify(visualStudioTestResultsFileParser).accept(new File("foo.trx"), results);
    verify(visualStudioTestResultsFileParser).accept(new File("bar.trx"), results);
    verify(xunitTestResultsFileParser).accept(new File("foo2.xml"), results);
    verify(xunitTestResultsFileParser).accept(new File("bar2.xml"), results);
    verify(nunitTestResultsFileParser).accept(new File("foo3.xml"), results);
    verify(nunitTestResultsFileParser).accept(new File("bar3.xml"), results);
  }

}
