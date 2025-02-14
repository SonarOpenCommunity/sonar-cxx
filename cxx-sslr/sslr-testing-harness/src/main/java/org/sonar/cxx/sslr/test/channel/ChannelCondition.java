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

import org.assertj.core.api.Condition;
import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

public class ChannelCondition<O> extends Condition<Channel<O>> {

  private final String sourceCode;
  private final O output;
  private final CodeReader reader;

  public ChannelCondition(String sourceCode, O output) {
    this.sourceCode = sourceCode;
    this.output = output;
    this.reader = new CodeReader(sourceCode);
    as(describe());
  }

  public ChannelCondition(CodeReader reader, O output) {
    this.output = output;
    this.sourceCode = new String(reader.peek(30));
    this.reader = reader;
    as(describe());
  }

  @Override
  public boolean matches(Channel<O> channel) {
    return channel.consume(reader, output);
  }

  private String describe() {
    return "Channel consumes '" + sourceCode + "'";
  }

}
