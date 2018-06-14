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
/*
 * Derived from https://github.com/Londran/sonar-ctc/blob/master/src/main/java/org/sonar/plugins/ctc/api/parser/CtcTextParser.java
 */
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.NoSuchElementException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;

import java.math.BigDecimal;

import org.apache.commons.io.FilenameUtils;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static org.sonar.cxx.sensors.coverage.TestwellCtcTxtResult.FILE_HEADER;
import static org.sonar.cxx.sensors.coverage.TestwellCtcTxtResult.FILE_RESULT;
import static org.sonar.cxx.sensors.coverage.TestwellCtcTxtResult.LINE_RESULT;
import static org.sonar.cxx.sensors.coverage.TestwellCtcTxtResult.SECTION_SEP;



/**
 * {@inheritDoc}
 */
public class TestwellCtcTxtParser extends CxxCoverageParser {

  private static final Logger LOG = Loggers.get(TestwellCtcTxtParser.class);
  
  private Scanner scanner;
  
  private static final int FROM_START = 0;
  private static final int CONDS_FALSE = 1;
  private static final int CONDS_TRUE = 2;
  private static final int LINE_NR_GROUP = 3;

  

  public TestwellCtcTxtParser() {
    // no operation but necessary for list of coverage parsers 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report, final Map<String, CoverageMeasures> coverageData) {
    LOG.debug("Parsing 'Testwell CTC++' textual format");

    try (Scanner s = new Scanner(report).useDelimiter(SECTION_SEP)) {
      scanner = s;
      Matcher headerMatcher = FILE_HEADER.matcher(scanner.next());
      while (parseUnit(coverageData, headerMatcher)) {
        headerMatcher.reset(scanner.next());
      }
    } catch (FileNotFoundException e) {
      LOG.warn("File not found '{}'", e.getMessage());
    } catch (NoSuchElementException e) {
      LOG.debug("File section not found.");
    }
  }

  private boolean parseUnit(final Map<String, CoverageMeasures> coverageData, Matcher headerMatcher) {
    LOG.debug(headerMatcher.toString());

    if (headerMatcher.find(FROM_START)) {
      parseFileUnit(coverageData, headerMatcher);
    } else {
      return false;
    }
    return true;
  }

  private void parseFileUnit(final Map<String, CoverageMeasures> coverageData, Matcher headerMatcher) {
    LOG.debug("Parsing file section...");

    String normalFilename;
    String filename = headerMatcher.group(1);
    if (new File(filename).isAbsolute()) {
      normalFilename = FilenameUtils.normalize(filename);
    } else {
      normalFilename = FilenameUtils.normalize("./" + filename);
    }
    File file = new File(normalFilename);
    addLines(file, coverageData);
  }

  private void addLines(File file, final Map<String, CoverageMeasures> coverageData) {
    LOG.debug("Parsing function sections...");

    CoverageMeasures coverageMeasures = CoverageMeasures.create();
    for (String nextLine = scanner.next(); !FILE_RESULT.matcher(nextLine).find(); nextLine = scanner.next()) {
      parseLineSection(coverageMeasures, nextLine);
    }
    coverageData.put(file.getPath(), coverageMeasures);
  }

  private void parseLineSection(CoverageMeasures coverageMeasures, String nextLine) {
    LOG.debug("Found line section...");
    
    Matcher lineMatcher = LINE_RESULT.matcher(nextLine);
    if (lineMatcher.find(FROM_START)) {
      addEachLine(coverageMeasures, lineMatcher);
    } else {
      LOG.warn("Neither File Result nor Line Result after FileHeader!");
    }
  }

  private void addEachLine(CoverageMeasures coverageMeasures, Matcher lineMatcher) {

    int lineHits = 0;
    int lineIdPrev = 0;
    int lineIdCond = 0;
    int conditions = 0;
    int coveredConditions = 0;
    boolean conditionIsDetected = false;

    do {
      int lineIdCur = Integer.parseInt(lineMatcher.group(LINE_NR_GROUP));

      String condsTrue = lineMatcher.group(CONDS_TRUE);
      String condsFalse = lineMatcher.group(CONDS_FALSE);

      if ((condsTrue != null) || (condsFalse != null)) {

        int lineHitsTrue = (condsTrue != null ? new BigDecimal(condsTrue).intValue() : 0);
        int lineHitsFalse = (condsFalse != null ? new BigDecimal(condsFalse).intValue() : 0);
        lineHits = lineHitsTrue + lineHitsFalse;

        if (lineIdPrev != lineIdCur) {
          coverageMeasures.setHits(lineIdCur, lineHits);

          if (lineIdCond > 0) {
            coverageMeasures.setConditions(lineIdCond, conditions, coveredConditions);
            lineIdCond = 0;
            conditions = 0;
            coveredConditions = 0;
            conditionIsDetected = false;
          }

          if ((condsTrue != null) && (condsFalse != null)) {
            // suppose single condition
            lineIdCond = lineIdCur;
            conditions = 2;
            coveredConditions = (lineHitsTrue > 0 ? 1 : 0) + (lineHitsFalse > 0 ? 1 : 0);
            conditionIsDetected = true;
          }
        } else {
          // multicondition

          if (conditionIsDetected) {
            // reset supposed single condition
            conditions = 0;
            coveredConditions = 0;
            conditionIsDetected = false;
          }
          lineIdCond = lineIdCur;
          conditions++;
          if (lineHits > 0) {
            coveredConditions++;
          }
        }
      } else {
        // Parse information for statement coverage needed in decising the line coverage
        setLinehitsByBlockend(coverageMeasures, lineIdCur, lineMatcher);
      }

      setLinehits(coverageMeasures, lineIdPrev, lineIdCur, lineHits);
      lineIdPrev = lineIdCur;
    } while (lineMatcher.find());
  }

  private void setLinehitsByBlockend(CoverageMeasures coverageMeasures, int lineIdCur, Matcher lineMatcher) {
    int lineHits;
    String blockEnd = lineMatcher.group(4);

    if (blockEnd != null) {
      if (blockEnd.endsWith("-")) {
        lineHits = 0;
      } else if (blockEnd.endsWith("+")) {
        lineHits = 1;
      } else {
        lineHits = 0;
        LOG.warn("Undefined information for statement coverage!");
      }
      coverageMeasures.setHits(lineIdCur, lineHits);
    }
  }

  private void setLinehits(CoverageMeasures coverageMeasures, int lineIdPrev, int lineIdCur, int lineHits) {

    if (lineIdPrev > 0) {
      int lineIdNext = lineIdPrev + 1;
      while (lineIdNext < lineIdCur) {
        coverageMeasures.setHits(lineIdNext, lineHits);
        lineIdNext++;
      }
    }
  }
}
