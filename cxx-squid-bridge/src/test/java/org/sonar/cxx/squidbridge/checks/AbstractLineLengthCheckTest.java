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
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

public class AbstractLineLengthCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  private static class Check extends AbstractLineLengthCheck<Grammar> {

    public int maximumLineLength = 80;

    @Override
    public int getMaximumLineLength() {
      return maximumLineLength;
    }

  }

  private final Check check = new Check();

  @Test
  public void lineLengthWithDefaultLength() {
    checkMessagesVerifier.verify(scanFile("/checks/line_length.mc", check).getCheckMessages())
      .next().atLine(3).withMessage("The line length is greater than 80 authorized.");
  }

  @Test
  public void lineLengthWithSpecificLength() {
    check.maximumLineLength = 7;

    checkMessagesVerifier.verify(scanFile("/checks/line_length.mc", check).getCheckMessages())
      .next().atLine(3)
      .next().atLine(4);
  }

  @Test
  public void wrong_parameter() {
    check.maximumLineLength = 0;
    IllegalArgumentException thrown = catchThrowableOfType(() -> {
      scanFile("/checks/line_length.mc", check);
    }, IllegalArgumentException.class);
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalArgumentException.class)
      .hasMessage("The maximal line length must be set to a value greater than 0, but given: 0");
  }

}
