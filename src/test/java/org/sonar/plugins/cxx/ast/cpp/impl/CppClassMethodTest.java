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
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;

public class CppClassMethodTest {
  
  private CxxClass sampleClass;
  private CxxClassMethod sampleMethod;
  
  @Before
  public void setup() {
    sampleClass = new CppClass("MyClass");
    sampleMethod = new CppClassMethod(sampleClass, "myMethod");
  }
  
  @Test
  public void getMethodBodyTest() {
    assertEquals(0, sampleMethod.getBody().getDetectedNames().size());
  }
  
  @Test
  public void getNameTest() {
    assertEquals("myMethod", sampleMethod.getName());
  }
    
  @Test
  public void getFullNameTest() {
    assertEquals("global::MyClass::myMethod", sampleMethod.getFullName());
  }
  
  @Test
  public void getArgumentsTest() {
    assertEquals(0, sampleMethod.getArguments().size());
    
    sampleMethod.addArgument( new CppMethodArgument("var", "int") );
    assertEquals(1, sampleMethod.getArguments().size());
    
    sampleMethod.addArgument( new CppMethodArgument("var", "int") );
    assertEquals(2, sampleMethod.getArguments().size());
    
    sampleMethod.addArgument(null);
    assertEquals(2, sampleMethod.getArguments().size());
  }
  
  @Test
  public void getOwnerClassTest() {
    assertEquals(sampleClass, sampleMethod.getOwnerClass());
  }
  
  @Test
  public void equalsTest() {
    CppClassMethod sampleMethod2 = new CppClassMethod(sampleClass, "myMethod");
    assertTrue(sampleMethod.equals(sampleMethod2));
    
    sampleMethod.addArgument( new CppMethodArgument("var", "int") );
    assertFalse(sampleMethod.equals(sampleMethod2));
    
    sampleMethod2.addArgument( new CppMethodArgument("var", "int") );
    assertTrue(sampleMethod.equals(sampleMethod2));
    
    sampleMethod2.getBody().addDetectedName("name");
    assertFalse(sampleMethod.equals(sampleMethod2));
    
    sampleMethod.getBody().addDetectedName("name");
    assertTrue(sampleMethod.equals(sampleMethod2));
    
    sampleMethod2.addArgument( new CppMethodArgument("VAR", "int") );
    assertFalse(sampleMethod.equals(sampleMethod2));
    
    sampleMethod.addArgument( new CppMethodArgument("var", "int") );
    assertFalse(sampleMethod.equals(sampleMethod2));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenNullNameTest() {
    new CppClassMethod(sampleClass, null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenEmptyNameTest() {
    new CppClassMethod(sampleClass, "");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenEmptyWithSpacesNameTest() {
    new CppClassMethod(sampleClass, "   ");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenNullOwnerClassTest() {
    new CppClassMethod(null, "myMethod");
  }  

}
