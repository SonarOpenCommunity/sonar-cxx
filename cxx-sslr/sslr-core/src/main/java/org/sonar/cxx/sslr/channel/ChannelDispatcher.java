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
package org.sonar.cxx.sslr.channel; // cxx: in use

import java.util.ArrayList;
import java.util.List;

public final class ChannelDispatcher<O> extends Channel<O> {

  private final boolean failIfNoChannelToConsumeOneCharacter;

  private final Channel<O>[] channels;

  private ChannelDispatcher(Builder builder) {
    this.channels = builder.channels.toArray(Channel[]::new);
    this.failIfNoChannelToConsumeOneCharacter = builder.failIfNoChannelToConsumeOneCharacter;
  }

  @Override
  public boolean consume(CodeReader code, O output) {
    int nextChar = code.peek();
    while (nextChar != -1) {
      var characterConsumed = false;
      for (var channel : channels) {
        if (channel.consume(code, output)) {
          characterConsumed = true;
          break;
        }
      }
      if (!characterConsumed) {
        if (failIfNoChannelToConsumeOneCharacter) {
          var message = "None of the channel has been able to handle character '" + (char) code.peek()
            + "' (decimal value " + code.peek() + ") at line " + code.getLinePosition()
            + ", column " + code.getColumnPosition();
          throw new IllegalStateException(message);
        }
        code.pop();
      }
      nextChar = code.peek();
    }
    return true;
  }

  Channel[] getChannels() {
    return channels;
  }

  /**
   * Get a Builder instance to build a new ChannelDispatcher
   */
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private final List<Channel> channels = new ArrayList<>();
    private boolean failIfNoChannelToConsumeOneCharacter = false;

    private Builder() {
    }

    public Builder addChannel(Channel channel) {
      channels.add(channel);
      return this;
    }

    public Builder addChannels(Channel... c) {
      for (var channel : c) {
        addChannel(channel);
      }
      return this;
    }

    /**
     * If this option is activated, an IllegalStateException will be thrown as soon as a character won't be consumed by
     * any channel.
     */
    public Builder failIfNoChannelToConsumeOneCharacter() {
      failIfNoChannelToConsumeOneCharacter = true;
      return this;
    }

    public <O> ChannelDispatcher<O> build() {
      return new ChannelDispatcher<>(this);
    }

  }
}
