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
package org.sonar.plugins.cxx.coverage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxCoverageSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.coverage.reportPath";
  public static final String IT_REPORT_PATH_KEY = "sonar.cxx.it-coverage.reportPath";
  private static final String DEFAULT_REPORT_PATH = "coverage-reports/coverage-*.xml";
  private static final String IT_DEFAULT_REPORT_PATH = "coverage-reports/it-coverage-*.xml";
  
  private Configuration conf = null;

  private static List<CoverageParser> parsers = new LinkedList<CoverageParser>();
  
  /**
   * {@inheritDoc}
   */
  public CxxCoverageSensor(Configuration conf) {
    this.conf = conf;
    parsers.add(new CoberturaParser());
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    List<File> reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                    REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
    CxxUtils.LOG.debug("Parsing coverage reports");
    analyseReports(project, context, reports);

    CxxUtils.LOG.debug("Parsing integration test coverage reports");
    List<File> itReports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
				      IT_REPORT_PATH_KEY, IT_DEFAULT_REPORT_PATH);
    analyseReports(project, context, itReports);
  }
  
  private void analyseReports(Project project, SensorContext context, 
			     List<File> reports) {
    Map<String, FileData> fileDataPerFilename = new HashMap<String, FileData>();
    for (File report : reports) {
      boolean parsed = false;
      for (CoverageParser parser: parsers){
	try{
	  parser.parseReport(report, fileDataPerFilename);

	  CxxUtils.LOG.debug("Added report '{}' (parsed by: {}) to the coverage data", report, parser);
	  parsed = true;
	  break;
	} catch (XMLStreamException e) {
	  CxxUtils.LOG.trace("Report {} cannot be parsed by {}", report, parser);
	}
      }
      
      if(!parsed){
	CxxUtils.LOG.error("Report {} cannot be parsed", report);
      }
    }
    
    for (FileData cci : fileDataPerFilename.values()) {
      String filePath = cci.getFileName();
      org.sonar.api.resources.File cxxfile =
        org.sonar.api.resources.File.fromIOFile(new File(filePath), project);
      if (fileExist(context, cxxfile)) {
        CxxUtils.LOG.debug("Saving coverage measures for file '{}'", filePath);
        for (Measure measure : cci.getMeasures()) {
          context.saveMeasure(cxxfile, measure);
        }
      } else {
        CxxUtils.LOG.debug("Cannot find the file '{}', ignoring coverage measures", filePath);
      }
    }
  }
  
  private boolean fileExist(SensorContext context, org.sonar.api.resources.File file) {
    return context.getResource(file) != null;
  }
}
