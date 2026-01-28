/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.checks;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.sonar.cxx.squidbridge.api.AstNodeTraversal;
import org.sonar.cxx.squidbridge.api.PreciseIssue;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.SymbolTable;
import org.sonar.cxx.squidbridge.api.Type;

/**
 * Base class for detection rules that analyze C++ code for specific patterns.
 *
 * <p>This class bridges the gap between the sonar-cxx check framework
 * ({@link SquidCheck}) and the CBOM (Cryptographic Bill of Materials) detection
 * engine pattern. It extends SquidCheck with convenience methods for:
 *
 * <ul>
 *   <li>Subtree traversal (compensating for the missing {@code accept(Visitor)} pattern)</li>
 *   <li>Function body analysis</li>
 *   <li>Type-based matching</li>
 *   <li>Symbol table access</li>
 * </ul>
 *
 * <p>Detection rules should extend this class and override the appropriate methods
 * for their detection pattern:
 *
 * <ul>
 *   <li>{@link #visitFunctionDefinition(AstNode)} for function-level analysis</li>
 *   <li>{@link #visitFunctionCall(AstNode)} for function call detection</li>
 *   <li>{@link #visitNewExpression(AstNode)} for constructor call detection</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * public class WeakCipherCheck extends CxxBaseDetectionRule {
 *     &#64;Override
 *     protected void visitFunctionCall(AstNode callNode) {
 *         String name = getFunctionCallName(callNode);
 *         if ("EVP_des_ecb".equals(name)) {
 *             addIssue(callNode, "Weak cipher: DES ECB mode detected");
 *         }
 *     }
 * }
 * </pre>
 */
public abstract class CxxBaseDetectionRule extends SquidCheck<Grammar> {

  /**
   * Override this method to specify the AST node types that trigger analysis.
   *
   * <p>The default implementation subscribes to function definitions,
   * which then traverses the function body for function calls and new expressions.
   * Override this to add additional node types or change the subscription pattern.
   *
   * @return array of node types to subscribe to
   */
  protected AstNodeType[] subscribedNodeTypes() {
    return new AstNodeType[0];
  }

  @Override
  public void init() {
    AstNodeType[] types = subscribedNodeTypes();
    if (types.length > 0) {
      subscribeTo(types);
    }
  }

  /**
   * Called for each function definition encountered in the file.
   *
   * <p>Override this method to implement function-level analysis.
   * The default implementation does nothing.
   *
   * @param functionDef the functionDefinition AstNode
   */
  protected void visitFunctionDefinition(AstNode functionDef) {
    // Override in subclasses
  }

  /**
   * Called for each function call encountered during subtree traversal.
   *
   * <p>Override this method to implement function call detection.
   * This method is called by {@link #traverseFunctionBody(AstNode)} during
   * subtree traversal.
   *
   * @param callNode the postfixExpression AstNode representing a function call
   */
  protected void visitFunctionCall(AstNode callNode) {
    // Override in subclasses
  }

  /**
   * Called for each new-expression (constructor call) encountered during subtree traversal.
   *
   * <p>Override this method to implement constructor call detection.
   *
   * @param newExpr the newExpression AstNode
   */
  protected void visitNewExpression(AstNode newExpr) {
    // Override in subclasses
  }

  /**
   * Traverse a subtree and invoke the visitor callback for all nodes.
   *
   * @param root the root of the subtree
   * @param visitor the callback to invoke for each node
   */
  protected void traverseSubtree(@Nullable AstNode root, Consumer<AstNode> visitor) {
    AstNodeTraversal.traverse(root, visitor);
  }

  /**
   * Traverse a subtree and invoke the visitor callback only for nodes matching
   * the specified types.
   *
   * @param root the root of the subtree
   * @param types the node types to match
   * @param visitor the callback to invoke for matching nodes
   */
  protected void traverseSubtree(@Nullable AstNode root, AstNodeType[] types,
                                  Consumer<AstNode> visitor) {
    AstNodeTraversal.traverse(root, types, visitor);
  }

  /**
   * Traverse a function body, dispatching to visitFunctionCall and visitNewExpression.
   *
   * <p>This method implements the IBaseMethodVisitor pattern: given a function
   * definition node, it traverses the function body and dispatches matching
   * nodes to the appropriate visit methods.
   *
   * @param functionDef the functionDefinition AstNode
   */
  protected void traverseFunctionBody(@Nullable AstNode functionDef) {
    if (functionDef == null) {
      return;
    }
    // Find the function body
    AstNode body = functionDef.getFirstChild(findNodeType("functionBody"));
    if (body == null) {
      // Try to find the body as a descendant
      for (AstNode child : functionDef.getChildren()) {
        if ("functionBody".equals(child.getType().toString())) {
          body = child;
          break;
        }
      }
    }
    if (body == null) {
      return;
    }
    AstNodeTraversal.traverse(body, node -> {
      processNodeDuringTraversal(node);
    });
  }

  /**
   * Process a node during function body traversal.
   *
   * <p>This method is called for every node during function body traversal.
   * The default implementation dispatches to visitFunctionCall and visitNewExpression
   * based on the node type. Override to add additional dispatch logic.
   *
   * @param node the current node being visited
   */
  protected void processNodeDuringTraversal(AstNode node) {
    // Check for function calls (postfixExpression with parenthesized arguments)
    if ("postfixExpression".equals(node.getType().toString())) {
      for (AstNode child : node.getChildren()) {
        if ("(".equals(child.getTokenValue())) {
          visitFunctionCall(node);
          break;
        }
      }
    }
    // Check for new expressions
    if ("newExpression".equals(node.getType().toString())) {
      visitNewExpression(node);
    }
  }

  /**
   * Get the symbol table for the current file.
   *
   * @return the current file's symbol table
   */
  protected SymbolTable getSymbolTable() {
    return getContext().getSymbolTable();
  }

  /**
   * Get the symbol associated with an AST node.
   *
   * @param node the AST node
   * @return the associated symbol, or null if none
   */
  @Nullable
  protected Symbol getSymbol(@Nullable AstNode node) {
    if (node == null) {
      return null;
    }
    return getContext().getSymbol(node);
  }

  /**
   * Get the type associated with an AST node.
   *
   * @param node the AST node (typically an expression)
   * @return the associated type, or null if no type information
   */
  @Nullable
  protected Type getType(@Nullable AstNode node) {
    if (node == null) {
      return null;
    }
    return org.sonar.cxx.squidbridge.api.AstNodeTypeExtension.getType(node);
  }

  /**
   * Report a precise issue with optional secondary locations.
   *
   * @param node the primary node where the issue occurs
   * @param message the issue message
   * @return the PreciseIssue for further configuration
   */
  protected PreciseIssue reportIssue(AstNode node, String message) {
    return addIssue(node, message);
  }

  /**
   * Try to find a node type by its string name.
   * This is used as a fallback when the specific grammar enum is not available.
   */
  private AstNodeType findNodeType(String name) {
    return new AstNodeType() {
      @Override
      public String toString() {
        return name;
      }
    };
  }
}
