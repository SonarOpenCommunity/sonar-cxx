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
package org.sonar.plugins.cxx.ast.cpp.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.cxx.ast.cpp.CxxMethodBody;

public class CppMethodBodyTest {

  CxxMethodBody body;
  
  @Before
  public void setup() {
    body = new CppMethodBody();
  }
  
  @Test
  public void getDetectedNamesTest() {
    assertEquals(0, body.getDetectedNames().size() );
  }
  
  @Test
  public void addDetectedNameTest() {
    assertEquals(0, body.getDetectedNames().size() );
    
    body.addDetectedName("name");
    assertEquals(1, body.getDetectedNames().size() );
    
    body.addDetectedName(null);
    assertEquals(1, body.getDetectedNames().size() );
    
    body.addDetectedName(" ");
    assertEquals(1, body.getDetectedNames().size() );
    
    body.addDetectedName("name2");
    assertEquals(2, body.getDetectedNames().size() );
  }
  
  @Test
  public void equalsTest() {
    CxxMethodBody body2 = new CppMethodBody();
    assertTrue(body.equals(body2));
    
    body2.addDetectedName("name");
    assertFalse(body.equals(body2));
    
    body.addDetectedName("name");
    assertTrue(body.equals(body2));
    
    body.addDetectedName("Name2");
    assertFalse(body.equals(body2));
    
    body.addDetectedName("name2");
    assertFalse(body.equals(body2));
  }
  
}
