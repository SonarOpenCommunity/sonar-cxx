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

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;

/**
 * Represents a test file in Sonar, i.e. a source code file which implements
 * tests. Holds all test cases along with all measures collected from the
 * reports.
 */
public class TestFile {

  private int errors;
  private int skipped;
  private int tests;
  private long time;
  private int failures;
  private final List<TestCase> testCases;
  private InputFile inputFile;

  /**
   * Creates a test file instance which corresponds and represents the passed
   * InputFile instance
   *
   * @param inputFile The InputFile in SQ which this TestFile proxies
   */
  public TestFile(InputFile inputFile) {
    this.inputFile = inputFile;
    this.testCases = new ArrayList<>();
  }

  public String getKey() {
    return inputFile.absolutePath(); //@todo: deprecated absolutePath
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

  public long getTime() {
    return time;
  }

  public int getFailures() {
    return failures;
  }

  /**
   * Adds the given test case to this test file maintaining the internal
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

  public InputFile getInputFile() {
    return inputFile;
  }
}
