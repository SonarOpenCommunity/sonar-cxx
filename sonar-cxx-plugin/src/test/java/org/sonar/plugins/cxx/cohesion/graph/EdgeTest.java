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
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

public class EdgeTest {

  private Edge edge1;
  private Edge edge2;
  private Edge edge3;
  
  private Node n1;
  private Node n2;
  private Node n3;
  
  @Before
  public void setup() {
    n1 = new Node("1");
    n2 = new Node("2");
    n3 = new Node("3");
    
    edge1 = new Edge(n1, n2);
    edge2 = new Edge(n2, n3);
    edge3 = new Edge(n2, n1);
  }
  
  @Test
  public void getNodeTest() {
    assertEquals(n1, edge1.getNodeA());
    assertEquals(n2, edge1.getNodeB());
    assertEquals(n2, edge2.getNodeA());
    assertEquals(n3, edge2.getNodeB());
    assertEquals(n2, edge3.getNodeA());
    assertEquals(n1, edge3.getNodeB());
    
  }
  
  @Test
  public void equalsTest() {
    assertEquals(edge1, edge3);
    assertEquals(edge3, edge1);
    assertNotSame(edge1, edge2);
    assertNotSame(edge3, edge2);
  }
  
  @Test
  public void hashCodeTest() {
    assertEquals(edge1.hashCode(), edge3.hashCode());
    assertNotSame(edge1.hashCode(), edge2.hashCode());
    assertNotSame(edge3.hashCode(), edge2.hashCode());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenMakingCircularReferenceEdgeTest() {
    new Edge( new Node("1"), new Node("1") );
  }
  
}
