/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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

import com.sonar.sslr.api.Grammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.cxx.squidbridge.measures.MetricDef;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;
import org.sonar.cxx.squidbridge.test.miniC.MiniCAstScanner.MiniCMetrics;

public class AbstractFileComplexityCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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

  private Check check = new Check();

  @Test
  public void fileComplexityEqualsMaximum() {
    check.maximumFileComplexity = 5;

    checkMessagesVerifier.verify(scanFile("/checks/complexity5.mc", check).getCheckMessages());
  }

  @Test
  public void fileComplexityGreaterMaximum() {
    check.maximumFileComplexity = 4;

    checkMessagesVerifier.verify(scanFile("/checks/complexity5.mc", check).getCheckMessages())
      .next().withMessage("The file is too complex (5 while maximum allowed is set to 4).");
  }

  @Test
  public void wrong_parameter() {
    check.maximumFileComplexity = 0;

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The complexity threshold must be set to a value greater than 0, but given: 0");
    scanFile("/checks/complexity5.mc", check);
  }

}
