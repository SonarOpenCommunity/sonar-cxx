/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.GenericTokenType;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.vm.Machine;
import org.sonar.cxx.sslr.internal.vm.NativeExpression;

public final class TillNewLineExpression extends NativeExpression implements Matcher {

  public static final TillNewLineExpression INSTANCE = new TillNewLineExpression();

  private TillNewLineExpression() {
  }

  @Override
  public void execute(Machine machine) {
    int currentLine = machine.getIndex() == 0 ? 1 : machine.tokenAt(-1).getLine();
    int offset = 0;
    var token = machine.tokenAt(offset);
    while (token.getLine() == currentLine && token.getType() != GenericTokenType.EOF) {
      offset++;
      token = machine.tokenAt(offset);
    }
    for (int i = 0; i < offset; i++) {
      machine.createLeafNode(this, 1);
    }
    machine.jump(1);
  }

  @Override
  public String toString() {
    return "TillNewLine";
  }

}
