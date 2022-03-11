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
package org.sonar.cxx.sslr.test.channel;

import org.sonar.cxx.sslr.channel.CodeReader;

public final class ChannelMatchers {

  private ChannelMatchers() {
  }

  public static <O> ChannelCondition<O> consume(String sourceCode, O output) {
    return new ChannelCondition<>(sourceCode, output);
  }

  public static <O> ChannelCondition<O> consume(CodeReader codeReader, O output) {
    return new ChannelCondition<>(codeReader, output);
  }

  public static ReaderHasNextCharCondition hasNextChar(char nextChar) {
    return new ReaderHasNextCharCondition(nextChar);
  }
}
