/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.coverage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.PropertiesBuilder;

public class FileData {
  private int lines = 0;
  private int conditions = 0;
  private int coveredLines = 0;
  private int coveredConditions = 0;

  private String fileName;
  private PropertiesBuilder<String, Integer> lineHitsBuilder =
    new PropertiesBuilder<String, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
  private PropertiesBuilder<String, String> branchHitsBuilder =
    new PropertiesBuilder<String, String>(CoreMetrics.BRANCH_COVERAGE_HITS_DATA);

  public void addLine(String lineId, int lineHits) {
    lines++;
    if (lineHits > 0) {
      coveredLines++;
    }
    Map<String, Integer> props = lineHitsBuilder.getProps();
    if (props.containsKey(lineId)) {
      props.put(lineId, props.get(lineId) + lineHits);
    } else {
      lineHitsBuilder.add(lineId, lineHits);
    }
  }

  public void addConditionLine(String lineId, int coveredConditions, int conditions, String label) {
    this.conditions += conditions;
    this.coveredConditions += coveredConditions;
    Map<String, String> props = branchHitsBuilder.getProps();
    if (props.containsKey(lineId)) {
      props.put(lineId, props.get(lineId) + ", " + label);
    } else {
      branchHitsBuilder.add(lineId, label);
    }
  }

  public FileData(String fileName) {
    this.fileName = fileName;
  }

  public List<Measure> getMeasures() {
    List<Measure> measures = new ArrayList<Measure>();
    if (lines > 0) {
      measures.add(new Measure(CoreMetrics.LINES_TO_COVER, (double) lines));
      measures.add(new Measure(CoreMetrics.UNCOVERED_LINES, (double) lines - coveredLines));
      measures.add(lineHitsBuilder.build().setPersistenceMode(PersistenceMode.DATABASE));

      if (conditions > 0) {
	measures.add(new Measure(CoreMetrics.CONDITIONS_TO_COVER, (double) conditions));
	measures.add(new Measure(CoreMetrics.UNCOVERED_CONDITIONS, (double) conditions - coveredConditions));
	measures.add(branchHitsBuilder.build().setPersistenceMode(PersistenceMode.DATABASE));
      }
    }
    return measures;
  }

  public String getFileName() {
    return fileName;
  }
}
