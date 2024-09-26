/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class VisualStudioTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = Loggers.get(VisualStudioTestResultsFileParser.class);

  @Override
  public void accept(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the Visual Studio Test Results file " + file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private final UnitTestResults unitTestResults;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final Pattern millisecondsPattern = Pattern.compile("(\\.(\\d{0,3}))\\d*+");

    private boolean foundCounters;

    Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    private static void checkRootTag(XmlParserHelper xmlParserHelper) {
      xmlParserHelper.checkRootTag("TestRun");
    }

    public void parse() {
      try (var xmlParserHelper = new XmlParserHelper(file)) {
        checkRootTag(xmlParserHelper);
        dispatchTags(xmlParserHelper);
        if (!foundCounters) {
          throw new IllegalArgumentException("The mandatory <Counters> tag is missing in " + file.getAbsolutePath());
        }
      } catch (IOException e) {
        throw new IllegalStateException("Unable to close report", e);
      }
    }

    private void dispatchTags(XmlParserHelper xmlParserHelper) {
      String tagName;
      while ((tagName = xmlParserHelper.nextStartTag()) != null) {
        if ("Counters".equals(tagName)) {
          handleCountersTag(xmlParserHelper);
        } else if ("Times".equals(tagName)) {
          handleTimesTag(xmlParserHelper);
        }
      }
    }

    private void handleCountersTag(XmlParserHelper xmlParserHelper) {
      foundCounters = true;

      var passed = xmlParserHelper.getIntAttributeOrZero("passed");
      var failed = xmlParserHelper.getIntAttributeOrZero("failed");
      var errors = xmlParserHelper.getIntAttributeOrZero("error");
      var timeout = xmlParserHelper.getIntAttributeOrZero("timeout");
      var aborted = xmlParserHelper.getIntAttributeOrZero("aborted");

      var inconclusive = xmlParserHelper.getIntAttributeOrZero("inconclusive");

      var tests = passed + failed + errors + timeout + aborted;
      var skipped = inconclusive;
      var failures = timeout + failed + aborted;

      unitTestResults.add(tests, passed, skipped, failures, errors, null);
    }

    private void handleTimesTag(XmlParserHelper xmlParserHelper) {
      var start = getRequiredDateAttribute(xmlParserHelper, "start");
      var finish = getRequiredDateAttribute(xmlParserHelper, "finish");
      long duration = finish.getTime() - start.getTime();

      unitTestResults.add(0, 0, 0, 0, 0, duration);
    }

    private Date getRequiredDateAttribute(XmlParserHelper xmlParserHelper, String name) {
      String value = xmlParserHelper.getRequiredAttribute(name);
      try {
        value = keepOnlyMilliseconds(value);
        return dateFormat.parse(value);
      } catch (ParseException e) {
        throw xmlParserHelper.parseError("Expected an valid date and time instead of \"" + value
                                           + "\" for the attribute \"" + name + "\". " + e.getMessage());
      }
    }

    private String keepOnlyMilliseconds(String value) {
      var sb = new StringBuilder(256);

      var matcher = millisecondsPattern.matcher(value);
      var trailingZeros = new StringBuilder(128);
      while (matcher.find()) {
        String milliseconds = matcher.group(2);
        trailingZeros.setLength(0);
        for (var i = 0; i < 3 - milliseconds.length(); i++) {
          trailingZeros.append('0');
        }
        matcher.appendReplacement(sb, "$1" + trailingZeros);
      }
      matcher.appendTail(sb);

      return sb.toString();
    }

  }

}
