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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {

  private Graph graph;
  private Node n1;
  private Node n2;
  private Node n3;
  
  @Before
  public void setup() {
    n1 = new Node("1");
    n2 = new Node("2");
    n3 = new Node("3");
    graph = new Graph();
    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
  }
  
  @Test
  public void getEdgesFromNodeTest() {
    Edge e1 = new Edge(n1, n2);
    Edge e2 = new Edge(n1, n3);
    graph.addEdge(e1);
    graph.addEdge(e2);
    
    assertEquals(2, graph.getEdgesFromNode(n1).size());
    assertEquals(1, graph.getEdgesFromNode(n2).size());
    assertEquals(1, graph.getEdgesFromNode(n3).size());
  }
  
  @Test
  public void visitAllNodesFromTest() {
    graph.addEdge( new Edge(n1, n2) );
    graph.addEdge( new Edge(n1, n3) );
   
    Set<Node> visited = graph.visitAllNodesFrom(n1);
    assertEquals(3, visited.size());
    assertTrue(visited.contains(n1));
    assertTrue(visited.contains(n2));
    assertTrue(visited.contains(n3));
    
    graph.addEdge( new Edge(n2, n3) );
    visited = graph.visitAllNodesFrom(n3);
    assertEquals(3, visited.size());
    assertTrue(visited.contains(n1));
    assertTrue(visited.contains(n2));
    assertTrue(visited.contains(n3));
  }

  @Test
  public void getEdgeBetweenTest() {
    assertEquals(null, graph.getEdgeBetween(n1, n2));
    
    Edge e1 = new Edge(n1, n2);
    graph.addEdge(e1);
    assertEquals(e1, graph.getEdgeBetween(n1, n2));
  }

  @Test
  public void addEdgeTest() {
    

    assertEquals(0, graph.getEdges().size() );

    graph.addEdge( new Edge(n1, n2) );
    assertEquals(1, graph.getEdges().size() );

    graph.addEdge( new Edge(n2, n1) );
    assertEquals(1, graph.getEdges().size() );

    graph.addEdge( new Edge(n1, n3) );
    assertEquals(2, graph.getEdges().size() );
  }

  @Test(expected = IllegalStateException.class)
  public void addEdgeShouldThrowWhenNodeIsInvalidTest() {
    new Graph().addEdge( new Edge(new Node("1"), new Node("2") ) );
  }
  
  @Test
  public void addNodeTest() {
    graph = new Graph();
    assertEquals(0, graph.getNodes().size());

    graph.addNode( new Node("1") );
    assertEquals(1, graph.getNodes().size());

    graph.addNode( new Node("2") );
    assertEquals(2, graph.getNodes().size());

    graph.addNode( new Node("2") );
    assertEquals(2, graph.getNodes().size());
  }

}