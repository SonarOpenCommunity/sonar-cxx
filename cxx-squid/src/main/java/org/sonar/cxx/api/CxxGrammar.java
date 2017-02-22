/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.api;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Rule;

public class CxxGrammar extends Grammar {

  public Rule test;

  public Rule bool;
  public Rule literal;

  // Top-level components
  public Rule translationUnit;

  // Expressions
  public Rule primaryExpression;
  public Rule idExpression;
  public Rule unqualifiedId;
  public Rule qualifiedId;
  public Rule nestedNameSpecifier;
  public Rule lambdaExpression;
  public Rule lambdaIntroducer;
  public Rule lambdaCapture;
  public Rule captureDefault;
  public Rule captureList;
  public Rule capture;
  public Rule lambdaDeclarator;
  public Rule postfixExpression;
  public Rule expressionList;
  public Rule pseudoDestructorName;
  public Rule unaryExpression;
  public Rule unaryOperator;
  public Rule newExpression;
  public Rule newPlacement;
  public Rule newTypeId;
  public Rule newDeclarator;
  public Rule noptrNewDeclarator;
  public Rule newInitializer;
  public Rule deleteExpression;
  public Rule noexceptExpression;
  public Rule castExpression;
  public Rule pmExpression;
  public Rule multiplicativeExpression;
  public Rule additiveExpression;
  public Rule shiftExpression;
  public Rule relationalExpression;
  public Rule equalityExpression;
  public Rule andExpression;
  public Rule exclusiveOrExpression;
  public Rule inclusiveOrExpression;
  public Rule logicalAndExpression;
  public Rule logicalOrExpression;
  public Rule conditionalExpression;
  public Rule assignmentExpression;
  public Rule assignmentOperator;
  public Rule expression;
  public Rule constantExpression;

  // Statements
  public Rule statement;
  public Rule labeledStatement;
  public Rule expressionStatement;
  public Rule compoundStatement;
  public Rule statementSeq;
  public Rule selectionStatement;
  public Rule condition;
  public Rule iterationStatement;
  public Rule forInitStatement;
  public Rule forRangeDeclaration;
  public Rule forRangeInitializer;
  public Rule jumpStatement;
  public Rule declarationStatement;

  // Declarations
  public Rule declarationSeq;
  public Rule declaration;
  public Rule blockDeclaration;
  public Rule aliasDeclaration;
  public Rule simpleDeclaration;
  public Rule staticAssertDeclaration;
  public Rule emptyDeclaration;
  public Rule attributeDeclaration;
  public Rule declSpecifier;

  public Rule conditionDeclSpecifierSeq;
  public Rule forRangeDeclSpecifierSeq;
  public Rule parameterDeclSpecifierSeq;
  public Rule functionDeclSpecifierSeq;
  public Rule simpleDeclSpecifierSeq;
  public Rule memberDeclSpecifierSeq;

  public Rule storageClassSpecifier;
  public Rule functionSpecifier;
  public Rule typedefName;
  public Rule typeSpecifier;
  public Rule trailingTypeSpecifier;
  public Rule typeSpecifierSeq;
  public Rule trailingTypeSpecifierSeq;
  public Rule simpleTypeSpecifier;
  public Rule typeName;
  public Rule decltypeSpecifier;
  public Rule elaboratedTypeSpecifier;
  public Rule enumName;
  public Rule enumSpecifier;
  public Rule enumHead;
  public Rule opaqueEnumDeclaration;
  public Rule enumKey;
  public Rule enumBase;
  public Rule enumeratorList;
  public Rule enumeratorDefinition;
  public Rule enumerator;
  public Rule namespaceName;
  public Rule originalNamespaceName;
  public Rule namespaceDefinition;
  public Rule namedNamespaceDefinition;
  public Rule originalNamespaceDefinition;
  public Rule extensionNamespaceDefinition;
  public Rule unnamedNamespaceDefinition;
  public Rule namespaceBody;
  public Rule namespaceAlias;
  public Rule namespaceAliasDefinition;
  public Rule qualifiedNamespaceSpecifier;
  public Rule usingDeclaration;
  public Rule usingDirective;
  public Rule asmDefinition;
  public Rule linkageSpecification;
  public Rule attributeSpecifierSeq;
  public Rule attributeSpecifier;
  public Rule alignmentSpecifier;
  public Rule attributeList;
  public Rule attribute;
  public Rule attributeToken;
  public Rule attributeScopedToken;
  public Rule attributeNamespace;
  public Rule attributeArgumentClause;
  public Rule balancedTokenSeq;
  public Rule balancedToken;

