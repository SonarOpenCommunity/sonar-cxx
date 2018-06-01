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

import static org.sonar.cxx.sensors.coverage.TestwellCtcResult.FILE_HEADER;
import static org.sonar.cxx.sensors.coverage.TestwellCtcResult.FILE_RESULT;
import static org.sonar.cxx.sensors.coverage.TestwellCtcResult.LINE_RESULT;
import static org.sonar.cxx.sensors.coverage.TestwellCtcResult.SECTION_SEP;
import static org.sonar.cxx.sensors.coverage.TestwellCtcResult.REPORT_FOOTER;



/**
 * {@inheritDoc}
 */
public class TestwellCtcParser extends CxxCoverageParser {

  private static final Logger LOG = Loggers.get(TestwellCtcParser.class);
  
  private Scanner scanner;
  private Matcher matcher;
  private State state;
  
  
  private static final int FROM_START = 0;
  private static final int CONDS_FALSE = 1;
  private static final int CONDS_TRUE = 2;
  private static final int LINE_NR_GROUP = 3;
  
  private enum State {
    BEGIN,
    PARSING,
    END;
  }
  

  public TestwellCtcParser() {
    // no operation but necessary for list of coverage parsers 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report, final Map<String, CoverageMeasures> coverageData) {
    LOG.debug("Parsing 'Testwell CTC++' format");
    
    try {
      this.scanner = new Scanner(report).useDelimiter(SECTION_SEP);
      this.matcher = FILE_HEADER.matcher("");
      
      this.state = State.BEGIN;
      parseReportHead();

      while(State.PARSING == this.state) {
          parseUnit(coverageData);
      }
      
    } catch (FileNotFoundException e) {
      LOG.warn("TestwellCtcParser file not found '{}'", e.getMessage());
    }

  }
  private void parseReportHead() {
    try {
      if (!matcher.reset(scanner.next()).find()) {
        LOG.info("'Testwell CTC++' file section not found.");
      } else {
        this.state = State.PARSING;
      }
    } catch (NoSuchElementException e) {
      LOG.debug("'Testwell CTC++' file section not found!");
    }
  }
  
  private void parseUnit(final Map<String, CoverageMeasures> coverageData) {
    LOG.debug(matcher.toString());
    
    if (matcher.usePattern(FILE_HEADER).find(FROM_START)) {
      parseFileUnit(coverageData);
    } else {
      state = State.END;
      scanner.close();
    }
  }
  
  private void parseFileUnit(final Map<String, CoverageMeasures> coverageData) {
    LOG.debug("Parsing file section...");
    
    String normalFilename;
    String filename;
    File file;
    
    filename = matcher.group(1);
    if((new File(filename)).isAbsolute() == false) {
      normalFilename = FilenameUtils.normalize("./" + filename);
    } else {
      normalFilename = FilenameUtils.normalize(filename);
    }
    file = new File(normalFilename);
    addLines(file, coverageData);
    matcher.reset(scanner.next());
  }
  
  private void addLines(File file, final Map<String, CoverageMeasures> coverageData) {
    LOG.debug("Parsing function sections...");

    CoverageMeasures coverageMeasures = CoverageMeasures.create();
    while (!matcher.reset(scanner.next()).usePattern(FILE_RESULT).find()) {
      parseLineSection(coverageMeasures);
    }
    coverageData.put(file.getPath(), coverageMeasures);
  }
  
  private void parseLineSection(CoverageMeasures coverageMeasures) {
    LOG.debug("Found line section...");
    
    if (matcher.usePattern(LINE_RESULT).find(FROM_START)) {
      addEachLine(coverageMeasures);
    } else {
      LOG.warn("Neither File Result nor Line Result after FileHeader!");
    }
  }
  
  private void addEachLine(CoverageMeasures coverageMeasures) {

    int lineIdCur;
    int lineIdPrev;
    int lineIdNext;
    int lineHits;
    int conditions;
    int coveredConditions;
    int lineIdCond;
    int lineHitsTrue;
    int lineHitsFalse;
    boolean conditionDetected;
    
    lineIdPrev = 0;
    conditions = 0;
    coveredConditions = 0;
    lineIdCond = 0;
    conditionDetected = false;
    lineHits = 0;
    
    do {
      lineIdCur = Integer.parseInt(matcher.group(LINE_NR_GROUP));
     
      String condsTrue = matcher.group(CONDS_TRUE);
      String condsFalse = matcher.group(CONDS_FALSE);
      
      if ((condsTrue != null) || (condsFalse != null)) {
        
        lineHitsTrue = (condsTrue != null ? new BigDecimal(condsTrue).intValue() : 0);
        lineHitsFalse = (condsFalse != null ? new BigDecimal(condsFalse).intValue() : 0);
        lineHits = lineHitsTrue + lineHitsFalse;
        
        if (lineIdPrev != lineIdCur) {
          coverageMeasures.setHits(lineIdCur, lineHits);
          
          if (lineIdCond > 0) {
            coverageMeasures.setConditions(lineIdCond, conditions, coveredConditions);
            lineIdCond = 0;
            conditions = 0;
            coveredConditions = 0;
            conditionDetected = false;
          }
          
          if ((condsTrue != null) && (condsFalse != null)) {
            // suppose single condition
            lineIdCond = lineIdCur;
            conditions = 2;
            coveredConditions = (lineHitsTrue > 0 ? 1 : 0) + (lineHitsFalse > 0 ? 1 : 0);
            conditionDetected = true;
          }
        } else {
          // multicondition
          
          if (conditionDetected == true) {
            // reset supposed single condition
            conditions = 0;
            coveredConditions = 0;
            conditionDetected = false;
          }
          lineIdCond = lineIdCur;
          conditions++;
          if (lineHits > 0) {
            coveredConditions++;
          }
        }
      } else {
        // Parse information for statement coverage needed in decising the line coverage
        String blockEnd = matcher.group(4);
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

      if (lineIdPrev > 0) {
        lineIdNext = lineIdPrev + 1;
        while (lineIdNext < lineIdCur) {
          coverageMeasures.setHits(lineIdNext, lineHits);
          lineIdNext++;
        }
      }
      lineIdPrev = lineIdCur;
    } while (matcher.find());
  }
}
