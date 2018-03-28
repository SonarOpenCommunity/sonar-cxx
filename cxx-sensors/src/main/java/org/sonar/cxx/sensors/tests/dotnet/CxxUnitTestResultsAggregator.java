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
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;

/**
 * CxxUnitTestResultsAggregator (from .Net test library)
 */
@ScannerSide
public class CxxUnitTestResultsAggregator {

  private static final String EXIST_CONFIGURATION_PARAMETER = "Exist configuration parameter: '{}':'{}'";
  private static final Logger LOG = Loggers.get(CxxUnitTestResultsAggregator.class);
  private final UnitTestConfiguration unitTestConf;
  private final Configuration settings;
  private final VisualStudioTestResultsFileParser visualStudioTestResultsFileParser;
  private final XUnitTestResultsFileParser xunitTestResultsFileParser;
  private final NUnitTestResultsFileParser nunitTestResultsFileParser;

  /**
   * CxxUnitTestResultsAggregator
   * @param language C or C++
   * @param settings SQ Configuration
   */
  public CxxUnitTestResultsAggregator(CxxLanguage language, Configuration settings) {
    this(new UnitTestConfiguration(language), settings,
      new VisualStudioTestResultsFileParser(),
      new XUnitTestResultsFileParser(),
      new NUnitTestResultsFileParser()
    );
  }

  CxxUnitTestResultsAggregator(UnitTestConfiguration unitTestConf, Configuration settings,
    VisualStudioTestResultsFileParser visualStudioTestResultsFileParser,
    XUnitTestResultsFileParser xunitTestResultsFileParser,
    NUnitTestResultsFileParser nunitTestResultsFileParser
  ) {
    this.unitTestConf = unitTestConf;
    this.settings = settings;
    this.visualStudioTestResultsFileParser = visualStudioTestResultsFileParser;
    this.xunitTestResultsFileParser = xunitTestResultsFileParser;
    this.nunitTestResultsFileParser = nunitTestResultsFileParser;
  }

  boolean hasUnitTestResultsProperty() {
    return hasVisualStudioTestResultsFile() || hasXUnitTestResultsFile() || hasNUnitTestResultsFile();
  }

  private boolean hasVisualStudioTestResultsFile() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(EXIST_CONFIGURATION_PARAMETER, unitTestConf.visualStudioTestResultsFilePropertyKey(),
        settings.hasKey(unitTestConf.visualStudioTestResultsFilePropertyKey()));
    }
    return settings.hasKey(unitTestConf.visualStudioTestResultsFilePropertyKey());
  }

  private boolean hasXUnitTestResultsFile() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(EXIST_CONFIGURATION_PARAMETER, unitTestConf.xunitTestResultsFilePropertyKey(),
        settings.hasKey(unitTestConf.xunitTestResultsFilePropertyKey()));
    }
    return settings.hasKey(unitTestConf.xunitTestResultsFilePropertyKey());
  }

  private boolean hasNUnitTestResultsFile() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(EXIST_CONFIGURATION_PARAMETER, unitTestConf.nunitTestResultsFilePropertyKey(),
        settings.hasKey(unitTestConf.nunitTestResultsFilePropertyKey()));
    }
    return settings.hasKey(unitTestConf.nunitTestResultsFilePropertyKey());
  }

  UnitTestResults aggregate(WildcardPatternFileProvider wildcardPatternFileProvider, UnitTestResults unitTestResults) {
    if (hasVisualStudioTestResultsFile()) {
      aggregate(wildcardPatternFileProvider,
        settings.getStringArray(unitTestConf.visualStudioTestResultsFilePropertyKey()),
        visualStudioTestResultsFileParser, unitTestResults);
    }

    if (hasXUnitTestResultsFile()) {
      aggregate(wildcardPatternFileProvider,
        settings.getStringArray(unitTestConf.xunitTestResultsFilePropertyKey()),
        xunitTestResultsFileParser, unitTestResults);
    }

    if (hasNUnitTestResultsFile()) {
      aggregate(wildcardPatternFileProvider,
        settings.getStringArray(unitTestConf.nunitTestResultsFilePropertyKey()),
        nunitTestResultsFileParser, unitTestResults);
    }

    return unitTestResults;
  }

  private static void aggregate(WildcardPatternFileProvider wildcardPatternFileProvider, String[] reportPaths,
    UnitTestResultsParser parser, UnitTestResults unitTestResults) {
    for (String reportPathPattern : reportPaths) {
      LOG.info("Report path pattern: '{}'", reportPathPattern);
      if (!reportPathPattern.isEmpty()) {
        for (File reportFile : wildcardPatternFileProvider.listFiles(reportPathPattern)) {
          parser.accept(reportFile, unitTestResults);
        }
      }
    }
  }
}
