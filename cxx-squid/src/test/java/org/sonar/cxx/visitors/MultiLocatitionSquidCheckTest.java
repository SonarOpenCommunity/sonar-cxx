/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.utils.CxxReportIssue;

class MultiLocatitionSquidCheckTest {

  @Test
  void addMultiLocationViolationOnFreshFileCreatesMessageSet() {
    var sourceFile = new SourceFile("com/sonarsource/FileName1.cpp", "FileName1.cpp");
    var issue = new CxxReportIssue("ruleId", "FileName1.cpp", "1", null, "info");

    MultiLocatitionSquidCheck.addMultiLocationViolation(sourceFile, issue);

    assertThat(MultiLocatitionSquidCheck.hasMultiLocationCheckMessages(sourceFile)).isTrue();
    assertThat(MultiLocatitionSquidCheck.getMultiLocationCheckMessages(sourceFile)).containsExactly(issue);
  }

  @Test
  void addMultiLocationViolationOnFileWithExistingMessagesAppends() {
    var sourceFile = new SourceFile("com/sonarsource/FileName2.cpp", "FileName2.cpp");
    var issue0 = new CxxReportIssue("ruleId0", "FileName2.cpp", "1", null, "info0");
    var issue1 = new CxxReportIssue("ruleId1", "FileName2.cpp", "2", null, "info1");

    MultiLocatitionSquidCheck.addMultiLocationViolation(sourceFile, issue0);
    MultiLocatitionSquidCheck.addMultiLocationViolation(sourceFile, issue1);

    assertThat(MultiLocatitionSquidCheck.getMultiLocationCheckMessages(sourceFile))
      .containsExactlyInAnyOrder(issue0, issue1);
  }
}
