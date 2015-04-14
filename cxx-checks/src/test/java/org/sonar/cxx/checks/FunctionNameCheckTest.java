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

public class FunctionNameCheckTest {

  @Test
  public void test() throws Exception {
    FunctionNameCheck check = new FunctionNameCheck();
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FunctionName.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(8).withMessage(
        "Rename function \"Badly_Named_Function\" to match the regular expression ^[a-z_][a-z0-9_]{2,30}$.")
      .next().atLine(12).withMessage(
        "Rename function \"too_long_function_name_because_it_has_more_than_30_characters\" "
        + "to match the regular expression ^[a-z_][a-z0-9_]{2,30}$.")
      .noMore();
  }

}
