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
package org.sonar.cxx.sslr.internal.vm.lexerful; // cxx: in use

import com.sonar.cxx.sslr.api.TokenType;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.vm.Machine;
import org.sonar.cxx.sslr.internal.vm.NativeExpression;

public class TokenTypeExpression extends NativeExpression implements Matcher {

  private final TokenType type;

  public TokenTypeExpression(TokenType type) {
    this.type = type;
  }

  @Override
  public void execute(Machine machine) {
    if (machine.length() == 0 || type != machine.tokenAt(0).getType()) {
      machine.backtrack();
      return;
    }
    machine.createLeafNode(this, 1);
    machine.jump(1);
  }

  @Override
  public String toString() {
    return "TokenType " + type;
  }

}
