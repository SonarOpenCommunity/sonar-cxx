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

import static org.junit.Assert.*;

import org.junit.Test;
import org.sonar.plugins.cxx.ast.cpp.CxxNamespace;

public class CppNamespaceTest {
    
  @Test
  public void getFullNameTest() {
    CxxNamespace namespace = new CppNamespace("MyNamespace");
    CxxNamespace fatherNamespace = new CppNamespace("FatherNamespace");
    CxxNamespace grandfatherNamespace = new CppNamespace("GrandfatherNamespace");
    
    assertEquals("MyNamespace", namespace.getFullName());
    assertEquals("FatherNamespace", fatherNamespace.getFullName());
    assertEquals("GrandfatherNamespace", grandfatherNamespace.getFullName());
    
    namespace.setNamespace(fatherNamespace);
    assertEquals("FatherNamespace::MyNamespace", namespace.getFullName());
    assertEquals("FatherNamespace", fatherNamespace.getFullName());
    assertEquals("GrandfatherNamespace", grandfatherNamespace.getFullName());
    
    fatherNamespace.setNamespace(grandfatherNamespace);
    assertEquals("GrandfatherNamespace::FatherNamespace::MyNamespace", namespace.getFullName());
    assertEquals("GrandfatherNamespace::FatherNamespace", fatherNamespace.getFullName());
    assertEquals("GrandfatherNamespace", grandfatherNamespace.getFullName());
  }
  
  
  @Test
  public void getParentTest() {
    CxxNamespace namespace = new CppNamespace();
    assertEquals(null, namespace.getNamespace());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void setParentTest() {
    CxxNamespace parentNamespace = new CppNamespace("ParentNamespace");
    
    CxxNamespace namespace = new CppNamespace();    
    assertEquals(null, namespace.getNamespace());
    
    namespace.setNamespace(parentNamespace);
    assertEquals(parentNamespace, namespace.getNamespace());
    
    namespace.setNamespace(null);
    assertEquals(null, namespace.getNamespace());
    
    namespace.setNamespace(namespace); //throws
  }
  
  @Test
  public void addClassTest() {
    CxxNamespace namespace = new CppNamespace();    
    assertEquals(0, namespace.getClasses().size());
  
    namespace.addClass( new CppClass() );
    assertEquals(1, namespace.getClasses().size());
    
    namespace.addClass( new CppClass() );
    assertEquals(1, namespace.getClasses().size());
    
    namespace.addClass(new CppClass("MyClass"));
    assertEquals(2, namespace.getClasses().size());
  }
  
  @Test
  public void getNameTest() {
    CxxNamespace defaultNamespace     = new CppNamespace();
    CxxNamespace notNamedNamespace    = new CppNamespace(null);
    CxxNamespace emptyNamedNamespace  = new CppNamespace("  ");
    CxxNamespace namedNamespace       = new CppNamespace("MyNamespace");
    
    assertEquals("MyNamespace", namedNamespace.getName());
    assertEquals(CppNamespace.DEFAULT_NAME, defaultNamespace.getName());
    assertEquals(CppNamespace.DEFAULT_NAME, notNamedNamespace.getName());
    assertEquals(CppNamespace.DEFAULT_NAME, emptyNamedNamespace.getName());
  }
  
  @Test
  public void setNameTest() {
    CxxNamespace namespace = new CppNamespace();
    assertEquals(CppNamespace.DEFAULT_NAME, namespace.getName());
    
    namespace.setName(" MyNamespace ");
    assertEquals("MyNamespace", namespace.getName());
  }
  
  @Test
  public void equalsTest() {
    CxxNamespace namespace1 = new CppNamespace("myNamespace");
    CxxNamespace namespace2 = new CppNamespace(" myNamespace  ");
    CxxNamespace namespace3 = new CppNamespace("mynamespace");
    CxxNamespace namespace4 = new CppNamespace();
    
    assertFalse(namespace1.equals(namespace3));
    assertFalse(namespace1.equals(namespace4));
    assertTrue(namespace1.equals(namespace2));
    assertTrue(new CppNamespace().equals(namespace4));
    
    namespace1.setNamespace(namespace4);
    assertFalse(namespace1.equals(namespace2));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenSettingNullNameTest() {
    CxxNamespace namespace = new CppNamespace();
    namespace.setName(null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenSettingEmptyNameTest() {
    CxxNamespace namespace = new CppNamespace();
    namespace.setName(" ");
  }  
  
  
}
