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
package org.sonar.cxx.squidbridge.checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.squidbridge.api.CheckMessage;

class CheckMessagesVerifierRuleTest {

  @Test
  void shouldNotFailIfNothingToVerify() {
    var rule = new CheckMessagesVerifierRule();
    rule.verify();
  }

  @Test
  void shouldNotFailIfVerificationsWereSuccessful() {
    var rule = new CheckMessagesVerifierRule();
    rule.verify(Collections.EMPTY_LIST);
    rule.verify(Collections.EMPTY_LIST);
    rule.verify();
  }

  @Test
  void shouldFailIfFirstVerificationFailed() {
    Collection<CheckMessage> messages = Arrays.asList(mock(CheckMessage.class));
    var rule = new CheckMessagesVerifierRule();

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      rule.verify(messages);
      rule.verify(Collections.EMPTY_LIST);
      rule.verify();
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessageContaining("\nNo more violations expected\ngot:");
  }

  @Test
  void shouldFailIfSecondVerificationFailed() {
    Collection<CheckMessage> messages = Arrays.asList(mock(CheckMessage.class));
    var rule = new CheckMessagesVerifierRule();

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      rule.verify(Collections.EMPTY_LIST);
      rule.verify(messages);
      rule.verify();
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessageContaining("\nNo more violations expected\ngot:");
  }

}
