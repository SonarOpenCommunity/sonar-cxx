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
package org.sonar.cxx.checks.metrics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTester;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.utils.CxxReportLocation;
import org.sonar.cxx.visitors.MultiLocatitionSquidCheck;
import org.sonar.squidbridge.api.SourceFile;

public class FunctionCognitiveComplexityCheckTest {

  @Test
  public void check() throws UnsupportedEncodingException, IOException {
    var check = new FunctionCognitiveComplexityCheck();
    check.setMaxComplexity(5);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester(
      "src/test/resources/checks/FunctionCognitiveComplexity.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.asFile(), check);
    Set<CxxReportIssue> issues = MultiLocatitionSquidCheck.getMultiLocationCheckMessages(file);
    assertThat(issues).isNotNull();

    var softly = new SoftAssertions();
    softly.assertThat(issues).hasSize(5);
    softly.assertThat(issues)
      .allSatisfy(issue -> assertThat(issue.getRuleId()).isEqualTo("FunctionCognitiveComplexity"));

    CxxReportIssue issue0 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("13"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 13"));
    softly.assertThat(issue0.getLocations()).containsOnly(
      new CxxReportLocation(null, "13", null,
                            "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
      new CxxReportLocation(null, "14", null, "+1: if statement"),
      new CxxReportLocation(null, "15", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "16", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "17", null, "+1: else statement"),
      new CxxReportLocation(null, "18", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "20", null, "+1: else statement"),
      new CxxReportLocation(null, "21", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "22", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "23", null, "+1: else statement"),
      new CxxReportLocation(null, "24", null, "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue1 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("33"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 33"));
    softly.assertThat(issue1.getLocations()).containsOnly(
      new CxxReportLocation(null, "33", null,
                            "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
      new CxxReportLocation(null, "34", null, "+1: if statement"),
      new CxxReportLocation(null, "35", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "36", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "37", null, "+1: else statement"),
      new CxxReportLocation(null, "38", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "40", null, "+1: else statement"),
      new CxxReportLocation(null, "41", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "42", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "43", null, "+1: else statement"),
      new CxxReportLocation(null, "44", null, "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue2 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("51"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 51"));
    softly.assertThat(issue2.getLocations()).containsOnly(
      new CxxReportLocation(null, "51", null,
                            "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
      new CxxReportLocation(null, "52", null, "+1: if statement"),
      new CxxReportLocation(null, "53", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "54", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "55", null, "+1: else statement"),
      new CxxReportLocation(null, "56", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "58", null, "+1: else statement"),
      new CxxReportLocation(null, "59", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "60", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "61", null, "+1: else statement"),
      new CxxReportLocation(null, "62", null, "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue3 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("72"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 72"));
    softly.assertThat(issue3.getLocations()).containsOnly(
      new CxxReportLocation(null, "72", null,
                            "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
      new CxxReportLocation(null, "73", null, "+1: if statement"),
      new CxxReportLocation(null, "74", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "75", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "76", null, "+1: else statement"),
      new CxxReportLocation(null, "77", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "79", null, "+1: else statement"),
      new CxxReportLocation(null, "80", null, "+2: if statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "81", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "82", null, "+1: else statement"),
      new CxxReportLocation(null, "83", null, "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue4 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("89"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 89"));
    softly.assertThat(issue4.getLocations()).containsOnly(
      new CxxReportLocation(null, "89", null,
                            "The Cognitive Complexity of this function is 18 which is greater than 5 authorized."),
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
      new CxxReportLocation(null, "107", null, "+2: iteration statement (incl 1 for nesting)"),
      new CxxReportLocation(null, "108", null, "+3: conditional operator (incl 2 for nesting)"),
      new CxxReportLocation(null, "110", null, "+2: conditional operator (incl 1 for nesting)"),
      new CxxReportLocation(null, "113", null, "+1: switch statement"));
    softly.assertAll();
  }

}
