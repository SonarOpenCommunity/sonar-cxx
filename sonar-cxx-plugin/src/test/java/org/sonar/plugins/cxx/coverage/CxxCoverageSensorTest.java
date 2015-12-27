/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx.coverage;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxCoverageSensorTest {

  private CxxCoverageSensor sensor;
  private SensorContext context;
  private Project project;
  private DefaultFileSystem fs;
  private Issuable issuable;
  private ResourcePerspectives perspectives;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    fs = TestUtils.mockFileSystem();
    context = mock(SensorContext.class);
  }

  @Test
  public void shouldReportCorrectCoverageOnUnitTestCoverage() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/tests/SAMPLE-test.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/tests/SAMPLE-test.h");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/tests/main.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/utils.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/code_chunks.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/application/main.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "builds/Unix Makefiles/COVERAGE/tests/moc_SAMPLE-test.cxx");
    sensor = new CxxCoverageSensor(settings, fs);
    sensor.analyse(project, context);
    verify(context, times(33)).saveMeasure((InputFile) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    settings.setProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY, "coverage-reports/cobertura/coverage-result-cobertura.xml");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/tests/SAMPLE-test.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/tests/SAMPLE-test.h");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/tests/main.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/utils.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/code_chunks.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/application/main.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "builds/Unix Makefiles/COVERAGE/tests/moc_SAMPLE-test.cxx");
    sensor = new CxxCoverageSensor(settings, fs);
    sensor.analyse(project, context);
    verify(context, times(99)).saveMeasure((InputFile) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportNoCoverageSaved() {
    sensor = new CxxCoverageSensor(new Settings(), fs);
    when(context.getResource((InputFile) anyObject())).thenReturn(null);
    sensor.analyse(project, context);
    verify(context, times(0)).saveMeasure((InputFile) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");
    sensor = new CxxCoverageSensor(settings, fs);
    sensor.analyse(project, context);
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesEmpty() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-cobertura-empty.xml");
    sensor = new CxxCoverageSensor(settings, fs);
    sensor.analyse(project, context);
    verify(context, times(0)).saveMeasure((InputFile) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportNoCoverageWhenInvalidFilesInvalid() {
    Settings settings = new Settings();
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-invalid.xml");
    sensor = new CxxCoverageSensor(settings, fs);
    sensor.analyse(project, context);
    verify(context, times(0)).saveMeasure((InputFile) anyObject(), any(Measure.class));
  }

  @Test
  public void shouldReportCoverageWhenVisualStudioCase() {
    Settings settings = new Settings();
    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio-win.xml");
      TestUtils.addInputFile(fs, perspectives, issuable, CxxUtils.normalizePath("C:/coveragetest/project1/source1.cpp"));
      TestUtils.addInputFile(fs, perspectives, issuable, CxxUtils.normalizePath("C:/coveragetest/project2/source1.cpp"));
      TestUtils.addInputFile(fs, perspectives, issuable, CxxUtils.normalizePath("C:/coveragetest/project2/source2.cpp"));
    } else {
      settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/cobertura/specific-cases/coverage-result-visual-studio-linux.xml");
      TestUtils.addInputFile(fs, perspectives, issuable, "/x/coveragetest/project1/source1.cpp");
      TestUtils.addInputFile(fs, perspectives, issuable, "/x/coveragetest/project2/source1.cpp");
      TestUtils.addInputFile(fs, perspectives, issuable, "/x/coveragetest/project2/source2.cpp");
    }
    sensor = new CxxCoverageSensor(settings, fs);
    sensor.analyse(project, context);
    verify(context, times(21)).saveMeasure((InputFile) anyObject(), any(Measure.class));
  }
}
