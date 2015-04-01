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

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

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
        .next().atLine(24).withMessage("Make this line start at column 9.")
        .next().atLine(31)
        .next().atLine(34)
        .next().atLine(35)
        .next().atLine(40)
        .next().atLine(58)
        .next().atLine(96)
        .next().atLine(101)
        .next().atLine(107)
        .next().atLine(137)
        .next().atLine(138)
        .next().atLine(150)
        .next().atLine(152)
        .next().atLine(155)
        .next().atLine(160)
        .next().atLine(161)
        .next().atLine(166)
        .next().atLine(169)
        .next().atLine(170)
        .next().atLine(173)
        .next().atLine(175)
        .next().atLine(179)
        .next().atLine(187)
        .next().atLine(190)
        .next().atLine(194)
        .next().atLine(197)
        .next().atLine(201);
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
  
  @Test
  public void verifySwitchIndentFalse() {
    IndentationCheck check = new IndentationCheck();
    check.indentSwitchCase = false;
    check.indentationLevel = 4;

    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/IndentationCheckSwitch.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().atLine(8).withMessage("Make this line start at column 5.")
        .next().atLine(10).withMessage("Make this line start at column 5.")
        .next().atLine(29).withMessage("Make this line start at column 9.")
        .noMore();
  }  
  
  @Test
  public void verifySwitchIndentTrue() {
    IndentationCheck check = new IndentationCheck();
    check.indentSwitchCase = true;
    check.indentationLevel = 4;

    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/IndentationCheckSwitch.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().atLine(16).withMessage("Make this line start at column 9.")
        .next().atLine(17).withMessage("Make this line start at column 9.")
        .next().atLine(22).withMessage("Make this line start at column 9.")
        .next().atLine(25).withMessage("Make this line start at column 17.")
        .next().atLine(26).withMessage("Make this line start at column 17.")
        .next().atLine(30).withMessage("Make this line start at column 9.")    
        .next().atLine(35).withMessage("Make this line start at column 9.")            
        .next().atLine(36).withMessage("Make this line start at column 13.")            
        .noMore();            
  }    
  

}
