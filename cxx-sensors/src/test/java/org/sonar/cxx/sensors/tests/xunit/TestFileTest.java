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

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

public class TestFileTest {

  TestFile testFile;

  @Before
  public void setUp() {
    InputFile inputFile = TestInputFileBuilder.create("ProjectKey", "test.cpp").build();
    testFile = new TestFile(inputFile);
  }

  @Test
  public void newBornTestFileShouldHaveVirginStatistics() {
    assertEquals(0, testFile.getTests());
    assertEquals(0, testFile.getErrors());
    assertEquals(0, testFile.getFailures());
    assertEquals(0, testFile.getSkipped());
    assertEquals(0, testFile.getTime());
    assertEquals("<tests-details></tests-details>", testFile.getDetails());
  }

  @Test
  public void addingTestCaseShouldIncrementStatistics() {
    int testBefore = testFile.getTests();
    long timeBefore = testFile.getTime();

    final int EXEC_TIME = 10;
    testFile.addTestCase(new TestCase("name", EXEC_TIME, "status", "stack", "msg",
      "classname", "tcfilename", "tsname", "tsfilename"));

    assertEquals(testFile.getTests(), testBefore + 1);
    assertEquals(testFile.getTime(), timeBefore + EXEC_TIME);
  }

  @Test
  public void addingAnErroneousTestCaseShouldIncrementErrorStatistic() {
    int errorsBefore = testFile.getErrors();
    TestCase error = mock(TestCase.class);
    when(error.isError()).thenReturn(true);

    testFile.addTestCase(error);

    assertEquals(testFile.getErrors(), errorsBefore + 1);
  }

  @Test
  public void addingAFailedTestCaseShouldIncrementFailedStatistic() {
    int failedBefore = testFile.getFailures();
    TestCase failedTC = mock(TestCase.class);
    when(failedTC.isFailure()).thenReturn(true);

    testFile.addTestCase(failedTC);

    assertEquals(testFile.getFailures(), failedBefore + 1);
  }

  @Test
  public void addingASkippedTestCaseShouldIncrementSkippedStatistic() {
    int skippedBefore = testFile.getSkipped();
    TestCase skippedTC = mock(TestCase.class);
    when(skippedTC.isSkipped()).thenReturn(true);

    testFile.addTestCase(skippedTC);

    assertEquals(testFile.getSkipped(), skippedBefore + 1);
  }
}
