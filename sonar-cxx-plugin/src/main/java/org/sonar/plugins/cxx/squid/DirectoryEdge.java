/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.squid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sonar.api.resources.Directory;
import org.sonar.graph.Edge;

class DirectoryEdge implements Edge<Directory> {

  private final Directory from;
  private final Directory to;
  private final Set<FileEdge> rootEdges;

  public DirectoryEdge(Directory from, Directory to) {
    this.from = from;
    this.to = to;
    this.rootEdges = new HashSet<>();
  }

  public void addRootEdge(FileEdge edge) {
    rootEdges.add(edge);
  }

  public Collection<FileEdge> getRootEdges() {
    return rootEdges;
  }

  @Override
  public int getWeight() {
    return rootEdges.size();
  }

  @Override
  public Directory getFrom() {
    return from;
  }

  @Override
  public Directory getTo() {
    return to;
  }
}
