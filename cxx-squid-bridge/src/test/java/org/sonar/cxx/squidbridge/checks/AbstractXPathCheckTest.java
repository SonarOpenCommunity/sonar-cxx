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
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

public class AbstractXPathCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static class Check extends AbstractXPathCheck<Grammar> {

    private String xpath;
    private String message;

    @Override
    public String getXPathQuery() {
      return xpath;
    }

    @Override
    public String getMessage() {
      return message;
    }

  }

  private Check check = new Check();

  @Test
  public void emptyXPathCheck() {
    check.xpath = "";
    check.message = "Empty XPath check.";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages());
  }

  @Test
  public void booleanXPathCheckWithResults() {
    check.xpath = "count(//VARIABLE_DEFINITION) > 0";
    check.message = "Boolean XPath rule with results.";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages())
      .next().withMessage("Boolean XPath rule with results.");
  }

  @Test
  public void booleanXPathCheckWithoutResults() {
    check.xpath = "count(//variableDefinition) > 2";
    check.message = "Boolean XPath rule without results.";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages());
  }

  @Test
  public void astNodesXpathCheck() {
    check.xpath = "//VARIABLE_DEFINITION";
    check.message = "No variable definitions allowed!";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages())
      .next().atLine(1).withMessage("No variable definitions allowed!")
      .next().atLine(5);
  }

  @Test
  public void parse_error() {
    check.xpath = "//VARIABLE_DEFINITION";

    checkMessagesVerifier.verify(scanFile("/checks/parse_error.mc", check).getCheckMessages());
  }

  @Test
  public void wrong_xpath() {
    check.xpath = "//";

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Unable to initialize the XPath engine, perhaps because of an invalid query: //");
    scanFile("/checks/xpath.mc", check);
  }

}
