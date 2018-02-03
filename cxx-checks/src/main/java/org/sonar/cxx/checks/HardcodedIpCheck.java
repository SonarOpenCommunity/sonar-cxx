/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "HardcodedIp",
  name = "IP addresses should not be hardcoded",
  tags = {Tag.CERT, Tag.SECURITY},
  priority = Priority.CRITICAL)
@ActivatedByDefault
@SqaleConstantRemediation("30min")
public class HardcodedIpCheck extends SquidCheck<Grammar> {

// full IPv6:
//  (^\d{20}$)|(^((:[a-fA-F0-9]{1,4}){6}|::)ffff:(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})
//  (\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3}$)|(^((:[a-fA-F0-9]{1,4}){6}|::)ffff
//  (:[a-fA-F0-9]{1,4}){2}$)|(^([a-fA-F0-9]{1,4}) (:[a-fA-F0-9]{1,4}){7}$)|(^:(:[a-fA-F0-9]{1,4}(::)?){1,6}$)|
//  (^((::)?[a-fA-F0-9]{1,4}:){1,6}:$)|(^::$)
// simple IPV4 and IPV6 address:
//  ([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|(\d{1,3}\.){3}\d{1,3}
// IPv4 with port number
//  (?:^|\s)([a-z]{3,6}(?=://))?(://)?((?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.
//  (?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.(?:25[0-5]|2[0-4]\d|[01]?\d\d?))(?::(\d{2,5}))?(?:\s|$)
  private static final String DEFAULT_REGULAR_EXPRESSION
    = "^.*((?<![\\d|\\.])(?:\\b(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b\\.){3}\\b(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b(?!\\d|\\.)).*$";
  private static volatile Matcher IP;

  @RuleProperty(
    key = "regularExpression",
    description = "The regular expression",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  public String getRegularExpression() {
    return regularExpression;
  }

  @SuppressWarnings("squid:S2696") // ... initialize SquidAstVisitor
  @Override
  public void init() {
    String regEx = getRegularExpression();
    Objects.requireNonNull(regEx, "getRegularExpression() should not return null");

    if (!regEx.isEmpty()) {
      try {
        IP = Pattern.compile(regEx).matcher("");
      } catch (RuntimeException e) {
        throw new IllegalStateException("Unable to compile regular expression: " + regEx, e);
      }
    }
    subscribeTo(CxxGrammarImpl.LITERAL);
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CxxGrammarImpl.LITERAL)) {
      IP.reset(node.getTokenOriginalValue());
      if (IP.find()) {
        String ip = IP.group(0).replaceAll("\"", "");
        getContext().createLineViolation(this, "Make this IP \"" + ip + "\" address configurable.", node);
      }
    }
  }

}
