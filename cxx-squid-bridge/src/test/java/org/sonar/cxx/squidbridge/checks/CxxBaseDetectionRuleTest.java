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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.cxx.squidbridge.CommentAnalyser;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.AstNodeTypeExtension;
import org.sonar.cxx.squidbridge.api.CheckMessage;
import org.sonar.cxx.squidbridge.api.CodeCheck;
import org.sonar.cxx.squidbridge.api.PreciseIssue;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceCodeSymbol;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.Type;

class CxxBaseDetectionRuleTest {

  @AfterEach
  void cleanup() {
    AstNodeSymbolExtension.clear();
    AstNodeTypeExtension.clear();
  }

  @Test
  void testSubscribedNodeTypesDefault() {
    var rule = new TestDetectionRule();
    assertThat(rule.subscribedNodeTypes()).isEmpty();
  }

  @Test
  void testSubscribedNodeTypesOverride() {
    var type1 = new TestNodeType("TYPE_A");
    var rule = new TestDetectionRule() {
      @Override
      protected AstNodeType[] subscribedNodeTypes() {
        return new AstNodeType[] { type1 };
      }
    };
    assertThat(rule.subscribedNodeTypes()).hasSize(1);
  }

  @Test
  void testTraverseSubtreeVisitsAllNodes() {
    var rule = new TestDetectionRule();
    var root = createNode(new TestNodeType("ROOT"), "root");
    var child = createNode(new TestNodeType("CHILD"), "child");
    root.addChild(child);

    List<String> visited = new ArrayList<>();
    rule.traverseSubtree(root, node -> visited.add(node.getTokenValue()));

    assertThat(visited).containsExactly("root", "child");
  }

  @Test
  void testTraverseSubtreeWithTypeFilter() {
    var rule = new TestDetectionRule();
    var typeA = new TestNodeType("TYPE_A");
    var typeB = new TestNodeType("TYPE_B");

    var root = createNode(typeA, "root");
    var child1 = createNode(typeB, "child1");
    var child2 = createNode(typeA, "child2");
    root.addChild(child1);
    root.addChild(child2);

    List<String> visited = new ArrayList<>();
    rule.traverseSubtree(root, new AstNodeType[] { typeB },
        node -> visited.add(node.getTokenValue()));

    assertThat(visited).containsExactly("child1");
  }

  @Test
  void testTraverseSubtreeNullRoot() {
    var rule = new TestDetectionRule();
    List<String> visited = new ArrayList<>();
    // Should not throw
    rule.traverseSubtree(null, node -> visited.add(node.getTokenValue()));
    assertThat(visited).isEmpty();
  }

  @Test
  void testVisitFunctionDefinitionDefault() {
    var rule = new TestDetectionRule();
    var node = createNode(new TestNodeType("functionDefinition"), "funcDef");
    // Should not throw - default implementation is empty
    rule.visitFunctionDefinition(node);
  }

  @Test
  void testVisitFunctionCallDefault() {
    var rule = new TestDetectionRule();
    var node = createNode(new TestNodeType("postfixExpression"), "funcCall");
    // Should not throw - default implementation is empty
    rule.visitFunctionCall(node);
  }

  @Test
  void testVisitNewExpressionDefault() {
    var rule = new TestDetectionRule();
    var node = createNode(new TestNodeType("newExpression"), "newExpr");
    // Should not throw - default implementation is empty
    rule.visitNewExpression(node);
  }

  @Test
  void testProcessNodeDuringTraversalDispatchesFunctionCall() {
    var calls = new ArrayList<String>();
    var rule = new TestDetectionRule() {
      @Override
      protected void visitFunctionCall(AstNode callNode) {
        calls.add("functionCall:" + callNode.getTokenValue());
      }
    };

    // Create a postfixExpression with "(" child to simulate a function call
    var postfixType = new TestNodeType("postfixExpression");
    var postfixNode = createNode(postfixType, "func");
    var parenToken = Token.builder()
        .setLine(1)
        .setColumn(4)
        .setValueAndOriginalValue("(")
        .setType(new TestTokenType())
        .setURI(java.net.URI.create("file:///test.cpp"))
        .build();
    var parenNode = new AstNode(parenToken);
    postfixNode.addChild(parenNode);

    rule.processNodeDuringTraversal(postfixNode);

    assertThat(calls).containsExactly("functionCall:func");
  }

