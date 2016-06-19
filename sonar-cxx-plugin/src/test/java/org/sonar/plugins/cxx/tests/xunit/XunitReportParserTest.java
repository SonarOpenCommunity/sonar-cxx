/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.tests.xunit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.sonar.api.utils.StaxParser; //@todo deprecated
import org.sonar.plugins.cxx.TestUtils;

public class XunitReportParserTest {

  XunitReportParser parserHandler = new XunitReportParser();
  StaxParser parser = new StaxParser(parserHandler, false);
  String pathPrefix = "/org/sonar/plugins/cxx/reports-project/xunit-reports/";

  @Test
  public void testParse() throws javax.xml.stream.XMLStreamException {

    Map<String, Integer> ioMap = new TreeMap<>();
    ioMap.put("xunit-result-2.xml", 5);
    ioMap.put("xunit-result-SAMPLE_with_fileName.xml", 3);
    ioMap.put("xunit-result-SAMPLE.xml", 3);
    ioMap.put("xunit-result-skippedonly.xml", 1);
    ioMap.put("xunit-result_with_emptyFileName.xml", 3);
    ioMap.put("nested_testsuites.xml", 2);

    for (Map.Entry<String, Integer> entry : ioMap.entrySet()) {
      parserHandler = new XunitReportParser();
      parser = new StaxParser(parserHandler, false);
      File report = TestUtils.loadResource(pathPrefix + entry.getKey());
      parser.parse(report);
      assertEquals((int) entry.getValue(), parserHandler.getTestCases().size());
    }
  }

  @Test(expected = javax.xml.stream.XMLStreamException.class)
  public void shouldThrowWhenGivenInvalidTime() throws javax.xml.stream.XMLStreamException {
    parserHandler = new XunitReportParser();
    parser = new StaxParser(parserHandler, false);
    File report = TestUtils.loadResource(pathPrefix + "invalid-time-xunit-report.xml");
    parser.parse(report);
  }
}
