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

import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.cxx.xunit.TestCase;

/**
 * Represents a unit test suite. Contains testcases, maintains some statistics.
 * Reports testcase details in sonar-conform XML
 */
public class TestSuite {

  private final String testSuiteName;
  private final String path;
  private int errors = 0;
  private int skipped = 0;
  private int tests = 0;
  private int time = 0;
  private int failures = 0;
  private List<TestCase> testCases;

  /**
   * Creates a testsuite instance uniquely identified by the given testSuiteName
   *
   * @param testSuiteName The testSuiteName to construct a testsuite for
   */
  public TestSuite(String testSuiteName, String path) {
    this.testSuiteName = testSuiteName;
    this.path = path;
    this.testCases = new ArrayList<TestCase>();
  }

  public String getTestFileName() {
    if (path == null) {
      return testSuiteName;
    }
    if (!path.equals("")) {
      return path;
    }
    return testSuiteName;
  }

  public String getTestSuiteName() {
    return testSuiteName;
  }

  public int getErrors() {
    return errors;
  }

  public int getSkipped() {
    return skipped;
  }

  public int getTests() {
    return tests;
  }

  public int getTime() {
    return time;
  }

  public int getFailures() {
    return failures;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestSuite that = (TestSuite) o;
    return testSuiteName.equals(that.testSuiteName);
  }

  @Override
  public int hashCode() {
    return testSuiteName.hashCode();
  }

  /**
   * Adds the given test case to this testsuite maintaining the internal
   * statistics
   *
   * @param tc the test case to add
   */
  public void addTestCase(TestCase tc) {
    if (tc.isSkipped()) {
      skipped++;
    } else if (tc.isFailure()) {
      failures++;
    } else if (tc.isError()) {
      errors++;
    }
    tests++;
    time += tc.getTime();
    testCases.add(tc);
  }

  /**
   * Returns execution details as sonar-conform XML
   */
  public String getDetails() {
    StringBuilder details = new StringBuilder();
    details.append("<tests-details>");
    for (TestCase tc : testCases) {
      details.append(tc.getDetails());
    }
    details.append("</tests-details>");
    return details.toString();
  }
}
