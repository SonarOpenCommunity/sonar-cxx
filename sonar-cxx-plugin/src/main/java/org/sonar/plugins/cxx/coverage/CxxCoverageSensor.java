/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.coverage;

import java.io.File;
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
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxCoverageSensor extends CxxReportSensor {
  public static final Logger LOG = Loggers.get(CxxCoverageSensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.coverage.reportPath";
  public static final String IT_REPORT_PATH_KEY = "sonar.cxx.coverage.itReportPath";
  public static final String OVERALL_REPORT_PATH_KEY = "sonar.cxx.coverage.overallReportPath";
  public static final String FORCE_ZERO_COVERAGE_KEY = "sonar.cxx.coverage.forceZeroCoverage";
  
  private final List<CoverageParser> parsers = new LinkedList<>();
  private final CxxCoverageCache cache;

  /**
   * {@inheritDoc}
   */
  public CxxCoverageSensor(Settings settings, CxxCoverageCache cache) {
    super(settings, null);
    this.cache = cache;
    parsers.add(new CoberturaParser());
    parsers.add(new BullseyeParser());
    parsers.add(new VisualStudioParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(CxxLanguage.KEY).name("CxxCoverageSensor");
  }
  
  /**
   * {@inheritDoc}
   */
  public void execute(SensorContext context, Map<InputFile, Set<Integer>> linesOfCode) {

    Map<String, CoverageMeasures> coverageMeasures = null;
    Map<String, CoverageMeasures> itCoverageMeasures = null;
    Map<String, CoverageMeasures> overallCoverageMeasures = null;

    LOG.debug("Coverage BaseDir '{}' ", context.fileSystem().baseDir());
    
    if (settings.hasKey(REPORT_PATH_KEY)) {
      LOG.debug("Parsing coverage reports");
      List<File> reports = getReports(settings, context.fileSystem().baseDir(), REPORT_PATH_KEY);
      coverageMeasures = processReports(context, reports, this.cache.unitCoverageCache());
      saveMeasures(context, coverageMeasures, CoverageType.UNIT);
    }

    if (settings.hasKey(IT_REPORT_PATH_KEY)) {
      LOG.debug("Parsing integration test coverage reports");
      List<File> itReports = getReports(settings, context.fileSystem().baseDir(), IT_REPORT_PATH_KEY);
      itCoverageMeasures = processReports(context, itReports, this.cache.integrationCoverageCache());
      saveMeasures(context, itCoverageMeasures, CoverageType.IT);
    }

    if (settings.hasKey(OVERALL_REPORT_PATH_KEY)) {
      LOG.debug("Parsing overall test coverage reports");
      List<File> overallReports = getReports(settings, context.fileSystem().baseDir(), OVERALL_REPORT_PATH_KEY);
      overallCoverageMeasures = processReports(context, overallReports, this.cache.overallCoverageCache());
      saveMeasures(context, overallCoverageMeasures, CoverageType.OVERALL);
    }
    
    if (settings.getBoolean(FORCE_ZERO_COVERAGE_KEY)) {
      LOG.debug("Zeroing coverage information for untouched files");
      zeroMeasuresWithoutReports(context, coverageMeasures, itCoverageMeasures, overallCoverageMeasures, linesOfCode);
    }
  }

  private void zeroMeasuresWithoutReports(
    SensorContext context,
    Map<String, CoverageMeasures> coverageMeasures,
    Map<String, CoverageMeasures> itCoverageMeasures,
    Map<String, CoverageMeasures> overallCoverageMeasures,
    Map<InputFile, Set<Integer>> linesOfCode
  ) {
    FileSystem fileSystem = context.fileSystem();
    FilePredicates p = fileSystem.predicates();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(p.and(p.hasType(InputFile.Type.MAIN), p.hasLanguage(CxxLanguage.KEY)));

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
  
  private void saveZeroValueForResource(InputFile inputFile, SensorContext context, CoverageType ctype, @Nullable Set<Integer> linesOfCode) {
    if (linesOfCode != null) {
      LOG.debug("Zeroing {} coverage measures for file '{}'", ctype, inputFile.relativePath());

      NewCoverage newCoverage = context.newCoverage()
        .onFile(inputFile)
        .ofType(ctype);

      for (Integer line : linesOfCode) {
        try {
          newCoverage.lineHits(line, 0);
        } catch (Exception ex) {
          LOG.error("Cannot save Line Hits for Line '{}' '{}' : '{}', ignoring measure", inputFile.relativePath(), line, ex.getMessage());
          CxxUtils.validateRecovery(ex, settings);
        }
      }

      try {
        newCoverage.save();
      } catch (Exception ex) {
        LOG.error("Cannot save measure '{}' : '{}', ignoring measure", inputFile.relativePath(), ex.getMessage());
        CxxUtils.validateRecovery(ex, settings);
      }
    }
  }

  private Map<String, CoverageMeasures> processReports(final SensorContext context, List<File> reports, Map<String, Map<String, CoverageMeasures>> cacheCov) {
    Map<String, CoverageMeasures> measuresTotal = new HashMap<>();
    Map<String, CoverageMeasures> measuresForReport = new HashMap<>();

    for (File report : reports) {
      if (!cacheCov.containsKey(report.getAbsolutePath())) {      
        boolean parsed = false;
        for (CoverageParser parser : parsers) {
          try {
            measuresForReport.clear();
            parser.processReport(context, report, measuresForReport);

            if (!measuresForReport.isEmpty()) {
              parsed = true;
              measuresTotal.putAll(measuresForReport);
              LOG.info("Added report '{}' (parsed by: {}) to the coverage data", report, parser);
              break;
            }
          } catch (XMLStreamException e) {
            LOG.trace("Report {} cannot be parsed by {}", report, parser);
          }
        }
        
        if (!parsed) {
          LOG.error("Report {} cannot be parsed", report);
        }

        LOG.debug("cached measures for '{}' : current cache content data = '{}'", report.getAbsolutePath(), cacheCov.size());
        cacheCov.put(report.getAbsolutePath(), measuresTotal);  
      } else {
        LOG.debug("Processing report '{}' skipped - already in cache", report);
        measuresTotal.putAll(cacheCov.get(report.getAbsolutePath()));
      }
    }

    return measuresTotal;
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
          if(measure.getType().equals(CoverageMeasure.CoverageType.LINE)) {
            try
            {
              newCoverage.lineHits(measure.getLine(), measure.getHits());
            } catch(Exception ex) {
              LOG.error("Cannot save Line Hits for Line '{}' '{}' : '{}', ignoring measure", filePath, measure.getLine(), ex.getMessage());
              CxxUtils.validateRecovery(ex, settings);
            }            
          }
          
          if(measure.getType().equals(CoverageMeasure.CoverageType.CONDITION)) {
            try
            {
              newCoverage.conditions(measure.getLine(), measure.getConditions(), measure.getCoveredConditions());
            } catch(Exception ex) {
              LOG.error("Cannot save Conditions Hits for Line '{}' '{}' : '{}', ignoring measure", filePath, measure.getLine(), ex.getMessage());
              CxxUtils.validateRecovery(ex, settings);
            }                         
          }                             
        }
        
        try
        {
          newCoverage.save();
        } catch(Exception ex) {
          LOG.error("Cannot save measure '{}' : '{}', ignoring measure", filePath, ex.getMessage());
          CxxUtils.validateRecovery(ex, settings);
        }        
      } else {
        LOG.debug("Cannot find the file '{}', ignoring coverage measures", filePath);
      }       
    }
  }
}
