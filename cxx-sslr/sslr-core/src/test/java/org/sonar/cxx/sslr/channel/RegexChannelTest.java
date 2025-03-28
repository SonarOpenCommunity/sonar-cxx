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
package org.sonar.cxx.sslr.channel;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RegexChannelTest {

  @Test
  void shouldMatch() {
    var dispatcher = ChannelDispatcher.builder().addChannel(new MyWordChannel())
      .addChannel(new BlackholeChannel()).build();
    var output = new StringBuilder();
    dispatcher.consume(new CodeReader("my word"), output);
    assertThat(output).hasToString("<w>my</w> <w>word</w>");
  }

  @Test
  void shouldMatchTokenLongerThanBuffer() {
    var dispatcher = ChannelDispatcher.builder().addChannel(new MyLiteralChannel()).build();
    var output = new StringBuilder();

    var codeReaderConfiguration = new CodeReaderConfiguration();

    int literalLength = 100000;
    var veryLongLiteral = String.format(String.format("%%0%dd", literalLength), 0).replace("0", "a");

    assertThat(veryLongLiteral).hasSize(100000);
    dispatcher.consume(new CodeReader("\">" + veryLongLiteral + "<\"", codeReaderConfiguration), output);
    assertThat(output).hasToString("<literal>\">" + veryLongLiteral + "<\"</literal>");
  }

  private static class MyLiteralChannel extends RegexChannel<StringBuilder> {

    public MyLiteralChannel() {
      super("\"[^\"]*+\"");
    }

    @Override
    protected void consume(CharSequence token, StringBuilder output) {
      output
        .append("<literal>")
        .append(token)
        .append("</literal>");
    }
  }

  private static class MyWordChannel extends RegexChannel<StringBuilder> {

    public MyWordChannel() {
      super("\\w++");
    }

    @Override
    protected void consume(CharSequence token, StringBuilder output) {
      output
        .append("<w>")
        .append(token)
        .append("</w>");
    }
  }

  private static class BlackholeChannel extends Channel<StringBuilder> {

    @Override
    public boolean consume(CodeReader code, StringBuilder output) {
      output.append((char) code.pop());
      return true;
    }
  }

}
