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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
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

  static CoverageMeasures create() {
    return new CoverageMeasures();
  }

  void setHits(int lineId, int hits) {
    lineMeasures.computeIfAbsent(lineId, v -> new CoverageMeasure(lineId));
    CoverageMeasure coverageMeasure = lineMeasures.get(lineId);
    coverageMeasure.setHits(hits);
  }

  void setConditions(int lineId, int totalConditions, int coveredConditions) {
    lineMeasures.computeIfAbsent(lineId, v -> new CoverageMeasure(lineId));
    CoverageMeasure coverageMeasure = lineMeasures.get(lineId);
    coverageMeasure.setConditions(totalConditions, coveredConditions);
  }

  Collection<CoverageMeasure> getCoverageMeasures() {
    Map<Integer, CoverageMeasure> measures = new HashMap<>();
    measures.putAll(lineMeasures);
    return measures.values();
  }

  Set<Integer> getCoveredLines() {
    Set<Integer> coveredLines = Sets.newHashSet();
    lineMeasures.forEach((key, value) -> {
      if (value.getHits() != 0) {
        coveredLines.add(value.getLine());
      }
    });
    return ImmutableSet.copyOf(coveredLines);
  }

  Set<Integer> getCoveredConditions() {
    Set<Integer> coveredConditionLines = Sets.newHashSet();
    lineMeasures.forEach((key, value) -> {
      if (value.getCoveredConditions() != 0) {
        coveredConditionLines.add(value.getLine());
      }
    });
    return ImmutableSet.copyOf(coveredConditionLines);
  }
}
