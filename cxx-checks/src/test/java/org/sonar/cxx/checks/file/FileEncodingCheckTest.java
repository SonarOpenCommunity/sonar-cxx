/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.checks.file;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.checks.CxxFileTester;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class FileEncodingCheckTest {

  private final FileEncodingCheck check = new FileEncodingCheck();

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testAsciiFileAsciiEncoding() throws UnsupportedEncodingException, IOException {
    Charset charset = Charset.forName("US-ASCII");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/TabCharacter.cc", ".", Charset.forName("US-ASCII"));
    SourceFile file = CxxAstScanner.scanSingleFileConfig(CxxFileTesterHelper.mockCxxLanguage(), tester.cxxFile, cxxConfig, check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testAsciiFileUtf8Encoding() throws UnsupportedEncodingException, IOException {
    Charset charset = Charset.forName("UTF-8");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/TabCharacter.cc", ".", Charset.forName("UTF-8"));
    SourceFile file = CxxAstScanner.scanSingleFileConfig(CxxFileTesterHelper.mockCxxLanguage(), tester.cxxFile, cxxConfig, check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testUnicodeFileUtf16Encoding() throws UnsupportedEncodingException, IOException {
    Charset charset = Charset.forName("UTF-16");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/Unicode.cc", ".", Charset.forName("UTF-16"));
    SourceFile file = CxxAstScanner.scanSingleFileConfig(CxxFileTesterHelper.mockCxxLanguage(), tester.cxxFile, cxxConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testUnicodeFileAsciiEncoding() throws IOException {
    Charset charset = Charset.forName("US-ASCII");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/Unicode.cc", ".", Charset.forName("US-ASCII"));
    SourceFile file = CxxAstScanner.scanSingleFileConfig(CxxFileTesterHelper.mockCxxLanguage(), tester.cxxFile, cxxConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage("Not all characters of the file can be encoded with the predefined charset " + charset.name() + ".")
      .noMore();
  }
}
