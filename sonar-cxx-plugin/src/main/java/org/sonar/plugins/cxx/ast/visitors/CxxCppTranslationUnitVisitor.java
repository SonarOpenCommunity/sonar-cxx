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

import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxTranslationUnit;
import org.sonar.plugins.cxx.ast.cpp.impl.CppTranslationUnit;

/**
 * Visits the main c++ source file nodes, and gathers information about classes in it.
 * Information is gathered by other, more specialised Visitors (ClassVisitor, FunctionVisitor etc).
 * @author Przemyslaw Kociolek
 */
public class CxxCppTranslationUnitVisitor extends ASTVisitor implements CxxTranslationUnit {

  private CxxTranslationUnit unit = null;
  
  /**
   * Default ctor, visits only translation unit node and class declaration nodes
   */
  public CxxCppTranslationUnitVisitor() {
    this.shouldVisitTranslationUnit = true;
    this.shouldVisitDeclSpecifiers = true;
    this.shouldVisitDeclarators = true;
  }

  public int visit(IASTTranslationUnit node) {  //main translation unit node
    unit = new CppTranslationUnit(node.getContainingFilename());
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  public int visit(IASTDeclSpecifier node) {  //class/struct nodes
    CxxCppClassVisitor classVisitor = new CxxCppClassVisitor(this); 
    node.accept(classVisitor);
    CxxClass pc = classVisitor.getProducedClass();
    unit.addClass(pc);
    return ASTVisitor.PROCESS_SKIP;
  }
  
  public int visit(IASTDeclarator node) { //method declaration
    if(node instanceof IASTFunctionDeclarator) {
     CxxCppMethodDeclarationVisitor visitor = new CxxCppMethodDeclarationVisitor(this, node.getParent());
     node.accept(visitor);
    }
    return ASTVisitor.PROCESS_SKIP;
  }
  
  public String getFilename() {
    validateUnit();
    return unit.getFilename();
  }

  public Set<CxxClass> getClasses() {
    validateUnit();
    return unit.getClasses();
  }

  public void addClass(CxxClass newClass) {
    validateUnit();
    unit.addClass(newClass);
  }

  private void validateUnit() {
    if(unit == null) {
      throw new IllegalStateException("No translation unit has been visited.");
    }
  }

  public CxxClass findClassByName(String className) {
    return unit.findClassByName(className);
  }
  
}
