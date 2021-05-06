/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.metrics;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import java.util.Collection;
import java.util.Set;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.measures.MetricDef;

public final class ComplexityVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private final MetricDef metric;
  private final Set<AstNodeType> astNodeTypes;
  private final Set<AstNodeType> exclusionAstNodeTypes;

  public static final class Builder<G extends Grammar> {

    private MetricDef metric;
    private Set<AstNodeType> astNodeTypes = Sets.newHashSet();
    private Set<AstNodeType> exclusionAstNodeTypes = Sets.newHashSet();

    private Builder() {
    }

    public Builder<G> setMetricDef(MetricDef metric) {
      this.metric = metric;
      return this;
    }

    public Builder<G> subscribeTo(AstNodeType... astNodeTypes) {
      for (AstNodeType astNodeType : astNodeTypes) {
        this.astNodeTypes.add(astNodeType);
      }
      return this;
    }

    public Builder<G> subscribeTo(Collection<AstNodeType> astNodeTypes) {
      this.astNodeTypes = Sets.newHashSet(astNodeTypes);
      return this;
    }

    public Builder<G> setExclusions(Collection<AstNodeType> exclusionAstNodeTypes) {
      this.exclusionAstNodeTypes = Sets.newHashSet(exclusionAstNodeTypes);
      return this;
    }

    public Builder<G> addExclusions(AstNodeType... exclusionAstNodeTypes) {
      for (AstNodeType exclusionAstNodeType : exclusionAstNodeTypes) {
        this.exclusionAstNodeTypes.add(exclusionAstNodeType);
      }
      return this;
    }

    public ComplexityVisitor<G> build() {
      return new ComplexityVisitor<G>(this);
    }

  }

  private ComplexityVisitor(Builder<G> builder) {
    this.metric = builder.metric;
    this.astNodeTypes = ImmutableSet.copyOf(builder.astNodeTypes);
    this.exclusionAstNodeTypes = ImmutableSet.copyOf(builder.exclusionAstNodeTypes);
  }

  public static <G extends Grammar> Builder<G> builder() {
    return new Builder<G>();
  }

  @Override
  public void init() {
    for (AstNodeType astNodeType : astNodeTypes) {
      subscribeTo(astNodeType);
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    for (AstNodeType exclusionAstNodeType : exclusionAstNodeTypes) {
      if (astNode.hasAncestor(exclusionAstNodeType)) {
        return;
      }
    }
    getContext().peekSourceCode().add(metric, 1);
  }

}
