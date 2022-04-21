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
package com.sonar.cxx.sslr.impl.channel; // cxx: in use

import org.sonar.cxx.sslr.channel.Channel;
import org.sonar.cxx.sslr.channel.CodeReader;

import com.sonar.cxx.sslr.impl.Lexer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows to skip characters, which match given regular expression.
 * <p>
 * Mostly this channel is used with regular expression "\s++" to remove all whitespace characters.
 * And in such case this channel should be the first one in a sequence of channels for performance reasons,
 * because generally whitespace characters are encountered more often than all other and especially between others.
 * </p>
 */
public class BlackHoleChannel extends Channel<Lexer> {

  private final Matcher matcher;

  /**
   * @throws java.util.regex.PatternSyntaxException if the expression's syntax is invalid
   */
  public BlackHoleChannel(String regexp) {
    matcher = Pattern.compile(regexp).matcher("");
  }

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    return code.popTo(matcher, EmptyAppendable.INSTANCE) != -1;
  }

  private static class EmptyAppendable implements Appendable {

    private static final Appendable INSTANCE = new EmptyAppendable();

    @Override
    public Appendable append(CharSequence csq) throws IOException {
      return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
      return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      return this;
    }
  }

}
