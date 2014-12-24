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
import java.nio.charset.Charset;

import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class FileEncodingCheckTest {

  private final FileEncodingCheck check = new FileEncodingCheck();

  @Test
  public void testAsciiFileAsciiEncoding() {
    Charset charset = Charset.forName("US-ASCII");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    SourceFile file = CxxAstScanner.scanSingleFileConfig(new File("src/test/resources/checks/TabCharacter.cc"), cxxConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void testAsciiFileUtf8Encoding() {
    Charset charset = Charset.forName("UTF-8");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    SourceFile file = CxxAstScanner.scanSingleFileConfig(new File("src/test/resources/checks/TabCharacter.cc"), cxxConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void testUnicodeFileUtf16Encoding() {
    Charset charset = Charset.forName("UTF-16");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    SourceFile file = CxxAstScanner.scanSingleFileConfig(new File("src/test/resources/checks/Unicode.cc"), cxxConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void testUnicodeFileAsciiEncoding() {
    Charset charset = Charset.forName("US-ASCII");
    CxxConfiguration cxxConfig = new CxxConfiguration(charset);
    SourceFile file = CxxAstScanner.scanSingleFileConfig(new File("src/test/resources/checks/Unicode.cc"), cxxConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage("Not all characters of the file can be encoded with the predefined charset " + charset.name() + ".")
      .noMore();
  }

}
