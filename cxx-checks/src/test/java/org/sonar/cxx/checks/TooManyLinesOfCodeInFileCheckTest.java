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

import com.sonar.sslr.squid.checks.CheckMessagesVerifier;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squid.api.SourceFile;
import org.sonar.cxx.checks.TooManyLinesOfCodeInFileCheck;
import java.io.File;

public class TooManyLinesOfCodeInFileCheckTest {


  private TooManyLinesOfCodeInFileCheck check = new TooManyLinesOfCodeInFileCheck();

  @Test
  public void test() {
    check.SetMax(1);
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/complexity.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().withMessage("This file has 22 lines of code, which is greater than 1 authorized. Split it into smaller files.")
        .noMore();
  }

  @Test
  public void test2() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/checks/complexity.cc"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .noMore();
  }

}
