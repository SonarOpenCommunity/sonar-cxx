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
package org.sonar.plugins.cxx.coverage;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.plugins.cxx.CxxLanguage;

/**
 * {@inheritDoc}
 */
public class CxxCoverageSensor extends CxxReportSensor {

  private static final int UNIT_TEST_COVERAGE = 0;
  public static final int IT_TEST_COVERAGE = 1;
  public static final int OVERALL_TEST_COVERAGE = 2;

  public static final String REPORT_PATH_KEY = "sonar.cxx.coverage.reportPath";
  public static final String IT_REPORT_PATH_KEY = "sonar.cxx.coverage.itReportPath";
  public static final String OVERALL_REPORT_PATH_KEY = "sonar.cxx.coverage.overallReportPath";
  private static final String DEFAULT_REPORT_PATH = "coverage-reports/coverage-*.xml";
  private static final String IT_DEFAULT_REPORT_PATH = "coverage-reports/it-coverage-*.xml";
  private static final String OVERALL_DEFAULT_REPORT_PATH = "coverage-reports/overall-coverage-*.xml";

  public static final String FORCE_ZERO_COVERAGE_KEY = "sonar.cxx.coverage.forceZeroCoverage";

  private static List<CoverageParser> parsers = new LinkedList<CoverageParser>();

