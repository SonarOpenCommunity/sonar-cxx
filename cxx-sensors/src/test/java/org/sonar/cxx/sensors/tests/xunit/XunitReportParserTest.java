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
package org.sonar.cxx.sensors.tests.xunit;

import java.io.File;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.sensors.utils.TestUtils;

class XunitReportParserTest {

  private XunitReportParser parserHandler = new XunitReportParser("");
  private StaxParser parser = new StaxParser(parserHandler, false);
  private final String pathPrefix = "/org/sonar/cxx/sensors/reports-project/xunit-reports/";

  @Test
  void testParse() throws javax.xml.stream.XMLStreamException {

    var ioMap = new TreeMap<String, Integer>();

    // report: number of tests
    ioMap.put("xunit-result-2.xml", 5);
    ioMap.put("xunit-result-SAMPLE_with_fileName.xml", 3);
    ioMap.put("xunit-result-SAMPLE.xml", 3);
    ioMap.put("xunit-result-skippedonly.xml", 1);
    ioMap.put("xunit-result_with_emptyFileName.xml", 3);
    ioMap.put("nested_testsuites.xml", 2);
    ioMap.put("xunit-result-no-testsuite.xml", 0);

    for (var entry : ioMap.entrySet()) {
      parserHandler = new XunitReportParser("");
      parser = new StaxParser(parserHandler, false);
      File report = TestUtils.loadResource(pathPrefix + entry.getKey());
      parser.parse(report);

      long tests = 0;
      for (var testFile : parserHandler.getTestFiles()) {
        tests += testFile.getTests();
      }
      assertThat(tests).isEqualTo((long) entry.getValue());
    }
  }

  @Test
  void shouldThrowWhenGivenInvalidTime() {
    parserHandler = new XunitReportParser("");
    parser = new StaxParser(parserHandler, false);
    File report = TestUtils.loadResource(pathPrefix + "invalid-time-xunit-report.xml");

    javax.xml.stream.XMLStreamException thrown = catchThrowableOfType(javax.xml.stream.XMLStreamException.class, () -> {
      parser.parse(report);
    });
    assertThat(thrown).isExactlyInstanceOf(javax.xml.stream.XMLStreamException.class);
  }

  @Test
  void testFilePaths() throws javax.xml.stream.XMLStreamException {
    parserHandler = new XunitReportParser("");
    parser = new StaxParser(parserHandler, false);
    File report = TestUtils.loadResource(pathPrefix + "xunit-result-SAMPLE-inconsistent-case.xml");
    parser.parse(report);

    var actualPaths = parserHandler.getTestFiles()
      .stream()
      .map(TestFile::getFilename)
      .filter(p -> !Strings.isNullOrEmpty(p))
      .map(s -> Path.of(s))
      .collect(Collectors.toList());

    var expectPaths = Stream.of(
      Path.of("/test/file.cpp"),
      Path.of("/test/File.cpp"),
      Path.of("/TEST/file.cpp"))
      .distinct()
      .toArray(n -> new Path[n]);

    assertThat(actualPaths).containsExactlyInAnyOrder(expectPaths);
  }
}
