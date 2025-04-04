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
package org.sonar.cxx.checks.regex;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

class LineRegularExpressionCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void lineRegExWithoutFilePattern() throws IOException {
    var check = new LineRegularExpressionCheck();
    check.regularExpression = "stdafx\\.h";
    check.message = "Found 'stdafx.h' in line!";
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/LineRegEx.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(2).withMessage(check.message)
      .next().atLine(3).withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void lineRegExInvertWithoutFilePattern() throws IOException {
    var check = new LineRegularExpressionCheck();
    check.regularExpression = "//.*";
    check.invertRegularExpression = true;
    check.message = "Found no comment in the line!";
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/LineRegExInvert.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(3).withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void lineRegExWithFilePattern1() throws IOException {
    var check = new LineRegularExpressionCheck();
    check.matchFilePattern = "/**/*.cc"; // all files with .cc file extension
    check.regularExpression = "#include\\s+\"stdafx\\.h\"";
    check.message = "Found '#include \"stdafx.h\"' in line in a .cc file!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/LineRegEx.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(2).withMessage(check.message)
      .next().atLine(3).withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void lineRegExWithFilePatternInvert() throws IOException {
    var check = new LineRegularExpressionCheck();
    check.matchFilePattern = "/**/*.xx"; // all files with not .xx file extension
    check.invertFilePattern = true;
    check.regularExpression = "#include\\s+\"stdafx\\.h\"";
    check.message = "Found '#include \"stdafx.h\"' in line in a not .xx file!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/LineRegEx.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(2).withMessage(check.message)
      .next().atLine(3).withMessage(check.message)
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  void lineRegExWithFilePattern2() throws IOException {
    var check = new LineRegularExpressionCheck();
    check.matchFilePattern = "/**/*.xx"; // all files with .xx file extension
    check.regularExpression = "#include\\s+\"stdafx\\.h\"";
    check.message = "Found '#include \"stdafx.h\"' in line in a .xx file!";

    var tester = CxxFileTesterHelper.create("src/test/resources/checks/LineRegEx.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

}
