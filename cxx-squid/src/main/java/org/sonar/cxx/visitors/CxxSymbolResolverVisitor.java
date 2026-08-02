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
 * Populates sonar-cxx's Symbol/SymbolTable semantic model by resolving declarations and
 * identifier usages during the normal AST scan.
 *
 * <p>Maintains a stack of nested {@link SymbolTable} scopes, pushing a new child scope on
 * entering a namespace, class/struct/union body, or function body, and popping it on exit. The
 * root (file-level/global) scope is published to the scan context via
 * {@code getContext().setSymbolTable(...)} once the whole file has been visited.
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
    // Guards against a leaked entry so state never accumulates across files on a reused instance.
    pendingFunctionScopes.clear();
    functionBodyCompoundStatements.clear();
    scopeStack.push(new SymbolTable());
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
      // The class's own TypeSymbol is given this new child scope as its memberScope() so that
      // member-access expressions (e.g. "s.fld") can resolve "fld" against the correct class's
      // members instead of an ambient/unqualified lookup -- see resolveIdentifierUsage. This
      // mirrors what resolveEnumDeclaration already does for scoped enums.
      if (classTypeSymbol != null) {
        classTypeSymbol.setMemberScope(currentScope());
      }
      resolveMemberFields(node);
      if (classTypeSymbol == null && enclosingScopeForAnonymousClass != null) {
        // Anonymous struct/union: there is no name to qualify its members by, so (matching real
        // C++ semantics for anonymous unions/structs, where the members are transparently visible
        // in the enclosing scope) copy its members into the enclosing scope too -- otherwise they
        // would only exist in this class's own scope, which nothing else can ever reach, since
        // there is no symbol anywhere pointing at it.
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
      // The compoundStatement this functionBody directly (or, via a function-try-block, one
      // level deeper) wraps is this same scope's own body -- record it so the later
      // compoundStatement visit for that exact node does not push a second, redundant scope.
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
   * Whether {@code visitNode} pushes a scope for this node, and so whether {@code leaveNode}
   * must pop one on exit. {@code IDENTIFIER}, {@code functionDefinition},
   * {@code simpleDeclaration}, {@code enumSpecifier}, and {@code aliasDeclaration} never push a
   * scope. A {@code functionBody} pushes only when it has a real compound-statement body, in
   * which case its own {@code compoundStatement} child is recorded in
   * {@code functionBodyCompoundStatements} so that node's own visit does not push a second scope
   * for the same block. Every other {@code compoundStatement} (a nested block, not a function's
   * own body) opens its own scope, so declarations in an inner block do not shadow-overwrite a
   * same-named declaration in an outer one. Every other node type, including
   * {@code classSpecifier}, opens a scope.
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
    // Pops the remaining root scope and publishes it for consumers (e.g. detection rules).
    if (!scopeStack.isEmpty()) {
      getContext().setSymbolTable(scopeStack.pop());
    }
  }

  /**
   * Returns the innermost active scope, or null if no file is currently being visited (i.e.
   * outside the visitFile/leaveFile lifecycle).
   *
   * @return the current scope, or null
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
   * Locates the compound-statement body of a {@code functionBody} node. Two grammar shapes carry
   * one: a plain body ({@code [ctorInitializer] compoundStatement}, a direct child) and a
   * function-try-block ({@code TRY [ctorInitializer] compoundStatement handlerSeq}, one level
   * deeper via {@code functionTryBlock}). Declaration-only bodies ({@code = delete;}/
   * {@code = default;}) have neither and yield null. Each shape's direct child is checked
   * explicitly rather than searching descendants, since a lambda inside a
   * {@code ctorInitializer} may contain its own nested {@code compoundStatement}.
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
   * Same check as {@link #hasCompoundStatementBody(AstNode)}, starting from the enclosing
   * {@code functionDefinition} node rather than its {@code functionBody} child. Declaration-only
   * functions (e.g. {@code = delete;}, {@code = default;}) have no such body.
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

    // A declaration-only function (e.g. "= delete;", "= default;") has no functionBody node to
    // consume a pending scope, so none is registered for it in the first place.
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
        // No body scope to bridge to; the parameter Symbol stays attached to functionSymbol
        // above, so signature/parameters() introspection keeps working.
        continue;
      }

      // Registers the parameter into the function's eventual body scope, created and pushed now
      // rather than waiting for the functionBody node, so parameters are visible throughout it.
      registerInPendingFunctionScope(functionDefinitionNode, parameterDeclaration, declaratorId, parameterSymbol);
    }

    SymbolTable enclosingScope = currentScope();
    if (enclosingScope != null) {
      enclosingScope.addSymbol(functionSymbol);
    }
  }

  private void registerInPendingFunctionScope(AstNode expectedFunctionDefinitionNode,
      AstNode parameterDeclaration, AstNode declaratorId, Symbol parameterSymbol) {
    // The function body's scope is created here, ahead of the functionBody node itself, and
    // keyed by the enclosing functionDefinition's identity so the functionBody visit later
    // reuses it instead of pushing a duplicate. getEnclosingFunction's result is compared
    // against the functionDefinition this parameter was collected for (rather than trusted on
    // its own) so a parameter belonging to a different, nested function declarator (e.g. a
    // function-pointer parameter's own parameter list) can never be attributed to the wrong
    // function's scope even if a future change to parameter collection widens it again.
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
    // Distinguish local variables (inside a function body scope) from global/namespace-scope
    // variables (no enclosing compoundStatement) from class data members (resolved separately by
    // resolveMemberFields). A simpleDeclaration is a data member only when memberSpecification is
    // the *nearest* enclosing boundary -- a member function's own body is itself nested inside
    // memberSpecification, so a plain unbounded ancestor search for memberSpecification would
    // wrongly classify every local variable declared inside a method as a data member and drop
    // it entirely, since resolveMemberFields (not this method) is what actually registers fields.
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
   * Resolves a variable or data member's own declared class/struct type (e.g. for {@code S s;},
   * the {@code TypeSymbol} for {@code S}) by extracting the referenced type name and looking it up
   * in the given scope's chain. Returns null for a builtin type, enum, typedef, or a type
   * reference that could not be resolved (e.g. it is declared later in the same translation unit,
   * or in a header not part of this scan).
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
      return; // this occurrence is a declaration site, already handled above
    }
    Symbol resolved;
    SymbolTable memberAccessScope = resolveMemberAccessScope(identifierNode);
    if (memberAccessScope != null) {
      // This identifier is the field name on the right of "." or "->" (e.g. "fld" in "s.fld") --
      // it must be looked up in the accessed object's own type's member scope, never in the
      // ambient/current scope: an ambient lookup here would incorrectly resolve it against any
      // unrelated same-named symbol that happens to be visible at this point in the file (e.g. a
      // same-named local variable shadowing an unrelated field of the same name).
      resolved = memberAccessScope.getSymbol(identifierNode.getTokenValue());
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
   * If {@code identifierNode} is the field-name operand of a member-access expression (the
   * {@code IDENTIFIER} directly following a {@code "."} or {@code "->"} inside a
   * {@code postfixExpression}, e.g. {@code "fld"} in {@code "s.fld"}), resolves the accessed
   * object's own declared type and returns that type's member scope, so the field name can be
   * looked up there instead of in the ambient scope.
   *
   * <p>Handles identifier chains ({@code "a.b.c"}) by relying on this visitor's left-to-right,
   * depth-first traversal order: by the time {@code "c"} is visited, {@code "b"} has already been
   * resolved and had its own symbol (and, transitively, its own declared type) recorded via
   * {@link AstNodeSymbolExtension}.
   *
   * <p>Only handles the case where the operand immediately before the operator is itself a plain
   * {@code IDENTIFIER} (a variable/field name, possibly mid-chain). More complex object
   * expressions -- a function call's result ({@code getObj().fld}), {@code this->fld}, array
   * subscript results, or pointer dereferences -- are not type-inferred by this visitor and are
   * left unresolved rather than guessed at, which is strictly no worse than before this method
   * existed (an unresolved usage, not a wrongly-resolved one).
   *
   * @return the member scope to look the field name up in, or null if this is not a member-access
   *         RHS identifier, or the accessed object's type/member-scope could not be determined
   */
  @CheckForNull
  private static SymbolTable resolveMemberAccessScope(AstNode identifierNode) {
    AstNode parent = identifierNode.getParent();
    if (parent == null || !parent.is(CxxGrammarImpl.postfixExpression)) {
      return null;
    }
    AstNode operatorNode = identifierNode.getPreviousSibling();
    if (operatorNode == null
        || !operatorNode.is(CxxPunctuator.DOT, CxxPunctuator.ARROW)) {
      return null;
    }
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
   * Classifies whether an identifier occurrence is the target of a plain assignment ({@code
   * WRITE}), a compound assignment such as {@code +=} ({@code READ_WRITE}), or neither ({@code
   * READ}). An occurrence is an assignment target when it is or is contained in its enclosing
   * {@code assignmentExpression}'s LHS. The grammar's LHS rule ({@code logicalOrExpression}) is
   * {@code skipIfOneChild()}, so for a bare identifier target it collapses away and the
   * identifier becomes the direct first child of {@code assignmentExpression}.
   */
  private static Symbol.Usage.UsageKind classifyUsageKind(AstNode identifierNode) {
    AstNode assignmentExpr = identifierNode.getFirstAncestor(CxxGrammarImpl.assignmentExpression);
    if (assignmentExpr == null) {
      return Symbol.Usage.UsageKind.READ;
    }
    AstNode lhs = assignmentExpr.getFirstChild();
    if (lhs == null || !isDescendantOrSelf(lhs, identifierNode)) {
      return Symbol.Usage.UsageKind.READ;
    }
    AstNode operatorNode = assignmentExpr.getFirstChild(CxxGrammarImpl.assignmentOperator);
    if (operatorNode != null && "=".equals(operatorNode.getTokenValue())) {
      return Symbol.Usage.UsageKind.WRITE;
    }
    return Symbol.Usage.UsageKind.READ_WRITE;
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
   * Copies every symbol directly defined in {@code sourceScope} into {@code targetScope}, so
   * members that were registered into a scope with no reachable owning symbol (an anonymous
   * struct/union, or an anonymous scoped enum) become visible from the enclosing scope instead --
   * matching real C++ semantics for anonymous unions/structs, whose members are transparently
   * visible in the scope that declares them.
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
        continue; // a typedef declares a type alias, not a data member
      }
      for (AstNode memberDeclarator : CxxAstNodeHelper.getMemberDeclarators(memberDeclaration)) {
        AstNode declaratorNode = memberDeclarator.getFirstChild(CxxGrammarImpl.declarator);
        if (declaratorNode == null) {
          // Bit-field alternative: [IDENTIFIER] [attributeSpecifierSeq] ":" constantExpression
          // [braceOrEqualInitializer] -- there is no declarator child at all, only a bare
          // (optional, since anonymous bit-field padding is legal) IDENTIFIER token.
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
   * Registers a member function's own symbol (a {@code SourceCodeFunctionSymbol}, with its
   * parameters) into the class scope, so a call to it from within the class resolves to a
   * function symbol rather than either finding nothing or -- if member data fields and member
   * functions were not distinguished -- a fabricated variable symbol with no real meaning.
   * Prototype-only member declarations (e.g. {@code void meth(int);} with no body) are member
   * functions reached only through this path: they are a {@code memberDeclarator}, never a
   * {@code functionDefinition}, so {@link #resolveFunctionDeclaration} never sees them.
   *
   * <p>Not counted in {@code lastResolvedFieldNames()}, which reports data members only.
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
   * Registers a bit-field member ({@code memberDeclarator}'s
   * {@code [IDENTIFIER] [attributeSpecifierSeq] ":" constantExpression [braceOrEqualInitializer]}
   * alternative). This shape has no {@code declarator} child at all -- the bit-field's name, when
   * present, is a bare {@code IDENTIFIER} token directly under {@code memberDeclarator}, unlike
   * every other member shape which wraps its name in a {@code declaratorId}. An anonymous bit-field
   * (used purely for padding, e.g. {@code int : 3;}) has no name at all and is legitimately skipped.
   */
  private void resolveBitFieldMember(SymbolTable classScope, AstNode memberDeclarator) {
    AstNode identifier = memberDeclarator.getFirstChild(GenericTokenType.IDENTIFIER);
    if (identifier == null) {
      return; // anonymous bit-field used only for padding -- nothing to register
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
   * Locates a class data member's initializer, given a {@code memberDeclarator} and its
   * {@code declarator} child. Only called for data-member declarators (function declarators are
   * handled separately by {@link #resolveMemberFunction}, so the {@code pureSpecifier}/
   * {@code virtSpecifier}-bearing shapes below never actually occur for a data member in
   * practice, but are still excluded defensively since the grammar itself does not prevent a
   * {@code memberDeclarator} from carrying one).
   *
   * <p>{@code braceOrEqualInitializer} is a {@code .skip()} grammar rule, so it never materializes
   * as its own {@link AstNode}: its children (the {@code "="} token plus the initializer clause,
   * or a {@code bracedInitList}) splice directly into {@code memberDeclarator} as trailing
   * siblings of {@code declarator}. The last such trailing child is the initializer's value node.
   *
   * <p>Other {@code memberDeclarator} alternatives place a {@code requiresClause}, a
   * {@code virtSpecifierSeq}/{@code virtSpecifier}, {@code cliFunctionModifiers}, or
   * {@code pureSpecifier} in that trailing position instead, so those node types are excluded.
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
    // A scoped enum (enum class/enum struct) only exposes its constants via qualified access
    // (Color::RED), so they're registered into their own child scope rather than the enclosing
    // one. An unscoped enum's constants are visible directly in the enclosing scope, not nested
    // inside the enum's own scope, so they're registered there instead. An ANONYMOUS scoped enum
    // (enum class { A, B };) has no name to qualify its constants with at all -- Color::RED needs
    // "Color" to exist -- so its own child scope would be unreachable from anywhere; its constants
    // are registered directly into the enclosing scope instead, same as an unscoped enum.
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
   * An enum is scoped ({@code enum class}/{@code enum struct}) when its {@code enumHead}'s {@code
   * enumKey} child contains a {@code CLASS} or {@code STRUCT} token, per the grammar {@code enumKey
   * = ENUM, [CLASS | STRUCT]}. A plain {@code enum} has neither.
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
