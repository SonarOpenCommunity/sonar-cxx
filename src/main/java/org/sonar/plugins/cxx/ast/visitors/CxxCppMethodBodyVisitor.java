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
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;

/**
 * Visits method body node and collects used names
 * @author Przemyslaw Kociolek
 */
public class CxxCppMethodBodyVisitor extends ASTVisitor {

  private CxxClassMethod visitedMethod;
  private int statementDepth = 0;

  public CxxCppMethodBodyVisitor(CxxClassMethod visitedMethod) {
    if(visitedMethod == null) {
      throw new IllegalArgumentException("Can't visit null method.");
    }
    this.visitedMethod = visitedMethod;
    this.shouldVisitNames = true;
    this.shouldVisitStatements = true;
    visitedMethod.setImplemented(true);
  }
  
  public int visit(IASTStatement node) {
    statementDepth++;
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  public int leave(IASTStatement node) {
    statementDepth--;
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  public int visit(IASTName node) {
    if(statementDepth > 0) {
      String detectedName = node.getRawSignature();
      visitedMethod.getBody().addDetectedName(detectedName);
    }
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
}
