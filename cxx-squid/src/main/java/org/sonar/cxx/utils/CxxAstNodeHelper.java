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
import org.sonar.cxx.parser.CxxPunctuator;
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
   * <p>For a chain such as {@code foo(a).bar(x)}, returns the arguments of the last call ({@code bar}).
   *
   * @param node a postfixExpression that represents a function call
   * @return list of argument expression nodes, empty if no arguments
   */
  public static List<AstNode> getFunctionCallArguments(@Nullable AstNode node) {
    if (node == null || !isFunctionCall(node)) {
      return Collections.emptyList();
    }
    var children = node.getChildren();
    int open = lastCallParenthesis(node);
    AstNode arguments = open + 1 < children.size() ? children.get(open + 1) : null;
    if (arguments == null || !arguments.is(CxxGrammarImpl.expressionList)) {
      return Collections.emptyList();
    }
    AstNode initializerList = arguments.getFirstChild(CxxGrammarImpl.initializerList);
    if (initializerList == null) {
      return arguments.getChildren();
    }
    return initializerList.getChildren().stream()
      .filter(argument -> !argument.is(CxxPunctuator.COMMA))
      .toList();
  }

  /**
   * Gets the index of the opening parenthesis of the last call in a postfixExpression.
   *
   * @param node a postfixExpression node
   * @return index of the parenthesis among the children of the node, -1 if it has none
   */
  private static int lastCallParenthesis(AstNode node) {
    var children = node.getChildren();
    for (int i = children.size() - 1; i >= 0; i--) {
      if (children.get(i).is(CxxPunctuator.BR_LEFT)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Check if a node is a member access operator ({@code .} or {@code ->}).
   */
  private static boolean isMemberAccessOperator(AstNode node) {
    return node.is(CxxPunctuator.DOT, CxxPunctuator.ARROW);
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
    if (isFunctionCall(node)) {
      // the called expression is what precedes the last call's parenthesis
      int open = lastCallParenthesis(node);
      var children = node.getChildren();
      if (open >= 2 && isMemberAccessOperator(children.get(open - 2))) {
        // member call: obj.method(...), p->q->method(...), foo(a).method(...)
        AstNode member = children.get(open - 1);
        return member.is(GenericTokenType.IDENTIFIER)
          ? member.getTokenValue()
          : getIdentifierText(member);
      }
      if (open != 1) {
        // the result of an expression is called, e.g. f(a)(b)
        return null;
      }
      return getCalleeName(children.get(0));
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
   * Gets the name of the called function ({@code foo}, {@code ns::foo}), also when the grammar
   * parses it as a type (typeName, simpleTypeSpecifier).
   *
   * @param callee the expression in front of the call's parenthesis
   * @return the function name, or null if the callee is not a name
   */
  @CheckForNull
  private static String getCalleeName(AstNode callee) {
    if (callee.is(GenericTokenType.IDENTIFIER)) {
      return callee.getTokenValue();
    }
    AstNode idExpr = callee.is(CxxGrammarImpl.idExpression)
      ? callee : callee.getFirstDescendant(CxxGrammarImpl.idExpression);
    if (idExpr != null) {
      return getIdentifierText(idExpr);
    }
    if (callee.is(CxxGrammarImpl.qualifiedId)) {
      return getIdentifierText(callee);
    }
    if (callee.is(CxxGrammarImpl.simpleTypeSpecifier)) {
      // qualified name parsed as a type, e.g. ns::foo(...)
      var sb = new StringBuilder();
      callee.getTokens().forEach(token -> sb.append(token.getValue()));
      return sb.toString();
    }
    AstNode typeName = callee.is(CxxGrammarImpl.typeName) ? callee : null;
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
    List<AstNode> parameters = paramDeclList.getChildren(CxxGrammarImpl.parameterDeclaration);
    if (parameters.size() == 1 && declaresNoParameter(parameters.get(0))) {
      return Collections.emptyList();
    }
    return parameters;
  }

  /**
   * Whether the only parameter declaration of a parameter list declares no parameter: the parser
   * reads an empty list, {@code f()}, as one empty parameter declaration, and {@code f(void)}
   * declares no parameter either.
   */
  private static boolean declaresNoParameter(AstNode parameterDeclaration) {
    if (parameterDeclaration.getNumberOfChildren() != 1) {
      return false;
    }
    AstNode type = parameterDeclaration.getFirstChild();
    if (type.is(CxxGrammarImpl.parameterDeclSpecifierSeq) && !type.hasChildren()) {
      return true;
    }
    return type.getToken() == type.getLastToken() && type.getToken().getType() == CxxKeyword.VOID;
  }

  /**
   * Whether a declarator declares a function ({@code int f(int)}, {@code int *f(int)}), not a
   * variable, a function pointer ({@code int (*fp)(int)}) or an array ({@code int (*a[2])(int)}).
   *
   * @param declaratorNode a declarator node, or null
   * @return true if this declarator declares a function
   */
  public static boolean isFunctionDeclarator(@Nullable AstNode declaratorNode) {
    AstNode node = getDeclaratorId(declaratorNode);
    if (node == null) {
      return false;
    }
    while (node != declaratorNode) {
      AstNode parent = node.getParent();
      if (parent.is(CxxGrammarImpl.ptrDeclarator) && parent.hasDirectChildren(CxxGrammarImpl.ptrOperator)) {
        return false; // a pointer or reference to what follows
      }
      // a declarator holds the parameter list itself when it has a trailing return type
      if (parent.is(CxxGrammarImpl.noptrDeclarator, CxxGrammarImpl.declarator)) {
        AstNode next = node.getNextSibling();
        while (next != null && next.is(CxxPunctuator.BR_RIGHT)) {
          next = next.getNextSibling(); // the end of a grouping parenthesis
        }
        if (next != null && next.is(CxxGrammarImpl.parametersAndQualifiers)) {
          return true;
        }
        if (next != null && next.is(CxxPunctuator.SQBR_LEFT)) {
          return false; // an array of what follows
        }
      }
      node = parent;
    }
    return false;
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
    // exprOrBracedInitList is a skipIfOneChild rule, so the returned expression is the child after
    // the return keyword, unless the statement ends there
    AstNode expression = node.getFirstChild().getNextSibling();
    return expression == null || expression.is(CxxPunctuator.SEMICOLON) ? null : expression;
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
    // the member following the last . or -> operator (a->b->c accesses c)
    var children = node.getChildren();
    for (int i = children.size() - 2; i >= 0; i--) {
      if (isMemberAccessOperator(children.get(i))) {
        AstNode member = children.get(i + 1);
        if (member.is(GenericTokenType.IDENTIFIER)) {
          return member.getTokenValue();
        }
        AstNode id = member.getFirstDescendant(GenericTokenType.IDENTIFIER);
        return id != null ? id.getTokenValue() : member.getTokenValue();
      }
    }
    return null;
  }

  /**
   * Check if a member access is applied to the result of a call, e.g. {@code foo(a).bar(x)} or
   * {@code make()->use()}, as in a builder pattern.
   *
   * @param node a postfixExpression node
   * @return true if a call precedes the last member access operator of the expression
   */
  public static boolean isCallOnCallResult(@Nullable AstNode node) {
    if (node == null || !node.is(CxxGrammarImpl.postfixExpression)) {
      return false;
    }
    var children = node.getChildren();
    int lastOperator = -1;
    for (int i = children.size() - 1; i >= 0; i--) {
      if (isMemberAccessOperator(children.get(i))) {
        lastOperator = i;
        break;
      }
    }
    for (int i = 0; i < lastOperator; i++) {
      if (children.get(i).is(CxxPunctuator.BR_LEFT)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the symbol of the variable an expression is being assigned to, via either an
   * assignment ({@code x = expr}) or a declaration with initializer ({@code auto x = expr}).
   *
   * @param expressionNode an expression node (e.g., a function call)
   * @return the symbol of the variable being assigned to, or null if none
   */
  @CheckForNull
  public static Symbol getAssignedSymbol(@Nullable AstNode expressionNode) {
    if (expressionNode == null) {
      return null;
    }

    // Case 1: Assignment expression (x = expr)
    AstNode assignmentExpr = getFirstAncestor(expressionNode, CxxGrammarImpl.assignmentExpression);
    if (assignmentExpr != null) {
      // A pointer declaration read as an assignment to a product (T *x = expr)
      AstNode declaredPointer = getPointerDeclarationIdentifierOf(assignmentExpr);
      if (declaredPointer != null) {
        return AstNodeSymbolExtension.getSymbol(declaredPointer);
      }
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
   * Checks if a function call (e.g. {@code obj.method()}) is invoked on the given variable.
   *
   * @param callNode a postfixExpression node representing a function call
   * @param variableSymbol the variable symbol to check against
   * @param acceptParentMemberAccess if true, also check parent chains (e.g. {@code a.b.method()})
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
   * Extracts the class/struct name referenced by a variable, data member or parameter's declared
   * type (e.g. {@code "S"} for {@code S s;}, {@code const S &s} or {@code struct S *s}).
   * {@code declSpecifierSeq}/{@code memberDeclSpecifierSeq}/{@code parameterDeclSpecifierSeq}
   * are {@code .skipIfOneChild()} rules, so both shapes are searched for directly.
   *
   * @param declaringNode a simpleDeclaration, memberDeclaration or parameterDeclaration node
   * @return the referenced class/struct name, or null if not a class/struct reference
   */
  @CheckForNull
  public static String getDeclaredClassTypeName(@Nullable AstNode declaringNode) {
    if (declaringNode == null) {
      return null;
    }
    AstNode specifierNode = declaringNode.getFirstChild(
      CxxGrammarImpl.declSpecifierSeq, CxxGrammarImpl.declSpecifier,
      CxxGrammarImpl.memberDeclSpecifierSeq, CxxGrammarImpl.parameterDeclSpecifierSeq);
    if (specifierNode == null) {
      return null;
    }
    AstNode className = specifierNode.getFirstDescendant(CxxGrammarImpl.className);
    if (className != null) {
      return getIdentifierName(className);
    }
    // an elaborated type specifier names the class without a className node, e.g. "struct S"
    AstNode elaboratedTypeSpecifier = specifierNode.getFirstDescendant(CxxGrammarImpl.elaboratedTypeSpecifier);
    if (elaboratedTypeSpecifier == null || !elaboratedTypeSpecifier.hasDirectChildren(CxxGrammarImpl.classKey)) {
      return null;
    }
    AstNode name = elaboratedTypeSpecifier.getLastChild(GenericTokenType.IDENTIFIER);
    return name != null ? name.getTokenValue() : null;
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
   * enumerator, classHeadName ancestor, or a direct child of aliasDeclaration), as opposed to a
   * usage. {@code className} is not checked directly -- it also matches ordinary type references,
   * only {@code classHeadName} distinguishes a real definition site.
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
   * Gets the untyped parameters ({@code name} in {@code T x(name);}) the parser reads when constructor
   * arguments look like a function declaration; they are arguments if the names name values.
   *
   * @param declaratorNode the declarator of an initDeclarator
   * @return the parameterDeclaration nodes, or an empty list if the declarator does not have this form
   */
  public static List<AstNode> getUntypedParameters(@Nullable AstNode declaratorNode) {
    if (declaratorNode == null || !declaratorNode.is(CxxGrammarImpl.declarator)) {
      return Collections.emptyList();
    }
    AstNode parent = declaratorNode.getParent();
    if (parent == null || !parent.is(CxxGrammarImpl.initDeclarator)) {
      return Collections.emptyList();
    }
    AstNode noptrDeclarator = declaratorNode.getFirstChild(CxxGrammarImpl.noptrDeclarator);
    if (noptrDeclarator == null
      || !noptrDeclarator.hasDirectChildren(CxxGrammarImpl.declaratorId)
      || !noptrDeclarator.hasDirectChildren(CxxGrammarImpl.parametersAndQualifiers)) {
      return Collections.emptyList();
    }
    List<AstNode> parameters = getDeclaratorParameters(noptrDeclarator);
    for (AstNode parameter : parameters) {
      if (getUntypedParameterName(parameter) == null) {
        return Collections.emptyList();
      }
    }
    return parameters;
  }

  /**
   * Gets the name a parameter declaration without a type declares, {@code name} in
   * {@code T x(name);}.
   *
   * @param parameterDeclarationNode a parameterDeclaration node
   * @return the IDENTIFIER node of the name, or null if the parameter has a type or no name
   */
  @CheckForNull
  public static AstNode getUntypedParameterName(@Nullable AstNode parameterDeclarationNode) {
    if (parameterDeclarationNode == null || !parameterDeclarationNode.is(CxxGrammarImpl.parameterDeclaration)) {
      return null;
    }
    AstNode type = parameterDeclarationNode.getFirstChild(CxxGrammarImpl.parameterDeclSpecifierSeq);
    AstNode declarator = parameterDeclarationNode.getFirstChild(CxxGrammarImpl.declarator);
    if (type == null || type.hasChildren() || declarator == null || declarator.getNumberOfChildren() != 1) {
      return null;
    }
    AstNode declaratorId = declarator.getFirstChild(CxxGrammarImpl.declaratorId);
    AstNode name = declaratorId != null ? declaratorId.getFirstChild() : null;
    return name != null && name.is(GenericTokenType.IDENTIFIER) ? name : null;
  }

  /**
   * Whether an IDENTIFIER node is the name of one of the untyped parameters of a declarator, see
   * {@link #getUntypedParameters}.
   *
   * @param identifierNode an IDENTIFIER token node
   * @return true if the identifier is the name of such a parameter
   */
  public static boolean isUntypedParameterName(@Nullable AstNode identifierNode) {
    AstNode parameter = identifierNode != null
      ? identifierNode.getFirstAncestor(CxxGrammarImpl.parameterDeclaration) : null;
    if (parameter == null || getUntypedParameterName(parameter) != identifierNode) {
      return false;
    }
    AstNode declarator = parameter.getFirstAncestor(CxxGrammarImpl.declarator);
    return declarator != null && getUntypedParameters(declarator).contains(parameter);
  }

  /**
   * Gets the variables of a pointer declaration ({@code T *x = init, *y;}) that the parser read as a
   * multiplication because {@code T} is not declared in the parsed code.
   *
   * @param expressionStatementNode an expressionStatement node
   * @return the IDENTIFIER nodes of the declared variables, or an empty list if not of this form
   */
  public static List<AstNode> getPointerDeclarationIdentifiers(@Nullable AstNode expressionStatementNode) {
    if (expressionStatementNode == null || !expressionStatementNode.is(CxxGrammarImpl.expressionStatement)) {
      return Collections.emptyList();
    }
    AstNode expression = expressionStatementNode.getFirstChild(CxxGrammarImpl.expression);
    if (expression == null) {
      return Collections.emptyList();
    }
    List<AstNode> declared = new ArrayList<>();
    for (AstNode operand : expression.getChildren()) {
      if (operand.is(CxxPunctuator.COMMA)) {
        continue;
      }
      AstNode identifier = declared.isEmpty()
        ? firstPointerDeclarator(declaratorOf(operand))
        : dereferencedIdentifier(declaratorOf(operand));
      if (identifier == null) {
        return Collections.emptyList();
      }
      declared.add(identifier);
    }
    return declared;
  }

  /**
   * Gets the variable declared by the operand of a pointer declaration read as an expression
   * statement (see {@link #getPointerDeclarationIdentifiers}) that contains the given node, e.g.
   * {@code y} for the call {@code g()} in {@code T *x = f(), *y = g();}.
   *
   * @param node a node inside the statement
   * @return the IDENTIFIER node of the variable, or null if the node is not inside such a
   *   declaration
   */
  @CheckForNull
  public static AstNode getPointerDeclarationIdentifierOf(@Nullable AstNode node) {
    AstNode statement = getFirstAncestor(node, CxxGrammarImpl.expressionStatement);
    List<AstNode> declared = getPointerDeclarationIdentifiers(statement);
    if (declared.isEmpty()) {
      return null;
    }
    AstNode expression = statement.getFirstChild(CxxGrammarImpl.expression);
    int index = 0;
    for (AstNode operand : expression.getChildren()) {
      if (operand.is(CxxPunctuator.COMMA)) {
        continue;
      }
      if (isSelfOrAncestor(operand, node)) {
        return declared.get(index);
      }
      index++;
    }
    return null;
  }

  private static boolean isSelfOrAncestor(AstNode ancestor, AstNode node) {
    for (AstNode current = node; current != null; current = current.getParent()) {
      if (current == ancestor) {
        return true;
      }
    }
    return false;
  }

  /** The declarator of an operand: the target of a plain assignment, or the operand itself. */
  private static AstNode declaratorOf(AstNode operand) {
    if (operand.is(CxxGrammarImpl.assignmentExpression)) {
      AstNode operator = operand.getFirstChild(CxxGrammarImpl.assignmentOperator);
      return operator != null && operator.hasDirectChildren(CxxPunctuator.ASSIGN) ? operand.getFirstChild() : null;
    }
    return operand;
  }

  /** The variable of the first declarator, {@code T *x} read as a product of {@code T} and {@code *x}. */
  @CheckForNull
  private static AstNode firstPointerDeclarator(@Nullable AstNode product) {
    if (product == null || !product.is(CxxGrammarImpl.multiplicativeExpression) || product.getNumberOfChildren() != 3
        || !product.getFirstChild().is(GenericTokenType.IDENTIFIER)
        || !product.getChildren().get(1).is(CxxPunctuator.MUL)) {
      return null;
    }
    return dereferencedIdentifier(product.getLastChild());
  }

  /** The identifier of {@code x}, {@code *x} or {@code **x}, or null for any other expression. */
  @CheckForNull
  private static AstNode dereferencedIdentifier(@Nullable AstNode declarator) {
    AstNode declared = declarator;
    while (declared != null && declared.is(CxxGrammarImpl.unaryExpression) && declared.getNumberOfChildren() == 2
        && declared.getFirstChild().is(CxxGrammarImpl.unaryOperator)
        && declared.getFirstChild().hasDirectChildren(CxxPunctuator.MUL)) {
      declared = declared.getLastChild();
    }
    return declared != null && declared.is(GenericTokenType.IDENTIFIER) ? declared : null;
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
