/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.squid;

import org.sonar.api.resources.Directory;
import org.sonar.graph.Edge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class DirectoryEdge implements Edge<Directory> {
  private Directory from;
  private Directory to;
  private Set<FileEdge> rootEdges;

  public DirectoryEdge(Directory from, Directory to) {
    this.from = from;
    this.to = to;
    this.rootEdges = new HashSet<FileEdge>();
  }

  public void addRootEdge(FileEdge edge) {
    rootEdges.add(edge);
  }

  public Collection<FileEdge> getRootEdges() {
    return rootEdges;
  }

  public int getWeight() {
    return rootEdges.size();
  }

  public Directory getFrom() {
    return from;
  }

  public Directory getTo() {
    return to;
  }
}
