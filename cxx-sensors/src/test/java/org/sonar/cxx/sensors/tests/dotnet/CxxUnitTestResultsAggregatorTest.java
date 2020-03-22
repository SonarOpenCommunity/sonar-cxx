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

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private CxxLanguage language;
  private final String key1 = UnitTestConfiguration.VISUAL_STUDIO_TEST_RESULTS_PROPERTY_KEY;
  private final String key2 = UnitTestConfiguration.XUNIT_TEST_RESULTS_PROPERTY_KEY;
  private final String key3 = UnitTestConfiguration.NUNIT_TEST_RESULTS_PROPERTY_KEY;

  @Before
  public void setUp() {
    language = TestUtils.mockCxxLanguage();
  }

  @Test
  public void hasUnitTestResultsProperty() {

    Configuration config = mock(Configuration.class);
    when(config.hasKey(key1)).thenReturn(false);
    when(config.hasKey(key2)).thenReturn(false);
    when(config.hasKey(key3)).thenReturn(false);
    assertThat(new UnitTestConfiguration(language, config).hasUnitTestResultsProperty()).isFalse();

    when(config.hasKey(key1)).thenReturn(true);
    when(config.hasKey(key2)).thenReturn(false);
    when(config.hasKey(key3)).thenReturn(false);
    assertThat(new UnitTestConfiguration(language, config).hasUnitTestResultsProperty()).isTrue();

    when(config.hasKey(key1)).thenReturn(false);
    when(config.hasKey(key2)).thenReturn(true);
    when(config.hasKey(key3)).thenReturn(false);
    assertThat(new UnitTestConfiguration(language, config).hasUnitTestResultsProperty()).isTrue();

    when(config.hasKey(key1)).thenReturn(false);
    when(config.hasKey(key2)).thenReturn(false);
    when(config.hasKey(key3)).thenReturn(true);
    assertThat(new UnitTestConfiguration(language, config).hasUnitTestResultsProperty()).isTrue();

    when(config.hasKey(key1)).thenReturn(true);
    when(config.hasKey(key2)).thenReturn(true);
    when(config.hasKey(key3)).thenReturn(true);
    assertThat(new UnitTestConfiguration(language, config).hasUnitTestResultsProperty()).isTrue();
  }

  @Test
  public void aggregate() {
    WildcardPatternFileProvider wildcardPatternFileProvider = mock(WildcardPatternFileProvider.class);
    var config = new MapSettings();

    // Visual Studio test results only
    config.setProperty(key1, "foo.trx");
    when(wildcardPatternFileProvider.listFiles("foo.trx")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "foo.trx"))));
    VisualStudioTestResultsFileParser visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    XUnitTestResultsFileParser xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    NUnitTestResultsFileParser nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    UnitTestResults results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(visualStudioTestResultsFileParser, xunitTestResultsFileParser,
                                     nunitTestResultsFileParser).aggregate(wildcardPatternFileProvider, results,
                                                                           new UnitTestConfiguration(language, config
                                                                                                     .asConfig()));
    verify(visualStudioTestResultsFileParser).accept(new File("foo.trx"), results);

    // XUnit test results only
    config.clear();
    config.setProperty(key2, "foo.xml");
    when(wildcardPatternFileProvider.listFiles("foo.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "foo.xml"))));
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(visualStudioTestResultsFileParser, xunitTestResultsFileParser,
                                     nunitTestResultsFileParser).aggregate(wildcardPatternFileProvider, results,
                                                                           new UnitTestConfiguration(language, config
                                                                                                     .asConfig()));
    verify(visualStudioTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(
                                                                      UnitTestResults.class));
    verify(xunitTestResultsFileParser).accept(new File("foo.xml"), results);
    verify(nunitTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(
                                                               UnitTestResults.class));

    // All configured
    config.clear();
    config.setProperty(key1, "foo.trx");
    when(wildcardPatternFileProvider.listFiles("foo.trx")).thenReturn(new HashSet<>(asList(new File("foo.trx"))));
    config.setProperty(key2, "foo.xml");
    when(wildcardPatternFileProvider.listFiles("foo.xml")).thenReturn(new HashSet<>(asList(new File("foo.xml"))));
    config.setProperty(key3, "foo1.xml");
    when(wildcardPatternFileProvider.listFiles("foo1.xml")).thenReturn(new HashSet<>(asList(new File("foo1.xml"))));
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(visualStudioTestResultsFileParser, xunitTestResultsFileParser,
                                     nunitTestResultsFileParser).aggregate(wildcardPatternFileProvider, results,
                                                                           new UnitTestConfiguration(language, config
                                                                                                     .asConfig()));
    verify(visualStudioTestResultsFileParser).accept(new File("foo.trx"), results);
    verify(xunitTestResultsFileParser).accept(new File("foo.xml"), results);
    verify(nunitTestResultsFileParser).accept(new File("foo1.xml"), results);

    // None configured
    config.clear();
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new CxxUnitTestResultsAggregator(visualStudioTestResultsFileParser, xunitTestResultsFileParser,
                                     nunitTestResultsFileParser).aggregate(wildcardPatternFileProvider, results,
                                                                           new UnitTestConfiguration(language, config
                                                                                                     .asConfig()));
    verify(visualStudioTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(
                                                                      UnitTestResults.class));
    verify(xunitTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(
                                                               UnitTestResults.class));
    verify(nunitTestResultsFileParser, Mockito.never()).accept(Mockito.any(File.class), Mockito.any(
                                                               UnitTestResults.class));

    // Multiple files configured
    Mockito.reset(wildcardPatternFileProvider);
    config.clear();
    config.setProperty(key1, ",*.trx  ,bar.trx");
    when(wildcardPatternFileProvider.listFiles("*.trx")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "foo.trx"))));
    when(wildcardPatternFileProvider.listFiles("bar.trx")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "bar.trx"))));
    config.setProperty(key2, ",foo2.xml  ,bar2.xml");
    when(wildcardPatternFileProvider.listFiles("foo2.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "foo2.xml"))));
    when(wildcardPatternFileProvider.listFiles("bar2.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "bar2.xml"))));
    config.setProperty(key3, ",foo3.xml  ,bar3.xml");
    when(wildcardPatternFileProvider.listFiles("foo3.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "foo3.xml"))));
    when(wildcardPatternFileProvider.listFiles("bar3.xml")).thenReturn(new HashSet<>(Collections.singletonList(new File(
      "bar3.xml"))));
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    xunitTestResultsFileParser = mock(XUnitTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);

    new CxxUnitTestResultsAggregator(visualStudioTestResultsFileParser, xunitTestResultsFileParser,
                                     nunitTestResultsFileParser).aggregate(wildcardPatternFileProvider, results,
                                                                           new UnitTestConfiguration(language, config
                                                                                                     .asConfig()));

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
