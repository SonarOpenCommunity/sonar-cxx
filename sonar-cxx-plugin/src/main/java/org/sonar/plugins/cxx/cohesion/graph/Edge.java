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
package org.sonar.plugins.cxx.cohesion.graph;


public class Edge {

  private Node node1;
  private Node node2;
  
  public Edge(Node node1, Node node2) {
    validateNodes(node1, node2);
    this.node1 = node1;
    this.node2 = node2;
  }
  
  private void validateNodes(Node n1, Node n2) {
    if(n1 == null || n2 == null || n1.equals(n2)) {
      throw new IllegalArgumentException("Nodes in edge can't be null or equal'");
    }
  }

  public Node getNodeA() {
    return node1;
  }
  
  public Node getNodeB() {
    return node2;
  }
  
  public Node getOtherNode(Node node) {
    if(node1.equals(node)) {
      return node2;
    }
    return node1;
  }
  
  public boolean equals(Object o) {
    if(o == this) {
      return true;
    }
    if(o instanceof Edge) {
      Edge other = (Edge)o;
      boolean ok1 = other.node1.equals(node1) || other.node2.equals(node1);
      boolean ok2 = other.node1.equals(node2) || other.node2.equals(node2);
      return ok1 && ok2;
    }
    return false;
  }
  
  public int hashCode() {
    return node1.hashCode() + node2.hashCode();
  }
  

}