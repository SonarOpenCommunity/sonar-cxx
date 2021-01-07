/*
 * Sonar C++ Plugin (Community)
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
package org.sonar.cxx.visitors;

import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.cxx.CxxAstScanner;

public class CxxParseErrorLoggerVisitorTest {

  @Rule
  public LogTester logTester = new LogTester();

  @Before
  public void scanFile() {
    String dir = "src/test/resources/visitors";
    InputFile inputFile = TestInputFileBuilder.create("", dir + "/syntaxerror.cc").build();
    SensorContextTester context = SensorContextTester.create(new File(dir));
    context.fileSystem().add(inputFile);

    logTester.setLevel(LoggerLevel.DEBUG);
    CxxAstScanner.scanSingleFile(new File(inputFile.uri().getPath()));
  }

  @Test
  public void handleParseErrorTest() throws Exception {
    String log = String.join("\n", logTester.logs(LoggerLevel.DEBUG));

    assertThat(log)
      .isNotEmpty()
      .contains("skip declaration: namespace X {")
      .contains("skip declaration: void test :: f1 ( ) {")
      .contains("syntax error: i = unsigend int ( i + 1 )")
      .contains("skip declaration: void test :: f3 ( ) {")
      .contains("syntax error: int i = 0 i ++");
  }

}
