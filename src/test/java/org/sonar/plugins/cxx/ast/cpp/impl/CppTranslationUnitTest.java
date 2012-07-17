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

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxNamespace;
import org.sonar.plugins.cxx.ast.cpp.CxxTranslationUnit;
import org.sonar.plugins.cxx.ast.cpp.HasClasses;

public class CppTranslationUnitTest {

  private static final String CLASS_TEST_CPP_PATH = "/org/sonar/plugins/cxx/ast/ClassTest.cpp";
  
  private File cppFile;
  
  @Before
  public void setup() throws URISyntaxException {
    cppFile = TestUtils.loadResource(CLASS_TEST_CPP_PATH);
  }
  
  @Test
  public void addClassTest() {
    CxxNamespace myNamespace = new CppNamespace("MyNamespace");
    CxxClass myClass = new CppClass("test");
    myClass.setNamespace(myNamespace);
    
    HasClasses unit = new CppTranslationUnit(cppFile);
    assertEquals(0, unit.getClasses().size());
    
    unit.addClass( new CppClass() );
    assertEquals(1, unit.getClasses().size());
    
    unit.addClass( new CppClass() );
    assertEquals(1, unit.getClasses().size());
    
    unit.addClass( new CppClass("test") );
    assertEquals(2, unit.getClasses().size());
    
    unit.addClass(myClass);
    assertEquals(3, unit.getClasses().size());
    
  }
  
  @Test
  public void ctorWithFileTest() throws URISyntaxException {
    CxxTranslationUnit unit = new CppTranslationUnit(cppFile);
    assertEquals(cppFile.getAbsolutePath(), unit.getFilename());
  }
  
  @Test
  public void ctorWithFileNameTest() throws URISyntaxException {
    CxxTranslationUnit unit = new CppTranslationUnit(cppFile.getAbsolutePath());
    assertEquals(cppFile.getAbsolutePath(), unit.getFilename());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenFileNameIsInvalidTest() {
    new CppTranslationUnit("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenFileIsInvalidTest() {
    new CppTranslationUnit( new File("test") );
  }
  
}
