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

import java.io.File;
import java.util.TreeMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.sensors.utils.TestUtils;

public class XunitReportParserTest {

  private XunitReportParser parserHandler = new XunitReportParser("");
  private StaxParser parser = new StaxParser(parserHandler, false);
  private final String pathPrefix = "/org/sonar/cxx/sensors/reports-project/xunit-reports/";

  @Test
  public void testParse() throws javax.xml.stream.XMLStreamException {

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
      assertEquals((long) entry.getValue(), tests);
    }
  }

  @Test(expected = javax.xml.stream.XMLStreamException.class)
  public void shouldThrowWhenGivenInvalidTime() throws javax.xml.stream.XMLStreamException {
    parserHandler = new XunitReportParser("");
    parser = new StaxParser(parserHandler, false);
    File report = TestUtils.loadResource(pathPrefix + "invalid-time-xunit-report.xml");
    parser.parse(report);
  }

}
