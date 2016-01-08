/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * sonarqube@googlegroups.com
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.SquidCheck;
import com.google.common.base.Strings;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.cxx.tag.Tag;

@Rule(
  key = "HardcodedAccount",
  name = "Do not hard code sensitive data in programs",
  tags = {Tag.CERT, Tag.SECURITY},
  priority = Priority.BLOCKER)
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("30min")
public class HardcodedAccountCheck extends SquidCheck<Grammar> {

  /*
   * more information for C++ security from CERT:
   * https://www.securecoding.cert.org/confluence/pages/viewpage.action?pageId=637
   *
   * MSC18-C. Be careful while handling sensitive data, such as passwords, in program code
   * https://www.securecoding.cert.org/confluence/display/seccode/MSC18-C.+Be+careful+while+handling+sensitive+data%2C+such+as+passwords%2C+in+program+code
   *
   */
  private static final String DEFAULT_REGULAR_EXPRESSION = "\\bDSN\\b.*=.*;\\b(UID|PWD)\\b=.*;";
  private static Matcher reg = null;

  @RuleProperty(
    key = "regularExpression",
    description = "literal regular expression rule",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  public String getRegularExpression() {
    return regularExpression;
  }

  @Override
  public void init() {
    String regEx = getRegularExpression();
    checkNotNull(regularExpression, "getRegularExpression() should not return null");

    if (!Strings.isNullOrEmpty(regEx)) {
      try {
        reg = Pattern.compile(regEx).matcher("");
      } catch (RuntimeException e) {
        throw new IllegalStateException("Unable to compile regular expression: " + regEx, e);
      }
    }
    subscribeTo(CxxGrammarImpl.LITERAL);
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CxxGrammarImpl.LITERAL)) {
      reg.reset(node.getTokenOriginalValue().replaceAll("\\s", ""));
      if (reg.find()) {
        getContext().createLineViolation(this, "Do not hard code sensitive data in programs.", node);
      }
    }
  }
}
