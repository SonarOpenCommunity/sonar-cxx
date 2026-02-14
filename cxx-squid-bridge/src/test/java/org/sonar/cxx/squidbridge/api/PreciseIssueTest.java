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
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

import static org.assertj.core.api.Assertions.assertThat;

class PreciseIssueTest {

  private static class TestCheck extends SquidCheck<Grammar> {
  }

  @Test
  void testBasicIssueCreation() {
    var check = new TestCheck();
    var node = createMockNode();

    var issue = new PreciseIssue(check, node, "Test message");

    assertThat(issue.getCheck()).isEqualTo(check);
    assertThat(issue.getPrimaryNode()).isEqualTo(node);
    assertThat(issue.getMessage()).isEqualTo("Test message");
    assertThat(issue.hasSecondaryLocations()).isFalse();
    assertThat(issue.getCost()).isNull();
    assertThat(issue.getGap()).isNull();
  }

  @Test
  void testIssueCreationWithPrimaryLocation() {
    var check = new TestCheck();
    var node = createMockNode();
    var location = new PreciseIssue.IssueLocation(node, "Primary message");

    var issue = new PreciseIssue(check, location);

    assertThat(issue.getCheck()).isEqualTo(check);
    assertThat(issue.getPrimaryNode()).isEqualTo(node);
    assertThat(issue.getMessage()).isEqualTo("Primary message");
  }

  @Test
  void testPrimaryLocation() {
    var check = new TestCheck();
    var node = createMockNode();

    var issue = new PreciseIssue(check, node, "Test message");
    var primaryLocation = issue.getPrimaryLocation();

    assertThat(primaryLocation).isNotNull();
    assertThat(primaryLocation.getNode()).isEqualTo(node);
    assertThat(primaryLocation.getMessage()).isEqualTo("Test message");
  }

  @Test
  void testSecondaryLocationWithNodeAndMessage() {
    var check = new TestCheck();
    var primaryNode = createMockNode();
    var secondaryNode = createMockNode();

    var issue = new PreciseIssue(check, primaryNode, "Primary message")
        .secondary(secondaryNode, "Secondary message");

    assertThat(issue.hasSecondaryLocations()).isTrue();
    assertThat(issue.getSecondaryLocations()).hasSize(1);
    assertThat(issue.getSecondaryLocations().get(0).getNode()).isEqualTo(secondaryNode);
    assertThat(issue.getSecondaryLocations().get(0).getMessage()).isEqualTo("Secondary message");
  }

  @Test
  void testSecondaryLocationWithIssueLocation() {
    var check = new TestCheck();
    var primaryNode = createMockNode();
    var secondaryNode = createMockNode();
    var secondaryLocation = new PreciseIssue.IssueLocation(secondaryNode, "Secondary message");

    var issue = new PreciseIssue(check, primaryNode, "Primary message")
        .secondary(secondaryLocation);

    assertThat(issue.hasSecondaryLocations()).isTrue();
    assertThat(issue.getSecondaryLocations()).hasSize(1);
    assertThat(issue.getSecondaryLocations().get(0)).isEqualTo(secondaryLocation);
  }

  @Test
  void testMultipleSecondaryLocations() {
    var check = new TestCheck();
    var primaryNode = createMockNode();
    var secondary1 = createMockNode();
    var secondary2 = createMockNode();
    var secondary3 = createMockNode();

    var issue = new PreciseIssue(check, primaryNode, "Primary message")
        .secondary(secondary1, "Secondary 1")
        .secondary(secondary2, "Secondary 2")
        .secondary(new PreciseIssue.IssueLocation(secondary3, "Secondary 3"));

    assertThat(issue.hasSecondaryLocations()).isTrue();
    assertThat(issue.getSecondaryLocations()).hasSize(3);
  }

  @Test
  void testWithCost() {
    var check = new TestCheck();
    var node = createMockNode();

    var issue = new PreciseIssue(check, node, "Test message")
        .withCost(15.5);

    assertThat(issue.getCost()).isEqualTo(15.5);
  }

  @Test
  void testWithGap() {
    var check = new TestCheck();
    var node = createMockNode();

    var issue = new PreciseIssue(check, node, "Test message")
        .withGap(3.0);

    assertThat(issue.getGap()).isEqualTo(3.0);
  }

  @Test
  void testFluentInterface() {
    var check = new TestCheck();
    var primaryNode = createMockNode();
    var secondary1 = createMockNode();
    var secondary2 = createMockNode();

    var issue = new PreciseIssue(check, primaryNode, "Primary message")
        .secondary(secondary1, "Secondary 1")
        .withCost(10.0)
        .secondary(secondary2, "Secondary 2")
        .withGap(2.0);

    assertThat(issue.getSecondaryLocations()).hasSize(2);
    assertThat(issue.getCost()).isEqualTo(10.0);
    assertThat(issue.getGap()).isEqualTo(2.0);
  }

  @Test
  void testIssueLocationCreation() {
    var node = createMockNode();
    var location = new PreciseIssue.IssueLocation(node, "Location message");

    assertThat(location.getNode()).isEqualTo(node);
    assertThat(location.getMessage()).isEqualTo("Location message");
  }

  @Test
  void testSecondaryLocationsListIsImmutable() {
    var check = new TestCheck();
    var primaryNode = createMockNode();
    var secondaryNode = createMockNode();

    var issue = new PreciseIssue(check, primaryNode, "Primary message")
        .secondary(secondaryNode, "Secondary message");

    var locations = issue.getSecondaryLocations();
    assertThat(locations).hasSize(1);

    // Verify that modifying the returned list doesn't affect the issue
    locations.clear();
    assertThat(issue.getSecondaryLocations()).hasSize(1);
  }

  private AstNode createMockNode() {
    var token = Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue("test")
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();

    return new AstNode(token);
  }

  private static class TestTokenType implements com.sonar.cxx.sslr.api.TokenType {
    @Override
    public String getName() {
      return "TEST";
    }

    @Override
    public String getValue() {
      return "test";
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }
  }
}
