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
package org.sonar.plugins.cxx.ast.visitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.api.resources.InputFile;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.ast.CxxCppParser;
import org.sonar.plugins.cxx.ast.CxxCppParserException;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMember;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.ast.cpp.impl.CppClass;
import org.sonar.plugins.cxx.ast.cpp.impl.CppClassMember;
import org.sonar.plugins.cxx.ast.cpp.impl.CppClassMethod;
import org.sonar.plugins.cxx.ast.cpp.impl.CppMethodArgument;

public class CxxCppTranslationUnitVisitorTest {
  
  private static final String                 TEST_FILE             = "/org/sonar/plugins/cxx/ast/VisitorTest.cpp";
  private static final int                    TEST_FILE_CLASS_COUNT = 4;
  private static final Map<String, CxxClass>  TEST_CLASSES          = new HashMap<String, CxxClass>();
  
  private CxxCppParser parser;
  private InputFile inputFile;
    
  @BeforeClass
  static public void setupTestClasses() {
    CxxClass myStruct = new CppClass("MyStruct");
    myStruct.addMember( new CppClassMember("i", "int") );
    myStruct.addMember( new CppClassMember("f", "float") );
    
    CxxClass firstClass = new CppClass("FirstClass");
    firstClass.addMember( new CppClassMember("member1", "int") );
    firstClass.addMember( new CppClassMember("member2", "float") );
    firstClass.addMember( new CppClassMember("member3", "MyStruct") );
    firstClass.addMethod( new CppClassMethod(firstClass, "calculate") );
    CxxClassMethod getMember = new CppClassMethod(firstClass, "getMember");
    getMember.getBody().addDetectedName("member1");
    firstClass.addMethod(getMember);
    
    CxxClassMethod setMember = new CppClassMethod(firstClass, "setMember");
    setMember.addArgument( new CppMethodArgument("value", "int") );
    setMember.getBody().addDetectedName("member1").addDetectedName("value");
    firstClass.addMethod(setMember);
    
    CxxClass secondClass = new CppClass("SecondClass");
    secondClass.addMember( new CppClassMember("member4", "double") );
    CxxClassMethod doSomething = new CppClassMethod(secondClass, "doSomething");
    doSomething.addArgument( new CppMethodArgument("a","int") );
    doSomething.addArgument( new CppMethodArgument("b","double") );
    doSomething.addArgument( new CppMethodArgument("c","float") );
    doSomething.addArgument( new CppMethodArgument("d","MyStruct") );
    doSomething.getBody().addDetectedName("member4");
    secondClass.addMethod(doSomething);
    
    CxxClass thirdClass = new CppClass("ThirdClass");
    CxxClassMethod calculate = new CppClassMethod(thirdClass, "calculate");
    CxxClassMethod ctor = new CppClassMethod(thirdClass, "ThirdClass");
    calculate.getBody().addDetectedName("member1").addDetectedName("member2");
    ctor.getBody().addDetectedName("member1").addDetectedName("member2").addDetectedName("member3").addDetectedName("member4")
                  .addDetectedName("i").addDetectedName("f");
    
    thirdClass.addMethod(ctor);
    thirdClass.addMethod( new CppClassMethod(thirdClass, "~ThirdClass") );
    thirdClass.addMethod(calculate);
    
    thirdClass.addAncestor(firstClass);
    thirdClass.addAncestor(secondClass);
    
    TEST_CLASSES.put(myStruct.getFullName(), myStruct);
    TEST_CLASSES.put(firstClass.getFullName(), firstClass);
    TEST_CLASSES.put(secondClass.getFullName(), secondClass);
    TEST_CLASSES.put(thirdClass.getFullName(), thirdClass);
  }
  
  @Before
  public void setup() throws URISyntaxException {    
    parser = new CxxCppParser();
    inputFile = mock(InputFile.class);
    when(inputFile.getFile()).thenReturn( TestUtils.loadResource(TEST_FILE) );
  }
  
  @Test
  public void visitCppSourceFileTest() throws CxxCppParserException {
    CxxCppTranslationUnitVisitor visitor = new CxxCppTranslationUnitVisitor();
    parser.parseFile(inputFile).getAst().accept(visitor);
    Iterator<CxxClass> classIt = visitor.getClasses().iterator();
    
    assertEquals(inputFile.getFile().getAbsolutePath(), visitor.getFilename());
    assertEquals("class count", TEST_FILE_CLASS_COUNT, visitor.getClasses().size());
    
    while(classIt.hasNext()) {
      CxxClass actualClass = classIt.next();
      CxxClass expectedClass = TEST_CLASSES.get(actualClass.getFullName());
      assertEquals(expectedClass.getFullName(), actualClass.getFullName());
      assertEquals(actualClass + " member count", expectedClass.getMembers().size(), actualClass.getMembers().size());
      assertEquals(actualClass + " method count", expectedClass.getMethods().size(), actualClass.getMethods().size());
      assertEquals(actualClass + " ancestor count", expectedClass.getAncestors().size(), actualClass.getAncestors().size());
      
      Iterator<CxxClassMember> memberIt = actualClass.getMembers().iterator();
      while(memberIt.hasNext()) {
        CxxClassMember actualMember = memberIt.next();
        CxxClassMember expectedMember = expectedClass.findMemberByName(actualMember.getName());
        assertEquals("Member names don't match", expectedMember.getName(), actualMember.getName());
        assertEquals("Member full names don't match", expectedMember.getFullName(), actualMember.getFullName());
        assertEquals("Member types don't match", expectedMember.getType(), actualMember.getType());
        assertEquals("Member's don't match", expectedMember, actualMember);
      } 
      
      Iterator<CxxClassMethod> methodIt = actualClass.getMethods().iterator();
      while(methodIt.hasNext()) {
        CxxClassMethod actualMethod = methodIt.next();
        CxxClassMethod expectedMethod = expectedClass.findMethodByName(actualMethod.getName());
        assertEquals("Method names for " + actualClass + "dont match", expectedMethod.getName(), actualMethod.getName());
        assertEquals("Method full names for " + actualClass + "dont match", expectedMethod.getFullName(), actualMethod.getFullName());
        assertEquals("Method arguments don't match", expectedMethod.getArguments(), actualMethod.getArguments());
        assertEquals("Method bodies don't match", expectedMethod.getBody(), actualMethod.getBody());
        assertEquals("Methods don't match", expectedMethod, actualMethod);
      } 
      
    }
  }
  
  @Test(expected = IllegalStateException.class)
  public void shouldThrowWhenNoFileNameIsPresent() {
    CxxCppTranslationUnitVisitor visitor = new CxxCppTranslationUnitVisitor();
    visitor.getFilename();  //throws
  }
  
  @Test(expected = IllegalStateException.class)
  public void shouldThrowWhenNoClassesArePresent() {
    CxxCppTranslationUnitVisitor visitor = new CxxCppTranslationUnitVisitor();
    visitor.getClasses();  //throws
  }  
  
  @Test(expected = IllegalStateException.class)
  public void shouldThrowWhenCouldNotAddClass() {
    CxxCppTranslationUnitVisitor visitor = new CxxCppTranslationUnitVisitor();
    visitor.addClass( new CppClass() ); //throws
  }    
    
}