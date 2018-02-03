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

/**
 *
 * @author jocs
 */
class CoverageMeasure {

  private final int line;
  private int hits;
  private int conditions;
  private int coveredConditions;

  CoverageMeasure(int line) {
    this.line = line;
  }

  int getHits() {
    return this.hits;
  }

  int getConditions() {
    return this.conditions;
  }

  int getCoveredConditions() {
    return this.coveredConditions;
  }

  void setHits(int hits) {
    this.hits += hits;
  }

  void setConditions(int totalConditions, int coveredConditions) {
    this.coveredConditions = coveredConditions;
    this.conditions = totalConditions;
  }

  int getLine() {
    return this.line;
  }
}
