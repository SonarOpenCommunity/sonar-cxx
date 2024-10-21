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
import java.util.Locale;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.squidbridge.api.CheckMessage;

class CheckMessagesVerifierTest {

  @Test
  void next() {
    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      CheckMessagesVerifier.verify(Collections.EMPTY_LIST)
        .next();
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("\nExpected violation");
  }

  @Test
  void noMore() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo"));

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .noMore();
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("\nNo more violations expected\ngot: at line 1");
  }

  @Test
  void line() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo"));

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .next().atLine(2);
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("\nExpected: 2\ngot: 1");
  }

  @Test
  void line_withoutHasNext() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo"));

    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .atLine(2);
    });
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Prior to this method you should call next()");
  }

  @Test
  void withMessage() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo"));

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .next().atLine(1).withMessage("bar");
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .asString().contains("Expected: \"bar\"", "got: \"foo\"");
  }

  @Test
  void withMessage_withoutHasNext() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo"));

    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .withMessage("foo");
    });
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Prior to this method you should call next()");
  }

  @Test
  void withMessageContaining() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo"));

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .next().atLine(1).withMessageContaining("bar");
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("\nExpected: a string containing \"bar\"\ngot: \"foo\"");
  }

  @Test
  void withCost() {
    Collection<CheckMessage> messages = Arrays.asList(mockCheckMessage(1, "foo", 0d));

    AssertionError thrown = catchThrowableOfType(AssertionError.class, () -> {
      CheckMessagesVerifier.verify(messages)
        .next().withCost(1d);
    });
    assertThat(thrown)
      .isExactlyInstanceOf(AssertionError.class)
      .asString().contains("Expected: 1.0", "got: 0.0");
  }

  @Test
  void ok() {
    Collection<CheckMessage> messages = Arrays.asList(
      mockCheckMessage(null, "b"),
      mockCheckMessage(2, "a", 1d),
      mockCheckMessage(null, "a"),
      mockCheckMessage(1, "b"),
      mockCheckMessage(1, "a"));
    CheckMessagesVerifier.verify(messages)
      .next().atLine(null).withMessage("a")
      .next().atLine(null).withMessageContaining("b")
      .next().atLine(1).withMessage("a")
      .next().atLine(1).withMessage("b")
      .next().atLine(2).withMessage("a").withCost(1d)
      .noMore();
  }

  private static CheckMessage mockCheckMessage(Integer line, String message, Double cost) {
    CheckMessage checkMessage = mock(CheckMessage.class);
    when(checkMessage.getLine()).thenReturn(line);
    when(checkMessage.getDefaultMessage()).thenReturn(message);
    when(checkMessage.getText(Mockito.any(Locale.class))).thenReturn(message);
    when(checkMessage.getCost()).thenReturn(cost);
    return checkMessage;
  }

  private static CheckMessage mockCheckMessage(Integer line, String message) {
    return mockCheckMessage(line, message, null);
  }

}
