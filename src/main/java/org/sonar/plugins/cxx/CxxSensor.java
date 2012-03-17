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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.resources.Project;

public abstract class CxxSensor implements Sensor {
  private static Logger logger = LoggerFactory.getLogger(Sensor.class);

  public boolean shouldExecuteOnProject(Project project) {
    return CxxLanguage.KEY.equals(project.getLanguageKey());
  }

  public File[] getReports(Configuration conf,
                           String baseDir,
                           String reportPathPropertyKey,
                           String defaultReportPath) {
    String reportPath = conf.getString(reportPathPropertyKey, null);
    if(reportPath == null){
      reportPath = defaultReportPath;
    }

    logger.debug("using pattern '{}' to find reports" + reportPath);
    
    DirectoryScanner scanner = new DirectoryScanner();
    String[] includes = new String[1];
    includes[0] = reportPath;
    scanner.setIncludes(includes);
    scanner.setBasedir(new File(baseDir));
    scanner.scan();
    String[] relPaths = scanner.getIncludedFiles();
    
    List<File> reports = new ArrayList<File>();
    for (String relPath : relPaths) {
      reports.add(new File(baseDir, relPath));
    }
    
    return reports.toArray(new File[0]);
  }
}
