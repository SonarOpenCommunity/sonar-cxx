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
package org.sonar.cxx.sensors.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CxxReportIssueTest {

  @Test
  public void reportLocationEquality() {
    CxxReportLocation location0 = new CxxReportLocation("path0.cpp", "1", "Boolean value assigned to pointer.");
    CxxReportLocation location1 = new CxxReportLocation("path0.cpp", "1", "Boolean value assigned to pointer.");
    assertEquals(location0, location1);
    assertEquals(location0.hashCode(), location1.hashCode());

    CxxReportLocation location2 = new CxxReportLocation("path2.cpp", "1", "Exception thrown in destructor.");
    assertNotEquals(location2, location0);
    assertNotEquals(location2.hashCode(), location0.hashCode());

    assertNotEquals(location2, location1);
    assertNotEquals(location2.hashCode(), location1.hashCode());
  }

  @Test
  public void reportIssueEquality() {
    CxxReportIssue issue0 = new CxxReportIssue("cppcheck", "nullPointer", "path0.cpp", "1", "Null pointer dereference: ptr");
    issue0.addLocation("path0.cpp", "1", "Assignment &apos;ptr=nullptr&apos;, assigned value is 0");

    CxxReportIssue issue1 = new CxxReportIssue("cppcheck", "exceptThrowInDestructor", "path2.cpp", "1", "Exception thrown in destructor.");
    CxxReportIssue issue2 = new CxxReportIssue("cppcheck", "exceptThrowInDestructor", "path2.cpp", "1", "Exception thrown in destructor.");

    assertEquals(issue1, issue2);
    assertEquals(issue1.hashCode(), issue2.hashCode());

    assertNotEquals(issue0, issue1);
    assertNotEquals(issue0.hashCode(), issue1.hashCode());

    assertNotEquals(issue0, issue2);
    assertNotEquals(issue0.hashCode(), issue2.hashCode());
  }
}
