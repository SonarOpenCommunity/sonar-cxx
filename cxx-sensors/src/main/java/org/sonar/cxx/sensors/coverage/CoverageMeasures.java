/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jocs
 */
public final class CoverageMeasures {

  private final Map<Integer, CoverageMeasure> lineMeasures = new HashMap<>();

  private CoverageMeasures() {
    // empty
  }

  public static CoverageMeasures create() {
    return new CoverageMeasures();
  }

  public void setHits(int lineId, int hits) {
    lineMeasures.computeIfAbsent(lineId, v -> new CoverageMeasure(lineId));
    var coverageMeasure = lineMeasures.get(lineId);
    coverageMeasure.setHits(hits);
  }

  public void setConditions(int lineId, int totalConditions, int coveredConditions) {
    lineMeasures.computeIfAbsent(lineId, v -> new CoverageMeasure(lineId));
    var coverageMeasure = lineMeasures.get(lineId);
    coverageMeasure.setConditions(totalConditions, coveredConditions);
  }

  Collection<CoverageMeasure> getCoverageMeasures() {
    var measures = new HashMap<Integer, CoverageMeasure>();
    measures.putAll(lineMeasures);
    return measures.values();
  }

  public Set<Integer> getCoveredLines() {
    var coveredLines = new HashSet<Integer>();
    lineMeasures.forEach((Integer key, CoverageMeasure value) -> {
      if (value.getHits() != 0) {
        coveredLines.add(value.getLine());
      }
    });
    return Collections.unmodifiableSet(coveredLines);
  }

  public Set<Integer> getCoveredConditions() {
    var coveredConditionLines = new HashSet<Integer>();
    lineMeasures.forEach((Integer key, CoverageMeasure value) -> {
      if (value.getCoveredConditions() != 0) {
        coveredConditionLines.add(value.getLine());
      }
    });
    return Collections.unmodifiableSet(coveredConditionLines);
  }

}
