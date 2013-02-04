/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

@Ignore
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
       "comment_lines_density", "comment_lines", "comment_blank_lines", "commented_out_code_lines",
       "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
       "complexity", "function_complexity", "violations", "violations_density",
       "coverage", "line_coverage", "branch_coverage",
       "test_success_density", "test_failures", "test_errors", "tests", "test_execution_time"
      };

    double[] values = new double[metricNames.length];
    for(int i = 0; i < metricNames.length; ++i){
      values[i] = getProjectMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {59.0, 94.0,
                               3.0, 3.0, 5.0,
                               16.9, 12.0, 5.0, 0.0,
                               21.3, 20.0, 2.0, 2.0,
                               6.0, 1.2, 39.0, 0.0,
                               44.0, 42.9, 50.0,
                               60.0, 2.0, 0.0, 5.0, 114.0};
    
    assertThat(values, is(expectedValues));
    assertThat(getProjectMeasure("function_complexity_distribution").getData(), is("1=4;2=1;4=0;6=0;8=0;10=0;12=0"));
  }

  @Test
  public void directoryMetrics() {
    String[] metricNames =
      {"ncloc", "lines",
       "files", "directories", "functions",
       "comment_lines_density", "comment_lines", "comment_blank_lines", "commented_out_code_lines",
       "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
       "complexity", "function_complexity",
       "violations", "violations_density",
       "coverage", "line_coverage", "branch_coverage"
      };

    double[] values = new double[metricNames.length];
    for(int i = 0; i < metricNames.length; ++i){
      values[i] = getPackageMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {41.0, 73.0,
                               2.0, 1.0, 3.0,
                               22.6, 12.0, 5.0, 0.0,
                               13.7, 10.0, 1.0, 1.0,
                               4.0, 1.3,
                               27.0, 0.0,
                               61.1, 64.3, 50.0};
    
    assertThat(values, is(expectedValues));
    assertThat(getPackageMeasure("function_complexity_distribution").getData(), is("1=2;2=1;4=0;6=0;8=0;10=0;12=0"));
  }

  @Test
  public void filesMetrics() {
    String[] metricNames =
      {"ncloc", "lines",
       "files", "functions",
       "comment_lines_density", "comment_lines", "comment_blank_lines",
       "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
       "complexity", "function_complexity",
       "violations", "violations_density",
       "coverage", "line_coverage", "branch_coverage"
      };

    double[] values = new double[metricNames.length];
    for(int i = 0; i < metricNames.length; ++i){
      values[i] = getFileMeasure(metricNames[i]).getValue();
    }
    
    double[] expectedValues = {33.0, 63.0,
                               1.0, 3.0,
                               26.7, 12.0, 5.0,
                               15.9, 10.0, 1.0, 1.0,
                               4.0, 1.3,
                               25.0, 0.0,
                               61.1, 64.3, 50.0};
    
    assertThat(values, is(expectedValues));
  }

  private Measure getProjectMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(PROJECT_SAMPLE, metricKey));
    return resource!=null ? resource.getMeasure(metricKey) : null;
  }

  private Measure getPackageMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(DIR_UTILS, metricKey));
    return resource!=null ? resource.getMeasure(metricKey) : null;
  }

  private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_CODECHUNKS, metricKey));
    return resource!=null ? resource.getMeasure(metricKey) : null;
  }
}