  // Declarators
  public Rule initDeclaratorList;
  public Rule initDeclarator;
  public Rule declarator;
  public Rule ptrDeclarator;
  public Rule noptrDeclarator;
  public Rule parametersAndQualifiers;
  public Rule trailingReturnType;
  public Rule ptrOperator;
  public Rule cvQualifierSeq;
  public Rule cvQualifier;
  public Rule refQualifier;
  public Rule declaratorId;
  public Rule typeId;
  public Rule abstractDeclarator;
  public Rule ptrAbstractDeclarator;
  public Rule noptrAbstractDeclarator;
  public Rule abstractPackDeclarator;
  public Rule noptrAbstractPackDeclarator;
  public Rule parameterDeclarationClause;
  public Rule parameterDeclarationList;
  public Rule parameterDeclaration;
  public Rule functionDefinition;
  public Rule functionBody;
  public Rule initializer;
  public Rule braceOrEqualInitializer;
  public Rule initializerClause;
  public Rule initializerList;
  public Rule bracedInitList;

  // Classes
  public Rule className;
  public Rule classSpecifier;
  public Rule classHead;
  public Rule classHeadName;
  public Rule classVirtSpecifier;
  public Rule classKey;
  public Rule memberSpecification;
  public Rule memberDeclaration;
  public Rule memberDeclaratorList;
  public Rule memberDeclarator;
  public Rule virtSpecifierSeq;
  public Rule virtSpecifier;
  public Rule pureSpecifier;

  // cli extension
  public Rule cliTopLevelVisibility;
  public Rule cliFinallyClause;
  public Rule cliEventDefinition;
  public Rule cliEventModifiers;
  public Rule cliPropertyOrEventName;
  public Rule cliEventType;
  public Rule cliParameterArray;
  public Rule cliPropertyDefinition;
  public Rule cliPropertyModifiers;
  public Rule cliFunctionDefinition;
  public Rule cliPropertyIndexes;
  public Rule cliPropertyIndexParameterList;
  public Rule cliAccessorSpecification;
  public Rule cliAccessorDeclaration;
  public Rule cliDelegateSpecifier;
  public Rule cliGenericDeclaration;
  public Rule cliGenericParameterList;
  public Rule cliConstraintClauseList;
  public Rule cliConstraintItemList;
  public Rule cliGenericParameter;
  public Rule cliGenericId;
  public Rule cliGenericName;
  public Rule cliGenericArgumentList;
  public Rule cliGenericArgument;
  public Rule cliConstraintClause;
  public Rule cliConstraintItem;

  // Derived classes
  public Rule baseClause;
  public Rule baseSpecifierList;
  public Rule baseSpecifier;
  public Rule classOrDecltype;
  public Rule baseTypeSpecifier;
  public Rule accessSpecifier;

  // Special member functions
  public Rule conversionFunctionId;
  public Rule conversionTypeId;
  public Rule conversionDeclarator;
  public Rule ctorInitializer;
  public Rule memInitializerList;
  public Rule memInitializer;
  public Rule memInitializerId;

  // Overloading
  public Rule operatorFunctionId;
  public Rule operator;
  public Rule literalOperatorId;

  // Templates
  public Rule templateDeclaration;
  public Rule templateParameterList;
  public Rule templateParameter;
  public Rule typeParameter;
  public Rule simpleTemplateId;
  public Rule templateId;
  public Rule templateName;
  public Rule templateArgumentList;
  public Rule templateArgument;
  public Rule typenameSpecifier;
  public Rule explicitInstantiation;
  public Rule explicitSpecialization;

  // Exception handling
  public Rule tryBlock;
  public Rule functionTryBlock;
  public Rule handlerSeq;
  public Rule handler;
  public Rule exceptionDeclaration;
  public Rule throwExpression;
  public Rule exceptionSpecification;
  public Rule dynamicExceptionSpecification;
  public Rule typeIdList;
  public Rule noexceptSpecification;

  @Override
  public Rule getRootRule() {
    return translationUnit;
  }
}
