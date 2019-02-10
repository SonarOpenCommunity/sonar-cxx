/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.sslr.api.AstNode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe a code scope (function definition, class definition, entire file etc) in terms of complexity sources
 */
public class CxxComplexityScope {

  private final List<CxxComplexitySource> sources;
  private int complexity;
  private int nesting;
  private final int startingLine;

  public CxxComplexityScope(int startingLine) {
    this.sources = new LinkedList<>();
    this.complexity = 0;
    this.nesting = 0;
    this.startingLine = startingLine;
  }

  public void addComplexitySource(AstNode node) {
    sources.add(new CxxComplexitySource(node.getTokenLine(), node.getType(), node.getToken().getType(), 0));
    ++complexity;
  }

  public void addComplexitySourceWithNesting(AstNode node) {
    sources.add(new CxxComplexitySource(node.getTokenLine(), node.getType(), node.getToken().getType(), nesting));
    complexity += (1 + nesting);
  }

  public List<CxxComplexitySource> getSources() {
    return Collections.unmodifiableList(sources);
  }

  public int getComplexity() {
    return complexity;
  }

  public String getStartingLine() {
    return Integer.toString(startingLine);
  }

  public void increaseNesting() {
    ++nesting;
  }

  public void decreaseNesting() {
    --nesting;
  }

}
