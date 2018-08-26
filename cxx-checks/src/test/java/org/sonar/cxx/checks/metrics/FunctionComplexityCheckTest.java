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

public class FunctionComplexityCheckTest {

  @Test
  public void check() throws UnsupportedEncodingException, IOException {
    FunctionComplexityCheck check = new FunctionComplexityCheck();
    check.setMaxComplexity(5);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/FunctionComplexity.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);

    Set<CxxReportIssue> issues = MultiLocatitionSquidCheck.getMultiLocationCheckMessages(file);
    assertThat(issues).isNotNull();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(issues).hasSize(5);
    softly.assertThat(issues).allSatisfy(issue -> "FunctionComplexity".equals(issue.getRuleId()));

    CxxReportIssue issue0 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("13"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 13"));
    softly.assertThat(issue0.getLocations()).containsOnly(
        new CxxReportLocation(null, "13",
            "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
        new CxxReportLocation(null, "13", "+1: function definition"),
        new CxxReportLocation(null, "14", "+1: if statement"),
        new CxxReportLocation(null, "15", "+1: if statement"),
        new CxxReportLocation(null, "16", "+1: conditional operator"),
        new CxxReportLocation(null, "18", "+1: conditional operator"),
        new CxxReportLocation(null, "21", "+1: if statement"),
        new CxxReportLocation(null, "22", "+1: conditional operator"),
        new CxxReportLocation(null, "24", "+1: conditional operator"));

    CxxReportIssue issue1 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("33"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 33"));
    softly.assertThat(issue1.getLocations()).containsOnly(
        new CxxReportLocation(null, "33",
            "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
        new CxxReportLocation(null, "33", "+1: function definition"),
        new CxxReportLocation(null, "34", "+1: if statement"),
        new CxxReportLocation(null, "35", "+1: if statement"),
        new CxxReportLocation(null, "36", "+1: conditional operator"),
        new CxxReportLocation(null, "38", "+1: conditional operator"),
        new CxxReportLocation(null, "41", "+1: if statement"),
        new CxxReportLocation(null, "42", "+1: conditional operator"),
        new CxxReportLocation(null, "44", "+1: conditional operator"));

    CxxReportIssue issue2 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("51"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 51"));
    softly.assertThat(issue2.getLocations()).containsOnly(
        new CxxReportLocation(null, "51",
            "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
        new CxxReportLocation(null, "51", "+1: function definition"),
        new CxxReportLocation(null, "52", "+1: if statement"),
        new CxxReportLocation(null, "53", "+1: if statement"),
        new CxxReportLocation(null, "54", "+1: conditional operator"),
        new CxxReportLocation(null, "56", "+1: conditional operator"),
        new CxxReportLocation(null, "59", "+1: if statement"),
        new CxxReportLocation(null, "60", "+1: conditional operator"),
        new CxxReportLocation(null, "62", "+1: conditional operator"));

    CxxReportIssue issue3 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("72"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 72"));
    softly.assertThat(issue3.getLocations()).containsOnly(
        new CxxReportLocation(null, "72",
            "The Cyclomatic Complexity of this function is 8 which is greater than 5 authorized."),
        new CxxReportLocation(null, "72", "+1: function definition"),
        new CxxReportLocation(null, "73", "+1: if statement"),
        new CxxReportLocation(null, "74", "+1: if statement"),
        new CxxReportLocation(null, "75", "+1: conditional operator"),
        new CxxReportLocation(null, "77", "+1: conditional operator"),
        new CxxReportLocation(null, "80", "+1: if statement"),
        new CxxReportLocation(null, "81", "+1: conditional operator"),
        new CxxReportLocation(null, "83", "+1: conditional operator"));

    CxxReportIssue issue4 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("89"))
        .findFirst().orElseThrow(() -> new AssertionError("No issue at line 89"));
    softly.assertThat(issue4.getLocations()).containsOnly(
        new CxxReportLocation(null, "89",
            "The Cyclomatic Complexity of this function is 14 which is greater than 5 authorized."),
        new CxxReportLocation(null, "89", "+1: function definition"),
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
        new CxxReportLocation(null, "107", "+1: while loop"),
        new CxxReportLocation(null, "108", "+1: conditional operator"),
        new CxxReportLocation(null, "110", "+1: conditional operator"));
    softly.assertAll();
  }

}
