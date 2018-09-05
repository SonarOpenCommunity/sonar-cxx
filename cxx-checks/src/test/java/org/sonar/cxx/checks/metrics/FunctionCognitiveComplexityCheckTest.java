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
package org.sonar.cxx.checks.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

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
    FunctionCognitiveComplexityCheck check = new FunctionCognitiveComplexityCheck();
    check.setMaxComplexity(5);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/FunctionCognitiveComplexity.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);
    Set<CxxReportIssue> issues = MultiLocatitionSquidCheck.getMultiLocationCheckMessages(file);
    assertThat(issues).isNotNull();

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(issues).hasSize(5);
    softly.assertThat(issues).allSatisfy(issue -> "FunctionCognitiveComplexity".equals(issue.getRuleId()));

    CxxReportIssue issue0 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("13"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 13"));
    softly.assertThat(issue0.getLocations()).containsOnly(
        new CxxReportLocation(null, "13", "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
        new CxxReportLocation(null, "14", "+1: if statement"),
        new CxxReportLocation(null, "15", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "16", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "17", "+1: else statement"),
        new CxxReportLocation(null, "18", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "20", "+1: else statement"),
        new CxxReportLocation(null, "21", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "22", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "23", "+1: else statement"),
        new CxxReportLocation(null, "24", "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue1 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("33"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 33"));
    softly.assertThat(issue1.getLocations()).containsOnly(
        new CxxReportLocation(null, "33", "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
        new CxxReportLocation(null, "34", "+1: if statement"),
        new CxxReportLocation(null, "35", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "36", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "37", "+1: else statement"),
        new CxxReportLocation(null, "38", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "40", "+1: else statement"),
        new CxxReportLocation(null, "41", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "42", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "43", "+1: else statement"),
        new CxxReportLocation(null, "44", "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue2 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("51"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 51"));
    softly.assertThat(issue2.getLocations()).containsOnly(
        new CxxReportLocation(null, "51", "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
        new CxxReportLocation(null, "52", "+1: if statement"),
        new CxxReportLocation(null, "53", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "54", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "55", "+1: else statement"),
        new CxxReportLocation(null, "56", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "58", "+1: else statement"),
        new CxxReportLocation(null, "59", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "60", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "61", "+1: else statement"),
        new CxxReportLocation(null, "62", "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue3 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("72"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 72"));
    softly.assertThat(issue3.getLocations()).containsOnly(
        new CxxReportLocation(null, "72", "The Cognitive Complexity of this function is 20 which is greater than 5 authorized."),
        new CxxReportLocation(null, "73", "+1: if statement"),
        new CxxReportLocation(null, "74", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "75", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "76", "+1: else statement"),
        new CxxReportLocation(null, "77", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "79", "+1: else statement"),
        new CxxReportLocation(null, "80", "+2: if statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "81", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "82", "+1: else statement"),
        new CxxReportLocation(null, "83", "+3: conditional operator (incl 2 for nesting)"));

    CxxReportIssue issue4 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("89"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 89"));
    softly.assertThat(issue4.getLocations()).containsOnly(
        new CxxReportLocation(null, "89", "The Cognitive Complexity of this function is 18 which is greater than 5 authorized."),
        new CxxReportLocation(null, "91", "+1: if statement"),
        new CxxReportLocation(null, "91", "+1: logical operator"),
        new CxxReportLocation(null, "91", "+1: logical operator"),
        new CxxReportLocation(null, "94", "+1: catch-clause"),
        new CxxReportLocation(null, "96", "+1: catch-clause"),
        new CxxReportLocation(null, "98", "+1: catch-clause"),
        new CxxReportLocation(null, "100", "+1: catch-clause"),
        new CxxReportLocation(null, "102", "+1: catch-clause"),
        new CxxReportLocation(null, "104", "+1: catch-clause"),
        new CxxReportLocation(null, "106", "+1: catch-clause"),
        new CxxReportLocation(null, "107", "+2: iteration statement (incl 1 for nesting)"),
        new CxxReportLocation(null, "108", "+3: conditional operator (incl 2 for nesting)"),
        new CxxReportLocation(null, "110", "+2: conditional operator (incl 1 for nesting)"),
        new CxxReportLocation(null, "113", "+1: switch statement"));
    softly.assertAll();
  }

}
