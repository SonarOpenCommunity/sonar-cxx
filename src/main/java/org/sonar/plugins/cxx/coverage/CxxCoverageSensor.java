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
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
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
  
  private final Settings settings;
  private static List<CoverageParser> parsers = new LinkedList<CoverageParser>();
  
  /**
   * {@inheritDoc}
   */
  public CxxCoverageSensor(Settings settings) {
    this.settings = settings;
    parsers.add(new CoberturaParser());
    parsers.add(new BullseyeParser());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    List<File> reports = getReports(settings, project.getFileSystem().getBasedir().getPath(),
                                    REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
    CxxUtils.LOG.debug("Parsing coverage reports");
    Map<String, CoverageMeasuresBuilder> coverageMeasures = parseReports(reports);
    saveMeasures(project, context, coverageMeasures, false);
    
    CxxUtils.LOG.debug("Parsing integration test coverage reports");
    List<File> itReports = getReports(settings, project.getFileSystem().getBasedir().getPath(),
                                      IT_REPORT_PATH_KEY, IT_DEFAULT_REPORT_PATH);
    coverageMeasures = parseReports(itReports);
    saveMeasures(project, context, coverageMeasures, true);
  }
  
  private Map<String, CoverageMeasuresBuilder> parseReports(List<File> reports) {
    Map<String, CoverageMeasuresBuilder>  coverageMeasures = new HashMap<String, CoverageMeasuresBuilder>();
    
    for (File report : reports) {
      boolean parsed = false;
      for (CoverageParser parser: parsers){
        try{
          coverageMeasures.clear();
          parser.parseReport(report, coverageMeasures);
          
          if (!coverageMeasures.isEmpty()) {
            parsed = true;
            CxxUtils.LOG.info("Added report '{}' (parsed by: {}) to the coverage data", report, parser);
            break;            
          }
        } catch (XMLStreamException e) {
          CxxUtils.LOG.trace("Report {} cannot be parsed by {}", report, parser);
        }
      }
      
      if(!parsed){
        CxxUtils.LOG.error("Report {} cannot be parsed", report);
      }
    }

    return coverageMeasures;
  }

  private void saveMeasures(Project project,
                            SensorContext context,
                            Map<String, CoverageMeasuresBuilder> coverageMeasures,
                            boolean itTest) {
    for(Map.Entry<String, CoverageMeasuresBuilder> entry: coverageMeasures.entrySet()) {
      String filePath = entry.getKey();
      org.sonar.api.resources.File cxxfile =
        org.sonar.api.resources.File.fromIOFile(new File(filePath), project);
      if (fileExist(context, cxxfile)) {
        CxxUtils.LOG.debug("Saving coverage measures for file '{}'", filePath);
        for (Measure measure : entry.getValue().createMeasures()) {
          measure = itTest ? convertToItMeasure(measure) : measure;
          context.saveMeasure(cxxfile, measure);
        }
      } else {
        CxxUtils.LOG.debug("Cannot find the file '{}', ignoring coverage measures", filePath);
      }
    }
  }
    
  Measure convertToItMeasure(Measure measure){
    Measure itMeasure = null;
    Metric metric = measure.getMetric();
    Double value = measure.getValue();
    
    if (CoreMetrics.LINES_TO_COVER.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_LINES_TO_COVER, value);
    } else if (CoreMetrics.UNCOVERED_LINES.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_UNCOVERED_LINES, value);
    } else if (CoreMetrics.COVERAGE_LINE_HITS_DATA.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_COVERAGE_LINE_HITS_DATA, measure.getData());
    } else if (CoreMetrics.CONDITIONS_TO_COVER.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_CONDITIONS_TO_COVER, value);
    } else if (CoreMetrics.UNCOVERED_CONDITIONS.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_UNCOVERED_CONDITIONS, value);
    } else if (CoreMetrics.COVERED_CONDITIONS_BY_LINE.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_COVERED_CONDITIONS_BY_LINE, measure.getData());
    } else if (CoreMetrics.CONDITIONS_BY_LINE.equals(metric)) {
      itMeasure = new Measure(CoreMetrics.IT_CONDITIONS_BY_LINE, measure.getData());
    }
    
    return itMeasure;
  }
  
  private boolean fileExist(SensorContext context, org.sonar.api.resources.File file) {
    return context.getResource(file) != null;
  }
}
