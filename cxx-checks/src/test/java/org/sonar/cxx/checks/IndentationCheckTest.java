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

import com.sonar.sslr.squid.checks.CheckMessagesVerifier;
import com.sonar.sslr.squid.checks.CheckMessagesVerifierRule;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squid.api.SourceFile;

import java.io.File;

public class IndentationCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Test
  public void detected() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/IndentationCheck.cc"), new IndentationCheck());
    checkMessagesVerifier.verify(file.getCheckMessages())
        .next().atLine(5).withMessage("Make this line start at column 3.")
        .next().atLine(11)
        .next().atLine(12)
        .next().atLine(16)
        .next().atLine(20)
        .next().atLine(23).withMessage("Make this line start at column 9.")
        .next().atLine(30) 
        .next().atLine(34) 
        .next().atLine(39)
        .next().atLine(73).withMessage("Make this line start at column 9.");
  }

  @Test
  public void custom() {
    IndentationCheck check = new IndentationCheck();
    check.indentationLevel = 4;

    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/IndentationCheck.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().atLine(4).withMessage("Make this line start at column 5.")
        .next().atLine(9)
        .next().atLine(11);
  }

}
