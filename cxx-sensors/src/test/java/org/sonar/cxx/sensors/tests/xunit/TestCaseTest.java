/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestCaseTest {

  @Test
  public void rendersRightDetails() {
    Map<String, TestCase> ioMap = new HashMap<>();

    ioMap.put("<testcase status=\"ok\" time=\"1\" name=\"name\"/>",
      new TestCase("name", 1, "ok", "", "", "", "", "", ""));
    ioMap.put("<testcase status=\"error\" time=\"1\" name=\"name\"><error message=\"errmsg\"><![CDATA[stack]]></error></testcase>",
      new TestCase("name", 1, "error", "stack", "errmsg", "", "", "", ""));
    ioMap.put("<testcase status=\"failure\" time=\"1\" name=\"name\"><failure message=\"errmsg\"><![CDATA[stack]]></failure></testcase>",
      new TestCase("name", 1, "failure", "stack", "errmsg", "", "", "", ""));

    for (Map.Entry<String, TestCase> entry : ioMap.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().getDetails());
    }
  }
}
