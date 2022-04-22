/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.checks.naming;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifierRule;

class FileNameCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();
  private final FileNameCheck check = new FileNameCheck();

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void bad_name() throws UnsupportedEncodingException, IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/badFile_name.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    var format = "(([a-z_][a-z0-9_]*)|([A-Z][a-zA-Z0-9]+))$";
    var message = "Rename this file to match this regular expression: \"%s\".";
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage(String.format(message, format));
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void good_name_camel_case() throws UnsupportedEncodingException, IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/FileName.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    checkMessagesVerifier.verify(file.getCheckMessages());
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void good_name_snake_case() throws UnsupportedEncodingException, IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/file_name.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);

    checkMessagesVerifier.verify(file.getCheckMessages());
  }

}
