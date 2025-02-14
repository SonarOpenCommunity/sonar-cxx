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
package org.sonar.cxx.sslr.test.channel;

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;
import static org.sonar.cxx.sslr.test.channel.ChannelMatchers.consume;
import static org.sonar.cxx.sslr.test.channel.ChannelMatchers.hasNextChar;

class ChannelMatchersTest {

  @Test
  void testConsumeMatcher() {
    Channel<StringBuilder> numberChannel = new Channel<StringBuilder>() {

      @Override
      public boolean consume(CodeReader code, StringBuilder output) {
        if (Character.isDigit(code.peek())) {
          output.append((char) code.pop());
          return true;
        }
        return false;
      }
    };
    var output = new StringBuilder();
    AssertionsForClassTypes.assertThat(numberChannel).has(consume("3", output));
    assertThat(output).hasToString("3");
    AssertionsForClassTypes.assertThat(numberChannel).has(consume(new CodeReader("333333"), output));

    output = new StringBuilder();
    AssertionsForClassTypes.assertThat(numberChannel).isNot(consume("n", output));
    assertThat(output).hasToString("");
    AssertionsForClassTypes.assertThat(numberChannel).isNot(consume(new CodeReader("n"), output));
  }

  @Test
  void testHasNextChar() {
    AssertionsForClassTypes.assertThat(new CodeReader("123")).is(hasNextChar('1'));
    AssertionsForClassTypes.assertThat(new CodeReader("123")).isNot(hasNextChar('n'));
  }
}
