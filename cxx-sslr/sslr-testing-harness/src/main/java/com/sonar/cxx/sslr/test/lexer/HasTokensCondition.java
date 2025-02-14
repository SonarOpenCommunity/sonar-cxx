/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.test.lexer;

import com.sonar.cxx.sslr.api.Token;
import java.util.List;
import org.assertj.core.api.Condition;

class HasTokensCondition extends Condition<List<Token>> {

  private final String[] tokenValues;

  HasTokensCondition(String... tokenValues) {
    this.tokenValues = tokenValues;
    as(describe());
  }

  @Override
  public boolean matches(List<Token> tokens) {
    for (int i = 0; i < tokens.size(); i++) {
      var token = tokens.get(i);
      if (!token.getValue().equals(tokenValues[i])) {
        return false;
      }
    }
    if (tokenValues.length != tokens.size()) {
      return false;
    }
    return true;
  }

  private String describe() {
    var desc = new StringBuilder(256);
    desc.append(tokenValues.length).append(" tokens(");
    for (int i = 0; i < tokenValues.length; i++) {
      desc.append("'").append(tokenValues[i]).append("'");
      if (i < tokenValues.length - 1) {
        desc.append(",");
      }
    }
    desc.append(")");
    return desc.toString();
  }
}
