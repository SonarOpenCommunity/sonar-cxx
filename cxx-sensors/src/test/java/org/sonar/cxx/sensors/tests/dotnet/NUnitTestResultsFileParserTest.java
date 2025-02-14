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
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import static org.mockito.Mockito.mock;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

class NUnitTestResultsFileParserTest {

  private static final String REPORT_PATH
    = "src/test/resources/org/sonar/cxx/sensors/reports-project/xunit-reports/nunit/";

  @RegisterExtension
  private final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void noCounters() {
    ParseErrorException thrown = catchThrowableOfType(ParseErrorException.class, () -> {
      new NUnitTestResultsFileParser().accept(new File(REPORT_PATH + "no_counters.xml"), mock(UnitTestResults.class));
    });
    assertThat(thrown).hasMessageContaining("Missing attribute \"total\" in element <test-results> in "
      + new File(REPORT_PATH + "no_counters.xml").getAbsolutePath());
  }

  @Test
  void wrongPassedNumber() {
    ParseErrorException thrown = catchThrowableOfType(ParseErrorException.class, () -> {
      new NUnitTestResultsFileParser().accept(new File(REPORT_PATH + "invalid_total.xml"), mock(UnitTestResults.class));
    });
    assertThat(thrown).hasMessageContaining("Expected an integer instead of \"invalid\" for the attribute \"total\" in "
      + new File(REPORT_PATH + "invalid_total.xml").getAbsolutePath());
  }

  @Test
  void valid() {
    var results = new UnitTestResults();
    new NUnitTestResultsFileParser().accept(new File(REPORT_PATH + "valid.xml"), results);

    assertThat(results.tests()).isEqualTo(196);
    assertThat(results.passedPercentage()).isEqualTo(146 * 100.0 / 196);
    assertThat(results.skipped()).isEqualTo(7);
    assertThat(results.failures()).isEqualTo(20);
    assertThat(results.errors()).isEqualTo(30);
    assertThat(results.executionTime()).isEqualTo(51);
  }

  @Test
  void validCommaInDouble() {
    var results = new UnitTestResults();
    new NUnitTestResultsFileParser().accept(new File(REPORT_PATH + "valid_comma_in_double.xml"), results);

    assertThat(results.executionTime()).isEqualTo(1051);
  }

  @Test
  void validNoExecutionTime() {
    var results = new UnitTestResults();
    new NUnitTestResultsFileParser().accept(new File(REPORT_PATH + "valid_no_execution_time.xml"), results);

    assertThat(results.tests()).isEqualTo(196);
    assertThat(results.passedPercentage()).isEqualTo(146 * 100.0 / 196);
    assertThat(results.skipped()).isEqualTo(7);
    assertThat(results.failures()).isEqualTo(20);
    assertThat(results.errors()).isEqualTo(30);
    assertThat(results.executionTime()).isNull();
  }

  @Test
  void empty() {
    var results = new UnitTestResults();
    new NUnitTestResultsFileParser().accept(new File(REPORT_PATH + "empty.xml"), results);

    assertThat(logTester.logs(Level.WARN))
      .contains("One of the assemblies contains no test result, please make sure this is expected.");
    assertThat(results.tests()).isZero();
    assertThat(results.passedPercentage()).isZero();
    assertThat(results.skipped()).isZero();
    assertThat(results.failures()).isZero();
    assertThat(results.errors()).isZero();
    assertThat(results.executionTime()).isNull();
  }

}
