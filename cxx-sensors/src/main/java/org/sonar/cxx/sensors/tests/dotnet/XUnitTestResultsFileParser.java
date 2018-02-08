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
import java.io.IOException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class XUnitTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = Loggers.get(XUnitTestResultsFileParser.class);

  @Override
  public void accept(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the XUnit Test Results file " + file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private final UnitTestResults unitTestResults;

    Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public void parse() {
      try (XmlParserHelper xmlParserHelper = new XmlParserHelper(file)) {

        String tag = xmlParserHelper.nextStartTag();
        if (!"assemblies".equals(tag) && !"assembly".equals(tag)) {
          throw xmlParserHelper.parseError("Expected either an <assemblies> or an <assembly> root tag, but got <"
            + tag + "> instead.");
        }

        do {
          if ("assembly".equals(tag)) {
            handleAssemblyTag(xmlParserHelper);
          }
        } while ((tag = xmlParserHelper.nextStartTag()) != null);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to close report", e);
      }
    }

    private void handleAssemblyTag(XmlParserHelper xmlParserHelper) {
      if (xmlParserHelper.stream().getAttributeCount() == 0) {
        LOG.warn("One of the assemblies contains no test result, please make sure this is expected.");
        return;
      }

      int total = xmlParserHelper.getRequiredIntAttribute("total");
      int passed = xmlParserHelper.getRequiredIntAttribute("passed");
      int failed = xmlParserHelper.getRequiredIntAttribute("failed");
      int skipped = xmlParserHelper.getRequiredIntAttribute("skipped");
      int errors = xmlParserHelper.getIntAttributeOrZero("errors");
      Double executionTime = xmlParserHelper.getDoubleAttribute("time");
      if (executionTime != null) {
        executionTime *= 1000;
      }

      unitTestResults.add(total, passed, skipped, failed, errors, executionTime != null
        ? (long) executionTime.doubleValue() : null);
    }

  }

}
