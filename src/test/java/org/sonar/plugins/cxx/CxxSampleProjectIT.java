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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

public class CxxSampleProjectIT {

  private static Sonar sonar;
  private static final String PROJECT_SAMPLE = "NETICOA:SAMPLE";
  private static final String DIR_UTILS = "NETICOA:SAMPLE:utils";
  private static final String FILE_CODECHUNKS = "NETICOA:SAMPLE:utils/code_chunks.cpp";

  @BeforeClass
  public static void buildServer() {
    sonar = Sonar.create("http://localhost:9000");
  }

  @Test
  public void cxxSampleIsAnalyzed() {
    assertThat(sonar.find(new ResourceQuery(PROJECT_SAMPLE)).getName(), is("SAMPLE"));
    assertThat(sonar.find(new ResourceQuery(PROJECT_SAMPLE)).getVersion(), is("0.0.1-SNAPSHOT"));
    assertThat(sonar.find(new ResourceQuery(DIR_UTILS)).getName(), is("utils"));
    assertThat(sonar.find(new ResourceQuery(FILE_CODECHUNKS)).getName(), is("code_chunks.cpp"));
  }

  @Test
  public void projectsMetrics() {
    String[] metricNames =
      {"ncloc", "lines",
       "files", "directories", "functions",
       "comment_lines_density", "comment_lines", "comment_blank_lines", "commented_out_code_lines",
       "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
       "complexity", "function_complexity", "violations", "violations_density"
      };

    double[] values = new double[metricNames.length];
    for(int i = 0; i < metricNames.length; ++i){
      values[i] = getProjectMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {89.0, 145.0, 6.0, 3.0, 6.0, 12.7, 13.0, 2.0, 21.0, 26.2, 38.0, 2.0, 1.0, 7.0, 1.2, 19.0, 60.7};
    assertThat(values, is(expectedValues));

    assertThat(getProjectMeasure("function_complexity_distribution").getData(), is("1=5;2=1;4=0;6=0;8=0;10=0;12=0"));
  }

  @Test
  public void directoryMetrics() {
    String[] metricNames =
      {"ncloc", "lines",
       "files", "directories", "functions",
       "comment_lines_density", "comment_lines", "comment_blank_lines", "commented_out_code_lines",
       "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
       "complexity", "function_complexity",
       "violations", "violations_density"
      };

    double[] values = new double[metricNames.length];
    for(int i = 0; i < metricNames.length; ++i){
      values[i] = getPackageMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {52.0, 94.0, 2.0, 1.0, 2.0, 16.1, 10.0, 2.0, 21.0, 40.4, 38.0, 2.0, 1.0, 3.0, 1.5, 11.0, 63.5};
    assertThat(values, is(expectedValues));

    assertThat(getPackageMeasure("function_complexity_distribution").getData(), is("1=1;2=1;4=0;6=0;8=0;10=0;12=0"));
  }

  @Test
  public void filesMetrics() {
    String[] metricNames =
      {"ncloc", "lines",
       "files", "functions",
       "comment_lines_density", "comment_lines", "comment_blank_lines", "commented_out_code_lines",
       "duplicated_lines_density", "duplicated_lines", "duplicated_blocks", "duplicated_files",
       "complexity", "function_complexity",
       "violations", "violations_density"
      };

    double[] values = new double[metricNames.length];
    for(int i = 0; i < metricNames.length; ++i){
      values[i] = getFileMeasure(metricNames[i]).getValue();
    }

    double[] expectedValues = {48.0, 88.0, 1.0, 1.0, 17.2, 10.0, 2.0, 21.0, 43.2, 38.0, 2.0, 1.0, 2.0, 2.0, 8.0, 66.7};
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
