/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
import java.util.List;
import java.util.Map;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.ReportException;

/**
 * {@inheritDoc}
 */
public abstract class CoverageSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CoverageSensor.class);

  private final CoverageParser parser;
  private final String reportPathsKey;

  protected CoverageSensor(String reportPathsKey, CoverageParser parser) {
    this.reportPathsKey = reportPathsKey;
    this.parser = parser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void executeImpl() {
    List<File> reports = getReports(reportPathsKey);
    for (var report : reports) {
      executeReport(report);
    }
  }

  /**
   * @param report to read
   */
  protected void executeReport(File report) {
    try {
      LOG.info("Processing report '{}'", report);
      processReport(report);
    } catch (EmptyReportException e) {
      LOG.warn(e.getMessage());
    } catch (ReportException e) {
      CxxUtils.validateRecovery(e.getMessage(), e, context.config());
    }
  }

  protected void processReport(File report) {
    var coverageData = parser.parse(report);
    if (coverageData.isEmpty()) {
      throw new EmptyReportException("Coverage report " + report + " result is empty (parsed by " + parser + ")");
    }

    saveMeasures(coverageData);
  }

  protected void saveMeasures(Map<String, CoverageMeasures> coverageMeasures) {
    for (var entry : coverageMeasures.entrySet()) {
      String filePath = PathUtils.sanitize(entry.getKey());
      if (filePath != null) {
        var cxxFile = getInputFileIfInProject(filePath);

        if (cxxFile != null) {
          var newCoverage = context.newCoverage().onFile(cxxFile);
          Collection<CoverageMeasure> measures = entry.getValue().getCoverageMeasures();
          measures.forEach((CoverageMeasure measure) -> checkCoverage(newCoverage, measure));

          try {
            newCoverage.save();
            LOG.debug("Saved '{}' coverage measures for file '{}'", measures.size(), filePath);
          } catch (RuntimeException e) {
            var msg = "Cannot save coverage measures for file '" + filePath + "'";
            CxxUtils.validateRecovery(msg, e, context.config());
          }
        } else {
          if (filePath.startsWith(context.fileSystem().baseDir().getAbsolutePath())) {
            LOG.warn("Cannot find the file '{}', ignoring coverage measures", filePath);
          } else {
            LOG.debug("Ignoring coverage measures for '{}'", filePath);
          }
        }
      } else {
        LOG.warn("Cannot sanitize file path '{}', ignoring coverage measures", entry.getKey());
      }
    }
  }

  /**
   * @param newCoverage
   * @param measure
   */
  protected void checkCoverage(NewCoverage newCoverage, CoverageMeasure measure) {
    try {
      newCoverage.lineHits(measure.getLine(), measure.getHits());
      newCoverage.conditions(measure.getLine(), measure.getConditions(), measure.getCoveredConditions());
    } catch (RuntimeException e) {
      var msg = "Cannot save Conditions Hits for Line '" + measure.getLine() + "'";
      CxxUtils.validateRecovery(msg, e, context.config());
    }
  }

}
