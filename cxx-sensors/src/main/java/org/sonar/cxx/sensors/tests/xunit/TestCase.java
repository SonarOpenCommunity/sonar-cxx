/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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

/**
 * Represents a unit test case. Has a couple of data items like name, status, time etc. associated. Reports testcase
 * details in sonar-conform XML
 */
public class TestCase {

  private static final String STATUS_OK = "ok";
  private static final String STATUS_ERROR = "error";
  private static final String STATUS_FAILURE = "failure";
  private static final String STATUS_SKIPPED = "skipped";

  private final String name;
  private String status = STATUS_OK;
  private final String stackTrace;
  private final String errorMessage;
  private final int time;
  private final String classname;
  private final String filename;
  private final String testSuiteName;

  /**
   * Constructs a testcase instance out of following parameters
   *
   * @params name The name of this testcase
   * @params time The execution time in milliseconds
   * @params status The execution status of the testcase
   * @params stack The stack trace occurred while executing of this testcase; pass "" if the testcase passed/skipped.
   * @params msg The error message accosiated with this testcase of the execution was errouneous; pass "" if not.
   * @params classname The name of the class this testcase is implemented by
   * @params filename The path of the file which implements the testcase
   * @params testSuiteName The name of the testsuite this testcase is in.
   */
  public TestCase(String testCaseName, int time, String status, String stack, String msg,
                  String classname, String filename, String testSuiteName) {
    this.name = testCaseName;
    this.time = time;
    this.stackTrace = stack;
    this.errorMessage = msg;
    this.status = status;
    this.classname = classname;
    this.filename = filename;
    this.testSuiteName = testSuiteName;
  }

  /**
   * Returns the name of the class which is implementing this testcase
   */
  public String getClassname() {
    return classname != null ? classname : testSuiteName;
  }

  /**
   * Returns the name of the class which is implementing this testcase
   */
  public String getFullname() {
    return testSuiteName + ":" + name;
  }

  /**
   * Returns the name of the file where this testcase is implemented
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Returns true if this testcase is ok, false otherwise
   */
  public boolean isOk() {
    return STATUS_OK.equals(status);
  }

  /**
   * Returns true if this testcase is an error, false otherwise
   */
  public boolean isError() {
    return STATUS_ERROR.equals(status);
  }

  /**
   * Returns true if this testcase is a failure, false otherwise
   */
  public boolean isFailure() {
    return STATUS_FAILURE.equals(status);
  }

  /**
   * Returns true if this testcase has been skipped, failure, false otherwise
   */
  public boolean isSkipped() {
    return STATUS_SKIPPED.equals(status);
  }

  /**
   * Error message in case testcase is not ok
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Stack trace in case testcase is not ok
   */
  public String getStackTrace() {
    return stackTrace;
  }

  /**
   * Execution time of testcase
   */
  public int getExecutionTime() {
    return time;
  }

}
