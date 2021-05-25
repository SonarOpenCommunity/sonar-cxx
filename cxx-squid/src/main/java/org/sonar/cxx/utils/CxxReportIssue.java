/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Issue with one or multiple locations
 */
public class CxxReportIssue {

  private final String ruleId;
  private final List<String> aliasRuleIds = new ArrayList<>();
  private final List<CxxReportLocation> locations = new ArrayList<>();
  private final List<CxxReportLocation> flow = new ArrayList<>();

  public CxxReportIssue(String ruleId, @Nullable String file, @Nullable String line, @Nullable String column,
                        String info) {
    this.ruleId = ruleId;
    addLocation(file, line, column, info);
  }

  public final void addLocation(@Nullable String file, @Nullable String line, @Nullable String column, String info) {
    locations.add(new CxxReportLocation(file, line, column, info));
  }

  public final void addFlowElement(@Nullable String file, @Nullable String line, @Nullable String column, String info) {
    flow.add(0, new CxxReportLocation(file, line, column, info));
  }

  public final void addAliasRuleId(String aliasRuleId) {
    aliasRuleIds.add(aliasRuleId);
  }

  public String getRuleId() {
    return ruleId;
  }

  public boolean hasAliasRuleIds() {
    return !aliasRuleIds.isEmpty();
  }

  public List<String> getAliasRuleIds() {
    return Collections.unmodifiableList(aliasRuleIds);
  }

  public List<CxxReportLocation> getLocations() {
    return Collections.unmodifiableList(locations);
  }

  public List<CxxReportLocation> getFlow() {
    return Collections.unmodifiableList(flow);
  }

  @Override
  public String toString() {
    String aliasIds = aliasRuleIds.stream().map(Object::toString).collect(Collectors.joining(", "));
    String locationsToString = locations.stream().map(Object::toString).collect(Collectors.joining(", "));
    String flowToString = flow.stream().map(Object::toString).collect(Collectors.joining(", "));
    return "CxxReportIssue [ruleId=" + ruleId
             + ", aliasIds=" + aliasIds
             + ", locations=" + locationsToString
             + ", flow=" + flowToString + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleId, aliasRuleIds, locations, flow);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    var other = (CxxReportIssue) obj;
    return Objects.equals(ruleId, other.ruleId)
             && Objects.equals(aliasRuleIds, other.aliasRuleIds)
             && Objects.equals(locations, other.locations)
             && Objects.equals(flow, other.flow);
  }

}
