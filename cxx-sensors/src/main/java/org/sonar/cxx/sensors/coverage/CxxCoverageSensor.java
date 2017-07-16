/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Settings;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;

/**
 * {@inheritDoc}
 */
public class CxxCoverageSensor extends CxxReportSensor {
  private static final Logger LOG = Loggers.get(CxxCoverageSensor.class);

  // Configuration properties before SQ 6.2
  @Deprecated
  public static final String REPORT_PATH_KEY = "coverage.reportPath";
  @Deprecated
  public static final String IT_REPORT_PATH_KEY = "coverage.itReportPath";
  @Deprecated
  public static final String OVERALL_REPORT_PATH_KEY = "coverage.overallReportPath";
  @Deprecated
  public static final String FORCE_ZERO_COVERAGE_KEY = "coverage.forceZeroCoverage";
  
  // Configuration properties for SQ 6.2
  public static final Version SQ_6_2 = Version.create(6, 2);
  private boolean isSQ_6_2_or_newer;
  public static final String REPORT_PATHS_KEY = "coverage.reportPaths";

  private final List<CoverageParser> parsers = new LinkedList<>();
  private final CxxCoverageCache cache;
  public static final String KEY = "Coverage";

  /**
   * {@inheritDoc}
   */
  public CxxCoverageSensor(CxxCoverageCache cache, CxxLanguage language, SensorContext context) {
    super(language);
    this.cache = cache;
    if (context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_2)) {
      isSQ_6_2_or_newer = true;
    }
    parsers.add(new CoberturaParser());
    parsers.add(new BullseyeParser());
    parsers.add(new VisualStudioParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(this.language.getKey()).name(language.getName() + " CoverageSensor");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {
//  public void execute(SensorContext context, Map<InputFile, Set<Integer>> linesOfCode) {
    String reportsKey = this.language.getStringOption(reportPathKey());
    LOG.info("Searching coverage reports by path with basedir '{}' and search prop '{}'", 
        context.fileSystem().baseDir(), reportPathKey());
    LOG.info("Searching for coverage reports '{}'", reportsKey);

    Map<String, CoverageMeasures> coverageMeasures = null;
    Map<String, CoverageMeasures> itCoverageMeasures = null;
    Map<String, CoverageMeasures> overallCoverageMeasures = null;

    LOG.debug("Coverage BaseDir '{}' ", context.fileSystem().baseDir());

    if (isSQ_6_2_or_newer) {
      if (context.settings().hasKey(FORCE_ZERO_COVERAGE_KEY)) {
        LOG.warn("Property '{}' is deprecated and its value will be ignored.", FORCE_ZERO_COVERAGE_KEY);
      }
    
      Set<File> reportPaths = getReportPaths(context);
      if (reportPaths.isEmpty()) {
        return;
      }
      
      LOG.debug("Parsing coverage reports");
      List<File> reports = getReports(this.language, context.fileSystem().baseDir(), REPORT_PATHS_KEY);
      coverageMeasures = processReports(context, reports, this.cache.unitCoverageCache());
      saveMeasures(context, coverageMeasures, CoverageType.UNIT);

    } else {
      if (this.language.hasKey(REPORT_PATH_KEY)) {
        LOG.debug("Parsing coverage reports");
        List<File> reports = getReports(this.language, context.fileSystem().baseDir(), REPORT_PATH_KEY);
        coverageMeasures = processReports(context, reports, this.cache.unitCoverageCache());
        saveMeasures(context, coverageMeasures, CoverageType.UNIT);
      }
  
      if (this.language.hasKey(IT_REPORT_PATH_KEY)) {
        LOG.debug("Parsing integration test coverage reports");
        List<File> itReports = getReports(this.language, context.fileSystem().baseDir(), IT_REPORT_PATH_KEY);
        itCoverageMeasures = processReports(context, itReports, this.cache.integrationCoverageCache());
        saveMeasures(context, itCoverageMeasures, CoverageType.IT);
      }
  
      if (this.language.hasKey(OVERALL_REPORT_PATH_KEY)) {
        LOG.debug("Parsing overall test coverage reports");
        List<File> overallReports = getReports(this.language, context.fileSystem().baseDir(), OVERALL_REPORT_PATH_KEY);
        overallCoverageMeasures = processReports(context, overallReports, this.cache.overallCoverageCache());
        saveMeasures(context, overallCoverageMeasures, CoverageType.OVERALL);
      }
  
//      if (this.language.getBooleanOption(FORCE_ZERO_COVERAGE_KEY)) {
//        LOG.info("Zeroing coverage information for untouched files");
//        zeroMeasuresWithoutReports(context, coverageMeasures, itCoverageMeasures, overallCoverageMeasures, linesOfCode);
//      }
    }
  }

  private void zeroMeasuresWithoutReports(
    SensorContext context,
    @Nullable Map<String, CoverageMeasures> coverageMeasures,
    @Nullable Map<String, CoverageMeasures> itCoverageMeasures,
    @Nullable Map<String, CoverageMeasures> overallCoverageMeasures,
    Map<InputFile, Set<Integer>> linesOfCode
  ) {

    if (!context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_2)) {

    FileSystem fileSystem = context.fileSystem();
    FilePredicates p = fileSystem.predicates();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(p.and(p.hasType(InputFile.Type.MAIN),
                                                           p.hasLanguage(this.language.getKey())));

    for (InputFile inputFile : inputFiles) {
      Set<Integer> linesOfCodeForFile = linesOfCode.get(inputFile);
      String file = CxxUtils.normalizePath(inputFile.absolutePath());

      if (coverageMeasures != null && !coverageMeasures.containsKey(file)) {
        saveZeroValueForResource(inputFile, context, CoverageType.UNIT, linesOfCodeForFile);
      }

      if (itCoverageMeasures != null && !itCoverageMeasures.containsKey(file)) {
        saveZeroValueForResource(inputFile, context, CoverageType.IT, linesOfCodeForFile);
      }

      if (overallCoverageMeasures != null && !overallCoverageMeasures.containsKey(file)) {
        saveZeroValueForResource(inputFile, context, CoverageType.OVERALL, linesOfCodeForFile);
      }
    }
    } else {
      LOG.warn("SQ 6.2 and newer use 'executable_lines_data' metric - ignored saveZeroValueForResource()");
    }
  }
  
