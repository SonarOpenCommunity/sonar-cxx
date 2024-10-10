/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

class AbstractCommentRegularExpressionCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  private static class Check extends AbstractCommentRegularExpressionCheck<Grammar> {

    private String regularExpression;
    private String message;

    @Override
    public String getRegularExpression() {
      return regularExpression;
    }

    @Override
    public String getMessage() {
      return message;
    }
  }

  private final Check check = new Check();

  @Test
  void empty() {
    check.regularExpression = "";
    check.message = "Empty regular expression.";

    checkMessagesVerifier.verify(scanFile("/checks/commentRegularExpression.mc", check).getCheckMessages());
  }

  @Test
  void case_insensitive() {
    check.regularExpression = "(?i).*TODO.*";
    check.message = "Avoid TODO.";

    checkMessagesVerifier.verify(scanFile("/checks/commentRegularExpression.mc", check).getCheckMessages())
      .next().atLine(3).withMessage("Avoid TODO.")
      .next().atLine(5)
      .next().atLine(7);
  }

  @Test
  void case_sensitive() {
    check.regularExpression = ".*TODO.*";
    check.message = "Avoid TODO.";

    checkMessagesVerifier.verify(scanFile("/checks/commentRegularExpression.mc", check).getCheckMessages())
      .next().atLine(3).withMessage("Avoid TODO.");
  }

  @Test
  void wrong_regular_expression() {
    check.regularExpression = "*";
    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      scanFile("/checks/commentRegularExpression.mc", check);
    });
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Unable to compile regular expression: *");
  }

}
