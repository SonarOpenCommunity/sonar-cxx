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
package org.sonar.cxx.sensors.utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Issue with one or multiple locations
 */
public class CxxReportIssue {
  final String ruleRepoKey;
  final String ruleId;
  final List<CxxReportLocation> locations;

  public CxxReportIssue(String ruleRepoKey, String ruleId, CxxReportLocation primaryLocation) {
    this(ruleRepoKey, ruleId, Collections.singletonList(primaryLocation));
  }

  public CxxReportIssue(String ruleRepoKey, String ruleId, List<CxxReportLocation> locations) {
    super();
    this.ruleRepoKey = ruleRepoKey;
    this.ruleId = ruleId;
    this.locations = locations;
  }

  public String getRuleRepoKey() {
    return ruleRepoKey;
  }

  public String getRuleId() {
    return ruleId;
  }

  public List<CxxReportLocation> getLocations() {
    return Collections.unmodifiableList(locations);
  }

  @Override
  public String toString() {
    String locationsToString = locations.stream().map(Object::toString).collect(Collectors.joining(", "));
    return "CxxReportIssue [ruleRepoKey=" + ruleRepoKey + ", ruleId=" + ruleId + ", locations=" + locationsToString
        + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(locations, ruleId, ruleRepoKey);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CxxReportIssue other = (CxxReportIssue) obj;
    return Objects.equals(locations, other.locations) && Objects.equals(ruleId, other.ruleId)
        && Objects.equals(ruleRepoKey, other.ruleRepoKey);
  }
}
