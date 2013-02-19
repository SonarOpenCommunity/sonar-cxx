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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.ast.cpp.impl.CppClassMethod;
import org.sonar.plugins.cxx.ast.visitors.internal.ClassVisitor;

/**
 * Visits class methods
 * @author Przemyslaw Kociolek
 */
public class CxxCppClassMethodVisitor extends ClassVisitor {

  private CxxClassMethod producedMethod = null;
  //private String methodName = null;
  
  public CxxCppClassMethodVisitor(CxxClass classToVisit) {
    this(classToVisit, null);
  }
      
  public CxxCppClassMethodVisitor(CxxClass foundClass, CxxClassMethod foundMethod) {
    super(foundClass);
    this.shouldVisitParameterDeclarations = true;
    this.shouldVisitDeclarators = true;
    this.shouldVisitStatements = true;
    this.shouldVisitNames = true;
    this.producedMethod = foundMethod;
  }

  public int leave(IASTDeclarator node) {
    getVisitingClass().addMethod(producedMethod);
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  public int visit(IASTParameterDeclaration node) {
    CxxCppMethodArgumentVisitor parameterVisitor = new CxxCppMethodArgumentVisitor(producedMethod);
    node.accept(parameterVisitor);
    return ASTVisitor.PROCESS_SKIP; 
  }
  
  public int visit(IASTName node) {
    //String methodName = node.getRawSignature(); 
    if(producedMethod == null) {
      producedMethod = new CppClassMethod(getVisitingClass(), node.getRawSignature());
    }
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  public int visit(IASTStatement node) {
    CxxCppMethodBodyVisitor bodyVisitor = new CxxCppMethodBodyVisitor(producedMethod);
    node.accept(bodyVisitor);
    return ASTVisitor.PROCESS_SKIP;
  }

}
