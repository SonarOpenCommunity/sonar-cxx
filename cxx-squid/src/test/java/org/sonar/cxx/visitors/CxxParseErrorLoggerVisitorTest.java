/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.slf4j.event.Level;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;

class CxxParseErrorLoggerVisitorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void handleParseErrorTest() throws Exception {
    logTester.setLevel(Level.DEBUG);
    var tester = CxxFileTesterHelper.create("src/test/resources/visitors/syntaxerror.cc", ".", "");
    CxxAstScanner.scanSingleInputFile(tester.asInputFile());

    var log = String.join("\n", logTester.logs(Level.DEBUG));

    assertThat(log)
      .isNotEmpty()
      .contains("skip declaration: namespace X {")
      .contains("skip declaration: void test :: f1 ( ) {")
      .contains("syntax error: i = unsigend int ( i + 1 )")
      .contains("skip declaration: void test :: f3 ( ) {")
      .contains("syntax error: int i = 0 i ++");
  }

}
