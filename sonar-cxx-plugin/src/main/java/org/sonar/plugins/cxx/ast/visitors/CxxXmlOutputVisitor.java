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

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.sonar.plugins.cxx.ast.visitors.internal.XmlNodeWriter;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * Goes through every node in AST and outputs the whole tree as
 * an XML file.
 * @author Przemyslaw Kociolek
 */
public class CxxXmlOutputVisitor extends ASTVisitor {

  private XmlNodeWriter nodeWriter = null;

  /**
   * Ctor
   * @param file  output file
   */
  public CxxXmlOutputVisitor(File file) {
    this(file.getAbsolutePath());
  }

  /**
   * Ctor
   * @param fileName  output file path
   */
  public CxxXmlOutputVisitor(String fileName) {
    super(true);
    this.shouldVisitAmbiguousNodes = false;
    try {
      this.nodeWriter = new XmlNodeWriter(fileName);
    } catch (IOException e) {
      CxxUtils.LOG.error(e.getMessage());
    }
  }

  //visit methods
  public int visit(IASTTranslationUnit node) {
    if(!nodeWriter.isValid()) {
      return ASTVisitor.PROCESS_ABORT;
    }
    return nodeWriter.openNode(node, node.getFilePath());
  }

  public int visit(IASTName node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTDeclaration node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTInitializer node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTParameterDeclaration node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTDeclarator node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTDeclSpecifier node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTArrayModifier node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTPointerOperator node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTExpression node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTStatement node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTTypeId node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(IASTEnumerator node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }
  
  public int visit( IASTProblem node ){
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(ICPPASTBaseSpecifier node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(ICPPASTNamespaceDefinition node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(ICPPASTTemplateParameter node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(ICPPASTCapture node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }

  public int visit(ICASTDesignator node) {
    return nodeWriter.openNode(node, getNodeToken(node));
  }
  
  //leave methods
  public int leave(IASTTranslationUnit node) {
    try {
      nodeWriter.closeNode(node.getClass().getSimpleName());
      nodeWriter.saveToFile();
    } catch (IOException e) {
      CxxUtils.LOG.error(e.getMessage());  
      return ASTVisitor.PROCESS_ABORT;
    }
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  public int leave(IASTName node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTDeclaration node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTInitializer node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTParameterDeclaration node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTDeclarator node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTDeclSpecifier node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTArrayModifier node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTPointerOperator node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTExpression node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTStatement node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTTypeId node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(IASTEnumerator node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }
  
  public int leave(IASTProblem node){
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }
  
  public int leave(ICPPASTBaseSpecifier node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(ICPPASTNamespaceDefinition node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(ICPPASTTemplateParameter node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(ICPPASTCapture node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }

  public int leave(ICASTDesignator node) {
    return nodeWriter.closeNode(node.getClass().getSimpleName());
  }
  
  private String getNodeToken(IASTNode node) {
    try {
      return node.getSyntax().toString();
    } catch (ExpansionOverlapsBoundaryException e) {
      CxxUtils.LOG.error(e.getMessage());
      return "";
    } catch(NullPointerException e) {
      return "";
    }
    
  }

}
