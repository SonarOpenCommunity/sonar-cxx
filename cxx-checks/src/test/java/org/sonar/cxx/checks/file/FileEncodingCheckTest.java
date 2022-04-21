/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.checks.CxxFileTesterHelper;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.checks.CheckMessagesVerifier;

public class FileEncodingCheckTest {

  private final FileEncodingCheck check = new FileEncodingCheck();

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testAsciiFileAsciiEncoding() throws UnsupportedEncodingException, IOException {
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.US_ASCII);
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/TabCharacter.cc", ".", StandardCharsets.US_ASCII);
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testAsciiFileUtf8Encoding() throws UnsupportedEncodingException, IOException {
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.UTF_8);
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/TabCharacter.cc", ".", StandardCharsets.UTF_8);
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);

    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testUnicodeFileUtf16Encoding() throws UnsupportedEncodingException, IOException {
    var squidConfig = new CxxSquidConfiguration("", StandardCharsets.UTF_16);
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/Unicode.cc", ".", StandardCharsets.UTF_16);
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... verify contains the assertion
  public void testUnicodeFileAsciiEncoding() throws IOException {
    Charset charset = StandardCharsets.US_ASCII;
    var squidConfig = new CxxSquidConfiguration("", charset);
    var tester = CxxFileTesterHelper.create("src/test/resources/checks/Unicode.cc", ".", charset);
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().withMessage("Not all characters of the file can be encoded with the predefined charset " + charset.name()
                            + ".")
      .noMore();
  }

}
