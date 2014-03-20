/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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

import com.google.common.base.Strings;



import org.sonar.api.utils.SonarException;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Rule(
  key = "NoHardcodedIp",
  description = "IP addresses should never be hardcoded into the source code",
  priority = Priority.CRITICAL)

public class HardcodedIpCheck extends SquidCheck<Grammar>  {

// full IPv6:
//  (^\d{20}$)|(^((:[a-fA-F0-9]{1,4}){6}|::)ffff:(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3}$)|(^((:[a-fA-F0-9]{1,4}){6}|::)ffff(:[a-fA-F0-9]{1,4}){2}$)|(^([a-fA-F0-9]{1,4}) (:[a-fA-F0-9]{1,4}){7}$)|(^:(:[a-fA-F0-9]{1,4}(::)?){1,6}$)|(^((::)?[a-fA-F0-9]{1,4}:){1,6}:$)|(^::$)
// simple IPV4 and IPV6 address:
//  ([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|(\d{1,3}\.){3}\d{1,3}
// IPv4 with port number
//  (?:^|\s)([a-z]{3,6}(?=://))?(://)?((?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.(?:25[0-5]|2[0-4]\d|[01]?\d\d?))(?::(\d{2,5}))?(?:\s|$)

  private static final String DEFAULT_REGULAR_EXPRESSION = "^.*((?<![\\d|\\.])(?:\\b(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b\\.){3}\\b(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b(?!\\d|\\.)).*$";
  private static Matcher IP = null;
  
  @RuleProperty(
      key = "regularExpression",
      defaultValue = DEFAULT_REGULAR_EXPRESSION)
    public String regularExpression = DEFAULT_REGULAR_EXPRESSION;
  
  public String getRegularExpression() {
    return regularExpression;
  }
  
  @Override
  public void init() {
    String regEx = getRegularExpression();
    checkNotNull(regEx, "getRegularExpression() should not return null");

    if (!Strings.isNullOrEmpty(regEx)) {
      try {    
        IP = Pattern.compile(regEx).matcher("");
      } catch (RuntimeException e) {
        throw new SonarException("Unable to compile regular expression: " + regEx, e);
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
