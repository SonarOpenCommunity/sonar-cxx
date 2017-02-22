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
package org.sonar.plugins.cxx.coverage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jocs
 */
class CoverageMeasures {
  private final Map<Integer, CoverageMeasure> measuresLines = new HashMap<>();
  private final Map<Integer, CoverageMeasure> measuresConditions = new HashMap<>();
  
  private CoverageMeasures() {
    
  }
  
  static CoverageMeasures create() {
    CoverageMeasures measures = new CoverageMeasures();
    return measures;
  }

  void setHits(int lineId, int i) {
    if (measuresLines.containsKey(lineId)) {
      CoverageMeasure existentData = measuresLines.get(lineId);
      existentData.setHits(lineId, i);      
    } else {
      CoverageMeasure newLineHit = new CoverageMeasure(CoverageMeasure.CoverageType.LINE, lineId);
      newLineHit.setHits(lineId, i);
      measuresLines.put(lineId, newLineHit);
    }
  }

  void setConditions(int lineId, int totalConditions, int coveredConditions) {
    if (measuresConditions.containsKey(lineId)) {
      CoverageMeasure existentData = measuresConditions.get(lineId);
      existentData.setConditions(totalConditions, coveredConditions);
    } else {
      CoverageMeasure newLineHit = new CoverageMeasure(CoverageMeasure.CoverageType.CONDITION, lineId);
      newLineHit.setConditions(totalConditions, coveredConditions);
      measuresConditions.put(lineId, newLineHit);
    }
  }

  Collection<CoverageMeasure> getCoverageMeasures() {
    Map<Integer, CoverageMeasure> measures = new HashMap<>();
    measures.putAll(measuresLines);
    measures.putAll(measuresConditions);
    return measures.values();
  }  
}
