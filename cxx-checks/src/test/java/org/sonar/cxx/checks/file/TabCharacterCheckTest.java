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
package org.sonar.cxx.checks.file;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

class TabCharacterCheckTest {

  private final TabCharacterCheck check = new TabCharacterCheck();

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void fileWithTabsOneMessagePerFile() throws IOException {
    check.createLineViolation = false;

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/TabCharacter.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage("Replace all tab characters in this file by sequences of white-spaces.")
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void fileWithTabsOneMessagePerLine() throws IOException {
    check.createLineViolation = true;
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/TabCharacter.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(3).withMessage("Replace all tab characters in this line by sequences of white-spaces.")
      .next().atLine(4)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void fileWithoutTabs() throws IOException {
    check.createLineViolation = false;
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/NonEmptyFile.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

}
