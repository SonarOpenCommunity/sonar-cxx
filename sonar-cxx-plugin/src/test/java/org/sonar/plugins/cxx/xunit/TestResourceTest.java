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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class TestResourceTest {
  TestResource resource;
  TestResource equalResource;
  TestResource otherResource;

  @Before
  public void setUp() {
    resource = new TestResource(null);
  }

  @Test
  public void newBornResourceShouldHaveVirginStatistics() {
    assertEquals(resource.getTests(), 0);
    assertEquals(resource.getErrors(), 0);
    assertEquals(resource.getFailures(), 0);
    assertEquals(resource.getSkipped(), 0);
    assertEquals(resource.getTime(), 0);
    assertEquals(resource.getDetails(), "<tests-details></tests-details>");
  }

  @Test
  public void addingTestCaseShouldIncrementStatistics() {
    int testBefore = resource.getTests();
    int timeBefore = resource.getTime();

    final int EXEC_TIME = 10;
    resource.addTestCase(new TestCase("name", EXEC_TIME, "status", "stack", "msg",
                                   "classname", "tsname", "tsfilename"));

    assertEquals(resource.getTests(), testBefore + 1);
    assertEquals(resource.getTime(), timeBefore + EXEC_TIME);
  }

  @Test
  public void addingAnErroneousTestCaseShouldIncrementErrorStatistic() {
    int errorsBefore = resource.getErrors();
    TestCase error = mock(TestCase.class);
    when(error.isError()).thenReturn(true);

    resource.addTestCase(error);

    assertEquals(resource.getErrors(), errorsBefore + 1);
  }

  @Test
  public void addingAFailedTestCaseShouldIncrementFailedStatistic() {
    int failedBefore = resource.getFailures();
    TestCase failedTC = mock(TestCase.class);
    when(failedTC.isFailure()).thenReturn(true);

    resource.addTestCase(failedTC);

    assertEquals(resource.getFailures(), failedBefore + 1);
  }

  @Test
  public void addingASkippedTestCaseShouldIncrementSkippedStatistic() {
    int skippedBefore = resource.getSkipped();
    TestCase skippedTC = mock(TestCase.class);
    when(skippedTC.isSkipped()).thenReturn(true);

    resource.addTestCase(skippedTC);

    assertEquals(resource.getSkipped(), skippedBefore + 1);
  }
}