  private void saveZeroValueForResource(InputFile inputFile, SensorContext context, CoverageType ctype, 
                                        @Nullable Set<Integer> linesOfCode) {
    if (linesOfCode != null) {
      LOG.debug("Zeroing {} coverage measures for file '{}'", ctype, inputFile.relativePath());

      NewCoverage newCoverage = context.newCoverage()
        .onFile(inputFile)
        .ofType(ctype);

      for (Integer line : linesOfCode) {
        try {
          newCoverage.lineHits(line, 0);
          } catch (RuntimeException ex) {
          LOG.error("Cannot save Line Hits for Line '{}' '{}' : '{}', ignoring measure", 
              inputFile.relativePath(), line, ex);
          CxxUtils.validateRecovery(ex, this.language);
        }
      }

      try {
        newCoverage.save();
      } catch (RuntimeException ex) {
        LOG.error("Cannot save measure '{}' : '{}', ignoring measure", inputFile.relativePath(), ex);
        CxxUtils.validateRecovery(ex, this.language);
      }
    }
  }

  private Map<String, CoverageMeasures> processReports(final SensorContext context, List<File> reports, 
                                                       Map<String, Map<String, CoverageMeasures>> cacheCov) {
    Map<String, CoverageMeasures> measuresTotal = new HashMap<>();

    for (File report : reports) {
      if (!cacheCov.containsKey(report.getAbsolutePath())) {      
        try {
        for (CoverageParser parser : parsers) {
          if (parseCoverageReport(parser, context, report, measuresTotal)) {
              if (LOG.isDebugEnabled()) {
        LOG.debug("cached measures for '{}' : current cache content data = '{}'", 
                                report.getAbsolutePath(), cacheCov.size());
              }
        cacheCov.put(report.getAbsolutePath(), measuresTotal);  
              // Only use first coverage parser with handles the data correctly
              break;
            }
          }
          measuresTotal.putAll(cacheCov.get(report.getAbsolutePath()));
        } catch (EmptyReportException e) {
          LOG.debug("Report is empty {}", e);
        }
      } else {
        LOG.debug("Processing report '{}' skipped - already in cache", report);
      }
    }

    return measuresTotal;
  }

  /**
   * @param parser
   * @param context
   * @param report
   * @param measuresTotal
   * @return true if report was parsed and results are available otherwise false
   */
  private boolean parseCoverageReport(CoverageParser parser, final SensorContext context, File report,
                                      Map<String, CoverageMeasures> measuresTotal) {
    Map<String, CoverageMeasures> measuresForReport = new HashMap<>();
    try {
      parser.processReport(context, report, measuresForReport);
    } catch (XMLStreamException e) {
      throw new EmptyReportException("Coverage report" + report.toString() + "cannot be parsed by" + parser, e); 
    }

    if (measuresForReport.isEmpty()) {
      LOG.warn("Coverage report {} result is empty (parsed by {})", report, parser);
      return false;
    }

    measuresTotal.putAll(measuresForReport);
    LOG.info("Added coverage report '{}' (parsed by: {})", report, parser);
    return true;
  }

