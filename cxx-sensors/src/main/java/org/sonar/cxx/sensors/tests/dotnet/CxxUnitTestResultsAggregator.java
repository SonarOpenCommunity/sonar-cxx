/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
import org.sonar.api.scanner.ScannerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CxxUnitTestResultsAggregator (from .Net test library)
 */
@ScannerSide
public class CxxUnitTestResultsAggregator {

  private static final Logger LOG = LoggerFactory.getLogger(CxxUnitTestResultsAggregator.class);

  private final VisualStudioTestResultsFileParser visualStudioTestResultsFileParser;
  private final NUnitTestResultsFileParser nunitTestResultsFileParser;

  /**
   * CxxUnitTestResultsAggregator
   *
   * @param language C or C++
   * @param settings SQ Configuration
   */
  public CxxUnitTestResultsAggregator() {
    this(new VisualStudioTestResultsFileParser(), new NUnitTestResultsFileParser());
  }

  CxxUnitTestResultsAggregator(VisualStudioTestResultsFileParser visualStudioTestResultsFileParser,
                               NUnitTestResultsFileParser nunitTestResultsFileParser) {
    this.visualStudioTestResultsFileParser = visualStudioTestResultsFileParser;
    this.nunitTestResultsFileParser = nunitTestResultsFileParser;
  }

  private static void aggregate(WildcardPatternFileProvider wildcardPatternFileProvider, String[] reportPaths,
                                UnitTestResultsParser parser, UnitTestResults unitTestResults) {
    for (var reportPathPattern : reportPaths) {
      LOG.info("Report path pattern: '{}'", reportPathPattern);
      if (!reportPathPattern.isEmpty()) {
        for (var reportFile : wildcardPatternFileProvider.listFiles(reportPathPattern)) {
          parser.accept(reportFile, unitTestResults);
        }
      }
    }
  }

  UnitTestResults aggregate(WildcardPatternFileProvider wildcardPatternFileProvider, UnitTestResults unitTestResults,
                            UnitTestConfiguration unitTestConf) {
    if (unitTestConf.hasVisualStudioTestResultsFile()) {
      aggregate(wildcardPatternFileProvider, unitTestConf.getVisualStudioTestResultsFiles(),
                visualStudioTestResultsFileParser, unitTestResults);
    }

    if (unitTestConf.hasNUnitTestResultsFile()) {
      aggregate(wildcardPatternFileProvider, unitTestConf.getNUnitTestResultsFiles(), nunitTestResultsFileParser,
                unitTestResults);
    }

    return unitTestResults;
  }

}
