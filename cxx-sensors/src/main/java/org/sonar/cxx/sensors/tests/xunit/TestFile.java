/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a test file in SQ, a source code file which implements tests. Holds all test cases along with all measures
 * collected from the reports.
 */
public class TestFile {

  private final String filename;
  private final List<TestCase> testCases;

  private int tests;
  private int failures;
  private int errors;
  private int skipped;
  private long time;

  /**
   * Creates a test file instance which corresponds and represents the passed InputFile instance
   *
   * @param filename test file with test cases
   */
  public TestFile(String filename) {
    this.filename = filename;
    this.testCases = new ArrayList<>();

  }

  public String getFilename() {
    return filename;
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

  public long getExecutionTime() {
    return time;
  }

  public int getFailures() {
    return failures;
  }

  /**
   * Adds the given test case to this test file maintaining the internal statistics
   *
   * @param tc the test case to add
   */
  public void add(TestCase tc) {
    testCases.add(tc);
    time += tc.getExecutionTime();
    tests++;

    if (tc.isFailure()) {
      failures++;
    } else if (tc.isError()) {
      errors++;
    } else if (tc.isSkipped()) {
      skipped++;
    }
  }

  public List<TestCase> getTestCases() {
    return new ArrayList<>(testCases);
  }

}
