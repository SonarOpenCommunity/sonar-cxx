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

import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class ParsingErrorCheckTest {

  @Test
  public void test_syntax_error_recognition() throws UnsupportedEncodingException, IOException {
    CxxConfiguration config = new CxxConfiguration();
    config.setErrorRecoveryEnabled(false);
        
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/parsingError1.cc", ".");    
    SourceFile file = CxxAstScanner.scanSingleFileConfig(tester.cxxFile, config, tester.sensorContext, new ParsingErrorCheck()); 
        
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(4).withMessageThat(containsString("Parse error"))
      .noMore();
  }

  @Test
  public void test_syntax_error_pperror() throws UnsupportedEncodingException, IOException {
    CxxConfiguration config = new CxxConfiguration();
    config.setErrorRecoveryEnabled(false);
        
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/parsingError2.cc", ".");        
    SourceFile file = CxxAstScanner.scanSingleFileConfig(tester.cxxFile, config, tester.sensorContext, new ParsingErrorCheck()); 
        
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(2).withMessageThat(containsString("Parse error"))
      .noMore();
  }

}
