/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2022 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.checks;

import com.sonar.cxx.sslr.api.Grammar;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

public class AbstractSingleLineCommentsSyntaxCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  private static class Check extends AbstractSingleLineCommentsSyntaxCheck<Grammar> {

    @Override
    public String getSingleLineCommentSyntaxPrefix() {
      return "//";
    }

  }

  @Test
  public void singleLineCommentsSyntax() {
    checkMessagesVerifier.verify(scanFile("/checks/single_line_comments_syntax.mc", new Check()).getCheckMessages())
      .next().atLine(1).withMessage("This single line comment should use the single line comment syntax \"//\"")
      .next().atLine(15);
  }

}
