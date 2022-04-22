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
import org.sonar.cxx.squidbridge.measures.MetricDef;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;
import org.sonar.cxx.squidbridge.test.miniC.MiniCAstScanner.MiniCMetrics;

class AbstractFileComplexityCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  private static class Check extends AbstractFileComplexityCheck<Grammar> {

    public int maximumFileComplexity = 100;

    @Override
    public int getMaximumFileComplexity() {
      return maximumFileComplexity;
    }

    @Override
    public MetricDef getComplexityMetric() {
      return MiniCMetrics.COMPLEXITY;
    }

  }

  private final Check check = new Check();

  @Test
  void fileComplexityEqualsMaximum() {
    check.maximumFileComplexity = 5;

    checkMessagesVerifier.verify(scanFile("/checks/complexity5.mc", check).getCheckMessages());
  }

  @Test
  void fileComplexityGreaterMaximum() {
    check.maximumFileComplexity = 4;

    checkMessagesVerifier.verify(scanFile("/checks/complexity5.mc", check).getCheckMessages())
      .next().withMessage("The file is too complex (5 while maximum allowed is set to 4).");
  }

  @Test
  void wrong_parameter() {
    check.maximumFileComplexity = 0;
    IllegalArgumentException thrown = catchThrowableOfType(() -> {
      scanFile("/checks/complexity5.mc", check);
    }, IllegalArgumentException.class);
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalArgumentException.class)
      .hasMessage("The complexity threshold must be set to a value greater than 0, but given: 0");
  }

}
