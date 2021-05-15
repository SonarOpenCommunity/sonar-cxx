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
package org.sonar.cxx.squidbridge.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import java.util.Set;

public abstract class AbstractMagicCheck<G extends Grammar> extends SquidCheck<G> {

  private AstNodeType[] inclusions;
  private AstNodeType[] exclusions;

  private int inclusionLevel;
  private int exclusionLevel;

  public abstract Set<AstNodeType> getPatterns();

  public abstract Set<AstNodeType> getInclusions();

  public abstract Set<AstNodeType> getExclusions();

  public abstract String getMessage();

  public abstract boolean isExcepted(AstNode candidate);

  @Override
  public void visitFile(AstNode fileNode) {
    inclusionLevel = 0;
    exclusionLevel = 0;
  }

  @Override
  public void init() {
    Set<AstNodeType> patternsSet = getPatterns();
    AstNodeType[] patterns = patternsSet.toArray(new AstNodeType[patternsSet.size()]);

    Set<AstNodeType> inclusionsSet = getInclusions();
    inclusions = inclusionsSet.toArray(new AstNodeType[inclusionsSet.size()]);

    Set<AstNodeType> exclusionsSet = getExclusions();
    exclusions = exclusionsSet.toArray(new AstNodeType[exclusionsSet.size()]);

    subscribeTo(patterns);
    subscribeTo(inclusions);
    subscribeTo(exclusions);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(inclusions)) {
      inclusionLevel++;
    } else if (astNode.is(exclusions)) {
      exclusionLevel++;
    } else if ((inclusions.length == 0 || inclusionLevel > 0) && exclusionLevel == 0 && !isExcepted(astNode)) {
      getContext().createLineViolation(this, getMessage(), astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(inclusions)) {
      inclusionLevel--;
    } else if (astNode.is(exclusions)) {
      exclusionLevel--;
    }
  }

}
