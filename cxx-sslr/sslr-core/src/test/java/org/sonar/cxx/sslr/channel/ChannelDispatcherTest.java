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
package org.sonar.cxx.sslr.channel;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ChannelDispatcherTest {

  @Test
  public void shouldRemoveSpacesFromString() {
    var dispatcher = ChannelDispatcher.builder().addChannel(new SpaceDeletionChannel())
      .build();
    var output = new StringBuilder();
    dispatcher.consume(new CodeReader("two words"), output);
    assertThat(output.toString()).isEqualTo("twowords");
  }

  @Test
  public void shouldAddChannels() {
    var dispatcher = ChannelDispatcher.builder().addChannels(new SpaceDeletionChannel(),
                                                         new FakeChannel()).build();
    assertThat(dispatcher.getChannels().length).isEqualTo(2);
    assertThat(dispatcher.getChannels()[0]).isInstanceOf(SpaceDeletionChannel.class);
    assertThat(dispatcher.getChannels()[1]).isInstanceOf(FakeChannel.class);
  }

  @Test
  public void shouldThrowExceptionWhenNoChannelToConsumeNextCharacter() {
    var thrown = catchThrowableOfType(() -> {
      var dispatcher = ChannelDispatcher.builder()
        .failIfNoChannelToConsumeOneCharacter()
        .build();
      dispatcher.consume(new CodeReader("two words"), new StringBuilder());
    }, IllegalStateException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  private static class SpaceDeletionChannel extends Channel<StringBuilder> {

    @Override
    public boolean consume(CodeReader code, StringBuilder output) {
      if (code.peek() == ' ') {
        code.pop();
      } else {
        output.append((char) code.pop());
      }
      return true;
    }
  }

  private static class FakeChannel extends Channel<StringBuilder> {

    @Override
    public boolean consume(CodeReader code, StringBuilder output) {
      var b = true;
      return b;
    }
  }

}
