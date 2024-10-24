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
package org.sonar.cxx.checks.metrics;

import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.utils.CxxReportLocation;
import org.sonar.cxx.visitors.MultiLocatitionSquidCheck;

class FunctionComplexityCheckTest {

  @Test
  void check() throws IOException {
    var check = new FunctionComplexityCheck();
    check.setMaxComplexity(5);
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FunctionComplexity.cc", ".");
    var file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    var issues = MultiLocatitionSquidCheck.getMultiLocationCheckMessages(file);
    assertThat(issues).isNotNull();
    var softly = new SoftAssertions();
    softly.assertThat(issues)
      .hasSize(5)
      .allSatisfy(issue -> assertThat(issue.getRuleId()).isEqualTo("FunctionComplexity"));

    var issue0 = issues.stream()
      .filter(issue -> issue.getLocations().get(0).getLine().equals("13"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 13"));
    softly.assertThat(issue0.getLocations()).containsOnly(
      new CxxReportLocation(null, "13", null,
        "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
      new CxxReportLocation(null, "13", null, "+1: function definition"),
      new CxxReportLocation(null, "14", null, "+1: if statement"),
      new CxxReportLocation(null, "15", null, "+1: if statement"),
      new CxxReportLocation(null, "16", null, "+1: conditional operator"),
      new CxxReportLocation(null, "18", null, "+1: conditional operator"),
      new CxxReportLocation(null, "21", null, "+1: if statement"),
      new CxxReportLocation(null, "22", null, "+1: conditional operator"),
      new CxxReportLocation(null, "24", null, "+1: conditional operator"));

    var issue1 = issues.stream()
      .filter(issue -> issue.getLocations().get(0).getLine().equals("33"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 33"));
    softly.assertThat(issue1.getLocations()).containsOnly(
      new CxxReportLocation(null, "33", null,
        "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
      new CxxReportLocation(null, "33", null, "+1: function definition"),
      new CxxReportLocation(null, "34", null, "+1: if statement"),
      new CxxReportLocation(null, "35", null, "+1: if statement"),
      new CxxReportLocation(null, "36", null, "+1: conditional operator"),
      new CxxReportLocation(null, "38", null, "+1: conditional operator"),
      new CxxReportLocation(null, "41", null, "+1: if statement"),
      new CxxReportLocation(null, "42", null, "+1: conditional operator"),
      new CxxReportLocation(null, "44", null, "+1: conditional operator"));

    var issue2 = issues.stream()
      .filter(issue -> issue.getLocations().get(0).getLine().equals("51"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 51"));
    softly.assertThat(issue2.getLocations()).containsOnly(
      new CxxReportLocation(null, "51", null,
        "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
      new CxxReportLocation(null, "51", null, "+1: function definition"),
      new CxxReportLocation(null, "52", null, "+1: if statement"),
      new CxxReportLocation(null, "53", null, "+1: if statement"),
      new CxxReportLocation(null, "54", null, "+1: conditional operator"),
      new CxxReportLocation(null, "56", null, "+1: conditional operator"),
      new CxxReportLocation(null, "59", null, "+1: if statement"),
      new CxxReportLocation(null, "60", null, "+1: conditional operator"),
      new CxxReportLocation(null, "62", null, "+1: conditional operator"));

    var issue3 = issues.stream()
      .filter(issue -> issue.getLocations().get(0).getLine().equals("72"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 72"));
    softly.assertThat(issue3.getLocations()).containsOnly(
      new CxxReportLocation(null, "72", null,
        "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
      new CxxReportLocation(null, "72", null, "+1: function definition"),
      new CxxReportLocation(null, "73", null, "+1: if statement"),
      new CxxReportLocation(null, "74", null, "+1: if statement"),
      new CxxReportLocation(null, "75", null, "+1: conditional operator"),
      new CxxReportLocation(null, "77", null, "+1: conditional operator"),
      new CxxReportLocation(null, "80", null, "+1: if statement"),
      new CxxReportLocation(null, "81", null, "+1: conditional operator"),
      new CxxReportLocation(null, "83", null, "+1: conditional operator"));

    var issue4 = issues.stream()
      .filter(issue -> issue.getLocations().get(0).getLine().equals("89"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 89"));
    softly.assertThat(issue4.getLocations()).containsOnly(
      new CxxReportLocation(null, "89", null,
        "The Cyclomatic Complexity of this function is 14 which is greater than 5 authorized."),
      new CxxReportLocation(null, "89", null, "+1: function definition"),
      new CxxReportLocation(null, "91", null, "+1: if statement"),
      new CxxReportLocation(null, "91", null, "+1: logical operator"),
      new CxxReportLocation(null, "91", null, "+1: logical operator"),
      new CxxReportLocation(null, "94", null, "+1: catch-clause"),
      new CxxReportLocation(null, "96", null, "+1: catch-clause"),
      new CxxReportLocation(null, "98", null, "+1: catch-clause"),
      new CxxReportLocation(null, "100", null, "+1: catch-clause"),
      new CxxReportLocation(null, "102", null, "+1: catch-clause"),
      new CxxReportLocation(null, "104", null, "+1: catch-clause"),
      new CxxReportLocation(null, "106", null, "+1: catch-clause"),
      new CxxReportLocation(null, "107", null, "+1: while loop"),
      new CxxReportLocation(null, "108", null, "+1: conditional operator"),
      new CxxReportLocation(null, "110", null, "+1: conditional operator"));
    softly.assertAll();
  }

}
