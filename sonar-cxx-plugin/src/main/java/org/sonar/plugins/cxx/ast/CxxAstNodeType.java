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
package org.sonar.plugins.cxx.ast;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
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

/**
 * Enum for recognizing ASTNode subclasses
 * @author Przemyslaw Kociolek
 */
public enum CxxAstNodeType {

  TRANSLATION_UNIT(0, IASTTranslationUnit.class),
  NAME(1, IASTName.class),
  DECLARATION(2, IASTDeclaration.class),
  INITIALIZER(3, IASTInitializer.class),
  PARAMETER_DECLARATION(4, IASTParameterDeclaration.class),
  DECLARATOR(5, IASTDeclarator.class),
  DECL_SPECIFIER(6, IASTDeclSpecifier.class),
  ARRAY_MODIFIER(7, IASTArrayModifier.class),
  POINTER_OPERATOR(8, IASTPointerOperator.class),
  EXPRESSION(9, IASTExpression.class),
  STATEMENT(10, IASTStatement.class),
  TYPE_ID(11, IASTTypeId.class),
  ENUMERATOR(12, IASTEnumerator.class),
  PROBLEM(13, IASTProblem.class),
  CPP_BASE_SPECIFIER(14, ICPPASTBaseSpecifier.class),
  CPP_NAMESPACE_DEFINITION(15, ICPPASTNamespaceDefinition.class),
  CPP_TEMPLATE_PARAMETER(16, ICPPASTTemplateParameter.class),
  CPP_CAPTURE(17, ICPPASTCapture.class),
  CAST_DESIGNATOR(18, ICASTDesignator.class);
  
  private final int code;
  private final Object clazz;
  
  private static Map<Object, CxxAstNodeType> cache = null;
    
  private CxxAstNodeType(int code, Object clazz) {
    this.code = code;
    this.clazz = clazz;
  }
  
  /**
   * @return  node type integer code
   */
  public int getCode() {
    return code;
  }
  
  /**
   * @return  node type class
   */
  public Object getClazz() {
    return clazz;
  }
  
  /**
   * @param node AST node
   * @return  type of node
   */
  public static CxxAstNodeType nodeToType(Class node) {
    if(cache == null) {
      cache = createCache();
    }
    
    if(!cache.containsKey(node)) {
      throw new IllegalStateException("No CxxAstNodeType for node of type: " + node.getClass().getSimpleName());
    }
    
    return cache.get(node);
  }
  
  private static Map<Object, CxxAstNodeType> createCache() {
    Map<Object, CxxAstNodeType> result = new HashMap<Object, CxxAstNodeType>();
    for(CxxAstNodeType nodeType : CxxAstNodeType.values()) {
     cache.put(nodeType.getClazz(), nodeType);
    }
    return result;
  }
  
}
