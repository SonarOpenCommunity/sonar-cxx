/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFileTest {

  private TestFile testFile;

  @BeforeEach
  public void setUp() {
    testFile = new TestFile("test.cpp");
  }

  @Test
  public void newBornTestFileShouldHaveVirginStatistics() {
    assertThat(testFile.getTests()).isEqualTo(0);
    assertThat(testFile.getErrors()).isEqualTo(0);
    assertThat(testFile.getFailures()).isEqualTo(0);
    assertThat(testFile.getSkipped()).isEqualTo(0);
    assertThat(testFile.getExecutionTime()).isEqualTo(0);
  }

  @Test
  public void addingTestCaseShouldIncrementStatistics() {
    int testBefore = testFile.getTests();
    long timeBefore = testFile.getExecutionTime();

    int EXEC_TIME = 10;
    testFile.add(new TestCase("name", EXEC_TIME, "status", "stack", "msg",
                              "classname", "tcfilename", "tsname"));

    assertThat(testBefore + 1).isEqualTo(testFile.getTests());
    assertThat(timeBefore + EXEC_TIME).isEqualTo(testFile.getExecutionTime());
  }

  @Test
  public void addingAnErroneousTestCaseShouldIncrementErrorStatistic() {
    int errorsBefore = testFile.getErrors();
    TestCase error = mock(TestCase.class);
    when(error.isError()).thenReturn(true);

    testFile.add(error);

    assertThat(errorsBefore + 1).isEqualTo(testFile.getErrors());
  }

  @Test
  public void addingAFailedTestCaseShouldIncrementFailedStatistic() {
    int failedBefore = testFile.getFailures();
    TestCase failedTC = mock(TestCase.class);
    when(failedTC.isFailure()).thenReturn(true);

    testFile.add(failedTC);

    assertThat(failedBefore + 1).isEqualTo(testFile.getFailures());
  }

  @Test
  public void addingASkippedTestCaseShouldIncrementSkippedStatistic() {
    int skippedBefore = testFile.getSkipped();
    TestCase skippedTC = mock(TestCase.class);
    when(skippedTC.isSkipped()).thenReturn(true);

    testFile.add(skippedTC);

    assertThat(skippedBefore + 1).isEqualTo(testFile.getSkipped());
  }

}
