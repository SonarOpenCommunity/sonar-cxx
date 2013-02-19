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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxTranslationUnit;
import org.sonar.plugins.cxx.ast.visitors.internal.ClassVisitor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * Analyzes class parents
 * @author Przemyslaw Kociolek
 *
 */
public class CxxCppClassInheritanceVisitor extends ClassVisitor {

  private CxxTranslationUnit translationUnit = null;
  
  public CxxCppClassInheritanceVisitor(CxxTranslationUnit translationUnit, CxxClass producedClass) {
    super(producedClass);
    this.shouldVisitNames = true;
    this.translationUnit = translationUnit;
  }
  
  public int visit(IASTName node) {
    String ancestorName = node.getRawSignature();
    CxxClass ancestorClass = translationUnit.findClassByName(ancestorName);
    if(ancestorClass != null) {
      getVisitingClass().addAncestor(ancestorClass);
    } else {
      CxxUtils.LOG.warn("Could not find base class ({}) for {} class)", ancestorName, getVisitingClass());
    }
    
    return ASTVisitor.PROCESS_ABORT;
  }
  
}
