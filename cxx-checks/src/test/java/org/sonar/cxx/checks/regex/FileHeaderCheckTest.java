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
package org.sonar.cxx.checks.regex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

public class FileHeaderCheckTest {

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void test() throws UnsupportedEncodingException, IOException {
    var check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    var tester = CxxFileTesterHelper
      .create("src/test/resources/checks/FileHeaderCheck/Class1.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 20\\d\\d";

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Class2.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012";
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo";

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\n// foo";

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r// foo";

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\r// foo";

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo\n\n\n\n\n\n\n\n\n\ngfoo";

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "/*foo http://www.example.org*/";

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Class3.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void regex() throws UnsupportedEncodingException, IOException {
    var check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d\\d\\d";
    check.isRegularExpression = true;

    var tester = CxxFileTesterHelper
      .create("src/test/resources/checks/FileHeaderCheck/Regex1.cc", ".");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage(
      "Add or update the header of this file.");
    // Check that the regular expression is compiled once
    check = new FileHeaderCheck();

    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage(
      "Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\n// mycompany";
    check.isRegularExpression = true;

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Regex2.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage(
      "Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\r?\\n// mycompany";
    check.isRegularExpression = true;

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Regex3.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\n// mycompany";
    check.isRegularExpression = true;

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Regex4.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage(
      "Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "^(?=.*?\\bCopyright\\b)(?=.*?\\bVendor\\b)(?=.*?\\d{4}(-\\d{4})?).*$";
    check.isRegularExpression = true;

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Regex5.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();

    check = new FileHeaderCheck();

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Regex6.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage(
      "Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "//\\s*<copyright>\\s*"
                           + "//\\s*Copyright \\(c\\) (AAA BBB|CCC DDD) GmbH. All rights reserved.\\s*"
                           + "//\\s*</copyright>\\s*";
    check.isRegularExpression = true;

    tester = CxxFileTesterHelper.create("src/test/resources/checks/FileHeaderCheck/Regex7.cc", ".");
    file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), check);
    assertThat(file.getCheckMessages()).isNullOrEmpty();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void should_fail_with_bad_regular_expression() {
    var check = new FileHeaderCheck();
    check.headerFormat = "[";
    check.isRegularExpression = true;

    IllegalStateException e = assertThrows(IllegalStateException.class, check::init);
    assertThat(e).hasMessage("Unable to compile the regular expression: \"^[\"");
  }

}
