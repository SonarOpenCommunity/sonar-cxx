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
package org.sonar.cxx.sslr.internal.toolkit;

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LineOffsetsTest {

  @Test
  void getStartOffset() {
    var foo = mock(Token.class);
    when(foo.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(foo.getValue()).thenReturn("foo");
    when(foo.getLine()).thenReturn(1);
    when(foo.getColumn()).thenReturn(0);

    var bar = mock(Token.class);
    when(bar.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(bar.getValue()).thenReturn("bar");
    when(bar.getLine()).thenReturn(2);
    when(bar.getColumn()).thenReturn(2);

    var lineOffsets = new LineOffsets("foo\n??bar");

    assertThat(lineOffsets.getStartOffset(foo)).isZero();
    assertThat(lineOffsets.getStartOffset(bar)).isEqualTo(6);
  }

  @Test
  void getEndOffsetSingleLine() {
    var foo = mock(Token.class);
    when(foo.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(foo.getValue()).thenReturn("foo");
    when(foo.getOriginalValue()).thenReturn("foo");
    when(foo.getLine()).thenReturn(1);
    when(foo.getColumn()).thenReturn(0);

    var bar = mock(Token.class);
    when(bar.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(bar.getValue()).thenReturn("bar");
    when(bar.getOriginalValue()).thenReturn("bar");
    when(bar.getLine()).thenReturn(2);
    when(bar.getColumn()).thenReturn(2);

    var lineOffsets = new LineOffsets("foo\n??bar...");

    assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3);
    assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(9);
  }

  @Test
  void getEndOffsetMultiLine() {
    var foo = mock(Token.class);
    when(foo.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(foo.getValue()).thenReturn("foo");
    when(foo.getOriginalValue()).thenReturn("foo");
    when(foo.getLine()).thenReturn(1);
    when(foo.getColumn()).thenReturn(0);

    var bar = mock(Token.class);
    when(bar.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(bar.getValue()).thenReturn("bar\nbaz");
    when(bar.getOriginalValue()).thenReturn("bar\nbaz");
    when(bar.getLine()).thenReturn(2);
    when(bar.getColumn()).thenReturn(2);

    var lineOffsets = new LineOffsets("foo\n??bar\nbaz...");

    assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3);
    assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(13);
  }

  @Test
  void getEndOffsetMultiLineRNSingleOffsetIncrement() {
    var foo = mock(Token.class);
    when(foo.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(foo.getValue()).thenReturn("foo");
    when(foo.getOriginalValue()).thenReturn("foo");
    when(foo.getLine()).thenReturn(1);
    when(foo.getColumn()).thenReturn(0);

    var bar = mock(Token.class);
    when(bar.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(bar.getValue()).thenReturn("bar\r\nbaz");
    when(bar.getOriginalValue()).thenReturn("bar\r\nbaz");
    when(bar.getLine()).thenReturn(2);
    when(bar.getColumn()).thenReturn(2);

    var lineOffsets = new LineOffsets("foo\n??bar\r\nbaz...");

    assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3);
    assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(13);
  }

  @Test
  void getEndOffsetMultiLineRNewLine() {
    var foo = mock(Token.class);
    when(foo.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(foo.getValue()).thenReturn("foo");
    when(foo.getOriginalValue()).thenReturn("foo");
    when(foo.getLine()).thenReturn(1);
    when(foo.getColumn()).thenReturn(0);

    var bar = mock(Token.class);
    when(bar.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(bar.getValue()).thenReturn("bar\rbaz");
    when(bar.getOriginalValue()).thenReturn("bar\rbaz");
    when(bar.getLine()).thenReturn(2);
    when(bar.getColumn()).thenReturn(2);

    var lineOffsets = new LineOffsets("foo\n??bar\rbaz...");

    assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3);
    assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(13);
  }

  @Test
  void getOffset() {
    var lineOffsets = new LineOffsets("int a = 0;\nint b = 0;");

    assertThat(lineOffsets.getOffset(2, 4)).isEqualTo(15);
    assertThat(lineOffsets.getOffset(2, 100)).isEqualTo(21);
    assertThat(lineOffsets.getOffset(100, 100)).isEqualTo(21);
  }

  @Test
  void getOffsetCariageReturnAsNewLine() {
    var lineOffsets = new LineOffsets("\rfoo");

    assertThat(lineOffsets.getOffset(1, 0)).isZero();
    assertThat(lineOffsets.getOffset(2, 0)).isEqualTo(1);
  }

  @Test
  void getOffsetCariageReturnAndLineFeedAsSingleOffset() {
    var lineOffsets = new LineOffsets("\r\nfoo");

    assertThat(lineOffsets.getOffset(1, 0)).isZero();
    assertThat(lineOffsets.getOffset(2, 0)).isEqualTo(1);
  }

  @Test
  void getOffsetBadLine() {
    var lineOffsets = new LineOffsets("");

    var thrown = catchThrowableOfType(() -> {
      lineOffsets.getOffset(0, 0);
    }, IllegalArgumentException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getOffsetBadColumn() {
    var lineOffsets = new LineOffsets("");

    var thrown = catchThrowableOfType(() -> {
      lineOffsets.getOffset(1, -1);
    }, IllegalArgumentException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalArgumentException.class);
  }

}
