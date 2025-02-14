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
package org.sonar.cxx.sslr.internal.vm;

import org.sonar.cxx.sslr.internal.matchers.Matcher;

public class StringExpression extends NativeExpression implements Matcher {

  private final String string;

  public StringExpression(String string) {
    this.string = string;
  }

  @Override
  public void execute(Machine machine) {
    if (machine.length() < string.length()) {
      machine.backtrack();
      return;
    }
    for (int i = 0; i < string.length(); i++) {
      if (machine.charAt(i) != string.charAt(i)) {
        machine.backtrack();
        return;
      }
    }
    machine.createLeafNode(this, string.length());
    machine.jump(1);
  }

  @Override
  public String toString() {
    return "String " + string;
  }

}
