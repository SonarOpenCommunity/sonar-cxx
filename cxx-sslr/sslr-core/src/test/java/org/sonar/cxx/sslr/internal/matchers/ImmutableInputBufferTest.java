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
package org.sonar.cxx.sslr.internal.matchers;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.internal.matchers.InputBuffer.Position;

public class ImmutableInputBufferTest {

  @Test
  public void test() {
    InputBuffer inputBuffer = new ImmutableInputBuffer("foo\r\nbar\nbaz\rqux\r".toCharArray());

    assertThat(inputBuffer.getLineCount()).isEqualTo(5);

    assertThat(inputBuffer.extractLine(1)).isEqualTo("foo\r\n");
    assertThat(inputBuffer.extractLine(2)).isEqualTo("bar\n");
    assertThat(inputBuffer.extractLine(3)).isEqualTo("baz\r");
    assertThat(inputBuffer.extractLine(4)).isEqualTo("qux\r");
    assertThat(inputBuffer.extractLine(5)).isEqualTo("");

    assertThat(inputBuffer.getPosition(0)).isEqualTo(new Position(1, 1));
    assertThat(inputBuffer.getPosition(4)).isEqualTo(new Position(1, 5));
    assertThat(inputBuffer.getPosition(5)).isEqualTo(new Position(2, 1));
    assertThat(inputBuffer.getPosition(8)).isEqualTo(new Position(2, 4));
    assertThat(inputBuffer.getPosition(9)).isEqualTo(new Position(3, 1));
    assertThat(inputBuffer.getPosition(12)).isEqualTo(new Position(3, 4));
    assertThat(inputBuffer.getPosition(13)).isEqualTo(new Position(4, 1));
    assertThat(inputBuffer.getPosition(16)).isEqualTo(new Position(4, 4));
    assertThat(inputBuffer.getPosition(17)).isEqualTo(new Position(5, 1));
  }

  @Test
  public void test_single_line() {
    InputBuffer inputBuffer = new ImmutableInputBuffer("foo".toCharArray());

    assertThat(inputBuffer.getLineCount()).isEqualTo(1);

    assertThat(inputBuffer.extractLine(1)).isEqualTo("foo");

    assertThat(inputBuffer.getPosition(0)).isEqualTo(new Position(1, 1));
    assertThat(inputBuffer.getPosition(1)).isEqualTo(new Position(1, 2));
    assertThat(inputBuffer.getPosition(2)).isEqualTo(new Position(1, 3));
    assertThat(inputBuffer.getPosition(3)).isEqualTo(new Position(1, 4));
  }

  @Test
  public void test_empty() {
    InputBuffer inputBuffer = new ImmutableInputBuffer("".toCharArray());

    assertThat(inputBuffer.getLineCount()).isEqualTo(1);

    assertThat(inputBuffer.extractLine(1)).isEqualTo("");

    assertThat(inputBuffer.getPosition(0)).isEqualTo(new Position(1, 1));
    assertThat(inputBuffer.getPosition(1)).isEqualTo(new Position(1, 2));
  }

  @Test
  public void test_empty_lines_with_LF() {
    InputBuffer inputBuffer = new ImmutableInputBuffer("\n\n".toCharArray());

    assertThat(inputBuffer.getLineCount()).isEqualTo(3);

    assertThat(inputBuffer.extractLine(1)).isEqualTo("\n");
    assertThat(inputBuffer.extractLine(2)).isEqualTo("\n");
    assertThat(inputBuffer.extractLine(3)).isEqualTo("");

    assertThat(inputBuffer.getPosition(0)).isEqualTo(new Position(1, 1));
    assertThat(inputBuffer.getPosition(1)).isEqualTo(new Position(2, 1));
    assertThat(inputBuffer.getPosition(2)).isEqualTo(new Position(3, 1));
  }

  @Test
  public void test_empty_lines_with_CR() {
    InputBuffer inputBuffer = new ImmutableInputBuffer("\r\r".toCharArray());

    assertThat(inputBuffer.getLineCount()).isEqualTo(3);

    assertThat(inputBuffer.extractLine(1)).isEqualTo("\r");
    assertThat(inputBuffer.extractLine(2)).isEqualTo("\r");
    assertThat(inputBuffer.extractLine(3)).isEqualTo("");

    assertThat(inputBuffer.getPosition(0)).isEqualTo(new Position(1, 1));
    assertThat(inputBuffer.getPosition(1)).isEqualTo(new Position(2, 1));
    assertThat(inputBuffer.getPosition(2)).isEqualTo(new Position(3, 1));
  }

  @Test
  public void test_empty_lines_with_CRLF() {
    InputBuffer inputBuffer = new ImmutableInputBuffer("\r\n\r\n".toCharArray());

    assertThat(inputBuffer.getLineCount()).isEqualTo(3);

    assertThat(inputBuffer.extractLine(1)).isEqualTo("\r\n");
    assertThat(inputBuffer.extractLine(2)).isEqualTo("\r\n");
    assertThat(inputBuffer.extractLine(3)).isEqualTo("");

    assertThat(inputBuffer.getPosition(0)).isEqualTo(new Position(1, 1));
    assertThat(inputBuffer.getPosition(1)).isEqualTo(new Position(1, 2));
    assertThat(inputBuffer.getPosition(2)).isEqualTo(new Position(2, 1));
    assertThat(inputBuffer.getPosition(3)).isEqualTo(new Position(2, 2));
    assertThat(inputBuffer.getPosition(4)).isEqualTo(new Position(3, 1));
  }

  @Test
  public void test_equality_and_hash_code_of_positions() {
    var position = new Position(0, 0);
    assertThat(position).isEqualTo(position);
    assertThat(position).isEqualTo(new Position(0, 0));
    assertThat(position.hashCode()).isEqualTo(new Position(0, 0).hashCode());
    assertThat(position).isNotEqualTo(new Position(0, 1));
    assertThat(position).isNotEqualTo(new Position(1, 1));
    assertThat(position).isNotEqualTo(new Object());
  }

}