  /**
   * {@inheritDoc}
   */
  public CxxCoverageSensor(Settings settings, ModuleFileSystem fs) {
    super(settings, fs);

    parsers.add(new CoberturaParser());
    parsers.add(new BullseyeParser());
    parsers.add(new VisualStudioParser());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    String baseDir = fs.baseDir().getPath();

    CxxUtils.LOG.debug("Parsing coverage reports");
    List<File> reports = getReports(conf, baseDir, REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
    Map<String, CoverageMeasuresBuilder> coverageMeasures = parseReports(reports);
    saveMeasures(project, context, coverageMeasures, UNIT_TEST_COVERAGE);
    if (isForceZeroCoverageActivated()) {
      CxxUtils.LOG.debug("ForceZeroCoverageActivated=true");
      zeroMeasuresWithoutReports(project, context, coverageMeasures);
    }

    CxxUtils.LOG.debug("Parsing integration test coverage reports");
    List<File> itReports = getReports(conf, baseDir, IT_REPORT_PATH_KEY, IT_DEFAULT_REPORT_PATH);
    Map<String, CoverageMeasuresBuilder> itCoverageMeasures = parseReports(itReports);
    saveMeasures(project, context, itCoverageMeasures, IT_TEST_COVERAGE);

    CxxUtils.LOG.debug("Parsing overall test coverage reports");
    List<File> overallReports = getReports(conf, baseDir, OVERALL_REPORT_PATH_KEY, OVERALL_DEFAULT_REPORT_PATH);
    Map<String, CoverageMeasuresBuilder> overallCoverageMeasures = parseReports(overallReports);
    saveMeasures(project, context, overallCoverageMeasures, OVERALL_TEST_COVERAGE);
  }

  private Map<String, CoverageMeasuresBuilder> parseReports(List<File> reports) {
    Map<String, CoverageMeasuresBuilder> measuresTotal = new HashMap<String, CoverageMeasuresBuilder>();
    Map<String, CoverageMeasuresBuilder> measuresForReport = new HashMap<String, CoverageMeasuresBuilder>();

    for (File report : reports) {
      boolean parsed = false;
      for (CoverageParser parser : parsers) {
        try {
          measuresForReport.clear();
          parser.parseReport(report, measuresForReport);

          if (!measuresForReport.isEmpty()) {
            parsed = true;
            measuresTotal.putAll(measuresForReport);
            CxxUtils.LOG.info("Added report '{}' (parsed by: {}) to the coverage data", report, parser);
            break;
          }
        } catch (XMLStreamException e) {
          CxxUtils.LOG.trace("Report {} cannot be parsed by {}", report, parser);
        }
      }

      if (!parsed) {
        CxxUtils.LOG.error("Report {} cannot be parsed", report);
      }
    }

    return measuresTotal;
  }

  private void saveMeasures(Project project,
    SensorContext context,
    Map<String, CoverageMeasuresBuilder> coverageMeasures,
    int coveragetype) {
    for (Map.Entry<String, CoverageMeasuresBuilder> entry : coverageMeasures.entrySet()) {
      String filePath = entry.getKey();
      org.sonar.api.resources.File cxxfile
        = org.sonar.api.resources.File.fromIOFile(new File(filePath), project);
      if (fileExist(context, cxxfile)) {
        CxxUtils.LOG.debug("Saving coverage measures for file '{}'", filePath);
        for (Measure measure : entry.getValue().createMeasures()) {
          switch (coveragetype) {
            case UNIT_TEST_COVERAGE:
              break;
            case IT_TEST_COVERAGE:
              measure = convertToItMeasure(measure);
              break;
            case OVERALL_TEST_COVERAGE:
              measure = convertForOverall(measure);
              break;
            default:
              break;
          }
          context.saveMeasure(cxxfile, measure);
        }
      } else {
        CxxUtils.LOG.warn("Cannot find the file '{}', ignoring coverage measures", filePath);
      }
    }
  }

  private void zeroMeasuresWithoutReports(Project project,
    SensorContext context,
    Map<String, CoverageMeasuresBuilder> coverageMeasures) {
    for (File file : fs.files(FileQuery.onSource().onLanguage(CxxLanguage.KEY))) {
      org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(file, project);
      if (fileExist(context, resource)) {
        String filePath = CxxUtils.normalizePath(file.getAbsolutePath());
        CoverageMeasuresBuilder fileCoverage = coverageMeasures.get(filePath);
        if (fileCoverage == null) {
          saveZeroValueForResource(resource, filePath, context);
        }
      }
    }
  }

  private void saveZeroValueForResource(org.sonar.api.resources.File resource, String filePath, SensorContext context) {
    PropertiesBuilder<Integer, Integer> lineHitsData = new PropertiesBuilder<Integer, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
    Measure ncloc = context.getMeasure(resource, CoreMetrics.NCLOC);
    Measure stmts = context.getMeasure(resource, CoreMetrics.STATEMENTS);
    if (ncloc != null && stmts != null) {
      if (ncloc.getValue() > 0 && stmts.getValue() > 0) {
        CxxUtils.LOG.debug("Zero coverage measures for file '{}'", filePath);
        for (int i = 1; i <= context.getMeasure(resource, CoreMetrics.LINES).getIntValue(); ++i) {
          lineHitsData.add(i, 0);
        }
        context.saveMeasure(resource, lineHitsData.build());
        context.saveMeasure(resource, CoreMetrics.LINES_TO_COVER, ncloc.getValue());
        context.saveMeasure(resource, CoreMetrics.UNCOVERED_LINES, ncloc.getValue());
      }
    }
  }

  private Measure convertToItMeasure(Measure measure) {
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

  private Measure convertForOverall(Measure measure) {
    Measure itMeasure = null;

    if (CoreMetrics.LINES_TO_COVER.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_LINES_TO_COVER, measure.getValue());
    } else if (CoreMetrics.UNCOVERED_LINES.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_UNCOVERED_LINES, measure.getValue());
    } else if (CoreMetrics.COVERAGE_LINE_HITS_DATA.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_COVERAGE_LINE_HITS_DATA, measure.getData());
    } else if (CoreMetrics.CONDITIONS_TO_COVER.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_CONDITIONS_TO_COVER, measure.getValue());
    } else if (CoreMetrics.UNCOVERED_CONDITIONS.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_UNCOVERED_CONDITIONS, measure.getValue());
    } else if (CoreMetrics.COVERED_CONDITIONS_BY_LINE.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_COVERED_CONDITIONS_BY_LINE, measure.getData());
    } else if (CoreMetrics.CONDITIONS_BY_LINE.equals(measure.getMetric())) {
      itMeasure = new Measure(CoreMetrics.OVERALL_CONDITIONS_BY_LINE, measure.getData());
    }

    return itMeasure;
  }

  private boolean fileExist(SensorContext context, org.sonar.api.resources.File file) {
    return context.getResource(file) != null;
  }

  private boolean isForceZeroCoverageActivated() {
    return conf.getBoolean(FORCE_ZERO_COVERAGE_KEY);
  }
}
