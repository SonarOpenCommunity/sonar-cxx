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
package org.sonar.cxx.checks.metrics;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

class TooManyLinesOfCodeInFileCheckTest {

  private final TooManyLinesOfCodeInFileCheck check = new TooManyLinesOfCodeInFileCheck();

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void test() throws IOException {
    check.setMax(1);
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/complexity.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(
        "This file has 22 lines of code, which is greater than 1 authorized. Split it into smaller files.")
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void test2() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/complexity.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

}
