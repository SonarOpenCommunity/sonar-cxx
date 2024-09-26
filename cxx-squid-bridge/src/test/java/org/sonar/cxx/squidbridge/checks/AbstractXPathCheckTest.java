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

class AbstractXPathCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

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

  private final Check check = new Check();

  @Test
  void emptyXPathCheck() {
    check.xpath = "";
    check.message = "Empty XPath check.";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages());
  }

  @Test
  void booleanXPathCheckWithResults() {
    check.xpath = "count(//VARIABLE_DEFINITION) > 0";
    check.message = "Boolean XPath rule with results.";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages())
      .next().withMessage("Boolean XPath rule with results.");
  }

  @Test
  void booleanXPathCheckWithoutResults() {
    check.xpath = "count(//variableDefinition) > 2";
    check.message = "Boolean XPath rule without results.";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages());
  }

  @Test
  void astNodesXpathCheck() {
    check.xpath = "//VARIABLE_DEFINITION";
    check.message = "No variable definitions allowed!";

    checkMessagesVerifier.verify(scanFile("/checks/xpath.mc", check).getCheckMessages())
      .next().atLine(1).withMessage("No variable definitions allowed!")
      .next().atLine(5);
  }

  @Test
  void parse_error() {
    check.xpath = "//VARIABLE_DEFINITION";

    checkMessagesVerifier.verify(scanFile("/checks/parse_error.mc", check).getCheckMessages());
  }

  @Test
  void wrong_xpath() {
    check.xpath = "//";
    IllegalStateException thrown = catchThrowableOfType(() -> {
      scanFile("/checks/xpath.mc", check);
    }, IllegalStateException.class);
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Unable to initialize the XPath engine, perhaps because of an invalid query: //");
  }

}
