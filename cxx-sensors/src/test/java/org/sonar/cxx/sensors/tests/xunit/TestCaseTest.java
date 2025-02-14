/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.sensors.tests.xunit;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TestCaseTest {

  @Test
  void rendersRightDetails() {
    var testCase = new TestCase("testCaseName", 1, "ok", "stack", "msg", "classname", "filename", "testSuiteName");
    assertThat(testCase.getClassname()).isEqualTo("classname");
    assertThat(testCase.getFullname()).isEqualTo("testSuiteName:testCaseName");
    assertThat(testCase.getFilename()).isEqualTo("filename");
    assertThat(testCase.isOk()).isTrue();
    assertThat(testCase.isError()).isFalse();
    assertThat(testCase.isFailure()).isFalse();
    assertThat(testCase.isSkipped()).isFalse();
    assertThat(testCase.getErrorMessage()).isEqualTo("msg");
    assertThat(testCase.getStackTrace()).isEqualTo("stack");
    assertThat(testCase.getExecutionTime()).isEqualTo(1);
  }

}
