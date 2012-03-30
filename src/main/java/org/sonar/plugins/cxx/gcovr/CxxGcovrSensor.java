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
package org.sonar.plugins.cxx.gcovr;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.cobertura.api.AbstractCoberturaParser;
import org.sonar.plugins.cxx.CxxSensor;

public class CxxGcovrSensor extends CxxSensor implements CoverageExtension{
  public static final String REPORT_PATH_KEY = "sonar.cxx.gcovr.reportPath";
  private static final String DEFAULT_REPORT_PATH = "gcovr-reports/gcovr-result-*.xml";
  private static Logger logger = LoggerFactory.getLogger(CxxGcovrSensor.class);
  
  private Configuration conf = null;

  public CxxGcovrSensor(Configuration conf) {
    this.conf = conf;
  }

  public void analyse(Project project, SensorContext context) {
    List<File> reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
    for (File report : reports) {
      parseReport(project, context, report);
    }
  }

  protected void parseReport(final Project project, SensorContext context, File report) {
    new AbstractCoberturaParser() {
      @Override
      protected Resource<?> getResource(String fileName) {
        logger.debug("Creating resource for {}", fileName);
        return org.sonar.api.resources.File.fromIOFile(new File(fileName), project);
      }
    }.parseReport(report, context);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
