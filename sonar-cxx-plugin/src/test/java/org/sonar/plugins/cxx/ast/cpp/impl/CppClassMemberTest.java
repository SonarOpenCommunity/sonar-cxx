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
import org.sonar.plugins.cxx.ast.cpp.impl.internal.CommonType;

public class CppClassMemberTest {

  @Test
  public void getNameTest() {
    CommonType member = new CppClassMember("member", "int");
    assertEquals("member", member.getName());
  }
  
  @Test
  public void getFullNameTest() {
    CommonType member = new CppClassMember("member", "int");
    assertEquals("member:int", member.getFullName());
  }
  
  @Test
  public void getTypeTest() {
    CommonType member = new CppClassMember("member", "int");
    assertEquals("int", member.getType());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void invalidConstructorNameArgument() {
    new CppClassMember("", "int");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void nullConstructorNameArgument() {
    new CppClassMember(null, "int");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void invalidConstructorTypeArgument() {
    new CppClassMember("member", "");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void nullConstructorTypeArgument() {
    new CppClassMember("member", null);
  }
  
  @Test
  public void equalsTest() {
    CommonType member1 = new CppClassMember("member1", "int");
    CommonType member2 = new CppClassMember("member1", "int");
    CommonType member3 = new CppClassMember("member2", "int");
    CommonType member4 = new CppClassMember("member2", "float");
    CommonType member5 = new CppClassMember("member2", "Float");
    
    assertTrue(member1.equals(member2));
    assertFalse(member1.equals(member3));
    assertFalse(member1.equals(member4));
    assertFalse(member3.equals(member4));
    assertFalse(member4.equals(member5));
  }
  
}