  @Test
  void testProcessNodeDuringTraversalDispatchesNewExpression() {
    var calls = new ArrayList<String>();
    var rule = new TestDetectionRule() {
      @Override
      protected void visitNewExpression(AstNode newExpr) {
        calls.add("newExpression:" + newExpr.getTokenValue());
      }
    };

    var newExprType = new TestNodeType("newExpression");
    var newExprNode = createNode(newExprType, "new");

    rule.processNodeDuringTraversal(newExprNode);

    assertThat(calls).containsExactly("newExpression:new");
  }

  @Test
  void testProcessNodeIgnoresUnrelatedTypes() {
    var calls = new ArrayList<String>();
    var rule = new TestDetectionRule() {
      @Override
      protected void visitFunctionCall(AstNode callNode) {
        calls.add("call");
      }

      @Override
      protected void visitNewExpression(AstNode newExpr) {
        calls.add("new");
      }
    };

    var otherType = new TestNodeType("someOtherType");
    var otherNode = createNode(otherType, "other");
    rule.processNodeDuringTraversal(otherNode);

    assertThat(calls).isEmpty();
  }

  @Test
  void testTraverseFunctionBodyNull() {
    var rule = new TestDetectionRule();
    assertThatCode(() -> rule.traverseFunctionBody(null)).doesNotThrowAnyException();
  }

  @Test
  void testTraverseFunctionBodyNoBody() {
    // functionDefinition with no "functionBody" child — traversal is a no-op
    var funcDef = createNode(new TestNodeType("functionDefinition"), "funcDef");
    var calls = new java.util.ArrayList<String>();
    var ruleWithSpy = new TestDetectionRule() {
      @Override
      protected void visitFunctionCall(AstNode callNode) {
        calls.add("call:" + callNode.getTokenValue());
      }
    };
    ruleWithSpy.traverseFunctionBody(funcDef);
    assertThat(calls).isEmpty();
  }

  @Test
  void testTraverseFunctionBodyDispatchesCallsInBody() {
    // Build: functionDef → [functionBody → [postfixExpression → ["func", "(",
    // ")"]]]
    var calls = new java.util.ArrayList<String>();
    var rule = new TestDetectionRule() {
      @Override
      protected void visitFunctionCall(AstNode callNode) {
        calls.add("call:" + callNode.getTokenValue());
      }
    };

    var postfixType = new TestNodeType("postfixExpression");
    var postfixNode = createNode(postfixType, "func");
    var parenToken = Token.builder()
        .setLine(1).setColumn(4)
        .setValueAndOriginalValue("(")
        .setType(new TestTokenType())
        .setURI(java.net.URI.create("file:///test.cpp"))
        .build();
    var parenNode = new AstNode(parenToken);
    postfixNode.addChild(parenNode);

    // "functionBody" type via string match
    var bodyNode = createNode(new TestNodeType("functionBody"), "body");
    bodyNode.addChild(postfixNode);

    var funcDef = createNode(new TestNodeType("functionDefinition"), "funcDef");
    funcDef.addChild(bodyNode);

    rule.traverseFunctionBody(funcDef);

    assertThat(calls).containsExactly("call:func");
  }

  private AstNode createNode(AstNodeType type, String value) {
    var token = Token.builder()
        .setLine(1)
        .setColumn(0)
        .setValueAndOriginalValue(value)
        .setType(new TestTokenType())
        .setURI(java.net.URI.create("file:///test.cpp"))
        .build();
    return new AstNode(type, value, token);
  }

  private static class TestDetectionRule extends CxxBaseDetectionRule {
  }

