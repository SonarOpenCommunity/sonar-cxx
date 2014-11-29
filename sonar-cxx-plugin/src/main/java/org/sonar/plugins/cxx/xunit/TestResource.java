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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a test resource in Sonar, i.e. a source code file which
 * implements tests.  Holds all testcases along with all measures
 * collected from the reports.
 */
public class TestResource {
  private int errors = 0;
  private int skipped = 0;
  private int tests = 0;
  private int time = 0;
  private int failures = 0;
  private List<TestCase> testCases;
  private org.sonar.api.resources.File sonarResource = null;

  /**
   * Creates a test resource instance which corresponds and represents the
   * passed resources.File instance
   * @param sonarResource The resource in SQ which this TestResource proxies
   */
  public TestResource(org.sonar.api.resources.File sonarResource) {
    this.sonarResource = sonarResource;
    this.testCases = new ArrayList<TestCase>();
  }

  public String getKey() {
    return sonarResource.getKey();
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

  /**
   * Adds the given test case to this testresource maintaining the
   * internal statistics
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

  public org.sonar.api.resources.File getSonarResource() {
    return sonarResource;
  }
}
