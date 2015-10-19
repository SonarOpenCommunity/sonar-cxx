/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * sonarqube@googlegroups.com
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

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

public class ReservedNamesCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Test
  public void detected() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/ReservedNamesCheck.cc"), new ReservedNamesCheck());
    checkMessagesVerifier.verify(file.getCheckMessages())
        .next().atLine(3).withMessage("Reserved name used for macro (keyword or alternative token redefined)")
        .next().atLine(4)
        .next().atLine(5)
        .next().atLine(6)
        .next().atLine(14).withMessage("Reserved name used for macro (begins with underscore followed by a capital letter)")
        .next().atLine(15)
        .next().atLine(16)
        .next().atLine(18).withMessage("Reserved name used for macro (contains two consecutive underscores)")
        .next().atLine(19)
        .next().atLine(20);
  }
}
