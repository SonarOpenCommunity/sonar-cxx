/*
 * C++ Community Plugin (cxx plugin)
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
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.utils.CxxReportLocation;
import org.sonar.cxx.visitors.MultiLocatitionSquidCheck;

public class ClassComplexityCheckTest {

  @Test
  public void test() throws UnsupportedEncodingException, IOException {
    var check = new ClassComplexityCheck();
    check.setMaxComplexity(5);

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/ClassComplexity.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    Set<CxxReportIssue> issues = MultiLocatitionSquidCheck.getMultiLocationCheckMessages(file);
    assertThat(issues).isNotNull();
    var softly = new SoftAssertions();
    softly.assertThat(issues).hasSize(3);
    softly.assertThat(issues).allSatisfy(issue -> assertThat(issue.getRuleId()).isEqualTo("ClassComplexity"));

    CxxReportIssue issue0 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("9"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 9"));
    softly.assertThat(issue0.getLocations()).containsOnly(
      new CxxReportLocation(null, "9", null,
                            "The Cyclomatic Complexity of this class is 12 which is greater than 5 authorized."),
      new CxxReportLocation(null, "14", null, "+1: function definition"),
      new CxxReportLocation(null, "16", null, "+1: function definition"),
      new CxxReportLocation(null, "21", null, "+1: function definition"),
      new CxxReportLocation(null, "22", null, "+1: function definition"),
      new CxxReportLocation(null, "25", null, "+1: function definition"),
      new CxxReportLocation(null, "26", null, "+1: if statement"),
      new CxxReportLocation(null, "27", null, "+1: if statement"),
      new CxxReportLocation(null, "28", null, "+1: conditional operator"),
      new CxxReportLocation(null, "30", null, "+1: conditional operator"),
      new CxxReportLocation(null, "33", null, "+1: if statement"),
      new CxxReportLocation(null, "34", null, "+1: conditional operator"),
      new CxxReportLocation(null, "36", null, "+1: conditional operator"));

    CxxReportIssue issue1 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("42"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 42"));
    softly.assertThat(issue1.getLocations()).containsOnly(
      new CxxReportLocation(null, "42", null,
                            "The Cyclomatic Complexity of this class is 10 which is greater than 5 authorized."),
      new CxxReportLocation(null, "47", null, "+1: function definition"),
      new CxxReportLocation(null, "49", null, "+1: function definition"),
      new CxxReportLocation(null, "51", null, "+1: switch label"),
      new CxxReportLocation(null, "53", null, "+1: switch label"),
      new CxxReportLocation(null, "57", null, "+1: function definition"),
      new CxxReportLocation(null, "58", null, "+1: for loop"),
      new CxxReportLocation(null, "59", null, "+1: if statement"),
      new CxxReportLocation(null, "59", null, "+1: logical operator"),
      new CxxReportLocation(null, "59", null, "+1: logical operator"),
      new CxxReportLocation(null, "65", null, "+1: function definition")
    );

    CxxReportIssue issue2 = issues.stream().filter(issue -> issue.getLocations().get(0).getLine().equals("45"))
      .findFirst().orElseThrow(() -> new AssertionError("No issue at line 45"));
    softly.assertThat(issue2.getLocations()).containsOnly(
      new CxxReportLocation(null, "45", null,
                            "The Cyclomatic Complexity of this class is 9 which is greater than 5 authorized."),
      new CxxReportLocation(null, "47", null, "+1: function definition"),
      new CxxReportLocation(null, "49", null, "+1: function definition"),
      new CxxReportLocation(null, "51", null, "+1: switch label"),
      new CxxReportLocation(null, "53", null, "+1: switch label"),
      new CxxReportLocation(null, "57", null, "+1: function definition"),
      new CxxReportLocation(null, "58", null, "+1: for loop"),
      new CxxReportLocation(null, "59", null, "+1: if statement"),
      new CxxReportLocation(null, "59", null, "+1: logical operator"),
      new CxxReportLocation(null, "59", null, "+1: logical operator"));

    softly.assertAll();

  }

}
