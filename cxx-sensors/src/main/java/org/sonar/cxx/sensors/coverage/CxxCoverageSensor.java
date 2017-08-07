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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import org.sonar.api.utils.PathUtils;
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

  // ToDo - cleanup reportPath properties 
  // a) deprecate old feature ant style search 
  // b) support comma separated list of coverage files
  //public static final String REPORT_PATHS_KEY = "coverage.reportPaths";

  private final List<CoverageParser> parsers = new LinkedList<>();
  private final CxxCoverageCache cache;
  public static final String KEY = "Coverage";

  /**
   * {@inheritDoc}
   * @param cache for all coverage data
   * @param language for current analysis
   * @param context for current file
   */
  public CxxCoverageSensor(CxxCoverageCache cache, CxxLanguage language, SensorContext context) {
    super(language, context.settings());
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
    // do nothing
  }

  /**
   * {@inheritDoc}
   * @param context for coverage analysis
   * @param linesOfCodeByFile use for FORCE_ZERO_COVERAGE_KEY feature 
   */
  public void execute(SensorContext context, Map<InputFile, Set<Integer>> linesOfCodeByFile) {
    Settings settings = context.settings();
    String[] reportsKey = settings.getStringArray(getReportPathKey());
    LOG.info("Searching coverage reports by path with basedir '{}' and search prop '{}'", 
        context.fileSystem().baseDir(), getReportPathKey());
    LOG.info("Searching for coverage reports '{}'", Arrays.toString(reportsKey));
    
    Map<String, CoverageMeasures> coverageMeasures = null;
    Map<String, CoverageMeasures> itCoverageMeasures = null;
    Map<String, CoverageMeasures> overallCoverageMeasures = null;

    LOG.info("Coverage BaseDir '{}' ", context.fileSystem().baseDir());

    if (context.settings().hasKey(getReportPathKey())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Parsing unit test coverage reports");
      }

      List<File> reports = getReports(context.settings(), context.fileSystem().baseDir(), getReportPathKey());
      coverageMeasures = processReports(context, reports, this.cache.unitCoverageCache());
      saveMeasures(context, coverageMeasures, CoverageType.UNIT);
    }

    if (settings.hasKey(getITReportPathKey())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Parsing integration test coverage reports");
      }
      warnUsageOfDeprecatedProperty(settings, getITReportPathKey());
      List<File> itReports = getReports(settings, context.fileSystem().baseDir(), getITReportPathKey());
      itCoverageMeasures = processReports(context, itReports, this.cache.integrationCoverageCache());
      saveMeasures(context, itCoverageMeasures, CoverageType.IT);
    }

    if (settings.hasKey(getOverallReportPathKey())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Parsing overall test coverage reports");
      }
      warnUsageOfDeprecatedProperty(settings, getOverallReportPathKey());
      List<File> overallReports = getReports(settings, 
                                             context.fileSystem().baseDir(), getOverallReportPathKey());
      overallCoverageMeasures = processReports(context, overallReports, this.cache.overallCoverageCache());
      saveMeasures(context, overallCoverageMeasures, CoverageType.OVERALL);
    }

    if (settings.getBoolean(getForceZeroCoverageKey())) {
      LOG.info("Zeroing coverage information for untouched files");
      zeroMeasuresWithoutReports(context, coverageMeasures,
                                          itCoverageMeasures,
                                          overallCoverageMeasures, 
                                          linesOfCodeByFile);
    }
  }

  private void zeroMeasuresWithoutReports(
    SensorContext context,
    @Nullable Map<String, CoverageMeasures> coverageMeasures,
    @Nullable Map<String, CoverageMeasures> itCoverageMeasures,
    @Nullable Map<String, CoverageMeasures> overallCoverageMeasures,
    Map<InputFile, Set<Integer>> linesOfCode
  ) {

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
  }

  private void saveZeroValueForResource(InputFile inputFile, SensorContext context, CoverageType ctype, 
                                        @Nullable Set<Integer> linesOfCode) {
    if (linesOfCode != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Zeroing {} coverage measures for file '{}'", ctype, inputFile.relativePath());
      }
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
              // Only use first coverage parser which handles the data correctly
              break;
            }
          }
          measuresTotal.putAll(cacheCov.get(report.getAbsolutePath()));
        } catch (EmptyReportException e) {
          LOG.debug("Report is empty {}", e);
        }
      } else {
        measuresTotal = cacheCov.get(report.getAbsolutePath());
        if (LOG.isDebugEnabled()) {
          LOG.debug("Processing report '{}' skipped - already in cache", report);
        }
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
      throw new EmptyReportException("Coverage report" + report + "cannot be parsed by" + parser, e); 
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
      String filePath = CxxUtils.normalizePathFull(PathUtils.sanitize(entry.getKey()),  
                                                  context.fileSystem().baseDir().getAbsolutePath());
      if (filePath!= null) {
        InputFile cxxFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(filePath));
        if (LOG.isDebugEnabled()) {
          LOG.debug("save coverage measure for file: '{}' cxxFile = '{}'", filePath, cxxFile);
        }
        if (cxxFile != null) {

          NewCoverage newCoverage = context.newCoverage()
                    .onFile(cxxFile)        
                    .ofType(ctype);

          Collection<CoverageMeasure> measures = entry.getValue().getCoverageMeasures();
          if (LOG.isDebugEnabled()) {
            LOG.debug("Saving '{}' coverage measures for file '{}'", measures.size(), filePath);
          }
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
          LOG.info("Saved '{}' coverage measures for file '{}'", measures.size(), filePath);
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Cannot find the file '{}', ignoring coverage measures", filePath);
          } else if (filePath.startsWith(context.fileSystem().baseDir().getAbsolutePath())) {
            LOG.warn("Cannot find the file '{}', ignoring coverage measures", filePath);
          }
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Cannot sanitize file path '{}'", entry.getKey());
        }
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

  private void warnUsageOfDeprecatedProperty(Settings settings, String reportPathProperty) {
    if (isSQ_6_2_or_newer && !settings.hasKey(getReportPathKey())) {
      LOG.warn("Property '{}' is deprecated. Please use '{}' instead.", reportPathProperty, getReportPathKey());
    }
  }
  
  @Override
  protected String getSensorKey() {
    return KEY;
  }  

  @Override
  public String getReportPathKey() {
// ToDo - Support new style
//    if (isSQ_6_2_or_newer) {
//      return this.language.getPluginProperty(REPORT_PATHS_KEY);
//    }
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  protected String getITReportPathKey() {
   return this.language.getPluginProperty(IT_REPORT_PATH_KEY);
  }

  protected String getOverallReportPathKey() {
    return this.language.getPluginProperty(OVERALL_REPORT_PATH_KEY);
   }

  protected String getForceZeroCoverageKey() {
    return this.language.getPluginProperty(FORCE_ZERO_COVERAGE_KEY);
   }

}

