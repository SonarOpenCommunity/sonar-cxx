/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestCaseTest {

  @Test
  public void rendersRightDetails() {
    var testCase = new TestCase("testCaseName", 1, "ok", "stack", "msg", "classname", "filename", "testSuiteName");
    assertEquals("classname", testCase.getClassname());
    assertEquals("testSuiteName:testCaseName", testCase.getFullname());
    assertEquals("filename", testCase.getFilename());
    assertEquals(true, testCase.isOk());
    assertEquals(false, testCase.isError());
    assertEquals(false, testCase.isFailure());
    assertEquals(false, testCase.isSkipped());
    assertEquals("msg", testCase.getErrorMessage());
    assertEquals("stack", testCase.getStackTrace());
    assertEquals(1, testCase.getExecutionTime());
  }

}
