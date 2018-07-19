/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;

/**
 * {@inheritDoc}
 */
public class CxxCoverageSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxCoverageSensor.class);

  // Configuration properties before SQ 6.2
  public static final String REPORT_PATH_KEY = "coverage.reportPath";

  private final List<CoverageParser> parsers = new LinkedList<>();
  private final CxxCoverageCache cache;
  public static final String KEY = "Coverage";

  /**
   * {@inheritDoc}
   *
   * @param cache for all coverage data
   * @param language for current analysis
   * @param context for current file
   */
  public CxxCoverageSensor(CxxCoverageCache cache, CxxLanguage language, SensorContext context) {
    super(language);
    this.cache = cache;
    parsers.add(new CoberturaParser());
    parsers.add(new BullseyeParser());
    parsers.add(new VisualStudioParser());
    parsers.add(new TestwellCtcTxtParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " CoverageSensor")
      .onlyOnLanguage(language.getKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return language.getPluginProperty(REPORT_PATH_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {
    Configuration conf = context.config();
    String[] reportsKey = conf.getStringArray(getReportPathKey());
    LOG.info("Searching coverage reports by path with basedir '{}' and search prop '{}'",
      context.fileSystem().baseDir(), getReportPathKey());
    LOG.info("Searching for coverage reports '{}'", Arrays.toString(reportsKey));

    Map<String, CoverageMeasures> coverageMeasures = null;

    LOG.info("Coverage BaseDir '{}' ", context.fileSystem().baseDir());

    if (context.config().hasKey(getReportPathKey())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Parsing unit test coverage reports");
      }

      List<File> reports = getReports(context.config(), context.fileSystem().baseDir(), getReportPathKey());
      coverageMeasures = processReports(context, reports, this.cache.unitCoverageCache());
      saveMeasures(context, coverageMeasures);
    }
  }

  private Map<String, CoverageMeasures> processReports(final SensorContext context, List<File> reports,
    Map<String, Map<String, CoverageMeasures>> cacheCov) {
    Map<String, CoverageMeasures> measuresTotal = new HashMap<>();

    for (File report : reports) {
      if (!cacheCov.containsKey(report.getAbsolutePath())) {
        for (CoverageParser parser : parsers) {
          try {
            parseCoverageReport(parser, context, report, measuresTotal);
            if (LOG.isDebugEnabled()) {
              LOG.debug("cached measures for '{}' : current cache content data = '{}'", report.getAbsolutePath(),
                cacheCov.size());
            }
            cacheCov.put(report.getAbsolutePath(), measuresTotal);
            // Only use first coverage parser which handles the data correctly
            break;
          } catch (EmptyReportException e) {
            LOG.debug("Report is empty {}", e);
          }
        }
        if (cacheCov.get(report.getAbsolutePath()) != null) {
          measuresTotal.putAll(cacheCov.get(report.getAbsolutePath()));
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
  private static void parseCoverageReport(CoverageParser parser, final SensorContext context, File report,
    Map<String, CoverageMeasures> measuresTotal) {
    Map<String, CoverageMeasures> measuresForReport = new HashMap<>();
    try {
      parser.processReport(context, report, measuresForReport);
    } catch (XMLStreamException e) {
      throw new EmptyReportException("Coverage report" + report + "cannot be parsed by" + parser, e);
    }

    if (measuresForReport.isEmpty()) {
      throw new EmptyReportException("Coverage report " + report + " result is empty (parsed by " + parser + ")");
    }

    measuresTotal.putAll(measuresForReport);
    LOG.info("Added coverage report '{}' (parsed by: {})", report, parser);
  }

  private void saveMeasures(SensorContext context,
    Map<String, CoverageMeasures> coverageMeasures) {
    for (Map.Entry<String, CoverageMeasures> entry : coverageMeasures.entrySet()) {
      String filePath = PathUtils.sanitize(entry.getKey());
      if (filePath != null) {
        filePath = CxxUtils.normalizePathFull(filePath, context.fileSystem().baseDir().getAbsolutePath());
        InputFile cxxFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(filePath));
        if (LOG.isDebugEnabled()) {
          LOG.debug("save coverage measure for file: '{}' cxxFile = '{}'", filePath, cxxFile);
        }
        if (cxxFile != null) {

          NewCoverage newCoverage = context.newCoverage().onFile(cxxFile);

          Collection<CoverageMeasure> measures = entry.getValue().getCoverageMeasures();
          if (LOG.isDebugEnabled()) {
            LOG.debug("Saving '{}' coverage measures for file '{}'", measures.size(), filePath);
          }

          measures.forEach((CoverageMeasure measure) -> checkCoverage(newCoverage, measure));

          try {
            newCoverage.save();
          } catch (RuntimeException ex) {
            LOG.error("Cannot save measure for file '{}' , ignoring measure. ", filePath, ex);
            CxxUtils.validateRecovery(ex, language);
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
  private void checkCoverage(NewCoverage newCoverage, CoverageMeasure measure) {
    try {
      newCoverage.lineHits(measure.getLine(), measure.getHits());
      newCoverage.conditions(measure.getLine(), measure.getConditions(), measure.getCoveredConditions());
      if (LOG.isDebugEnabled()) {
        LOG.debug("line '{}' Hits '{}' Conditions '{}:{}'",measure.getLine(), measure.getHits(),
                                                           measure.getConditions(), measure.getCoveredConditions() );
      }
    } catch (RuntimeException ex) {
      LOG.error("Cannot save Conditions Hits for Line '{}' , ignoring measure. ",
        measure.getLine(), ex.getMessage());
      CxxUtils.validateRecovery(ex, language);
    }
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }

  @Override
  protected Optional<CxxMetricsFactory.Key> getMetricKey() {
    return Optional.empty();
  }

}
