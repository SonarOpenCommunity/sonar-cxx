/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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
            .next().atLine(35)
            .next().atLine(36)
            .next().atLine(42)
            .next().atLine(61)
            .next().atLine(66) // @todo wrong: { of switch
            .next().atLine(67) // @todo wrong:
            .next().atLine(76) // @todo wrong:
            .next().atLine(99)
            .next().atLine(104)
            .next().atLine(110)
            .next().atLine(141).withMessage("Make this line start at column 3.")
            .next().atLine(142).withMessage("Make this line start at column 5.")
            .next().atLine(157).withMessage("Make this line start at column 1.")
            .next().atLine(159).withMessage("Make this line start at column 3.")
            .next().atLine(163)
            .next().atLine(170)
            .next().atLine(171)
            .next().atLine(177)
            //.next().atLine(180) //@todo missing: break in switch
            //.next().atLine(181) //@todo missing: default in switch
            .next().atLine(184)
            .next().atLine(185) // @todo wrong: { from switch
            .next().atLine(186)
            //.next().atLine(190) //@todo missing: break in switch
            .next().atLine(199)
            .next().atLine(202)
            .next().atLine(206)
            .next().atLine(209)
            .next().atLine(213)
            .noMore();
  }

  @Test
  public void custom() {
    IndentationCheck check = new IndentationCheck();
    check.indentationLevel = 4;

    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/IndentationCheck.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
            .next().atLine(4).withMessage("Make this line start at column 5.")
            .next().atLine(9).withMessage("Make this line start at column 9.")
            .next().atLine(11).withMessage("Make this line start at column 5.");
  }

}
