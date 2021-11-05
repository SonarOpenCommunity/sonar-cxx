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
package org.sonar.cxx.checks.naming;

import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

public class MethodNameCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test() throws Exception {
    var check = new MethodNameCheck();
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/MethodName.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
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
      .next().atLine(96).withMessage(
      "Rename method \"Third_Level_Nested_Class_getX\" "
        + "to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .noMore();
  }

}
