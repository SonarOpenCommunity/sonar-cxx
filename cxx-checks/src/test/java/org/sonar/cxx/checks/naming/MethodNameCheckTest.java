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
package org.sonar.cxx.checks.naming;

import org.junit.Test;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;
import org.sonar.cxx.CxxAstScanner;

import org.sonar.cxx.checks.CxxFileTester;
import org.sonar.cxx.checks.CxxFileTesterHelper;

public class MethodNameCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test() throws Exception {
    MethodNameCheck check = new MethodNameCheck();
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/MethodName.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(12).withMessage(
      "Rename method \"Badly_Named_Method2\" to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .next().atLine(15).withMessage(
      "Rename method \"TooLongMethodNameBecauseItHasMoreThan30Characters2\" "
      + "to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .next().atLine(22).withMessage(
      "Rename method \"Badly_Named_Method1\" "
      + "to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .next().atLine(26).withMessage(
      "Rename method \"TooLongMethodNameBecauseItHasMoreThan30Characters1\" "
      + "to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .noMore();
  }

}
