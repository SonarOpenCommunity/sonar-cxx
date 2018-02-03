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
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class SafetyTagCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test() throws UnsupportedEncodingException, IOException {
    SafetyTagCheck check = new SafetyTagCheck();
    check.regularExpression = "<Safetykey>.*</Safetykey>";
    check.suffix = "_SAFETY";

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/SafetyTagCheck.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(21).withMessage("Source files implementing risk mitigations shall use special name suffix '_SAFETY' : <Safetykey>MyRimName</Safetykey>");

    check = new SafetyTagCheck();
    check.regularExpression = "<Safetykey>.*</Safetykey>";
    check.suffix = "_SAFETY";

    tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/SafetyTagCheck_SAFETY.cc", ".");
    file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new SafetyTagCheck();
    check.regularExpression = "<Safetykey>";
    check.suffix = "_SAFETY";

    tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/SafetyTagCheck_SAFETY.cc", ".");
    file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new SafetyTagCheck();
    check.regularExpression = "@hazard";

    tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/SafetyTagCheck_SAFETY.cc", ".");
    file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

}
