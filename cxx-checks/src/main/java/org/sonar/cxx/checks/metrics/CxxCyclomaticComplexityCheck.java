/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.checks.metrics;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.cxx.CxxComplexityConstants;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.visitors.CxxComplexityScope;
import org.sonar.cxx.visitors.MultiLocatitionSquidCheck;

/**
 * This is an enhanced version of org.sonar.cxx.squidbridge.metrics.ComplexityVisitor, which is used in order to compute
 * the Cyclomatic Complexity.
 *
 * @param <G>
 */
public abstract class CxxCyclomaticComplexityCheck<G extends Grammar> extends MultiLocatitionSquidCheck<G> {

  /**
   * Stack for tracking the nested scopes (e.g. declaration of classes can be nested). Complexity of the inner scopes is
   * added to the complexity of outer scopes.
   */
  private Deque<CxxComplexityScope> complexityScopes;

  @Override
  public void init() {
    subscribeTo(CxxComplexityConstants.getCyclomaticComplexityTypes());
    Optional<AstNodeType> scopeType = getScopeType();
    if (scopeType.isPresent()) {
      AstNodeType additionalNode = scopeType.get();
      if (!getAstNodeTypesToVisit().contains(additionalNode)) {
        subscribeTo(additionalNode);
      }
    }

    complexityScopes = new LinkedList<>();
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    if (!getScopeType().isPresent()) {
      complexityScopes.addFirst(new CxxComplexityScope(1));
    }
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    if (!getScopeType().isPresent()) {
      analyzeScopeComplexity();
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.getToken().isGeneratedCode()) {
      return;
    }

    Optional<AstNodeType> scopeType = getScopeType();
    if (scopeType.isPresent() && astNode.is(scopeType.get())) {
      complexityScopes.addFirst(new CxxComplexityScope(astNode.getTokenLine()));
    }

    if (astNode.is(CxxComplexityConstants.getCyclomaticComplexityTypes())) {
      // for nested scopes (e.g. nested classes) the inner classes
      // add complexity to the outer ones
      for (var scope : complexityScopes) {
        scope.addComplexitySource(astNode);
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.getToken().isGeneratedCode()) {
      return;
    }

    Optional<AstNodeType> scopeType = getScopeType();
    if (scopeType.isPresent() && astNode.is(scopeType.get())) {
      analyzeScopeComplexity();
    }
  }

  private void analyzeScopeComplexity() {
    CxxComplexityScope scope = complexityScopes.removeFirst();

    int maxComplexity = getMaxComplexity();
    int currentComplexity = scope.getComplexity();
    if (scope.getComplexity() > maxComplexity) {
      var msg = new StringBuilder(256);
      msg.append("The Cyclomatic Complexity of this ")
        .append(getScopeName()).append(" is ").append(currentComplexity)
        .append(" which is greater than ").append(maxComplexity).append(" authorized.");

      var issue = new CxxReportIssue(getRuleKey(), null, scope.getStartingLine(), null, msg.toString());
      for (var source : scope.getSources()) {
        issue.addLocation(null, source.getLine(), null, source.getExplanation());
      }
      createMultiLocationViolation(issue);
    }
  }

  /**
   * @return the maximum allowed complexity for this scope
   */
  protected abstract int getMaxComplexity();

  /**
   * @return valid AstNodeType if complexity is calculated for some language constructs only (e.g. function definition,
   * class definition etc). Return Optional.empty() if the complexity is calculated for entire file.
   */
  protected abstract Optional<AstNodeType> getScopeType();

  /**
   * @return the name of analyzed scope: "function", "class", "file" etc
   */
  protected abstract String getScopeName();

}
