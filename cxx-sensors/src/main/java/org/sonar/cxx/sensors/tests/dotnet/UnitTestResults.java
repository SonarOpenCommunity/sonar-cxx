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
package org.sonar.cxx.sensors.tests.dotnet;

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class UnitTestResults {

  private int tests;
  private int passed;
  private int skipped;
  private int failures;
  private int errors;
  private Long executionTime;

  public void add(int tests, int passed, int skipped, int failures, int errors, @Nullable Long executionTime) {
    this.tests += tests;
    this.passed += passed;
    this.skipped += skipped;
    this.failures += failures;
    this.errors += errors;

    if (executionTime != null) {
      if (this.executionTime == null) {
        this.executionTime = 0L;
      }
      this.executionTime += executionTime;
    }
  }

  public int tests() {
    return tests;
  }

  public double passedPercentage() {
    return tests != 0 ? (passed * 100.0 / tests) : 0;
  }

  public int skipped() {
    return skipped;
  }

  public int failures() {
    return failures;
  }

  public int errors() {
    return errors;
  }

  @CheckForNull
  public Long executionTime() {
    return executionTime;
  }

}
