/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.cohesion.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Graph {
 
  private Set<Node> nodes = new HashSet<Node>();
  private Set<Edge> edges = new HashSet<Edge>();
  private Map<Node, Set<Edge> > edgeCache = new HashMap<Node, Set<Edge> >();
  
  public Set<Node> getNodes() {
    return nodes;
  }

  public void addNode(Node node) {
    nodes.add(node);
  }

  public void addEdge(Edge edge) {
    if(!nodes.contains(edge.getNodeA()) || !nodes.contains(edge.getNodeB())) {
      throw new IllegalStateException("Can't add edge when nodes are outside of graph.");
    }

    edges.add(edge);
    putEdgeToCache(edge);
  }

  private void putEdgeToCache(Edge edge) {
    if(!edgeCache.containsKey(edge.getNodeA()) ) {
      edgeCache.put(edge.getNodeA(), new HashSet<Edge>() );
    }
    edgeCache.get(edge.getNodeA()).add(edge);
    
    if(!edgeCache.containsKey(edge.getNodeB()) ) {
      edgeCache.put(edge.getNodeB(), new HashSet<Edge>() );
    }
    edgeCache.get(edge.getNodeB()).add(edge);
  }

  public Set<Edge> getEdges() {
    return edges;
  }
  
  public Set<Edge> getEdgesFromNode(Node node) {
    if(edgeCache.containsKey(node)) {
      return edgeCache.get(node);
    }
    return new HashSet<Edge>();
  }
  
  public Edge getEdgeBetween(Node n1, Node n2) {
    Edge result = new Edge(n1, n2);
    if(edges.contains(result)) {
      return result;
    }
    return null;
  }

  public Set<Node> visitAllNodesFrom(Node startNode) {
    Set<Node> visited = new HashSet<Node>();
    Queue<Node> todo = new LinkedList<Node>();
    
    todo.add(startNode);
    while(!todo.isEmpty()) {
      Node currentNode = todo.poll();
      if(canVisit(visited, currentNode)) {
        visited.add(currentNode);
        Set<Edge> nodesEdges = getEdgesFromNode(currentNode);
        Iterator<Edge> it = nodesEdges.iterator();
        while(it.hasNext()) {
          Edge edge = it.next();
          Node node = edge.getOtherNode(currentNode); 
          if(!visited.contains(node)) {
            todo.add(node);
          }
        }
      }
    }
    return visited;
  }

  private boolean canVisit(Set<Node> visited, Node currentNode) {
    return currentNode != null && !visited.contains(currentNode);
  }

}
