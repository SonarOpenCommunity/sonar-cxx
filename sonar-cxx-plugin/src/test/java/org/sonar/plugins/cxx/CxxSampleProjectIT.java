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
package org.sonar.plugins.cxx;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

//@Ignore
public class CxxSampleProjectIT {

  private static Sonar sonar;
  private static final String PROJECT_SAMPLE = "CxxPlugin:Sample";
  private static final String DIR_UTILS = "CxxPlugin:Sample:lib";
  private static final String FILE_CODECHUNKS = "CxxPlugin:Sample:lib/component1.cc";

  @BeforeClass
  public static void buildServer() {
    sonar = Sonar.create("http://localhost:9000");
  }

  @Test
  public void cxxSampleIsAnalyzed() {
    assertThat(sonar.find(new ResourceQuery(PROJECT_SAMPLE)).getName(), is("Sample"));
    assertThat(sonar.find(new ResourceQuery(PROJECT_SAMPLE)).getVersion(), is("0.0.1"));
    assertThat(sonar.find(new ResourceQuery(DIR_UTILS)).getName(), is("lib"));
    assertThat(sonar.find(new ResourceQuery(FILE_CODECHUNKS)).getName(), is("component1.cc"));
  }

  @Test
  public void projectsMetrics() {
    String[] metricNames =
    {"ncloc", "lines",
      "files", "directories", "functions",
      "comment_lines_density", "comment_lines",
      "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
      "complexity", "function_complexity",
      "violations", "violations_density",
      "coverage", "line_coverage", "branch_coverage",
      "test_success_density", "test_failures", "test_errors", "tests"
    };

    double[] values = new double[metricNames.length];
    for (int i = 0; i < metricNames.length; ++i) {
      values[i] = getProjectMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {52.0, 124.0,
      4.0, 3.0, 5.0,
      31.6, 24.0,
      69.4, 86.0, 2.0, 2.0,
      7.0, 1.4,
      34.0, 0.0,
      84.0, 100.0, 50.0,
      60.0, 2.0, 0.0, 5.0};

    assertThat(values, is(expectedValues));
    assertThat(getProjectMeasure("function_complexity_distribution").getData(), is("1=3;2=2;4=0;6=0;8=0;10=0;12=0;20=0;30=0"));
  }

  @Test
  public void directoryMetrics() {
    String[] metricNames =
    {"ncloc", "lines",
      "files", "directories", "functions",
      "comment_lines_density", "comment_lines",
      "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
      "complexity", "function_complexity",
      "violations", "violations_density",
      "coverage", "line_coverage", "branch_coverage"
    };

    double[] values = new double[metricNames.length];
    for (int i = 0; i < metricNames.length; ++i) {
      values[i] = getPackageMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {47.0, 115.0,
      3.0, 1.0, 4.0,
      33.8, 24.0,
      74.8, 86.0, 2.0, 2.0,
      6.0, 1.5,
      32.0, 0.0,
      84.0, 100.0, 50.0};

    assertThat(values, is(expectedValues));
    assertThat(getPackageMeasure("function_complexity_distribution").getData(), is("1=2;2=2;4=0;6=0;8=0;10=0;12=0;20=0;30=0"));
  }

  @Test
  public void filesMetrics() {
    String[] metricNames =
    {"ncloc", "lines",
      "files", "functions",
      "comment_lines_density", "comment_lines",
      "complexity", "function_complexity",
      "violations", "violations_density",
      "coverage", "line_coverage", "branch_coverage"
    };

    double[] values = new double[metricNames.length];
    for (int i = 0; i < metricNames.length; ++i) {
      values[i] = getFileMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {21.0, 53.0,
      1.0, 2.0,
      36.4, 12.0,
      3.0, 1.5,
      19.0, 0.0,
      84.0, 100.0, 50.0};

    assertThat(values, is(expectedValues));
  }

  private Measure getProjectMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(PROJECT_SAMPLE, metricKey));
    return resource != null ? resource.getMeasure(metricKey) : null;
  }

  private Measure getPackageMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(DIR_UTILS, metricKey));
    return resource != null ? resource.getMeasure(metricKey) : null;
  }

  private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_CODECHUNKS, metricKey));
    return resource != null ? resource.getMeasure(metricKey) : null;
  }
}
