/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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

import java.io.StringReader;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CodeReaderTest {

  @Test
  void testPopWithAppendable() {
    var reader = new CodeReader("package org.sonar;");

    var sw = new StringBuilder();
    reader.pop(sw);
    assertThat(sw).hasToString("p");
    reader.pop(sw);
    assertThat(sw).hasToString("pa");

  }

  @Test
  void testPeekACharArray() {
    var reader = new CodeReader(new StringReader("bar"));
    var chars = reader.peek(2);
    assertThat(chars).hasSize(2);
    assertThat(chars[0]).isEqualTo('b');
    assertThat(chars[1]).isEqualTo('a');
  }

  @Test
  void testPeekTo() {
    var reader = new CodeReader(new StringReader("package org.sonar;"));
    var result = new StringBuilder();
    reader.peekTo((int endFlag) -> 'r' == (char) endFlag, result);
    assertThat(result).hasToString("package o");
    assertThat(reader.peek()).isEqualTo((int) 'p'); // never called pop()
  }

  @Test
  void peekToShouldStopAtEndOfInput() {
    var reader = new CodeReader("foo");
    var result = new StringBuilder();
    reader.peekTo(i -> false, result);
    assertThat(result).hasToString("foo");
  }

  @Test
  void testPopToWithRegex() {
    var reader = new CodeReader(new StringReader("123ABC"));
    var token = new StringBuilder();
    assertThat(reader.popTo(Pattern.compile("\\d+").matcher(new String()), token)).isEqualTo(3);
    assertThat(token).hasToString("123");
    assertThat(reader.popTo(Pattern.compile("\\d+").matcher(new String()), token)).isEqualTo(-1);
    assertThat(reader.popTo(Pattern.compile("\\w+").matcher(new String()), token)).isEqualTo(3);
    assertThat(token).hasToString("123ABC");
    assertThat(reader.popTo(Pattern.compile("\\w+").matcher(new String()), token)).isEqualTo(-1);

    // Should reset matcher with empty string:
    var matcher = Pattern.compile("\\d+").matcher("");
    reader.popTo(matcher, token);
    try {
      matcher.find(1);
      fail("exception expected");
    } catch (IndexOutOfBoundsException e) {
      assertThat(e.getMessage()).isEqualTo("Illegal start index");
    }
  }

  @Test
  void testStackOverflowError() {
    var sb = new StringBuilder();
    sb.append("\n");
    for (int i = 0; i < 10000; i++) {
      sb.append(Integer.toHexString(i));
    }
    var reader = new CodeReader(sb.toString());
    reader.pop();
    reader.pop();

    var thrown = catchThrowableOfType(ChannelException.class,
      () -> reader.popTo(Pattern.compile("([a-fA-F]|\\d)+").matcher(""), new StringBuilder())
    );
    assertThat(thrown)
      .hasMessage("Unable to apply regular expression '([a-fA-F]|\\d)+' at line 2 and column 1,"
        + " because it led to a stack overflow error."
        + " This error may be due to an inefficient use of alternations - see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050507");
  }

  @Test
  void testPopToWithRegexAndFollowingMatcher() {
    var digitMatcher = Pattern.compile("\\d+").matcher(new String());
    var alphabeticMatcher = Pattern.compile("[a-zA-Z]").matcher(new String());
    var token = new StringBuilder();
    assertThat(new CodeReader(new StringReader("123 ABC")).popTo(digitMatcher, alphabeticMatcher, token)).isEqualTo(-1);
    assertThat(token).hasToString("");
    assertThat(new CodeReader(new StringReader("123ABC")).popTo(digitMatcher, alphabeticMatcher, token)).isEqualTo(3);
    assertThat(token).hasToString("123");
  }
}
