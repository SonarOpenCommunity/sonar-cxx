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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;
import static org.assertj.core.api.Assertions.*;

class AbstractNamingCheckTest {

  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  private static class Check extends AbstractNamingCheck<Grammar> {

    private String regularExpression;

    @Override
    public AstNodeType[] getRules() {
      return new AstNodeType[]{
        MiniCGrammar.BIN_FUNCTION_DEFINITION,
        MiniCGrammar.BIN_VARIABLE_DEFINITION
      };
    }

    @Override
    public String getName(AstNode astNode) {
      return astNode.getTokenValue();
    }

    @Override
    public String getRegexp() {
      return regularExpression;
    }

    @Override
    public String getMessage(String name) {
      return "\"" + name + "\" is a bad name.";
    }

    @Override
    public boolean isExcluded(AstNode astNode) {
      return "LINE".equals(astNode.getTokenValue());
    }

  }

  private final Check check = new Check();

  @Test
  void detected() {
    check.regularExpression = "[a-z]+";
    checkMessagesVerifier.verify(scanFile("/checks/naming.mc", check).getCheckMessages())
      .next().atLine(5).withMessage("\"BAD\" is a bad name.")
      .next().atLine(12).withMessage("\"myFunction\" is a bad name.");
  }

  @Test
  void wrong_regular_expression() {
    check.regularExpression = "*";
    IllegalStateException thrown = catchThrowableOfType(() -> {
      scanFile("/checks/naming.mc", check);
    }, IllegalStateException.class);
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Unable to compile regular expression: *");
  }

}
