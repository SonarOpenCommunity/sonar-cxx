/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.checks;

import org.junit.Test;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;
import org.sonar.cxx.CxxAstScanner;

import java.io.File;

public class MethodNameCheckTest {

  @Test
  public void test() throws Exception {
    MethodNameCheck check = new MethodNameCheck();
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/MethodName.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(9).withMessage(
        "Rename method \"Badly_Named_Method\" to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .next().atLine(10).withMessage(
        "Rename method \"TooLongMethodNameBecauseItHasMoreThan30Characters\" "
        + "to match the regular expression ^[A-Z][A-Za-z0-9]{2,30}$.")
      .noMore();
  }

}
