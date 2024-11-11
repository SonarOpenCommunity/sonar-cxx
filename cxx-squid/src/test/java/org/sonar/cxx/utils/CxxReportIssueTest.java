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
package org.sonar.cxx.utils;

import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CxxReportIssueTest {

  @Test
  void reportLocationEquality() {
    var location0 = new CxxReportLocation(
      "path0.cpp", "1", null, "Boolean value assigned to pointer."
    );
    var location1 = new CxxReportLocation(
      "path0.cpp", "1", null, "Boolean value assigned to pointer."
    );
    assertThat(location1)
      .isEqualTo(location0)
      .hasSameHashCodeAs(location0);

    var location2 = new CxxReportLocation(
      "path2.cpp", "1", null, "Exception thrown in destructor."
    );
    assertThat(location0).isNotEqualTo(location2);
    assertThat(location0.hashCode()).isNotEqualTo(location2.hashCode());

    assertThat(location1).isNotEqualTo(location2);
    assertThat(location1.hashCode()).isNotEqualTo(location2.hashCode());
  }

  @Test
  void reportIssueEquality() {
    var issue0 = new CxxReportIssue(
      "nullPointer", "path0.cpp", "1", null, "Null pointer dereference: ptr"
    );
    issue0.addLocation("path0.cpp", "1", null, "Assignment &apos;ptr=nullptr&apos;, assigned value is 0");

    var issue1 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );
    var issue2 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );

    assertThat(issue2)
      .isEqualTo(issue1)
      .hasSameHashCodeAs(issue1);

    assertThat(issue1).isNotEqualTo(issue0);
    assertThat(issue1.hashCode()).isNotEqualTo(issue0.hashCode());

    assertThat(issue2).isNotEqualTo(issue0);
    assertThat(issue2.hashCode()).isNotEqualTo(issue0.hashCode());
  }

  @Test
  void reportMappedInfo() {
    var issue0 = new CxxReportIssue(
      "nullPointer", "path0.cpp", "1", null, "issueInfo"
    );
    issue0.addLocation("path0.cpp", "1", null, "locInfo");
    issue0.addMappedInfo();
    assertThat(issue0.getRuleId()).isEqualTo("nullPointer");
    assertThat(issue0.getLocations().get(0).getInfo()).isEqualTo("Unknown 'nullPointer': issueInfo");
  }

  @Test
  void reportIssueEqualityConsideringFlow() {
    var issue0 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );
    issue0.addFlowElement("path0.cpp", "1", null, "a");
    issue0.addFlowElement("path1.cpp", "1", null, "b");
    issue0.addFlowElement("path2.cpp", "1", null, "c");

    var issue1 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );
    issue1.addFlowElement("path0.cpp", "1", null, "a");
    issue1.addFlowElement("path1.cpp", "1", null, "b");
    issue1.addFlowElement("path2.cpp", "1", null, "c");

    var issue2 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );
    issue2.addFlowElement("path1.cpp", "1", null, "b");
    issue2.addFlowElement("path2.cpp", "1", null, "c");

    var issue3 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );

    assertThat(issue1)
      .isEqualTo(issue0)
      .hasSameHashCodeAs(issue0);

    assertThat(issue2).isNotEqualTo(issue0);
    assertThat(issue2.hashCode()).isNotEqualTo(issue0.hashCode());

    assertThat(issue2).isNotEqualTo(issue1);
    assertThat(issue2.hashCode()).isNotEqualTo(issue1.hashCode());

    assertThat(issue3).isNotEqualTo(issue0);
    assertThat(issue3.hashCode()).isNotEqualTo(issue0.hashCode());
  }

  @Test
  void reportIssueFlowOrder() {
    var issue0 = new CxxReportIssue(
      "exceptThrowInDestructor", "path2.cpp", "1", null, "Exception thrown in destructor."
    );
    issue0.addFlowElement("path0.cpp", "1", null, "a");
    issue0.addFlowElement("path1.cpp", "2", null, "b");
    issue0.addFlowElement("path2.cpp", "3", null, "c");

    List<CxxReportLocation> flow = issue0.getFlow();
    assertThat(flow.get(0)).isEqualTo(new CxxReportLocation("path2.cpp", "3", null, "c"));
    assertThat(flow.get(1)).isEqualTo(new CxxReportLocation("path1.cpp", "2", null, "b"));
    assertThat(flow.get(2)).isEqualTo(new CxxReportLocation("path0.cpp", "1", null, "a"));
  }
}
