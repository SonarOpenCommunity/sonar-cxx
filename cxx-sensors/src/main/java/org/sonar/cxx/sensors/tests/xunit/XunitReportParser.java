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

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.ElementFilter;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser.XmlStreamHandler;

/**
 * {@inheritDoc}
 */
public class XunitReportParser implements XmlStreamHandler {

  private final List<TestCase> testCases = new LinkedList<>();

  /**
   * Returns successfully parsed testcases.
   */
  public List<TestCase> getTestCases() {
    return new LinkedList<>(testCases);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
    SMInputCursor testSuiteCursor = rootCursor.constructDescendantCursor(new ElementFilter("testsuite"));
    try {
      testSuiteCursor.getNext();
    } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
      throw new EmptyReportException("Cannot read Xunit report", eofExc);
    }

    do {
      parseTestSuiteTag(testSuiteCursor);
    } while (testSuiteCursor.getNext() != null);
  }

  public void parseTestSuiteTag(SMInputCursor testSuiteCursor)
    throws XMLStreamException {
    String testSuiteName = testSuiteCursor.getAttrValue("name");
    String testSuiteFName = testSuiteCursor.getAttrValue("filename");

    SMInputCursor childCursor = testSuiteCursor.childElementCursor();
    while (childCursor.getNext() != null) {
      String elementName = childCursor.getLocalName();
      if ("testsuite".equals(elementName)) {
        parseTestSuiteTag(childCursor);
      } else if ("testcase".equals(elementName)) {
        testCases.add(parseTestCaseTag(childCursor, testSuiteName, testSuiteFName));
      }
    }
  }

  private static TestCase parseTestCaseTag(SMInputCursor testCaseCursor, String tsName, String tsFilename)
    throws XMLStreamException {
    String classname = testCaseCursor.getAttrValue("classname");
    String tcFilename = testCaseCursor.getAttrValue("filename");
    String name = parseTestCaseName(testCaseCursor);
    Double time = parseTime(testCaseCursor);
    String status = "ok";
    String stack = "";
    String msg = "";
    final String SKIPPED_STATUS = "skipped";

    // Googletest-reports mark the skipped tests with status="notrun"
    String statusattr = testCaseCursor.getAttrValue("status");
    if ("notrun".equals(statusattr)) {
      status = SKIPPED_STATUS;
    } else {
      SMInputCursor childCursor = testCaseCursor.childElementCursor();
      if (childCursor.getNext() != null) {
        String elementName = childCursor.getLocalName();
        if (SKIPPED_STATUS.equals(elementName)) {
          status = SKIPPED_STATUS;
        } else if ("failure".equals(elementName)) {
          status = "failure";
          msg = childCursor.getAttrValue("message");
          stack = childCursor.collectDescendantText();
        } else if ("error".equals(elementName)) {
          status = "error";
          msg = childCursor.getAttrValue("message");
          stack = childCursor.collectDescendantText();
        }
      }
    }

    return new TestCase(name, time.intValue(), status, stack, msg, classname, tcFilename, tsName, tsFilename);
  }

  private static double parseTime(SMInputCursor testCaseCursor)
    throws XMLStreamException {
    double time = 0.0;
    try {
      String sTime = testCaseCursor.getAttrValue("time");
      if (sTime != null && !sTime.isEmpty()) {
        Double tmp = ParsingUtils.parseNumber(sTime, Locale.ENGLISH);
        if (!Double.isNaN(tmp)) {
          time = ParsingUtils.scaleValue(tmp * 1000, 3);
        }
      }
    } catch (ParseException e) {
      throw new XMLStreamException(e);
    }

    return time;
  }

  private static String parseTestCaseName(SMInputCursor testCaseCursor) throws XMLStreamException {
    String name = testCaseCursor.getAttrValue("name");
    String classname = testCaseCursor.getAttrValue("classname");
    if (classname != null) {
      name = classname + "/" + name;
    }
    return name;
  }
}
