/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import java.math.BigInteger;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PPNumberTest {

  @Test
  void decode_strings() {
    assertThat(PPNumber.decodeString("1")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("067")).isEqualTo(new BigInteger("67", 8));
    assertThat(PPNumber.decodeString("0b11")).isEqualTo(new BigInteger("11", 2));
    assertThat(PPNumber.decodeString("0xab")).isEqualTo(new BigInteger("ab", 16));

    assertThat(PPNumber.decodeString("1L")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("1l")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("1U")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("1u")).isEqualTo(new BigInteger("1", 10));

    assertThat(PPNumber.decodeString("1ul")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("1ll")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("1i64")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decodeString("1ui64")).isEqualTo(new BigInteger("1", 10));

    assertThat(PPNumber.decodeString("067ll")).isEqualTo(new BigInteger("67", 8));
    assertThat(PPNumber.decodeString("0b11ul")).isEqualTo(new BigInteger("11", 2));
    assertThat(PPNumber.decodeString("0xabui64")).isEqualTo(new BigInteger("ab", 16));

    assertThat(PPNumber.decodeString("1'234")).isEqualTo(new BigInteger("1234", 10));
    assertThat(PPNumber.decodeString("0b1111'0000'1111")).isEqualTo(new BigInteger("111100001111", 2));
    assertThat(PPNumber.decodeString("0xAAAA'bbbb")).isEqualTo(new BigInteger("AAAAbbbb", 16));
  }

  @Test
  void decode_charcters() {
    assertThat(PPNumber.decodeCharacter("")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\0")).isEqualTo(BigInteger.valueOf(0));

    assertThat(PPNumber.decodeCharacter("1")).isEqualTo(BigInteger.valueOf('1'));
    assertThat(PPNumber.decodeCharacter("A")).isEqualTo(BigInteger.valueOf('A'));

    // simple escape sequences
    assertThat(PPNumber.decodeCharacter("\\'")).isEqualTo(BigInteger.valueOf('\''));
    assertThat(PPNumber.decodeCharacter("\\\"")).isEqualTo(BigInteger.valueOf('\"'));
    assertThat(PPNumber.decodeCharacter("\\?")).isEqualTo(BigInteger.valueOf(0x3f)); // \?
    assertThat(PPNumber.decodeCharacter("\\\\")).isEqualTo(BigInteger.valueOf('\\'));
    assertThat(PPNumber.decodeCharacter("\\a")).isEqualTo(BigInteger.valueOf(0x07)); // \a
    assertThat(PPNumber.decodeCharacter("\\b")).isEqualTo(BigInteger.valueOf('\b'));
    assertThat(PPNumber.decodeCharacter("\\f")).isEqualTo(BigInteger.valueOf('\f'));
    assertThat(PPNumber.decodeCharacter("\\n")).isEqualTo(BigInteger.valueOf('\n'));
    assertThat(PPNumber.decodeCharacter("\\r")).isEqualTo(BigInteger.valueOf('\r'));
    assertThat(PPNumber.decodeCharacter("\\t")).isEqualTo(BigInteger.valueOf('\t'));
    assertThat(PPNumber.decodeCharacter("\\v")).isEqualTo(BigInteger.valueOf(0x0b)); // \v

    // numeric escape sequences
    assertThat(PPNumber.decodeCharacter("\\0")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\123")).isEqualTo(BigInteger.valueOf(83));
    assertThat(PPNumber.decodeCharacter("\\o{123}")).isEqualTo(BigInteger.valueOf(83));
    assertThat(PPNumber.decodeCharacter("\\x00")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\x0f")).isEqualTo(BigInteger.valueOf(15));
    assertThat(PPNumber.decodeCharacter("\\x{FF}")).isEqualTo(BigInteger.valueOf(255));

    // universal character names
    assertThat(PPNumber.decodeCharacter("\\u12345")).isEqualTo(BigInteger.valueOf(0x1234));
    assertThat(PPNumber.decodeCharacter("\\U123456789")).isEqualTo(BigInteger.valueOf(0x12345678));
    assertThat(PPNumber.decodeCharacter("\\u{1234}")).isEqualTo(BigInteger.valueOf(0x1234));
    assertThat(PPNumber.decodeCharacter("\\N{NULL}")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\N{NUL}")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\N{NEW LINE}")).isEqualTo(BigInteger.valueOf(1));
  }

}
