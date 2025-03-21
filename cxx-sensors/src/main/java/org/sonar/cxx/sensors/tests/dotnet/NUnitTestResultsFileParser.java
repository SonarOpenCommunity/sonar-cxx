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
import java.io.File;
import java.io.IOException;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NUnitTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(NUnitTestResultsFileParser.class);

  @Override
  public void accept(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the NUnit Test Results file '{}'", file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private final UnitTestResults unitTestResults;

    Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    private static boolean checkRootTag(XmlParserHelper xmlParserHelper) {
      try {
        xmlParserHelper.checkRootTag("test-results");
        return true;
      } catch (ParseErrorException e) {
        LOG.warn("One of the assemblies contains no test result, please make sure this is expected.", e);
        return false;
      }
    }

    @CheckForNull
    private static Double readExecutionTimeFromDirectlyNestedTestSuiteTags(XmlParserHelper xmlParserHelper) {
      Double executionTime = null;

      String tag;
      var level = 0;
      while ((tag = xmlParserHelper.nextStartOrEndTag()) != null) {
        if ("<test-suite>".equals(tag)) {
          level++;
          var time = xmlParserHelper.getDoubleAttribute("time");

          if (level == 1 && time != null) {
            if (executionTime == null) {
              executionTime = 0D;
            }
            executionTime += time * 1000;
          }
        } else if ("</test-suite>".equals(tag)) {
          level--;
        }
      }

      return executionTime;
    }

    public void parse() {
      try (var xmlParserHelper = new XmlParserHelper(file)) {
        if (checkRootTag(xmlParserHelper)) {
          handleTestResultsTag(xmlParserHelper);
        }
      } catch (IOException e) {
        throw new IllegalStateException("Unable to close report", e);
      }
    }

    private void handleTestResultsTag(XmlParserHelper xmlParserHelper) {
      var total = xmlParserHelper.getRequiredIntAttribute("total");
      var errors = xmlParserHelper.getRequiredIntAttribute("errors");
      var failures = xmlParserHelper.getRequiredIntAttribute("failures");
      var inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");
      var ignored = xmlParserHelper.getRequiredIntAttribute("ignored");

      var tests = total - inconclusive;
      var passed = total - errors - failures - inconclusive;
      var skipped = inconclusive + ignored;

      Double executionTime = readExecutionTimeFromDirectlyNestedTestSuiteTags(xmlParserHelper);

      unitTestResults.add(tests, passed, skipped, failures, errors,
        executionTime != null ? (long) executionTime.doubleValue() : null);
    }

  }

}
