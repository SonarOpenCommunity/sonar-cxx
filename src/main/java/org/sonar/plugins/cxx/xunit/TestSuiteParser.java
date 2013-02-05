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
package org.sonar.plugins.cxx.xunit;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.ElementFilter;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser.XmlStreamHandler;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class TestSuiteParser implements XmlStreamHandler {
  
  private Map<String, TestSuite> testSuites = new HashMap<String, TestSuite>();
  
  /**
   * {@inheritDoc}
   */
  public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
    SMInputCursor testSuiteCursor = rootCursor.constructDescendantCursor(new ElementFilter("testsuite"));
    while (testSuiteCursor.getNext() != null) {
      String testSuiteClassName = testSuiteCursor.getAttrValue("name");      
      String testFileName = testSuiteCursor.getAttrValue("filename");
      SMInputCursor testCaseCursor = testSuiteCursor.childElementCursor("testcase");
      while (testCaseCursor.getNext() != null) {
        String testClassName = getClassname(testCaseCursor, testSuiteClassName);
        TestSuite report = testSuites.get(testClassName);
        if (report == null) {
          report = new TestSuite(testClassName, testFileName);
          testSuites.put(testClassName, report);
        }
        report.addTestCase(parseTestCaseTag(testCaseCursor));
      }
    }
  }
  
  /**
   * Returns successfully parsed reports as a collection of CxxTestSuite objects.
   */
  public Collection<TestSuite> getParsedReports() {
    return testSuites.values();
  }

  private String getClassname(SMInputCursor testCaseCursor, String defaultClassname)
    throws XMLStreamException
  {
    String testClassName = testCaseCursor.getAttrValue("classname");
    return testClassName == null ? defaultClassname : testClassName;
  }
  
  private TestCase parseTestCaseTag(SMInputCursor testCaseCursor)
    throws XMLStreamException
  {
    // TODO: get a decent grammar for the junit format and check the
    // logic inside this method against it.
    
    String name = parseTestCaseName(testCaseCursor).replace("/", ".");
    Double time = parseTime(testCaseCursor);
    String status = parseTestStatus(testCaseCursor);
    String stack = "";
    String msg = "";
    
    SMInputCursor childCursor = testCaseCursor.childElementCursor();
    if (childCursor.getNext() != null) {
      String elementName = childCursor.getLocalName();
      if (elementName.equals("skipped")) {
        status = "skipped";
      } else if (elementName.equals("failure")) {
        status = "failure";
        msg = childCursor.getAttrValue("message");
        stack = childCursor.collectDescendantText();
      } else if (elementName.equals("error")) {
        status = "error";
        msg = childCursor.getAttrValue("message");
        stack = childCursor.collectDescendantText();
      }
    }
    
    CxxUtils.LOG.debug("Name '{}' time '{}'",
                         name, time.intValue());
    CxxUtils.LOG.debug("Status '{}' msg '{}'",
                         status, msg);
    
    return new TestCase(name, time.intValue(), status, stack, msg);
  }

  private double parseTime(SMInputCursor testCaseCursor)
    throws XMLStreamException
  {
    double time = 0.0;
    try {
      String sTime = testCaseCursor.getAttrValue("time");
      if (!StringUtils.isEmpty(sTime)) {
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

  private String parseTestStatus(SMInputCursor testCaseCursor) {
      try {
        return testCaseCursor.getAttrValue("status");
      } catch (XMLStreamException e) {
          return "ok";
      }
  }
  
  private String parseTestCaseName(SMInputCursor testCaseCursor) throws XMLStreamException {
    String name = testCaseCursor.getAttrValue("name");
    String classname = testCaseCursor.getAttrValue("classname");
    if (classname != null) {
      name = classname + "/" + name;
    }
    return name;
  }
}