  private static class TestNodeType implements AstNodeType {
    private final String name;

    TestNodeType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @Test
  void testInitWithNoSubscribedTypes() {
    // init() with default (empty) subscribedNodeTypes does not call subscribeTo
    var rule = new TestDetectionRule();
    var ctx = new StubContext();
    rule.setContext(ctx);
    assertThatCode(() -> rule.init()).doesNotThrowAnyException();
  }

  @Test
  void testInitWithSubscribedTypes() {
    // init() with overridden subscribedNodeTypes subscribes to those types
    var typeA = new TestNodeType("TYPE_A");
    var rule = new TestDetectionRule() {
      @Override
      protected AstNodeType[] subscribedNodeTypes() {
        return new AstNodeType[] { typeA };
      }
    };
    var ctx = new StubContext();
    rule.setContext(ctx);
    assertThatCode(() -> rule.init()).doesNotThrowAnyException();
  }

  @Test
  void testReportIssue() {
    var rule = new TestDetectionRule();
    var ctx = new StubContext();
    rule.setContext(ctx);

    var node = createNode(new TestNodeType("expr"), "x");
    PreciseIssue issue = rule.reportIssue(node, "test issue message");

    assertThat(issue).isNotNull();
    assertThat(issue.getMessage()).isEqualTo("test issue message");
    assertThat(ctx.getIssues()).hasSize(1);
    assertThat(ctx.getIssues().get(0)).isSameAs(issue);
  }

  @Test
  void testGetSymbolTable() {
    var rule = new TestDetectionRule();
    var ctx = new StubContext();
    rule.setContext(ctx);

    assertThat(rule.getSymbolTable()).isNotNull();
  }

  @Test
  void testGetSymbol() {
    var rule = new TestDetectionRule();
    var ctx = new StubContext();
    rule.setContext(ctx);

    var node = mock(AstNode.class);
    var sym = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(node, sym);

    assertThat(rule.getSymbol(node)).isEqualTo(sym);
    assertThat(rule.getSymbol(null)).isNull();
  }

  @Test
  void testGetType() {
    var rule = new TestDetectionRule();
    var ctx = new StubContext();
    rule.setContext(ctx);

    var node = mock(AstNode.class);
    var type = new Type.CxxType("int");
    AstNodeTypeExtension.setType(node, type);

    assertThat(rule.getType(node)).isEqualTo(type);
    assertThat(rule.getType(null)).isNull();
  }

  private static class TestTokenType implements com.sonar.cxx.sslr.api.TokenType {
    @Override
    public String getName() {
      return "TEST";
    }

    @Override
    public String getValue() {
      return "test";
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }
  }

  @SuppressWarnings({"deprecation", "java:S1874"})
  private static class StubContext extends SquidAstVisitorContext<Grammar> {
    @Override
    public File getFile() {
      return null;
    }

    @Override
    public InputFile getInputFile() {
      return null;
    }

    @Override
    public String getInputFileContent() {
      return "";
    }

    @Override
    public List<String> getInputFileLines() {
      return List.of();
    }

    @Override
    public Grammar getGrammar() {
      return null;
    }

    @Override
    public void addSourceCode(SourceCode child) {
      // test stub
    }

    @Override
    public void popSourceCode() {
      // test stub
    }

    @Override
    public SourceCode peekSourceCode() {
      return null;
    }

    @Override
    public CommentAnalyser getCommentAnalyser() {
      return null;
    }

    @Override
    public void createFileViolation(CodeCheck check, String message, Object... params) {
      // test stub
    }

    @Override
    public void createLineViolation(CodeCheck check, String message, AstNode node, Object... params) {
      // test stub
    }

    @Override
    public void createLineViolation(CodeCheck check, String message, Token token, Object... params) {
      // test stub
    }

    @Override
    public void createLineViolation(CodeCheck check, String message, int line, Object... params) {
      // test stub
    }

    @Override
    public void log(CheckMessage message) {
      // test stub
    }
  }
}
