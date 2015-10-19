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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxCoverageSensor extends CxxReportSensor {

  private enum CoverageType {

    UT_COVERAGE, IT_COVERAGE, OVERALL_COVERAGE
  }

  public static final String REPORT_PATH_KEY = "sonar.cxx.coverage.reportPath";
  public static final String IT_REPORT_PATH_KEY = "sonar.cxx.coverage.itReportPath";
  public static final String OVERALL_REPORT_PATH_KEY = "sonar.cxx.coverage.overallReportPath";
  public static final String FORCE_ZERO_COVERAGE_KEY = "sonar.cxx.coverage.forceZeroCoverage";

  private List<CoverageParser> parsers = new LinkedList<>();
  private final ProjectReactor reactor;

  /**
   * {@inheritDoc}
   */
  public CxxCoverageSensor(Settings settings, FileSystem fs, ProjectReactor reactor) {
    super(settings, fs, reactor);

    this.reactor = reactor;
    final String baseDir = fs.baseDir().getAbsolutePath();
    parsers.add(new CoberturaParser(baseDir));
    parsers.add(new BullseyeParser(baseDir));
    parsers.add(new VisualStudioParser(baseDir));
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(fs.predicates().hasLanguage(CxxLanguage.KEY))
      && (isForceZeroCoverageActivated()
      || conf.hasKey(REPORT_PATH_KEY)
      || conf.hasKey(IT_REPORT_PATH_KEY)
      || conf.hasKey(OVERALL_REPORT_PATH_KEY));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {

    Map<String, CoverageMeasuresBuilder> coverageMeasures = null;
    Map<String, CoverageMeasuresBuilder> itCoverageMeasures = null;
    Map<String, CoverageMeasuresBuilder> overallCoverageMeasures = null;

    if (conf.hasKey(REPORT_PATH_KEY)) {
      CxxUtils.LOG.debug("Parsing coverage reports");
      List<File> reports = getReports(conf,
        reactor.getRoot().getBaseDir().getAbsolutePath(),
        fs.baseDir().getPath(),
        REPORT_PATH_KEY);
      coverageMeasures = processReports(project, context, reports);
      saveMeasures(context, coverageMeasures, CoverageType.UT_COVERAGE);
    }

    if (conf.hasKey(IT_REPORT_PATH_KEY)) {
      CxxUtils.LOG.debug("Parsing integration test coverage reports");
      List<File> itReports = getReports(conf,
        reactor.getRoot().getBaseDir().getAbsolutePath(),
        fs.baseDir().getPath(),
        IT_REPORT_PATH_KEY);
      itCoverageMeasures = processReports(project, context, itReports);
      saveMeasures(context, itCoverageMeasures, CoverageType.IT_COVERAGE);
    }

    if (conf.hasKey(OVERALL_REPORT_PATH_KEY)) {
      CxxUtils.LOG.debug("Parsing overall test coverage reports");
      List<File> overallReports = getReports(conf,
        reactor.getRoot().getBaseDir().getAbsolutePath(),
        fs.baseDir().getPath(),
        OVERALL_REPORT_PATH_KEY);
      overallCoverageMeasures = processReports(project, context, overallReports);
      saveMeasures(context, overallCoverageMeasures, CoverageType.OVERALL_COVERAGE);
    }

    if (isForceZeroCoverageActivated()) {
      CxxUtils.LOG.debug("Zeroing coverage information for untouched files");
      zeroMeasuresWithoutReports(context, coverageMeasures, itCoverageMeasures, overallCoverageMeasures);
    }
  }

  private Map<String, CoverageMeasuresBuilder> processReports(final Project project, final SensorContext context, List<File> reports) {
    Map<String, CoverageMeasuresBuilder> measuresTotal = new HashMap<>();
    Map<String, CoverageMeasuresBuilder> measuresForReport = new HashMap<>();

    for (File report : reports) {
      CxxUtils.LOG.info("Processing report '{}'", report);
      boolean parsed = false;
      for (CoverageParser parser : parsers) {
        try {
          measuresForReport.clear();
          parser.processReport(project, context, report, measuresForReport);

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

  private void saveMeasures(SensorContext context,
    Map<String, CoverageMeasuresBuilder> coverageMeasures,
    CoverageType ctype) {
    for (Map.Entry<String, CoverageMeasuresBuilder> entry : coverageMeasures.entrySet()) {
      String filePath = entry.getKey();
      InputFile cxxFile = fs.inputFile(fs.predicates().hasPath(filePath));
      if (cxxFile != null) {
        Collection<Measure> measures = entry.getValue().createMeasures();
        CxxUtils.LOG.debug("Saving '{}' coverage measures for file '{}'", measures.size(), filePath);
        for (Measure measure : measures) {
          switch (ctype) {
            case IT_COVERAGE:
              measure = convertToItMeasure(measure);
              break;
            case OVERALL_COVERAGE:
              measure = convertForOverall(measure);
              break;
          }
          context.saveMeasure(cxxFile, measure);
        }
      } else {
        CxxUtils.LOG.warn("Cannot find the file '{}', ignoring coverage measures", filePath);
      }
    }
  }

  private void zeroMeasuresWithoutReports(SensorContext context,
    Map<String, CoverageMeasuresBuilder> coverageMeasures,
    Map<String, CoverageMeasuresBuilder> itCoverageMeasures,
    Map<String, CoverageMeasuresBuilder> overallCoverageMeasures
  ) {
    FilePredicates p = fs.predicates();
    Iterable<InputFile> inputFiles = fs.inputFiles(p.and(p.hasType(InputFile.Type.MAIN), p.hasLanguage(CxxLanguage.KEY)));
    for (InputFile inputFile : inputFiles) {
      String filePath = CxxUtils.normalizePath(inputFile.absolutePath());

      if (conf.hasKey(REPORT_PATH_KEY)) {
        if (coverageMeasures == null || coverageMeasures.get(filePath) == null) {
          saveZeroValueForResource(inputFile, filePath, context, CoverageType.UT_COVERAGE);
        }
      }

      if (conf.hasKey(IT_REPORT_PATH_KEY)) {
        if (itCoverageMeasures == null || itCoverageMeasures.get(filePath) == null) {
          saveZeroValueForResource(inputFile, filePath, context, CoverageType.IT_COVERAGE);
        }
      }

      if (conf.hasKey(OVERALL_REPORT_PATH_KEY)) {
        if (overallCoverageMeasures == null || overallCoverageMeasures.get(filePath) == null) {
          saveZeroValueForResource(inputFile, filePath, context, CoverageType.OVERALL_COVERAGE);
        }
      }
    }
  }

  private void saveZeroValueForResource(InputFile inputFile,
    String filePath,
    SensorContext context,
    CoverageType ctype) {

    Resource resource = context.getResource(inputFile);
    Measure ncloc = context.getMeasure(resource, CoreMetrics.NCLOC);
    Measure stmts = context.getMeasure(resource, CoreMetrics.STATEMENTS);
    if (ncloc != null && stmts != null
      && ncloc.getValue() > 0 && stmts.getValue() > 0) {
      String coverageKind = "unit test ";
      Metric hitsDataMetric = CoreMetrics.COVERAGE_LINE_HITS_DATA;
      Metric linesToCoverMetric = CoreMetrics.LINES_TO_COVER;
      Metric uncoveredLinesMetric = CoreMetrics.UNCOVERED_LINES;

      switch (ctype) {
        case IT_COVERAGE:
          coverageKind = "integration test ";
          hitsDataMetric = CoreMetrics.IT_COVERAGE_LINE_HITS_DATA;
          linesToCoverMetric = CoreMetrics.IT_LINES_TO_COVER;
          uncoveredLinesMetric = CoreMetrics.IT_UNCOVERED_LINES;
          break;
        case OVERALL_COVERAGE:
          coverageKind = "overall ";
          hitsDataMetric = CoreMetrics.OVERALL_COVERAGE_LINE_HITS_DATA;
          linesToCoverMetric = CoreMetrics.OVERALL_LINES_TO_COVER;
          uncoveredLinesMetric = CoreMetrics.OVERALL_UNCOVERED_LINES;
        default:
      }

      CxxUtils.LOG.debug("Zeroing {}coverage measures for file '{}'", coverageKind, filePath);

      PropertiesBuilder<Integer, Integer> lineHitsData = new PropertiesBuilder<>(hitsDataMetric);
      for (int i = 1; i <= inputFile.lines(); ++i) {
        lineHitsData.add(i, 0);
      }
      context.saveMeasure(inputFile, lineHitsData.build());
      context.saveMeasure(inputFile, linesToCoverMetric, ncloc.getValue());
      context.saveMeasure(inputFile, uncoveredLinesMetric, ncloc.getValue());
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

  private boolean isForceZeroCoverageActivated() {
    return conf.getBoolean(FORCE_ZERO_COVERAGE_KEY);
  }
}
