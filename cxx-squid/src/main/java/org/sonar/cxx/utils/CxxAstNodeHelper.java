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
package org.sonar.cxx.utils;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.GenericTokenType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.Symbol;

/**
 * Static utility methods for C++ AST navigation patterns.
 *
 * <p>This class provides convenience methods for common AST navigation tasks
 * specific to C++ grammar rules.
 */
public final class CxxAstNodeHelper {

  private CxxAstNodeHelper() {
  }

  /**
   * Check if a postfixExpression node represents a function call.
   *
   * <p>A function call in C++ grammar is a postfixExpression followed by
   * parenthesized argument list: {@code expr(args...)}.
   *
   * @param node the AST node to check
   * @return true if the node represents a function call
   */
  public static boolean isFunctionCall(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.postfixExpression)) {
      return false;
    }
    // postfixExpression with "(" ... ")" suffix indicates a function call
    for (var child : node.getChildren()) {
      if ("(".equals(child.getTokenValue()) && child.getParent() == node) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if a node represents a constructor call (new-expression).
   *
   * @param node the AST node to check
   * @return true if the node is a new-expression
   */
  public static boolean isConstructorCall(@Nullable AstNode node) {
    if (node == null) {
      return false;
    }
    return node.is(CxxGrammarImpl.newExpression);
  }

  /**
   * Extract argument nodes from a function call postfixExpression.
   *
   * <p>Returns the expressionList children, which represent the arguments
   * passed to the function.
   *
   * @param node a postfixExpression that represents a function call
   * @return list of argument expression nodes, empty if no arguments
   */
  public static List<AstNode> getFunctionCallArguments(@Nullable AstNode node) {
    if (node == null || !isFunctionCall(node)) {
      return Collections.emptyList();
    }
    AstNode expressionList = node.getFirstDescendant(CxxGrammarImpl.expressionList);
    if (expressionList != null) {
      return expressionList.getChildren();
    }
    return Collections.emptyList();
  }

  /**
   * Extract the function name string from a function call postfixExpression.
   *
   * <p>For simple calls like {@code foo(...)}, returns "foo".
   * For qualified calls like {@code ns::foo(...)}, returns "ns::foo".
   * For member calls like {@code obj.method(...)}, returns "method".
   *
   * @param node a postfixExpression node
   * @return the function name, or null if it cannot be determined
   */
  @CheckForNull
  public static String getFunctionCallName(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.postfixExpression)) {
      return null;
    }
    // The first child of a postfixExpression is typically the callee expression.
    // For simple calls, it's a primaryExpression containing an idExpression.
    AstNode idExpr = node.getFirstDescendant(CxxGrammarImpl.idExpression);
    if (idExpr != null) {
      return getIdentifierText(idExpr);
    }

    // as functional-style type conversions, producing typeName > className instead of
    // idExpression. Extract the name from the className node in this case.
    AstNode typeName = node.getFirstChild(CxxGrammarImpl.typeName);
    if (typeName != null) {
      AstNode className = typeName.getFirstChild(CxxGrammarImpl.className);
      if (className != null) {
        return getIdentifierName(className);
      }
    }
    return null;
  }

  /**
   * Extract the name from a functionDefinition node.
   *
   * @param node a functionDefinition node
   * @return the function name, or null if it cannot be determined
   */
  @CheckForNull
  public static String getFunctionDefinitionName(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.functionDefinition)) {
      return null;
    }
    AstNode declarator = node.getFirstChild(CxxGrammarImpl.declarator);
    if (declarator != null) {
      AstNode declaratorId = declarator.getFirstDescendant(CxxGrammarImpl.declaratorId);
      if (declaratorId != null) {
        return getIdentifierText(declaratorId);
      }
    }
    return null;
  }

  /**
   * Get the function body node from a functionDefinition.
   *
   * @param node a functionDefinition node
   * @return the functionBody node, or null if not found
   */
  @CheckForNull
  public static AstNode getFunctionDefinitionBody(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.functionDefinition)) {
      return null;
    }
    return node.getFirstChild(CxxGrammarImpl.functionBody);
  }

  /**
   * Get parameter declaration nodes from a functionDefinition.
   *
   * @param node a functionDefinition node
   * @return list of parameterDeclaration nodes, empty if no parameters
   */
  public static List<AstNode> getFunctionDefinitionParameters(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.functionDefinition)) {
      return Collections.emptyList();
    }
    return getDeclaratorParameters(node.getFirstChild(CxxGrammarImpl.declarator));
  }

  /**
   * Get parameter declaration nodes from any declarator that is a function declarator (has a
   * {@code parametersAndQualifiers} descendant), such as a member function's own declarator in a
   * {@code memberDeclarator} that has no enclosing {@code functionDefinition} (a prototype-only
   * member declaration with no body).
   *
   * @param declaratorNode a declarator node, or null
   * @return list of parameterDeclaration nodes, empty if this declarator is not a function
   *         declarator or has no parameters
   */
  public static List<AstNode> getDeclaratorParameters(@Nullable AstNode declaratorNode) {
    if (declaratorNode == null) {
      return Collections.emptyList();
    }
    // A parameter's own type can itself be a function pointer/reference, which carries its own
    // nested parametersAndQualifiers and, inside that, its own nested parameterDeclaration nodes
    // (e.g. void outer(int (*cb)(int inner1, int inner2))). Only the direct-child path from this
    // declarator's own parametersAndQualifiers through parameterDeclarationClause/
    // parameterDeclarationList is walked, so a nested declarator's own parameters are never
    // mistaken for this declarator's parameters.
    AstNode paramsAndQuals = declaratorNode.getFirstDescendant(CxxGrammarImpl.parametersAndQualifiers);
    if (paramsAndQuals == null) {
      return Collections.emptyList();
    }
    AstNode paramDeclClause = paramsAndQuals.getFirstChild(CxxGrammarImpl.parameterDeclarationClause);
    if (paramDeclClause == null) {
      return Collections.emptyList();
    }
    AstNode paramDeclList = paramDeclClause.getFirstChild(CxxGrammarImpl.parameterDeclarationList);
    if (paramDeclList == null) {
      return Collections.emptyList();
    }
    return paramDeclList.getChildren(CxxGrammarImpl.parameterDeclaration);
  }

  /**
   * Whether a declarator is a function declarator (declares a function, not a variable/field),
   * identified by the presence of a {@code parametersAndQualifiers} descendant -- only a function
   * declarator's grammar shape includes one.
   *
   * @param declaratorNode a declarator node, or null
   * @return true if this declarator declares a function
   */
  public static boolean isFunctionDeclarator(@Nullable AstNode declaratorNode) {
    return declaratorNode != null
        && declaratorNode.getFirstDescendant(CxxGrammarImpl.parametersAndQualifiers) != null;
  }

  /**
   * Find the enclosing functionDefinition for a given node.
   *
   * @param node the starting node
   * @return the enclosing functionDefinition node, or null if not inside a function
   */
  @CheckForNull
  public static AstNode getEnclosingFunction(@Nullable AstNode node) {
    return getFirstAncestor(node, CxxGrammarImpl.functionDefinition);
  }

  /**
   * Find the enclosing classSpecifier for a given node.
   *
   * @param node the starting node
   * @return the enclosing classSpecifier node, or null if not inside a class
   */
  @CheckForNull
  public static AstNode getEnclosingClass(@Nullable AstNode node) {
    return getFirstAncestor(node, CxxGrammarImpl.classSpecifier);
  }

  /**
   * Extract the identifier name from a node that contains an IDENTIFIER token.
   *
   * @param node the AST node
   * @return the identifier text, or null if no identifier found
   */
  @CheckForNull
  public static String getIdentifierName(@Nullable AstNode node) {
    if (node == null) {
      return null;
    }
    if (node.is(GenericTokenType.IDENTIFIER)) {
      return node.getTokenValue();
    }
    AstNode identifier = node.getFirstDescendant(GenericTokenType.IDENTIFIER);
    if (identifier != null) {
      return identifier.getTokenValue();
    }
    return null;
  }

  /**
   * Check if a jumpStatement is a return statement.
   *
   * @param node a jumpStatement node
   * @return true if this is a return statement
   */
  public static boolean isReturnStatement(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.jumpStatement)) {
      return false;
    }
    AstNode firstChild = node.getFirstChild();
    return firstChild != null && firstChild.is(CxxKeyword.RETURN);
  }

  /**
   * Get the return expression from a return statement.
   *
   * @param node a jumpStatement node that is a return statement
   * @return the return value expression node, or null if returning void
   */
  @CheckForNull
  public static AstNode getReturnExpression(@Nullable AstNode node) {
    if (node == null || !isReturnStatement(node)) {
      return null;
    }
    return node.getFirstChild(CxxGrammarImpl.exprOrBracedInitList);
  }

  /**
   * Check if a postfixExpression represents member access (. or ->).
   *
   * @param node a postfixExpression node
   * @return true if this is a member access expression
   */
  public static boolean isMemberAccess(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.postfixExpression)) {
      return false;
    }
    for (var child : node.getChildren()) {
      String value = child.getTokenValue();
      if (".".equals(value) || "->".equals(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the left-hand side (qualifier) of a member access expression.
   *
   * <p>For {@code obj.member}, returns the node for {@code obj}.
   *
   * @param node a postfixExpression node that is a member access
   * @return the qualifier node, or null if not a member access
   */
  @CheckForNull
  public static AstNode getMemberAccessQualifier(@Nullable AstNode node) {
    if (node == null || !isMemberAccess(node)) {
      return null;
    }
    // The first child is typically the LHS expression
    return node.getFirstChild();
  }

  /**
   * Get the right-hand side (member name) of a member access expression.
   *
   * <p>For {@code obj.member}, returns "member".
   *
   * @param node a postfixExpression node that is a member access
   * @return the member name, or null if it cannot be determined
   */
  @CheckForNull
  public static String getMemberAccessName(@Nullable AstNode node) {
    if (node == null || !isMemberAccess(node)) {
      return null;
    }
    // After the . or -> operator, find the identifier
    boolean foundOperator = false;
    for (var child : node.getChildren()) {
      if (foundOperator) {
        AstNode id = child.getFirstDescendant(GenericTokenType.IDENTIFIER);
        if (id != null) {
          return id.getTokenValue();
        }
        // If child is itself an IDENTIFIER
        if (child.is(GenericTokenType.IDENTIFIER)) {
          return child.getTokenValue();
        }
        return child.getTokenValue();
      }
      String value = child.getTokenValue();
      if (".".equals(value) || "->".equals(value)) {
        foundOperator = true;
      }
    }
    return null;
  }

  /**
   * Get the symbol of the variable that an expression is being assigned to.
   *
   * <p>Handles two C++ assignment patterns:
   * <ul>
   *   <li>Assignment expression: {@code x = expr} — navigates up from the
   *       expression to the enclosing {@code assignmentExpression} and returns
   *       the symbol associated with the LHS identifier.</li>
   *   <li>Simple declaration with initializer: {@code auto x = expr} — navigates
   *       up from the expression to the enclosing {@code initDeclarator} and returns
   *       the symbol associated with the declarator identifier.</li>
   * </ul>
   *
   * @param expressionNode an expression node (e.g., a function call)
   * @return the symbol of the variable being assigned to, or null if the
   *         expression is not in an assignment context or no symbol is available
   */
  @CheckForNull
  public static Symbol getAssignedSymbol(@Nullable AstNode expressionNode) {
    if (expressionNode == null) {
      return null;
    }

    // Case 1: Assignment expression (x = expr)
    AstNode assignmentExpr = getFirstAncestor(expressionNode, CxxGrammarImpl.assignmentExpression);
    if (assignmentExpr != null) {
      // The LHS is the first child (logicalOrExpression that resolves to an identifier)
      AstNode lhs = assignmentExpr.getFirstChild();
      if (lhs != null) {
        // Check if the LHS has a symbol associated with it
        Symbol lhsSymbol = AstNodeSymbolExtension.getSymbol(lhs);
        if (lhsSymbol != null) {
          return lhsSymbol;
        }
        // Try to find an identifier in the LHS and look up its symbol
        AstNode lhsId = lhs.getFirstDescendant(GenericTokenType.IDENTIFIER);
        if (lhsId != null) {
          return AstNodeSymbolExtension.getSymbol(lhsId);
        }
      }
    }

    // Case 2: Declaration with initializer (auto x = expr, or Type x = expr)
    AstNode initDeclarator = getFirstAncestor(expressionNode, CxxGrammarImpl.initDeclarator);
    if (initDeclarator != null) {
      AstNode declarator = initDeclarator.getFirstChild(CxxGrammarImpl.declarator);
      if (declarator != null) {
        // Get symbol from the declarator
        Symbol declSymbol = AstNodeSymbolExtension.getSymbol(declarator);
        if (declSymbol != null) {
          return declSymbol;
        }
        // Try the declaratorId
        AstNode declaratorId = declarator.getFirstDescendant(CxxGrammarImpl.declaratorId);
        if (declaratorId != null) {
          Symbol idSymbol = AstNodeSymbolExtension.getSymbol(declaratorId);
          if (idSymbol != null) {
            return idSymbol;
          }
          // Try the identifier token itself
          AstNode id = declaratorId.getFirstDescendant(GenericTokenType.IDENTIFIER);
          if (id != null) {
            return AstNodeSymbolExtension.getSymbol(id);
          }
        }
      }
    }

    return null;
  }

  /**
   * Check if a function call is an invocation on a specific variable.
   *
   * <p>For member function calls like {@code obj.method()}, this checks whether
   * the qualifier ({@code obj}) has a symbol that matches the specified variable
   * symbol. This is useful for tracking method calls on specific objects in
   * detection rules.
   *
   * @param callNode a postfixExpression node representing a function call
   * @param variableSymbol the variable symbol to check against
   * @param acceptParentMemberAccess if true, also check parent member access
   *        chains (e.g., {@code a.b.method()} matches for symbol of {@code a})
   * @return true if the function call is invoked on the specified variable
   */
  public static boolean isInvocationOnVariable(@Nullable AstNode callNode,
                                                @Nullable Symbol variableSymbol,
                                                boolean acceptParentMemberAccess) {
    if (callNode == null || variableSymbol == null || !isFunctionCall(callNode)) {
      return false;
    }

    // Check if this is a member function call (obj.method() or obj->method())
    if (!isMemberAccess(callNode)) {
      return false;
    }

    AstNode qualifier = getMemberAccessQualifier(callNode);
    if (qualifier == null) {
      return false;
    }

    // Direct match: check if qualifier's symbol matches
    Symbol qualSymbol = AstNodeSymbolExtension.getSymbol(qualifier);
    if (qualSymbol != null && qualSymbol == variableSymbol) {
      return true;
    }

    // Try identifier within qualifier
    AstNode qualId = qualifier.getFirstDescendant(GenericTokenType.IDENTIFIER);
    if (qualId != null) {
      Symbol idSymbol = AstNodeSymbolExtension.getSymbol(qualId);
      if (idSymbol != null && idSymbol == variableSymbol) {
        return true;
      }
    }

    // If acceptParentMemberAccess, check chained member access (a.b.method())
    if (acceptParentMemberAccess && qualifier.is(CxxGrammarImpl.postfixExpression)
      && isMemberAccess(qualifier)) {
      return isInvocationOnVariable(qualifier, variableSymbol, true);
    }

    return false;
  }

  /**
   * Get the full text representation of an identifier expression, including
   * namespace qualifiers.
   *
   * @param node an idExpression, qualifiedId, or unqualifiedId node
   * @return the full identifier text including qualifiers
   */
  @CheckForNull
  private static String getIdentifierText(@Nullable AstNode node) {
    if (node == null) {
      return null;
    }
    // For qualified ids like ns::name, collect all tokens
    AstNode qualifiedId = node.is(CxxGrammarImpl.qualifiedId)
      ? node
      : node.getFirstDescendant(CxxGrammarImpl.qualifiedId);
    if (qualifiedId != null) {
      var sb = new StringBuilder();
      for (var token : qualifiedId.getTokens()) {
        sb.append(token.getValue());
      }
      return sb.toString();
    }
    // For simple unqualified ids
    AstNode identifier = node.getFirstDescendant(GenericTokenType.IDENTIFIER);
    if (identifier != null) {
      return identifier.getTokenValue();
    }
    return node.getTokenValue();
  }

  /**
   * Checks whether a declSpecifierSeq (or memberDeclSpecifierSeq) contains the {@code typedef}
   * keyword, indicating the associated declarator names are type aliases rather than variables.
   *
   * @param declSpecifierSeqNode a declSpecifierSeq or memberDeclSpecifierSeq node
   * @return true if a typedef keyword token is present among its children
   */
  public static boolean isTypedefKeywordPresent(@Nullable AstNode declSpecifierSeqNode) {
    if (declSpecifierSeqNode == null) {
      return false;
    }
    for (AstNode declSpecifier : declSpecifierSeqNode.getChildren()) {
      for (AstNode child : declSpecifier.getChildren()) {
        if ("typedef".equals(child.getTokenValue())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Extracts the initDeclarator nodes from a simpleDeclaration's initDeclaratorList.
   *
   * @param simpleDeclarationNode a simpleDeclaration node
   * @return list of initDeclarator nodes, empty if none present
   */
  public static List<AstNode> getInitDeclarators(@Nullable AstNode simpleDeclarationNode) {
    if (simpleDeclarationNode == null) {
      return Collections.emptyList();
    }
    AstNode initDeclaratorList = simpleDeclarationNode.getFirstChild(CxxGrammarImpl.initDeclaratorList);
    if (initDeclaratorList == null) {
      return Collections.emptyList();
    }
    return initDeclaratorList.getChildren(CxxGrammarImpl.initDeclarator);
  }

  /**
   * Finds the declaratorId descendant of a declarator (or initDeclarator/memberDeclarator, which
   * both wrap a declarator), which holds the declared name.
   *
   * @param declaratorNode a declarator, initDeclarator, or memberDeclarator node
   * @return the declaratorId node, or null if not found
   */
  @CheckForNull
  public static AstNode getDeclaratorId(@Nullable AstNode declaratorNode) {
    if (declaratorNode == null) {
      return null;
    }
    return declaratorNode.getFirstDescendant(CxxGrammarImpl.declaratorId);
  }

  /**
   * Extracts the class/struct/union's own name from a classSpecifier node.
   *
   * @param classSpecifierNode a classSpecifier node
   * @return the class name, or null if anonymous or not found
   */
  @CheckForNull
  public static String getClassName(@Nullable AstNode classSpecifierNode) {
    if (classSpecifierNode == null) {
      return null;
    }
    AstNode classHead = classSpecifierNode.getFirstChild(CxxGrammarImpl.classHead);
    if (classHead == null) {
      return null;
    }
    AstNode classHeadName = classHead.getFirstChild(CxxGrammarImpl.classHeadName);
    if (classHeadName == null) {
      return null;
    }
    AstNode className = classHeadName.getFirstChild(CxxGrammarImpl.className);
    return getIdentifierName(className);
  }

  /**
   * Extracts the class-key keyword ("class", "struct", or "union") from a classSpecifier node.
   *
   * @param classSpecifierNode a classSpecifier node
   * @return the keyword text, or null if not found
   */
  @CheckForNull
  public static String getClassKeyword(@Nullable AstNode classSpecifierNode) {
    if (classSpecifierNode == null) {
      return null;
    }
    AstNode classHead = classSpecifierNode.getFirstChild(CxxGrammarImpl.classHead);
    if (classHead == null) {
      return null;
    }
    AstNode classKey = classHead.getFirstChild(CxxGrammarImpl.classKey);
    if (classKey == null || classKey.getFirstChild() == null) {
      return null;
    }
    return classKey.getFirstChild().getTokenValue();
  }

  /**
   * Extracts the memberDeclarator nodes from a memberDeclaration's memberDeclaratorList.
   *
   * @param memberDeclarationNode a memberDeclaration node
   * @return list of memberDeclarator nodes, empty if this member-declaration has no declarator
   *         list (e.g. it is a functionDefinition, using-declaration, or similar)
   */
  public static List<AstNode> getMemberDeclarators(@Nullable AstNode memberDeclarationNode) {
    if (memberDeclarationNode == null) {
      return Collections.emptyList();
    }
    AstNode memberDeclaratorList = memberDeclarationNode.getFirstChild(CxxGrammarImpl.memberDeclaratorList);
    if (memberDeclaratorList == null) {
      return Collections.emptyList();
    }
    return memberDeclaratorList.getChildren(CxxGrammarImpl.memberDeclarator);
  }

  /**
   * Extracts the class/struct name referenced by a variable or data member's own declared type,
   * given its enclosing {@code simpleDeclaration} or {@code memberDeclaration} node -- e.g. for
   * {@code S s;} or {@code S fld;}, returns {@code "S"}. Used to resolve a variable/field's static
   * type to that type's own {@code TypeSymbol}, so member-access expressions ({@code s.fld}) can
   * look the member up in the correct class scope instead of the ambient scope.
   *
   * <p>{@code declSpecifierSeq}/{@code memberDeclSpecifierSeq} are both {@code .skipIfOneChild()}
   * grammar rules, so for the common single-specifier case (no {@code static}/{@code const}/etc.
   * qualifiers) the node collapses directly to its own {@code declSpecifier} child; both shapes are
   * handled here by searching the first child that is either node type -- always the specifier
   * sequence, never the {@code initDeclaratorList}/{@code memberDeclaratorList} sibling that comes
   * after it, so an initializer's own type references (e.g. {@code S s = OtherType{};}) are never
   * mistaken for the declared type.
   *
   * @param declaringNode a simpleDeclaration or memberDeclaration node
   * @return the referenced class/struct name, or null if the declared type is not a class/struct
   *         reference (e.g. a builtin type, enum, or typedef) or none could be found
   */
  @CheckForNull
  public static String getDeclaredClassTypeName(@Nullable AstNode declaringNode) {
    if (declaringNode == null) {
      return null;
    }
    AstNode specifierNode = declaringNode.getFirstChild(
      CxxGrammarImpl.declSpecifierSeq, CxxGrammarImpl.declSpecifier,
      CxxGrammarImpl.memberDeclSpecifierSeq);
    if (specifierNode == null) {
      return null;
    }
    AstNode className = specifierNode.getFirstDescendant(CxxGrammarImpl.className);
    return getIdentifierName(className);
  }

  /**
   * Extracts the enum's own name from an enumSpecifier node.
   *
   * @param enumSpecifierNode an enumSpecifier node
   * @return the enum name, or null if anonymous or not found
   */
  @CheckForNull
  public static String getEnumName(@Nullable AstNode enumSpecifierNode) {
    if (enumSpecifierNode == null) {
      return null;
    }
    AstNode enumHead = enumSpecifierNode.getFirstChild(CxxGrammarImpl.enumHead);
    if (enumHead == null) {
      return null;
    }
    AstNode enumHeadName = enumHead.getFirstChild(CxxGrammarImpl.enumHeadName);
    return getIdentifierName(enumHeadName);
  }

  /**
   * Extracts the enumerator nodes (one per enum constant) from an enumSpecifier node.
   *
   * @param enumSpecifierNode an enumSpecifier node
   * @return list of enumerator nodes, empty if none present
   */
  public static List<AstNode> getEnumerators(@Nullable AstNode enumSpecifierNode) {
    if (enumSpecifierNode == null) {
      return Collections.emptyList();
    }
    AstNode enumeratorList = enumSpecifierNode.getFirstChild(CxxGrammarImpl.enumeratorList);
    if (enumeratorList == null) {
      return Collections.emptyList();
    }
    List<AstNode> result = new ArrayList<>();
    for (AstNode enumeratorDefinition : enumeratorList.getChildren(CxxGrammarImpl.enumeratorDefinition)) {
      AstNode enumerator = enumeratorDefinition.getFirstChild(CxxGrammarImpl.enumerator);
      if (enumerator != null) {
        result.add(enumerator);
      }
    }
    return result;
  }

  /**
   * Checks whether an IDENTIFIER node is part of a declaration (declaratorId, memberDeclarator,
   * enumerator, classHeadName, enumHeadName ancestor, or the direct name-token child of an
   * aliasDeclaration), as opposed to being a usage reached through an expression
   * (primaryExpression/postfixExpression/idExpression not wrapped by one of those declaration
   * node types).
   *
   * <p>{@code className} is deliberately not checked directly: it is the generic
   * identifier-shaped-like-a-class-name grammar rule, used both at a class/struct/union's own
   * definition site ({@code classHead -> classHeadName -> className}) and at any ordinary
   * reference to an existing type by name ({@code typeName -> className}, e.g. the right-hand
   * side of {@code using Alias = SomeExistingType;}). Only {@code classHeadName} distinguishes
   * the definition-site shape; checking {@code className} directly would treat every type
   * reference as a declaration site and never register it as a usage.
   *
   * <p>{@code aliasDeclaration}'s own name ({@code USING IDENTIFIER ... "=" definingTypeId ";"})
   * is a bare IDENTIFIER token with no wrapper node of its own, unlike every other declaration
   * shape this method recognizes -- so it is checked only when the identifier is a direct child
   * of aliasDeclaration, not via the general ancestor walk below. A general ancestor check would
   * incorrectly also suppress genuine identifier usages inside the RHS definingTypeId (e.g. a
   * type name referenced in {@code using Alias = SomeType;}), which are real usages, not
   * declarations.
   *
   * @param identifierNode an IDENTIFIER token node
   * @return true if this identifier occurrence is a declaration site, not a usage
   */
  public static boolean isInsideDeclarator(@Nullable AstNode identifierNode) {
    if (identifierNode == null) {
      return false;
    }
    AstNode parent = identifierNode.getParent();
    if (parent != null && parent.is(CxxGrammarImpl.aliasDeclaration)
        && parent.getFirstChild(GenericTokenType.IDENTIFIER) == identifierNode) {
      return true;
    }
    AstNode current = parent;
    while (current != null) {
      if (current.is(CxxGrammarImpl.declaratorId)
        || current.is(CxxGrammarImpl.memberDeclarator)
        || current.is(CxxGrammarImpl.enumerator)
        || current.is(CxxGrammarImpl.classHeadName)
        || current.is(CxxGrammarImpl.enumHeadName)) {
        return true;
      }
      if (current.is(CxxGrammarImpl.primaryExpression) || current.is(CxxGrammarImpl.postfixExpression)) {
        return false;
      }
      current = current.getParent();
    }
    return false;
  }

  /**
   * Find the first ancestor of a node that matches the given type.
   */
  @CheckForNull
  private static AstNode getFirstAncestor(@Nullable AstNode node, AstNodeType type) {
    if (node == null) {
      return null;
    }
    AstNode current = node.getParent();
    while (current != null) {
      if (current.is(type)) {
        return current;
      }
      current = current.getParent();
    }
    return null;
  }
}
