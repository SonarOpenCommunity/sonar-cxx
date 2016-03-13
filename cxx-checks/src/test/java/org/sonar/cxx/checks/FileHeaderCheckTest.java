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
import org.junit.rules.ExpectedException;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class FileHeaderCheckTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class1.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 20\\d\\d";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class1.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\n// foo";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r// foo";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\r// foo";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo\n\n\n\n\n\n\n\n\n\ngfoo";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "/*foo http://www.example.org*/";

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Class3.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void regex() {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d\\d\\d";
    check.isRegularExpression = true;
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Regex1.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");;
    // Check that the regular expression is compiled once
    check = new FileHeaderCheck();
    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Regex1.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\n// mycompany";
    check.isRegularExpression = true;

    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Regex2.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");;

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\r?\\n// mycompany";
    check.isRegularExpression = true;
    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Regex3.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\n// mycompany";
    check.isRegularExpression = true;
    file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/FileHeaderCheck/Regex4.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");;
  }

  @Test
  public void should_fail_with_bad_regular_expression() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[" + FileHeaderCheck.class.getSimpleName() + "] Unable to compile the regular expression: *");

    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "*";
    check.isRegularExpression = true;
    check.init();
  }


}
