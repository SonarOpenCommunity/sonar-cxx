/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.xunit;

//import com.google.common.collect.Iterables;
import java.io.File;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.TestUtils;

public class TestSuiteParserTest {
  TestSuiteParser parserHandler = new TestSuiteParser();
  StaxParser parser = new StaxParser(parserHandler, false);
  String REPORTS_PATH = "/org/sonar/plugins/cxx/reports-project/xunit-reports/";

  @Test
  public void suiteDoesntEqualsNull() throws XMLStreamException {
    File xmlReport = TestUtils.loadResource(REPORTS_PATH + "xunit-result-SAMPLE_with_fileName.xml");
    parser.parse(xmlReport);

    TestSuite fileReport = (TestSuite)parserHandler.getParsedReports().toArray()[0];
    assertEquals(fileReport.getKey(), "test/file.cpp");
    assertEquals(fileReport.getTests(), 3);
  }

  @Test
  public void parserDoesntAcceptEmptyFilepaths() throws XMLStreamException {
    File xmlReport = TestUtils.loadResource(REPORTS_PATH + "xunit-result_with_emptyFileName.xml");
    parser.parse(xmlReport);

    TestSuite fileReport = (TestSuite)parserHandler.getParsedReports().toArray()[0];
    assertEquals("SAMPLEtest", fileReport.getKey());
  }
}
