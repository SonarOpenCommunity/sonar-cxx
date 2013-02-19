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

import org.junit.Test;

public class NodeTest {
    
  @Test
  public void getInnerObjectTest() {
    String node1Object = "test";
    Integer node2Object = Integer.valueOf(5);
    Node node1 = new Node(node1Object);
    Node node2 = new Node(node2Object);
    
    assertEquals(node1Object, node1.getInnerObject());
    assertEquals(node2Object, node2.getInnerObject());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void getInnerObjectShouldThrowExceptionTest() {
    Node node = new Node(null);
    node.getInnerObject();
  }
  
  @Test
  public void equalsTest() {
    Node node1 = new Node("test1");
    Node node2 = new Node("test2");
    Node node3 = new Node("test1");

    assertEquals(node1, node1);
    assertEquals(node1, node3);
    assertEquals(node3, node1);
    assertNotSame(node1, node2);
    assertNotSame(node2, node1);
    assertNotSame(node3, node2);
    assertNotSame(node2, node3);
  }
  
  @Test
  public void hashCodeTest() {
    Node node1 = new Node("test1");
    Node node2 = new Node("test2");
    Node node3 = new Node("test1");
    
    assertEquals(node1.hashCode(), node3.hashCode());
    assertNotSame(node1.hashCode(), node2.hashCode());
    assertNotSame(node2.hashCode(), node3.hashCode());
  }
  
}