  private void saveMeasures(SensorContext context,
    Map<String, CoverageMeasures> coverageMeasures,
    CoverageType ctype) {
    for (Map.Entry<String, CoverageMeasures> entry : coverageMeasures.entrySet()) {
      String filePath = entry.getKey();
      InputFile cxxFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(filePath));
      if (cxxFile != null) {
        
        NewCoverage newCoverage = context.newCoverage()
                  .onFile(cxxFile)        
                  .ofType(ctype);
      
        Collection<CoverageMeasure> measures = entry.getValue().getCoverageMeasures();
        LOG.debug("Saving '{}' coverage measures for file '{}'", measures.size(), filePath);
        for (CoverageMeasure measure : measures) {
          checkLineCoverage(newCoverage, measure);
          checkConditionCoverage(newCoverage, measure);
        }

        try {
          newCoverage.save();
        } catch(RuntimeException ex) {
          LOG.error("Cannot save measure for file '{}' , ignoring measure. ", filePath, ex);
          CxxUtils.validateRecovery(ex, this.language);
        }
      } else {
        LOG.debug("Cannot find the file '{}', ignoring coverage measures", filePath);
      }       
    }
  }

  /**
   * @param newCoverage
   * @param measure
   */
  private void checkConditionCoverage(NewCoverage newCoverage, CoverageMeasure measure) {
    if(measure.getType() == CoverageMeasure.CoverageType.CONDITION) {
      try {
        newCoverage.conditions(measure.getLine(), measure.getConditions(), measure.getCoveredConditions());
      } catch(RuntimeException ex) {
        LOG.error("Cannot save Conditions Hits for Line '{}' , ignoring measure. ", 
                   measure.getLine(), ex);
        CxxUtils.validateRecovery(ex, this.language);
      }
    }
  }

  /**
   * @param newCoverage
   * @param measure
   */
  private void checkLineCoverage(NewCoverage newCoverage, CoverageMeasure measure) {
    if(measure.getType() == CoverageMeasure.CoverageType.LINE ) {
      try { 
        newCoverage.lineHits(measure.getLine(), measure.getHits());
      } catch(RuntimeException ex) {
        LOG.error("Cannot save Line Hits for Line '{}', ignoring measure. ", 
                    measure.getLine(), ex);
        CxxUtils.validateRecovery(ex, this.language);
      }
    }
  }  

  private static Set<File> getReportPaths(SensorContext context) {
    Set<File> reportPaths = new HashSet<>();
    Settings settings = context.settings();
    FileSystem fs = context.fileSystem();

    if (settings.hasKey(REPORT_PATHS_KEY)) {
      for (String reportPath : settings.getStringArray(REPORT_PATHS_KEY)) {
        File report = fs.resolvePath(reportPath);
        if (!report.isFile()) {
            LOG.info("Coverage report not found: '{}'", reportPath);
        } else {
          LOG.info("Add coverage report: '{}' '{}'", reportPath, report.getAbsolutePath());
          reportPaths.add(report);
        }
      }
    }

    if (settings.hasKey(REPORT_PATH_KEY)) {
      warnUsageOfDeprecatedProperty(settings, REPORT_PATH_KEY);
      File report = fs.resolvePath(settings.getString(REPORT_PATH_KEY));
      if (!report.isFile()) {
        LOG.info("Coverage UT report not found: '{}'", settings.getString(REPORT_PATH_KEY));
      } else {
        reportPaths.add(report);
      }
    }

    if (settings.hasKey(IT_REPORT_PATH_KEY)) {
      warnUsageOfDeprecatedProperty(settings, IT_REPORT_PATH_KEY);
      File report = fs.resolvePath(settings.getString(IT_REPORT_PATH_KEY));
      if (!report.isFile()) {
        LOG.info("Coverage IT report not found: '{}'", settings.getString(IT_REPORT_PATH_KEY));
      } else {
        reportPaths.add(report);
      }
    }

    return reportPaths;
  }

  private static void warnUsageOfDeprecatedProperty(Settings settings, String reportPathProperty) {
    if (!settings.hasKey(REPORT_PATHS_KEY)) {
      LOG.warn("Property '{}' is deprecated. Please use '{}' instead.", reportPathProperty, REPORT_PATHS_KEY);
    }
  }
  
  @Override
  protected String getSensorKey() {
    return KEY;
  }  

  @Override
  protected String reportPathKey() {
    if (isSQ_6_2_or_newer) {
      return REPORT_PATHS_KEY;
    }
    return REPORT_PATH_KEY;
  }
}
