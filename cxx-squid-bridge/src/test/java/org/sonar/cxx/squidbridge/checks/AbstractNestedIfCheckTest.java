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

import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.test.minic.MiniCGrammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.SonarException;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

public class AbstractNestedIfCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static class Check extends AbstractNestedIfCheck<Grammar> {

    public int maximumNestingLevel = 3;

    @Override
    public int getMaximumNestingLevel() {
      return maximumNestingLevel;
    }

    @Override
    public AstNodeType getIfRule() {
      return MiniCGrammar.IF_STATEMENT;
    }

  }

  private final Check check = new Check();

  @Test
  public void nestedIfWithDefaultNesting() {
    checkMessagesVerifier.verify(scanFile("/checks/nested_if.mc", check).getCheckMessages())
      .next().atLine(9).withMessage("This if has a nesting level of 4, which is higher than the maximum allowed 3.");
  }

  @Test
  public void nestedIfWithSpecificNesting() {
    check.maximumNestingLevel = 2;

    checkMessagesVerifier.verify(scanFile("/checks/nested_if.mc", check).getCheckMessages())
      .next().atLine(7)
      .next().atLine(27);
  }

  @Test
  public void wrong_parameter() {
    check.maximumNestingLevel = 0;

    thrown.expect(SonarException.class);
    thrown.expectMessage("The maximal if nesting level must be set to a value greater than 0, but given: 0");
    scanFile("/checks/nested_if.mc", check);
  }

}
