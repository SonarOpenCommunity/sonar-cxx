/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package org.sonar.cxx.sslr.internal.vm.lexerful; // cxx: in use

import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.vm.Machine;
import org.sonar.cxx.sslr.internal.vm.NativeExpression;

public class TokensBridgeExpression extends NativeExpression implements Matcher {

  private final TokenType from;
  private final TokenType to;

  public TokensBridgeExpression(TokenType from, TokenType to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public void execute(Machine machine) {
    int length = machine.length();
    if (length < 2 || machine.tokenAt(0).getType() != from) {
      machine.backtrack();
      return;
    }
    int offset = 0;
    int bridgeLevel = 1;
    Token token;
    do {
      offset++;
      if (offset >= length) {
        machine.backtrack();
        return;
      }
      token = machine.tokenAt(offset);
      if (token.getType() == from) {
        bridgeLevel++;
      } else if (token.getType() == to) {
        bridgeLevel--;
      } else {
        // nop
      }
    } while (bridgeLevel != 0);
    for (int i = 0; i <= offset; i++) {
      machine.createLeafNode(this, 1);
    }
    machine.jump(1);
  }

  @Override
  public String toString() {
    return "Bridge[" + from + "," + to + "]";
  }

}
