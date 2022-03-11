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
package com.sonar.cxx.sslr.impl.channel;

import com.sonar.cxx.sslr.impl.Lexer;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.channel.CodeReader;
import static org.sonar.cxx.sslr.test.channel.ChannelMatchers.*;

public class BlackHoleChannelTest {

  private final Lexer lexer = Lexer.builder().build();
  private final BlackHoleChannel channel = new BlackHoleChannel("[ \\t]+");

  @Test
  public void testConsumeOneCharacter() {
    AssertionsForClassTypes.assertThat(channel).is(consume(" ", lexer));
    AssertionsForClassTypes.assertThat(channel).is(consume("\t", lexer));
    AssertionsForClassTypes.assertThat(channel).isNot(consume("g", lexer));
    AssertionsForClassTypes.assertThat(channel).isNot(consume("-", lexer));
    AssertionsForClassTypes.assertThat(channel).isNot(consume("1", lexer));
  }

  @Test
  public void consumeSeveralCharacters() {
    var reader = new CodeReader("   \t123");
    AssertionsForClassTypes.assertThat(channel).is(consume(reader, lexer));
    AssertionsForClassTypes.assertThat(reader).has(hasNextChar('1'));
  }
}
