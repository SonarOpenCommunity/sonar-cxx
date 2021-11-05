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

public class FileComplexityCheckTest {

  @Test
  public void check() throws UnsupportedEncodingException, IOException {
    var check = new FileComplexityCheck();
    check.setMaxComplexity(1);

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/functions.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    Set<CxxReportIssue> issues = MultiLocatitionSquidCheck.getMultiLocationCheckMessages(file);
    assertThat(issues).isNotNull();
    var softly = new SoftAssertions();
    softly.assertThat(issues).hasSize(1);

    CxxReportIssue actualIssue = issues.iterator().next();
    softly.assertThat(actualIssue.getRuleId()).isEqualTo("FileComplexity");
    softly.assertThat(actualIssue.getLocations()).containsOnly(
      new CxxReportLocation(null, "1", null,
                            "The Cyclomatic Complexity of this file is 2 which is greater than 1 authorized."),
      new CxxReportLocation(null, "3", null, "+1: function definition"),
      new CxxReportLocation(null, "5", null, "+1: function definition")
    );
    softly.assertAll();
  }

}
