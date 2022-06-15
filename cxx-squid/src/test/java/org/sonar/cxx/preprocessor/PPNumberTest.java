/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

    assertThat(PPNumber.decodeCharacter("\\t")).isEqualTo(BigInteger.valueOf('\t'));
    assertThat(PPNumber.decodeCharacter("\\b")).isEqualTo(BigInteger.valueOf('\b'));
    assertThat(PPNumber.decodeCharacter("\\n")).isEqualTo(BigInteger.valueOf('\n'));
    assertThat(PPNumber.decodeCharacter("\\r")).isEqualTo(BigInteger.valueOf('\r'));
    assertThat(PPNumber.decodeCharacter("\\f")).isEqualTo(BigInteger.valueOf('\f'));
    assertThat(PPNumber.decodeCharacter("\\'")).isEqualTo(BigInteger.valueOf('\''));
    assertThat(PPNumber.decodeCharacter("\\\"")).isEqualTo(BigInteger.valueOf('\"'));
    assertThat(PPNumber.decodeCharacter("\\\\")).isEqualTo(BigInteger.valueOf('\\'));

    assertThat(PPNumber.decodeCharacter("\\0")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\1")).isEqualTo(BigInteger.valueOf(1));

    assertThat(PPNumber.decodeCharacter("\\x00")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\x01")).isEqualTo(BigInteger.valueOf(1));
    assertThat(PPNumber.decodeCharacter("\\X00")).isEqualTo(BigInteger.valueOf(0));
    assertThat(PPNumber.decodeCharacter("\\X01")).isEqualTo(BigInteger.valueOf(1));
  }

}
