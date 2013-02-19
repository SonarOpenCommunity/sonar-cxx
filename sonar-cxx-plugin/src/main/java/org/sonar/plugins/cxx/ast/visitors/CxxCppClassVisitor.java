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
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxTranslationUnit;
import org.sonar.plugins.cxx.ast.cpp.impl.CppClass;

/**
 * Visits class nodes
 * @author Przemyslaw Kociolek
 */
public class CxxCppClassVisitor extends ASTVisitor {

  private static final String[] CLASS_TOKENS = {"struct", "class"};

  private CxxClass producedClass = null;
  private CxxTranslationUnit translationUnit = null;

  public CxxCppClassVisitor(CxxTranslationUnit translationUnit) {
    this.shouldVisitNames = true;
    this.shouldVisitDeclSpecifiers = true;
    this.shouldVisitDeclarations = true;
    this.shouldVisitBaseSpecifiers = true;
    this.translationUnit = translationUnit;
  }

  public CxxClass getProducedClass() {
    return producedClass;
  }

  public int visit(IASTDeclSpecifier node) {
    String token = getNodeToken(node);
    if(isClassToken(token)) {
      return ASTVisitor.PROCESS_CONTINUE;
    }    
    return ASTVisitor.PROCESS_ABORT;
  }

  public int visit(ICPPASTBaseSpecifier node) { //visit class inheritance list
    CxxCppClassInheritanceVisitor inheritanceVisitor = new CxxCppClassInheritanceVisitor(translationUnit, producedClass);
    node.accept(inheritanceVisitor);
    return ASTVisitor.PROCESS_SKIP;
  }

  public int visit(IASTName node) {  //visit class name
    producedClass = new CppClass( getNodeToken(node) );
    return ASTVisitor.PROCESS_CONTINUE;
  }

  public int visit(IASTDeclaration node) {  //visit class members / methods
    if(isMethodDeclarationNode(node)) {
      CxxCppClassMethodVisitor memberVisitor = new CxxCppClassMethodVisitor(producedClass);
      node.accept(memberVisitor);
    } 
    else if(isMemberDeclarationNode(node)) {
      CxxCppClassMemberVisitor memberVisitor = new CxxCppClassMemberVisitor(producedClass);
      node.accept(memberVisitor);
    }  
    return ASTVisitor.PROCESS_SKIP;
  }

  private boolean isMethodDeclarationNode(IASTDeclaration node) {
    for(IASTNode child : node.getChildren()) {
      if(child instanceof CPPASTFunctionDeclarator) {
        return true;
      }
    }
    return false;
  }

  private boolean isMemberDeclarationNode(IASTDeclaration node) {
    return !isMethodDeclarationNode(node);
  }

  private boolean isClassToken(String nodeToken) {
    for(String token : CLASS_TOKENS) {
      if(token.equals(nodeToken)) { 
        return true;
      }
    }
    return false;
  }

  private String getNodeToken(IASTNode node) {
    try {
      return node.getSyntax().toString();
    } catch (ExpansionOverlapsBoundaryException e) {
      return "";
    } catch (NullPointerException e) {
      return "";      
    }
  }

}
