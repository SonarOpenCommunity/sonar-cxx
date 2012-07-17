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

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.ast.cpp.HasClasses;
import org.sonar.plugins.cxx.ast.cpp.impl.CppNamespace;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * Visits method declared from source file, and not from class definition
 * @author Przemyslaw Kociolek
 */
public class CxxCppMethodDeclarationVisitor extends ASTVisitor {

  private HasClasses classContainer;
  private IASTNode statementNode;
  
  public CxxCppMethodDeclarationVisitor(CxxCppTranslationUnitVisitor classContainer, IASTNode statementNode) {
    this.statementNode = statementNode;
    this.classContainer = classContainer;
    this.shouldVisitNames = true;
  }

  public int visit(IASTName node) {
    String qualifiedName = CppNamespace.DEFAULT_NAME + CppNamespace.SEPARATOR + node.getRawSignature();
    String className = StringUtils.substringBeforeLast(qualifiedName, CppNamespace.SEPARATOR);
    String methodName = StringUtils.substringAfterLast(qualifiedName, CppNamespace.SEPARATOR);
    
    CxxClass foundClass = findClass(className);
    if(foundClass != null) {
      CxxClassMethod foundMethod = foundClass.findMethodByName(methodName);
      if(foundMethod != null) {
        CxxCppMethodBodyVisitor visitor = new CxxCppMethodBodyVisitor(foundMethod);  
        statementNode.accept(visitor);  
      } else {
        CxxUtils.LOG.warn("Can't find proper class for method: " + qualifiedName);    
      }
    } else {
      CxxUtils.LOG.warn("Can't find proper class for method: " + qualifiedName);  
    }
    return ASTVisitor.PROCESS_ABORT;
  }
  
  private CxxClass findClass(String className) {
    Iterator<CxxClass> it = classContainer.getClasses().iterator();
    while(it.hasNext()) {
      CxxClass clazz = it.next();
      if(clazz.getFullName().equals(className)) {
        return clazz;
      }
    }
    return null;
  }

}
