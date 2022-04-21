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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.regex.Pattern;

public abstract class AbstractNamingCheck<G extends Grammar> extends SquidCheck<G> {

  private Pattern pattern;

  public abstract AstNodeType[] getRules();

  public abstract String getName(AstNode astNode);

  public abstract String getRegexp();

  public abstract String getMessage(String name);

  public abstract boolean isExcluded(AstNode astNode);

  @Override
  public void init() {
    AstNodeType[] rules = getRules();
    checkNotNull(rules, "getRules() must not return null");
    checkArgument(rules.length > 0, "getRules() must return at least one rule");

    subscribeTo(getRules());

    String regexp = getRegexp();
    checkNotNull(regexp, "getRegexp() must not return null");

    try {
      this.pattern = Pattern.compile(regexp);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to compile regular expression: " + regexp, e);
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isExcluded(astNode)) {
      String name = getName(astNode);
      checkNotNull(name, "getName() must not return null");

      if (!pattern.matcher(name).matches()) {
        getContext().createLineViolation(this, getMessage(name), astNode);
      }
    }
  }

}
