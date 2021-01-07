/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class VisualStudioTestResultsFileParserTest {

  private static final String REPORT_PATH = "src/test/resources/org/sonar/cxx/sensors/reports-project/MSTest-reports/";

  @Test
  public void no_counters() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
                                              new VisualStudioTestResultsFileParser().accept(new File(REPORT_PATH
                                                                                                        + "no_counters.trx"),
                                                                                             mock(UnitTestResults.class));
                                            });
    assertThat(e).hasMessageContaining("The mandatory <Counters> tag is missing in "
                                         + new File(REPORT_PATH + "no_counters.trx").getAbsolutePath());
  }

  @Test
  public void wrong_passed_number() {
    ParseErrorException e = assertThrows(ParseErrorException.class, () -> {
                                         new VisualStudioTestResultsFileParser().accept(new File(REPORT_PATH
                                                                                                   + "wrong_passed_number.trx"),
                                                                                        mock(
                                                                                          UnitTestResults.class));
                                       });
    assertThat(e).hasMessageContaining("Expected an integer instead of \"foo\" for the attribute \"passed\" in "
                                         + new File(REPORT_PATH + "wrong_passed_number.trx").getAbsolutePath());
  }

  @Test
  public void valid() throws Exception {
    var results = new UnitTestResults();
    new VisualStudioTestResultsFileParser().accept(new File(REPORT_PATH + "valid.trx"), results);

    assertThat(results.tests()).isEqualTo(31);
    assertThat(results.passedPercentage()).isEqualTo(14 * 100.0 / 31);
    assertThat(results.skipped()).isEqualTo(11);
    assertThat(results.failures()).isEqualTo(14);
    assertThat(results.errors()).isEqualTo(3);
    assertThat(results.executionTime()).isEqualTo(816l);
  }

  @Test
  public void valid_missing_attributes() throws Exception {
    var results = new UnitTestResults();
    new VisualStudioTestResultsFileParser().accept(new File(REPORT_PATH + "valid_missing_attributes.trx"), results);

    assertThat(results.tests()).isEqualTo(3);
    assertThat(results.passedPercentage()).isEqualTo(3 * 100.0 / 3);
    assertThat(results.skipped()).isZero();
    assertThat(results.failures()).isZero();
    assertThat(results.errors()).isZero();
  }

}
