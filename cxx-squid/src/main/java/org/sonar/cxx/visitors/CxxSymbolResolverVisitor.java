/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.GenericTokenType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.SourceCodeSymbol;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.SymbolTable;
import org.sonar.cxx.utils.CxxAstNodeHelper;

/**
 * Resolves declarations and identifier usages into the Symbol/SymbolTable model during the AST
 * scan. Maintains a stack of nested scopes, pushed on entering a namespace, class/struct/union
 * body, or function body, and popped on exit; the root scope is published via
 * {@code getContext().setSymbolTable(...)} after the file is visited.
 */
public class CxxSymbolResolverVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private final Deque<SymbolTable> scopeStack = new ArrayDeque<>();
  private final Map<AstNode, SymbolTable> pendingFunctionScopes = new IdentityHashMap<>();
  private final Set<AstNode> functionBodyCompoundStatements =
    Collections.newSetFromMap(new IdentityHashMap<>());

  private String lastFunctionName;
  private final List<String> lastParameterNames = new ArrayList<>();
  private final List<String> lastLocalVariableNames = new ArrayList<>();

  private String lastClassName;
  private boolean lastClassIsStruct;
  private final List<String> lastFieldNames = new ArrayList<>();

  private String lastEnumName;
  private final List<String> lastEnumConstantNames = new ArrayList<>();
  private final List<String> lastTypedefNames = new ArrayList<>();

  private final List<String> lastGlobalVariableNames = new ArrayList<>();
  private int usageResolutionCounter;

  @Override
  public void init() {
    subscribeTo(
      CxxGrammarImpl.classSpecifier,
      CxxGrammarImpl.functionBody,
      CxxGrammarImpl.compoundStatement,
      CxxGrammarImpl.namespaceDefinition,
      CxxGrammarImpl.functionDefinition,
      CxxGrammarImpl.simpleDeclaration,
      CxxGrammarImpl.enumSpecifier,
      CxxGrammarImpl.aliasDeclaration,
      GenericTokenType.IDENTIFIER);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    lastParameterNames.clear();
    lastLocalVariableNames.clear();
    lastFunctionName = null;
    lastClassName = null;
    lastClassIsStruct = false;
    lastFieldNames.clear();
    lastEnumName = null;
    lastEnumConstantNames.clear();
    lastTypedefNames.clear();
    lastGlobalVariableNames.clear();
    usageResolutionCounter = 0;
    scopeStack.clear();
    pendingFunctionScopes.clear();
    functionBodyCompoundStatements.clear();
    scopeStack.push(new SymbolTable());
    // AstNodeSymbolExtension is process-global; clear per scan to avoid unbounded growth.
    AstNodeSymbolExtension.clear();
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(GenericTokenType.IDENTIFIER)) {
      resolveIdentifierUsage(node);
      return;
    }
    if (node.is(CxxGrammarImpl.functionDefinition)) {
      resolveFunctionDeclaration(node);
      return;
    }
    if (node.is(CxxGrammarImpl.simpleDeclaration)) {
      if (CxxAstNodeHelper.isTypedefKeywordPresent(node.getFirstChild(CxxGrammarImpl.declSpecifierSeq))) {
        resolveTypedefDeclaration(node);
      } else {
        resolveLocalVariableDeclaration(node);
      }
      return;
    }
    if (node.is(CxxGrammarImpl.aliasDeclaration)) {
      resolveAliasDeclaration(node);
      return;
    }
    if (node.is(CxxGrammarImpl.enumSpecifier)) {
      resolveEnumDeclaration(node);
      return;
    }
    if (node.is(CxxGrammarImpl.classSpecifier)) {
      SourceCodeSymbol.SourceCodeTypeSymbol classTypeSymbol = resolveClassDeclaration(node);
      SymbolTable enclosingScopeForAnonymousClass = currentScope();
      pushScope();
      // memberScope() lets resolveIdentifierUsage resolve "s.fld" against Outer's own members.
      if (classTypeSymbol != null) {
        classTypeSymbol.setMemberScope(currentScope());
      }
      resolveMemberFields(node);
      if (classTypeSymbol == null && enclosingScopeForAnonymousClass != null) {
        // Anonymous struct/union: no name to qualify members by, so expose them in the enclosing
        // scope too, matching real C++ semantics.
        copySymbolsToEnclosingScope(currentScope(), enclosingScopeForAnonymousClass);
      }
      return;
    }
    if (node.is(CxxGrammarImpl.functionBody)) {
      if (!hasCompoundStatementBody(node)) {
        return;
      }
      AstNode functionDefinitionNode = node.getFirstAncestor(CxxGrammarImpl.functionDefinition);
      SymbolTable pendingScope = functionDefinitionNode != null
        ? pendingFunctionScopes.remove(functionDefinitionNode) : null;
      scopeStack.push(pendingScope != null ? pendingScope
        : (currentScope() != null ? currentScope().createChildScope() : new SymbolTable()));
      // Record this functionBody's own compoundStatement so its later visit does not push
      // a second, redundant scope.
      AstNode ownBody = findCompoundStatementBody(node);
      if (ownBody != null) {
        functionBodyCompoundStatements.add(ownBody);
      }
      return;
    }
    if (node.is(CxxGrammarImpl.compoundStatement)) {
      if (functionBodyCompoundStatements.contains(node)) {
        return;
      }
      pushScope();
      return;
    }
    pushScope();
  }

  @Override
  public void leaveNode(AstNode node) {
    if (!opensScope(node)) {
      return;
    }
    popScope();
  }

  /**
   * Whether {@code visitNode} pushes a scope for this node, so {@code leaveNode} knows whether
   * to pop one.
   *
   * @param node the node being entered or left
   * @return true if {@code visitNode} pushes a scope for this node
   */
  private boolean opensScope(AstNode node) {
    if (node.is(GenericTokenType.IDENTIFIER, CxxGrammarImpl.functionDefinition,
        CxxGrammarImpl.simpleDeclaration, CxxGrammarImpl.enumSpecifier, CxxGrammarImpl.aliasDeclaration)) {
      return false;
    }
    if (node.is(CxxGrammarImpl.functionBody)) {
      return hasCompoundStatementBody(node);
    }
    if (node.is(CxxGrammarImpl.compoundStatement)) {
      return !functionBodyCompoundStatements.contains(node);
    }
    return true;
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    // Publishes the root scope for consumers (e.g. detection rules).
    if (!scopeStack.isEmpty()) {
      getContext().setSymbolTable(scopeStack.pop());
    }
  }

  /**
   * @return the innermost active scope, or null outside the visitFile/leaveFile lifecycle
   */
  @CheckForNull
  public SymbolTable currentScope() {
    return scopeStack.peek();
  }

  /**
   * Test-observability accessor: name of the most recently resolved function declaration.
   *
   * @return the function name, or null if none resolved in the last scanned file
   */
  @CheckForNull
  public String lastResolvedFunctionName() {
    return lastFunctionName;
  }

  /**
   * Test-observability accessor: parameter names of the most recently resolved function.
   *
   * @return list of parameter names, in declaration order
   */
  public List<String> lastResolvedParameterNames() {
    return new ArrayList<>(lastParameterNames);
  }

  /**
   * Test-observability accessor: local-variable names resolved in the most recently visited
   * function body.
   *
   * @return list of local variable names, in declaration order
   */
  public List<String> lastResolvedLocalVariableNames() {
    return new ArrayList<>(lastLocalVariableNames);
  }

  /**
   * Test-observability accessor: name of the most recently resolved class/struct/union.
   *
   * @return the class name, or null if none resolved
   */
  @CheckForNull
  public String lastResolvedClassName() {
    return lastClassName;
  }

  /**
   * Test-observability accessor: whether the most recently resolved class-like type was declared
   * with the {@code struct} keyword.
   *
   * @return true if it was a struct
   */
  public boolean lastResolvedClassIsStruct() {
    return lastClassIsStruct;
  }

  /**
   * Test-observability accessor: field names resolved from the most recently visited class body.
   *
   * @return list of field names, in declaration order
   */
  public List<String> lastResolvedFieldNames() {
    return new ArrayList<>(lastFieldNames);
  }

  /**
   * Test-observability accessor: name of the most recently resolved enum type.
   *
   * @return the enum name, or null if none resolved
   */
  @CheckForNull
  public String lastResolvedEnumName() {
    return lastEnumName;
  }

  /**
   * Test-observability accessor: enum constant names resolved from the most recently visited
   * enum specifier.
   *
   * @return list of enumerator names, in declaration order
   */
  public List<String> lastResolvedEnumConstantNames() {
    return new ArrayList<>(lastEnumConstantNames);
  }

  /**
   * Test-observability accessor: typedef/alias names resolved so far in the current file.
   *
   * @return list of typedef names, in declaration order
   */
  public List<String> lastResolvedTypedefNames() {
    return new ArrayList<>(lastTypedefNames);
  }

  /**
   * Test-observability accessor: global (namespace/file-scope) variable names resolved in the
   * current file.
   *
   * @return list of global variable names, in declaration order
   */
  public List<String> lastResolvedGlobalVariableNames() {
    return new ArrayList<>(lastGlobalVariableNames);
  }

  /**
   * Test-observability accessor: count of identifier usage sites successfully resolved to a
   * declared symbol so far in the current file.
   *
   * @return the resolved-usage count
   */
  public int usageResolutionCount() {
    return usageResolutionCounter;
  }

  /**
   * Test-observability accessor: number of entries currently held in the pending
   * function-scope map, bridging a function's parameter scope to its eventual functionBody.
   * Always zero once a file scan (visitFile/leaveFile) has completed.
   *
   * @return the number of pending function scopes currently tracked
   */
  int pendingScopeCount() {
    return pendingFunctionScopes.size();
  }

  /**
   * Pushes a new child scope of the current scope onto the stack.
   */
  protected void pushScope() {
    SymbolTable parent = scopeStack.peek();
    scopeStack.push(parent != null ? parent.createChildScope() : new SymbolTable());
  }

  /**
   * Pops the innermost scope off the stack.
   */
  protected void popScope() {
    if (!scopeStack.isEmpty()) {
      scopeStack.pop();
    }
  }

  private static boolean hasCompoundStatementBody(AstNode functionBodyNode) {
    return findCompoundStatementBody(functionBodyNode) != null;
  }

  /**
   * Locates a {@code functionBody}'s own {@code compoundStatement}, either direct or one level
   * deeper via a function-try-block. Declaration-only bodies ({@code = delete;}) have neither.
   *
   * @param functionBodyNode a {@code functionBody} node
   * @return the function's own {@code compoundStatement} body, or null if it has none
   */
  @CheckForNull
  private static AstNode findCompoundStatementBody(AstNode functionBodyNode) {
    AstNode compoundStatement = functionBodyNode.getFirstChild(CxxGrammarImpl.compoundStatement);
    if (compoundStatement != null) {
      return compoundStatement;
    }
    AstNode functionTryBlock = functionBodyNode.getFirstChild(CxxGrammarImpl.functionTryBlock);
    return functionTryBlock != null
      ? functionTryBlock.getFirstChild(CxxGrammarImpl.compoundStatement) : null;
  }

  /**
   * Same as {@link #hasCompoundStatementBody(AstNode)}, starting from the enclosing
   * {@code functionDefinition} node.
   */
  private static boolean functionDefinitionHasCompoundStatementBody(AstNode functionDefinitionNode) {
    AstNode functionBodyNode = CxxAstNodeHelper.getFunctionDefinitionBody(functionDefinitionNode);
    return functionBodyNode != null && hasCompoundStatementBody(functionBodyNode);
  }

  private void resolveFunctionDeclaration(AstNode functionDefinitionNode) {
    String functionName = CxxAstNodeHelper.getFunctionDefinitionName(functionDefinitionNode);
    if (functionName == null) {
      return;
    }
    var functionSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol(functionName, null);
    lastFunctionName = functionName;
    lastParameterNames.clear();

    // Declaration-only function (e.g. "= delete;") has no body scope to register into.
    boolean hasBody = functionDefinitionHasCompoundStatementBody(functionDefinitionNode);

    for (AstNode parameterDeclaration : CxxAstNodeHelper.getFunctionDefinitionParameters(functionDefinitionNode)) {
      AstNode declaratorId = parameterDeclaration.getFirstDescendant(CxxGrammarImpl.declaratorId);
      String parameterName = CxxAstNodeHelper.getIdentifierName(declaratorId);
      if (parameterName == null) {
        continue;
      }
      var parameterSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol(parameterName, null);
      parameterSymbol.setParameter(true);
      parameterSymbol.setOwner(functionSymbol);
      parameterSymbol.setDeclaration(declaratorId);
      functionSymbol.addParameter(parameterSymbol);
      lastParameterNames.add(parameterName);

      if (!hasBody) {
        continue; // parameter stays attached to functionSymbol for parameters() introspection
      }

      registerInPendingFunctionScope(functionDefinitionNode, parameterDeclaration, declaratorId, parameterSymbol);
    }

    SymbolTable enclosingScope = currentScope();
    if (enclosingScope != null) {
      enclosingScope.addSymbol(functionSymbol);
    }
  }

  private void registerInPendingFunctionScope(AstNode expectedFunctionDefinitionNode,
      AstNode parameterDeclaration, AstNode declaratorId, Symbol parameterSymbol) {
    // Registers the parameter into the function's body scope, created here ahead of the
    // functionBody node so parameters are visible throughout it.
    AstNode functionDefinitionNode = CxxAstNodeHelper.getEnclosingFunction(parameterDeclaration);
    if (functionDefinitionNode == null || functionDefinitionNode != expectedFunctionDefinitionNode) {
      return;
    }
    SymbolTable pendingScope = pendingFunctionScopes.computeIfAbsent(functionDefinitionNode,
      key -> {
        SymbolTable parent = currentScope();
        return parent != null ? parent.createChildScope() : new SymbolTable();
      });
    pendingScope.addSymbol(parameterSymbol);
    if (declaratorId != null) {
      AstNodeSymbolExtension.setSymbol(declaratorId, parameterSymbol);
    }
  }

  private void resolveLocalVariableDeclaration(AstNode simpleDeclarationNode) {
    // Distinguishes local/global variables from data members (resolved by resolveMemberFields).
    // Uses the *nearest* enclosing boundary, since a member function's body nests inside
    // memberSpecification too.
    AstNode nearestBoundary = simpleDeclarationNode.getFirstAncestor(
      CxxGrammarImpl.compoundStatement, CxxGrammarImpl.memberSpecification);
    if (nearestBoundary != null && nearestBoundary.is(CxxGrammarImpl.memberSpecification)) {
      return; // data members are resolved by resolveMemberFields, not here
    }
    SymbolTable scope = currentScope();
    if (scope == null) {
      return;
    }
    boolean isGlobal = nearestBoundary == null;
    for (AstNode initDeclarator : CxxAstNodeHelper.getInitDeclarators(simpleDeclarationNode)) {
      AstNode declarator = initDeclarator.getFirstChild(CxxGrammarImpl.declarator);
      AstNode declaratorId = CxxAstNodeHelper.getDeclaratorId(declarator);
      String variableName = CxxAstNodeHelper.getIdentifierName(declaratorId);
      if (variableName == null) {
        continue;
      }
      var variableSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol(variableName, null);
      if (isGlobal) {
        variableSymbol.setGlobalVariable(true);
        lastGlobalVariableNames.add(variableName);
      } else {
        variableSymbol.setLocalVariable(true);
        lastLocalVariableNames.add(variableName);
      }
      variableSymbol.setDeclaration(declaratorId);
      AstNode initializer = initDeclarator.getFirstChild(CxxGrammarImpl.initializer);
      if (initializer != null) {
        variableSymbol.setInitializer(initializer);
      }
      variableSymbol.setDeclaredType(resolveDeclaredTypeSymbol(simpleDeclarationNode, scope));
      scope.addSymbol(variableSymbol);
      if (declaratorId != null) {
        AstNodeSymbolExtension.setSymbol(declaratorId, variableSymbol);
      }
    }
  }

  /**
   * Resolves a variable or data member's own declared class/struct type (e.g. the
   * {@code TypeSymbol} for {@code S} in {@code S s;}). Returns null if it isn't a class/struct
   * type, or the type couldn't be resolved.
   */
  @CheckForNull
  private static Symbol.TypeSymbol resolveDeclaredTypeSymbol(AstNode declaringNode, SymbolTable scope) {
    String typeName = CxxAstNodeHelper.getDeclaredClassTypeName(declaringNode);
    if (typeName == null) {
      return null;
    }
    Symbol resolved = scope.lookupSymbol(typeName);
    return resolved instanceof Symbol.TypeSymbol typeSymbol ? typeSymbol : null;
  }

  private void resolveIdentifierUsage(AstNode identifierNode) {
    if (CxxAstNodeHelper.isInsideDeclarator(identifierNode)) {
      return; // declaration site, already handled above
    }
    Symbol resolved;
    if (isMemberAccessRhs(identifierNode)) {
      // "fld" in "s.fld" must resolve against s's own type, never the ambient scope, or a
      // same-named unrelated symbol could shadow it. Left unresolved if the type can't be found.
      SymbolTable memberAccessScope = resolveMemberAccessScope(identifierNode);
      resolved = memberAccessScope != null
        ? memberAccessScope.getSymbol(identifierNode.getTokenValue())
        : null;
    } else {
      SymbolTable scope = currentScope();
      if (scope == null) {
        return;
      }
      resolved = scope.lookupSymbol(identifierNode.getTokenValue());
    }
    if (resolved == null) {
      return;
    }
    AstNodeSymbolExtension.setSymbol(identifierNode, resolved);
    usageResolutionCounter++;
    if (resolved instanceof SourceCodeSymbol sourceCodeSymbol) {
      sourceCodeSymbol.addUsage(
        new SourceCodeSymbol.SourceCodeUsage(identifierNode, resolved, classifyUsageKind(identifierNode)));
    }
  }

  /**
   * True if {@code identifierNode} is a member-access field name (the {@code IDENTIFIER} after
   * {@code "."}/{@code "->"}, e.g. {@code "fld"} in {@code "s.fld"}), regardless of whether the
   * object's type can be resolved.
   */
  private static boolean isMemberAccessRhs(AstNode identifierNode) {
    AstNode parent = identifierNode.getParent();
    if (parent == null || !parent.is(CxxGrammarImpl.postfixExpression)) {
      return false;
    }
    AstNode operatorNode = identifierNode.getPreviousSibling();
    return operatorNode != null && operatorNode.is(CxxPunctuator.DOT, CxxPunctuator.ARROW);
  }

  /**
   * Resolves the accessed object's declared type (see {@link #isMemberAccessRhs}) and returns
   * its member scope. Handles chains ({@code "a.b.c"}) via left-to-right traversal order: by the
   * time {@code "c"} is visited, {@code "b"} is already resolved. Only a plain
   * {@code IDENTIFIER} operand is handled; a call result, {@code this->fld}, or array/pointer
   * expressions are left unresolved. Callers must check {@link #isMemberAccessRhs} first -- a
   * null result here must not fall back to an ambient lookup.
   *
   * @return the member scope to look the field name up in, or null if unresolved
   */
  @CheckForNull
  private static SymbolTable resolveMemberAccessScope(AstNode identifierNode) {
    AstNode operatorNode = identifierNode.getPreviousSibling();
    AstNode objectNode = operatorNode.getPreviousSibling();
    if (objectNode == null || !objectNode.is(GenericTokenType.IDENTIFIER)) {
      return null;
    }
    Symbol objectSymbol = AstNodeSymbolExtension.getSymbol(objectNode);
    if (!(objectSymbol instanceof Symbol.VariableSymbol variableSymbol)) {
      return null;
    }
    Symbol.TypeSymbol declaredType = variableSymbol.declaredType();
    return declaredType != null ? declaredType.memberScope() : null;
  }

  /**
   * Classifies an identifier occurrence as {@code WRITE} (plain assignment), {@code READ_WRITE}
   * (compound assignment), or {@code READ}. For a member-access LHS ({@code s.fld = 1}), only the
   * final field ({@code fld}) is WRITE -- an object operand like {@code s} is only read to
   * navigate to it.
   */
  private static Symbol.Usage.UsageKind classifyUsageKind(AstNode identifierNode) {
    AstNode assignmentExpr = identifierNode.getFirstAncestor(CxxGrammarImpl.assignmentExpression);
    if (assignmentExpr == null) {
      return Symbol.Usage.UsageKind.READ;
    }
    AstNode lhs = assignmentExpr.getFirstChild();
    if (lhs == null || !isDescendantOrSelf(lhs, identifierNode) || isMemberAccessObjectOperand(identifierNode)) {
      return Symbol.Usage.UsageKind.READ;
    }
    AstNode operatorNode = assignmentExpr.getFirstChild(CxxGrammarImpl.assignmentOperator);
    if (operatorNode != null && "=".equals(operatorNode.getTokenValue())) {
      return Symbol.Usage.UsageKind.WRITE;
    }
    return Symbol.Usage.UsageKind.READ_WRITE;
  }

  /**
   * True if {@code identifierNode} is a member-access object operand (e.g. {@code s} in
   * {@code s.fld}), i.e. immediately followed by {@code "."}/{@code "->"}.
   */
  private static boolean isMemberAccessObjectOperand(AstNode identifierNode) {
    AstNode nextSibling = identifierNode.getNextSibling();
    return nextSibling != null && nextSibling.is(CxxPunctuator.DOT, CxxPunctuator.ARROW);
  }

  private static boolean isDescendantOrSelf(AstNode ancestor, AstNode node) {
    for (AstNode current = node; current != null; current = current.getParent()) {
      if (current == ancestor) {
        return true;
      }
    }
    return false;
  }

  /**
   * Copies every symbol in {@code sourceScope} into {@code targetScope}, so members of an
   * anonymous struct/union/scoped-enum become visible in the enclosing scope.
   */
  private static void copySymbolsToEnclosingScope(@Nullable SymbolTable sourceScope, SymbolTable targetScope) {
    if (sourceScope == null) {
      return;
    }
    for (Symbol symbol : sourceScope.getSymbols()) {
      targetScope.addSymbol(symbol);
    }
  }

  @CheckForNull
  private SourceCodeSymbol.SourceCodeTypeSymbol resolveClassDeclaration(AstNode classSpecifierNode) {
    String className = CxxAstNodeHelper.getClassName(classSpecifierNode);
    String keyword = CxxAstNodeHelper.getClassKeyword(classSpecifierNode);
    if (className == null) {
      return null;
    }
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol(className, null);
    if ("struct".equals(keyword)) {
      typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.STRUCT);
    } else if ("union".equals(keyword)) {
      typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.UNION);
    } else {
      typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.CLASS);
    }
    typeSymbol.setDeclaration(classSpecifierNode);
    lastClassName = className;
    lastClassIsStruct = typeSymbol.isStruct();

    SymbolTable enclosingScope = currentScope();
    if (enclosingScope != null) {
      enclosingScope.addSymbol(typeSymbol);
    }
    return typeSymbol;
  }

  private void resolveMemberFields(AstNode classSpecifierNode) {
    lastFieldNames.clear();
    SymbolTable classScope = currentScope();
    if (classScope == null) {
      return;
    }
    AstNode memberSpecification = classSpecifierNode.getFirstChild(CxxGrammarImpl.memberSpecification);
    if (memberSpecification == null) {
      return;
    }
    for (AstNode memberDeclaration : memberSpecification.getChildren(CxxGrammarImpl.memberDeclaration)) {
      if (CxxAstNodeHelper.isTypedefKeywordPresent(
          memberDeclaration.getFirstChild(CxxGrammarImpl.memberDeclSpecifierSeq))) {
        continue; // typedef declares a type alias, not a data member
      }
      for (AstNode memberDeclarator : CxxAstNodeHelper.getMemberDeclarators(memberDeclaration)) {
        AstNode declaratorNode = memberDeclarator.getFirstChild(CxxGrammarImpl.declarator);
        if (declaratorNode == null) {
          // Bit-field alternative has no declarator child, just an optional bare IDENTIFIER.
          resolveBitFieldMember(classScope, memberDeclarator);
          continue;
        }
        AstNode declaratorId = CxxAstNodeHelper.getDeclaratorId(declaratorNode);
        String memberName = CxxAstNodeHelper.getIdentifierName(declaratorId);
        if (memberName == null) {
          continue;
        }
        if (CxxAstNodeHelper.isFunctionDeclarator(declaratorNode)) {
          resolveMemberFunction(classScope, memberName, declaratorNode, declaratorId);
        } else {
          resolveDataMember(classScope, memberName, memberDeclaration, memberDeclarator, declaratorNode,
            declaratorId);
        }
      }
    }
  }

  /**
   * Registers a member function's symbol into the class scope. Prototype-only declarations
   * (e.g. {@code void meth(int);}) are reached only here, never via
   * {@link #resolveFunctionDeclaration}. Not counted in {@code lastResolvedFieldNames()}.
   */
  private void resolveMemberFunction(SymbolTable classScope, String functionName,
      AstNode declaratorNode, @Nullable AstNode declaratorId) {
    var functionSymbol = new SourceCodeSymbol.SourceCodeFunctionSymbol(functionName, null);
    functionSymbol.setDeclaration(declaratorId);
    for (AstNode parameterDeclaration : CxxAstNodeHelper.getDeclaratorParameters(declaratorNode)) {
      AstNode paramDeclaratorId = parameterDeclaration.getFirstDescendant(CxxGrammarImpl.declaratorId);
      String parameterName = CxxAstNodeHelper.getIdentifierName(paramDeclaratorId);
      if (parameterName == null) {
        continue;
      }
      var parameterSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol(parameterName, null);
      parameterSymbol.setParameter(true);
      parameterSymbol.setOwner(functionSymbol);
      parameterSymbol.setDeclaration(paramDeclaratorId);
      functionSymbol.addParameter(parameterSymbol);
    }
    classScope.addSymbol(functionSymbol);
    if (declaratorId != null) {
      AstNodeSymbolExtension.setSymbol(declaratorId, functionSymbol);
    }
  }

  /**
   * Registers a bit-field member. Its name, when present, is a bare {@code IDENTIFIER} under
   * {@code memberDeclarator}, unlike every other member shape's {@code declaratorId}. An
   * anonymous bit-field (padding, e.g. {@code int : 3;}) has no name and is skipped.
   */
  private void resolveBitFieldMember(SymbolTable classScope, AstNode memberDeclarator) {
    AstNode identifier = memberDeclarator.getFirstChild(GenericTokenType.IDENTIFIER);
    if (identifier == null) {
      return; // anonymous padding bit-field
    }
    String fieldName = identifier.getTokenValue();
    var fieldSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol(fieldName, null);
    fieldSymbol.setField(true);
    fieldSymbol.setDeclaration(identifier);
    classScope.addSymbol(fieldSymbol);
    lastFieldNames.add(fieldName);
    AstNodeSymbolExtension.setSymbol(identifier, fieldSymbol);
  }

  private void resolveDataMember(SymbolTable classScope, String fieldName, AstNode memberDeclarationNode,
      AstNode memberDeclarator, AstNode declaratorNode, @Nullable AstNode declaratorId) {
    var fieldSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol(fieldName, null);
    fieldSymbol.setField(true);
    fieldSymbol.setDeclaration(declaratorId);
    AstNode fieldInitializer = getMemberInitializer(memberDeclarator, declaratorNode);
    if (fieldInitializer != null) {
      fieldSymbol.setInitializer(fieldInitializer);
    }
    fieldSymbol.setDeclaredType(resolveDeclaredTypeSymbol(memberDeclarationNode, classScope));
    classScope.addSymbol(fieldSymbol);
    lastFieldNames.add(fieldName);
    if (declaratorId != null) {
      AstNodeSymbolExtension.setSymbol(declaratorId, fieldSymbol);
    }
  }

  /**
   * Locates a data member's initializer. {@code braceOrEqualInitializer} is a {@code .skip()}
   * grammar rule, so its children splice directly into {@code memberDeclarator} as trailing
   * siblings of {@code declarator} -- the last such child is the initializer, unless it's one of
   * the non-initializer trailing shapes ({@code requiresClause}, {@code virtSpecifier}, etc.).
   *
   * @param memberDeclarator the {@code memberDeclarator} node
   * @param declaratorNode the {@code declarator} child of {@code memberDeclarator}, or null
   * @return the initializer's value node, or null if this member has none
   */
  @CheckForNull
  private static AstNode getMemberInitializer(AstNode memberDeclarator, @Nullable AstNode declaratorNode) {
    if (declaratorNode == null) {
      return null;
    }
    AstNode lastChild = memberDeclarator.getLastChild();
    if (lastChild == null || lastChild == declaratorNode
        || lastChild.is(CxxGrammarImpl.requiresClause, CxxGrammarImpl.virtSpecifierSeq,
            CxxGrammarImpl.virtSpecifier, CxxGrammarImpl.cliFunctionModifiers,
            CxxGrammarImpl.pureSpecifier)) {
      return null;
    }
    return lastChild;
  }

  private void resolveEnumDeclaration(AstNode enumSpecifierNode) {
    lastEnumConstantNames.clear();
    String enumName = CxxAstNodeHelper.getEnumName(enumSpecifierNode);
    lastEnumName = enumName;
    SymbolTable enclosingScope = currentScope();
    if (enclosingScope == null) {
      return;
    }
    boolean scoped = isScopedEnum(enumSpecifierNode);
    SourceCodeSymbol.SourceCodeTypeSymbol typeSymbol = null;
    if (enumName != null) {
      typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol(enumName, null);
      typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.ENUM);
      typeSymbol.setScopedEnum(scoped);
      typeSymbol.setDeclaration(enumSpecifierNode);
      enclosingScope.addSymbol(typeSymbol);
    }
    // A named scoped enum exposes constants only via qualified access (Color::RED), so they go
    // in their own child scope. An unscoped or anonymous scoped enum's constants go directly in
    // the enclosing scope (an anonymous scoped enum has no name to qualify them with anyway).
    SymbolTable constantScope;
    if (scoped && typeSymbol != null) {
      constantScope = enclosingScope.createChildScope();
      typeSymbol.setMemberScope(constantScope);
    } else {
      constantScope = enclosingScope;
    }
    for (AstNode enumerator : CxxAstNodeHelper.getEnumerators(enumSpecifierNode)) {
      String constantName = CxxAstNodeHelper.getIdentifierName(enumerator);
      if (constantName == null) {
        continue;
      }
      var constantSymbol = new SourceCodeSymbol(constantName, Symbol.Kind.ENUM_CONSTANT, null);
      constantSymbol.setDeclaration(enumerator);
      constantScope.addSymbol(constantSymbol);
      lastEnumConstantNames.add(constantName);
      AstNodeSymbolExtension.setSymbol(enumerator, constantSymbol);
    }
  }

  /**
   * An enum is scoped ({@code enum class}/{@code enum struct}) when its {@code enumKey} contains
   * a {@code CLASS} or {@code STRUCT} token.
   */
  private static boolean isScopedEnum(AstNode enumSpecifierNode) {
    AstNode enumHead = enumSpecifierNode.getFirstChild(CxxGrammarImpl.enumHead);
    if (enumHead == null) {
      return false;
    }
    AstNode enumKey = enumHead.getFirstChild(CxxGrammarImpl.enumKey);
    if (enumKey == null) {
      return false;
    }
    return enumKey.hasDirectChildren(CxxKeyword.CLASS) || enumKey.hasDirectChildren(CxxKeyword.STRUCT);
  }

  private void resolveTypedefDeclaration(AstNode simpleDeclarationNode) {
    SymbolTable scope = currentScope();
    if (scope == null) {
      return;
    }
    for (AstNode initDeclarator : CxxAstNodeHelper.getInitDeclarators(simpleDeclarationNode)) {
      AstNode declarator = initDeclarator.getFirstChild(CxxGrammarImpl.declarator);
      AstNode declaratorId = CxxAstNodeHelper.getDeclaratorId(declarator);
      registerTypedefName(CxxAstNodeHelper.getIdentifierName(declaratorId), declaratorId, scope);
    }
  }

  private void resolveAliasDeclaration(AstNode aliasDeclarationNode) {
    SymbolTable scope = currentScope();
    if (scope == null) {
      return;
    }
    AstNode identifier = aliasDeclarationNode.getFirstChild(GenericTokenType.IDENTIFIER);
    registerTypedefName(identifier != null ? identifier.getTokenValue() : null, identifier, scope);
  }

  private void registerTypedefName(@Nullable String aliasName, @Nullable AstNode declarationNode,
      SymbolTable scope) {
    if (aliasName == null) {
      return;
    }
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol(aliasName, null);
    typeSymbol.setTypeKind(SourceCodeSymbol.SourceCodeTypeSymbol.TypeKind.TYPEDEF);
    typeSymbol.setDeclaration(declarationNode);
    scope.addSymbol(typeSymbol);
    lastTypedefNames.add(aliasName);
    if (declarationNode != null) {
      AstNodeSymbolExtension.setSymbol(declarationNode, typeSymbol);
    }
  }
}
