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
package org.sonar.cxx.checks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class TooManyStatementsPerLineCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test() throws UnsupportedEncodingException, IOException {
    TooManyStatementsPerLineCheck check = new TooManyStatementsPerLineCheck();
    check.excludeCaseBreak = false;

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/TooManyStatementsPerLine.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(17).withMessage("At most one statement is allowed per line, but 2 statements were found on this line.")
      .next().atLine(20)
      .next().atLine(23)
      .next().atLine(27)
      .next().atLine(29)
      .next().atLine(31)
      .noMore();
  }

  @Test
  public void testDefaultExcludeCaseBreak() {
    TooManyStatementsPerLineCheck check = new TooManyStatementsPerLineCheck();
    assertThat(check.excludeCaseBreak).isFalse();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testExcludeCaseBreak() throws UnsupportedEncodingException, IOException {
    TooManyStatementsPerLineCheck check = new TooManyStatementsPerLineCheck();
    check.excludeCaseBreak = true;

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/TooManyStatementsPerLine.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(17).withMessage("At most one statement is allowed per line, but 2 statements were found on this line.")
      .next().atLine(20)
      .next().atLine(23)
      .next().atLine(27)
      .next().atLine(31)
      .noMore();
  }

}
