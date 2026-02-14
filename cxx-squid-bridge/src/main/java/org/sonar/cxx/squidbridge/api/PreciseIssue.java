/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import com.sonar.cxx.sslr.api.AstNode;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Precise issue representation with support for secondary locations and cost tracking.
 *
 * <p>This class provides a builder pattern for creating issues with rich context,
 * including support for secondary locations.
 *
 * <p>Usage example:
 * <pre>
 * PreciseIssue issue = new PreciseIssue(check, node, "Security vulnerability detected")
 *     .secondary(relatedNode, "Related code here")
 *     .withCost(10.0)
 *     .withGap(5.0);
 * </pre>
 */
public class PreciseIssue {

  private final Object check;
  private final AstNode primaryNode;
  private final String message;
  private final List<IssueLocation> secondaryLocations;
  private Double cost;
  private Double gap;

  /**
   * Create a new precise issue.
   *
   * @param check the check raising this issue (typically a SquidCheck instance)
   * @param primaryNode the primary AST node where the issue occurs
   * @param message the issue message
   */
  public PreciseIssue(Object check, AstNode primaryNode, String message) {
    this.check = check;
    this.primaryNode = primaryNode;
    this.message = message;
    this.secondaryLocations = new ArrayList<>();
    this.cost = null;
    this.gap = null;
  }

  /**
   * Create a new precise issue with a primary location.
   *
   * @param check the check raising this issue
   * @param primaryLocation the primary location (node and message)
   */
  public PreciseIssue(Object check, IssueLocation primaryLocation) {
    this.check = check;
    this.primaryNode = primaryLocation.getNode();
    this.message = primaryLocation.getMessage();
    this.secondaryLocations = new ArrayList<>();
    this.cost = null;
    this.gap = null;
  }

  /**
   * Add a secondary location to this issue.
   *
   * @param node the AST node for the secondary location
   * @param secondaryMessage the message for this secondary location
   * @return this issue for chaining
   */
  public PreciseIssue secondary(AstNode node, String secondaryMessage) {
    secondaryLocations.add(new IssueLocation(node, secondaryMessage));
    return this;
  }

  /**
   * Add a secondary location to this issue.
   *
   * @param location the issue location to add as a secondary location
   * @return this issue for chaining
   */
  public PreciseIssue secondary(IssueLocation location) {
    secondaryLocations.add(location);
    return this;
  }

  /**
   * Set the remediation cost for this issue in minutes.
   *
   * @param cost the cost in minutes
   * @return this issue for chaining
   */
  public PreciseIssue withCost(double cost) {
    this.cost = cost;
    return this;
  }

  /**
   * Set the gap for this issue.
   *
   * @param gap the gap value
   * @return this issue for chaining
   */
  public PreciseIssue withGap(double gap) {
    this.gap = gap;
    return this;
  }

  public Object getCheck() {
    return check;
  }

  public AstNode getPrimaryNode() {
    return primaryNode;
  }

  public String getMessage() {
    return message;
  }

  /**
   * Get the primary location of this issue.
   *
   * @return the primary location
   */
  public IssueLocation getPrimaryLocation() {
    return new IssueLocation(primaryNode, message);
  }

  /**
   * Get all secondary locations for this issue.
   *
   * @return list of secondary locations (defensive copy)
   */
  public List<IssueLocation> getSecondaryLocations() {
    return new ArrayList<>(secondaryLocations);
  }

  @Nullable
  public Double getCost() {
    return cost;
  }

  @Nullable
  public Double getGap() {
    return gap;
  }

  /**
   * Check if this issue has secondary locations.
   *
   * @return true if there are secondary locations
   */
  public boolean hasSecondaryLocations() {
    return !secondaryLocations.isEmpty();
  }

  /**
   * Represents a location (primary or secondary) for an issue.
   */
  public static class IssueLocation {
    private final AstNode node;
    private final String message;

    public IssueLocation(AstNode node, String message) {
      this.node = node;
      this.message = message;
    }

    public AstNode getNode() {
      return node;
    }

    public String getMessage() {
      return message;
    }
  }
}
