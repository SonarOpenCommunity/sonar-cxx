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

import org.junit.Test;

public class CppMethodArgumentTest {

  @Test
  public void getNameTest() {
    CppMethodArgument arg = new CppMethodArgument("var", "int");
    assertEquals("var", arg.getName());
  }
  
  @Test
  public void getTypeTest() {
    CppMethodArgument arg = new CppMethodArgument("var", "int");
    assertEquals("int", arg.getType());
  }
  
  @Test
  public void getFullNameTest() {
    CppMethodArgument arg = new CppMethodArgument("var", "int");
    assertEquals("var:int", arg.getFullName());
  }
  
  @Test
  public void equalsTest() {
    CppMethodArgument arg1 = new CppMethodArgument("var", "int");
    CppMethodArgument arg2 = new CppMethodArgument("var", "int");
    CppMethodArgument arg3 = new CppMethodArgument("var", "Int");
    
    assertTrue(arg1.equals(arg2));
    assertFalse(arg1.equals(arg3));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenEmptyNameTest() {
    new CppMethodArgument("", "int");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenNullNameTest() {
    new CppMethodArgument(null, "int");
  }  
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenEmptyTypeTest() {
    new CppMethodArgument("var", "");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenNullTypeTest() {
    new CppMethodArgument("var", null);
  }  
  
  
}
