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

public class PPNumberTest {

  @Test
  void decode_numbers() {
    assertThat(PPNumber.decode("1")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("067")).isEqualTo(new BigInteger("67", 8));
    assertThat(PPNumber.decode("0b11")).isEqualTo(new BigInteger("11", 2));
    assertThat(PPNumber.decode("0xab")).isEqualTo(new BigInteger("ab", 16));

    assertThat(PPNumber.decode("1L")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("1l")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("1U")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("1u")).isEqualTo(new BigInteger("1", 10));

    assertThat(PPNumber.decode("1ul")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("1ll")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("1i64")).isEqualTo(new BigInteger("1", 10));
    assertThat(PPNumber.decode("1ui64")).isEqualTo(new BigInteger("1", 10));

    assertThat(PPNumber.decode("067ll")).isEqualTo(new BigInteger("67", 8));
    assertThat(PPNumber.decode("0b11ul")).isEqualTo(new BigInteger("11", 2));
    assertThat(PPNumber.decode("0xabui64")).isEqualTo(new BigInteger("ab", 16));

    assertThat(PPNumber.decode("1'234")).isEqualTo(new BigInteger("1234", 10));
    assertThat(PPNumber.decode("0b1111'0000'1111")).isEqualTo(new BigInteger("111100001111", 2));
    assertThat(PPNumber.decode("0xAAAA'bbbb")).isEqualTo(new BigInteger("AAAAbbbb", 16));
  }

}
