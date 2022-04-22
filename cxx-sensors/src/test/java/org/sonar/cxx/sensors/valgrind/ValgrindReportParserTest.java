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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sensors.utils.TestUtils;

class ValgrindReportParserTest {

  private ValgrindReportParser parser;

  @BeforeEach
  public void setUp() {
    parser = new ValgrindReportParser();
    TestUtils.mockFileSystem();
  }

  @Test
  void shouldParseCorrectNumberOfErrors() throws javax.xml.stream.XMLStreamException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "valgrind-reports/valgrind-result-SAMPLE_1.xml");
    Set<ValgrindError> valgrindErrors = parser.parse(absReportFile);
    assertThat(valgrindErrors).hasSize(13);
  }

  @Test
  void parseAnErrorWithMultipleStacks() throws javax.xml.stream.XMLStreamException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "valgrind-reports/valgrind-result-SAMPLE_2.xml");
    Set<ValgrindError> valgrindErrors = parser.parse(absReportFile);
    // single <error>
    assertThat(valgrindErrors).hasSize(1);

    ValgrindError error = valgrindErrors.iterator().next();
    // merge <what> and <auxwhat>
    assertThat(error.getText())
      .isEqualTo("Invalid write of size 4: Address 0xd820468 is 0 bytes after a block of size 40 alloc'd");
    // <error> contains two <stack> entries
    assertThat(error.getStacks()).hasSize(2);
  }

  @Test
  void parseAnErrorWithMultipleAuxwhat() throws javax.xml.stream.XMLStreamException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "valgrind-reports/valgrind-result-SAMPLE_3.xml");
    Set<ValgrindError> valgrindErrors = parser.parse(absReportFile);
    // single <error>
    assertThat(valgrindErrors).hasSize(1);

    ValgrindError error = valgrindErrors.iterator().next();
    // merge <what>, <auxwhat> and one more <auxwhat>
    // see PROTOCOL for version 3
    // https://github.com/pathscale/valgrind-mmt/blob/master/docs/internals/xml-output.txt
    assertThat(error.getText()).isEqualTo("Invalid write of size 4: Details0; Details1");
    // <error> contains one <stack> entry
    assertThat(error.getStacks()).hasSize(1);
  }

  @Test
  void shouldThrowWhenGivenAnIncompleteReport_1() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "valgrind-reports/incorrect-valgrind-result_1.xml");

    javax.xml.stream.XMLStreamException thrown = catchThrowableOfType(() -> {
      // error contains no kind-tag
      parser.parse(absReportFile);
    }, javax.xml.stream.XMLStreamException.class);
    assertThat(thrown).isExactlyInstanceOf(javax.xml.stream.XMLStreamException.class);
  }

  @Test
  void shouldThrowWhenGivenAnIncompleteReport_2() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "valgrind-reports/incorrect-valgrind-result_2.xml");

    javax.xml.stream.XMLStreamException thrown = catchThrowableOfType(() -> {
      // error contains no what- or xwhat-tag
      parser.parse(absReportFile);
    }, javax.xml.stream.XMLStreamException.class);
    assertThat(thrown).isExactlyInstanceOf(javax.xml.stream.XMLStreamException.class);
  }

  @Test
  void shouldThrowWhenGivenAnIncompleteReport_3() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "valgrind-reports/incorrect-valgrind-result_3.xml");

    javax.xml.stream.XMLStreamException thrown = catchThrowableOfType(() -> {
      // error contains no stack-tag
      parser.parse(absReportFile);
    }, javax.xml.stream.XMLStreamException.class);
    assertThat(thrown).isExactlyInstanceOf(javax.xml.stream.XMLStreamException.class);
  }

}
