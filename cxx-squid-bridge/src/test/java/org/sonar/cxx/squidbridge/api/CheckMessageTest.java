/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.api;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CheckMessageTest {

  @Test
  void testFormatDefaultMessage() {
    var message = new CheckMessage(null, "Value is {0,number,integer}, expected value is {1,number,integer}.",
      3, 7);
    assertThat(message.formatDefaultMessage()).isEqualTo("Value is 3, expected value is 7.");
  }

  @Test
  void testNotFormatMessageWithoutParameters() {
    // This message can't be used as a pattern by the MessageFormat class
    var message = new CheckMessage(null, "public void main(){.");
    assertThat(message.formatDefaultMessage()).isEqualTo("public void main(){.");
  }
}
