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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.impl.CppClassMember;
import org.sonar.plugins.cxx.ast.visitors.internal.ClassVisitor;

/**
 * Visits class members nodes
 * @author Przemyslaw Kociolek
 */
public class CxxCppClassMemberVisitor extends ClassVisitor {

  private String memberType = null;
  private String memberName = null;
  
  public CxxCppClassMemberVisitor(CxxClass classToVisit) {
    super(classToVisit);
    this.shouldVisitDeclSpecifiers = true;
    this.shouldVisitNames = true;
  }

  public int visit(IASTName node) {
    memberName = node.getRawSignature();
    return ASTVisitor.PROCESS_CONTINUE;
  } 
  
  public int leave(IASTName node) {
    getVisitingClass().addMember( new CppClassMember(memberName, memberType) );
    return ASTVisitor.PROCESS_ABORT;
  }
  
  public int visit(IASTDeclSpecifier node) {
    memberType = node.toString();
    return ASTVisitor.PROCESS_SKIP;
  }
   
}
