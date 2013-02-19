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

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.junit.Test;

public class CxxAstNodeTypeTest {
   
  @Test
  public void translationUnitTest() {
    assertEquals(0, CxxAstNodeType.TRANSLATION_UNIT.getCode());
    assertEquals(IASTTranslationUnit.class, CxxAstNodeType.TRANSLATION_UNIT.getClazz());
  }
  
  @Test
  public void nameTest() {
    assertEquals(1, CxxAstNodeType.NAME.getCode());
    assertEquals(IASTName.class, CxxAstNodeType.NAME.getClazz());
  }
  
  @Test
  public void declarationTest() {
    assertEquals(2, CxxAstNodeType.DECLARATION.getCode());
    assertEquals(IASTDeclaration.class, CxxAstNodeType.DECLARATION.getClazz());
  }
  
  @Test
  public void initializerTest() {
    assertEquals(3, CxxAstNodeType.INITIALIZER.getCode());
    assertEquals(IASTInitializer.class, CxxAstNodeType.INITIALIZER.getClazz());
  }
  
  @Test
  public void parameterDeclarationTest() {
    assertEquals(4, CxxAstNodeType.PARAMETER_DECLARATION.getCode());
    assertEquals(IASTParameterDeclaration.class, CxxAstNodeType.PARAMETER_DECLARATION.getClazz());
  }
  
  @Test
  public void declaratorTest() {
    assertEquals(5, CxxAstNodeType.DECLARATOR.getCode());
    assertEquals(IASTDeclarator.class, CxxAstNodeType.DECLARATOR.getClazz());
  }
  
  @Test
  public void declSpecifierTest() {
    assertEquals(6, CxxAstNodeType.DECL_SPECIFIER.getCode());
    assertEquals(IASTDeclSpecifier.class, CxxAstNodeType.DECL_SPECIFIER.getClazz());
  }
  
  @Test
  public void arrayModifierTest() {
    assertEquals(7, CxxAstNodeType.ARRAY_MODIFIER.getCode());
    assertEquals(IASTArrayModifier.class, CxxAstNodeType.ARRAY_MODIFIER.getClazz());
  }
  
  @Test
  public void pointerOperatorTest() {
    assertEquals(8, CxxAstNodeType.POINTER_OPERATOR.getCode());
    assertEquals(IASTPointerOperator.class, CxxAstNodeType.POINTER_OPERATOR.getClazz());
  }
  
  @Test
  public void expressionTest() {
    assertEquals(9, CxxAstNodeType.EXPRESSION.getCode());
    assertEquals(IASTExpression.class, CxxAstNodeType.EXPRESSION.getClazz());
  }
  
  @Test
  public void statementTest() {
    assertEquals(10, CxxAstNodeType.STATEMENT.getCode());
    assertEquals(IASTStatement.class, CxxAstNodeType.STATEMENT.getClazz());
  }
  
  @Test
  public void typeIdTest() {
    assertEquals(11, CxxAstNodeType.TYPE_ID.getCode());
    assertEquals(IASTTypeId.class, CxxAstNodeType.TYPE_ID.getClazz());
  }
  
  @Test
  public void enumeratorTest() {
    assertEquals(12, CxxAstNodeType.ENUMERATOR.getCode());
    assertEquals(IASTEnumerator.class, CxxAstNodeType.ENUMERATOR.getClazz());
  }
  
  @Test
  public void problemTest() {
    assertEquals(13, CxxAstNodeType.PROBLEM.getCode());
    assertEquals(IASTProblem.class, CxxAstNodeType.PROBLEM.getClazz());
  }
  
  @Test
  public void cppBaseSpecifierTest() {
    assertEquals(14, CxxAstNodeType.CPP_BASE_SPECIFIER.getCode());
    assertEquals(ICPPASTBaseSpecifier.class, CxxAstNodeType.CPP_BASE_SPECIFIER.getClazz());
  }
  
  @Test
  public void cppNamespaceDefinitionTest() {
    assertEquals(15, CxxAstNodeType.CPP_NAMESPACE_DEFINITION.getCode());
    assertEquals(ICPPASTNamespaceDefinition.class, CxxAstNodeType.CPP_NAMESPACE_DEFINITION.getClazz());
  }
  
  @Test
  public void cppTemplateParameterTest() {
    assertEquals(16, CxxAstNodeType.CPP_TEMPLATE_PARAMETER.getCode());
    assertEquals(ICPPASTTemplateParameter.class, CxxAstNodeType.CPP_TEMPLATE_PARAMETER.getClazz());
  }
  
  @Test
  public void cppCaptureTest() {
    assertEquals(17, CxxAstNodeType.CPP_CAPTURE.getCode());
    assertEquals(ICPPASTCapture.class, CxxAstNodeType.CPP_CAPTURE.getClazz());
  }
  
  @Test
  public void castDesignatorTest() {
    assertEquals(18, CxxAstNodeType.CAST_DESIGNATOR.getCode());
    assertEquals(ICASTDesignator.class, CxxAstNodeType.CAST_DESIGNATOR.getClazz());
  }
  
}
