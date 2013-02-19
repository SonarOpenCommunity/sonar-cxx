/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx.valgrind;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.cxx.TestUtils;

public class ValgrindReportParserTest {
  private ValgrindReportParser parser;

  @Before
  public void setUp() {
    parser = new ValgrindReportParser();
  }

  @Test
  public void shouldParseCorrectNumberOfErrors() throws javax.xml.stream.XMLStreamException {
    File report = TestUtils.loadResource("valgrind-reports/valgrind-result-SAMPLE.xml");
    Set<ValgrindError> valgrindErrors = parser.parseReport(report);
    assertEquals(valgrindErrors.size(), 6);
  }

  @Test(expected = javax.xml.stream.XMLStreamException.class)
  public void shouldThrowWhenGivenAnIncompleteReport_1() throws javax.xml.stream.XMLStreamException {
    // error contains no kind-tag
    File report = TestUtils.loadResource("valgrind-reports/incorrect-valgrind-result_1.xml");
    Set<ValgrindError> valgrindErrors = parser.parseReport(report);
  }

  @Test(expected = javax.xml.stream.XMLStreamException.class)
  public void shouldThrowWhenGivenAnIncompleteReport_2() throws javax.xml.stream.XMLStreamException {
    // error contains no what- or xwhat-tag
    File report = TestUtils.loadResource("valgrind-reports/incorrect-valgrind-result_2.xml");
    Set<ValgrindError> valgrindErrors = parser.parseReport(report);
  }

  @Test(expected = javax.xml.stream.XMLStreamException.class)
  public void shouldThrowWhenGivenAnIncompleteReport_3() throws javax.xml.stream.XMLStreamException {
    // error contains no stack-tag
    File report = TestUtils.loadResource("valgrind-reports/incorrect-valgrind-result_3.xml");
    Set<ValgrindError> valgrindErrors = parser.parseReport(report);
  }
}
