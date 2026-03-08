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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.checks;

import static org.assertj.core.api.Assertions.*;
import com.sonar.cxx.sslr.api.AstNode;
import org.sonar.cxx.squidbridge.CommentAnalyser;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;
import org.sonar.cxx.squidbridge.api.CheckMessage;
import org.sonar.cxx.squidbridge.api.CodeCheck;
import org.sonar.cxx.squidbridge.api.PreciseIssue;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.api.batch.fs.InputFile;

class SquidCheckTest {

  @Test
  void testGetKey() {
    var squidCheck = new SquidCheck() {
    };
    assertThat(squidCheck.getKey()).isNull();
  }

  @Test
  void testAddIssueWithNodeAndMessage() {
    var check = new TestSquidCheck();
    var context = new StubContext();
    check.setContext(context);

    var node = createNode("test");
    var issue = check.addIssue(node, "Test message");

    assertThat(issue).isNotNull();
    assertThat(issue.getMessage()).isEqualTo("Test message");
    assertThat(issue.getPrimaryNode()).isSameAs(node);
    assertThat(context.getIssues()).hasSize(1);
    assertThat(context.getIssues().get(0)).isSameAs(issue);
  }

  @Test
  void testAddIssueWithPreciseIssue() {
    var check = new TestSquidCheck();
    var context = new StubContext();
    check.setContext(context);

    var node = createNode("test");
    var issue = new PreciseIssue(check, node, "Pre-built issue");
    check.addIssue(issue);

    assertThat(context.getIssues()).hasSize(1);
    assertThat(context.getIssues().get(0)).isSameAs(issue);
  }

  @Test
  void testCreateIssueDoesNotAddToContext() {
    var check = new TestSquidCheck();
    var context = new StubContext();
    check.setContext(context);

    var node = createNode("test");
    var issue = check.createIssue(node, "Created but not added");

    assertThat(issue).isNotNull();
    assertThat(issue.getMessage()).isEqualTo("Created but not added");
    // createIssue should NOT add to context
    assertThat(context.getIssues()).isEmpty();
  }

  @Test
  void testAddIssueReturnsIssueForChaining() {
    var check = new TestSquidCheck();
    var context = new StubContext();
    check.setContext(context);

    var node = createNode("test");
    var primaryNode = createNode("secondary");
    var issue = check.addIssue(node, "Main issue")
      .secondary(primaryNode, "Secondary location");

    assertThat(issue.hasSecondaryLocations()).isTrue();
    assertThat(issue.getSecondaryLocations()).hasSize(1);
    assertThat(issue.getSecondaryLocations().get(0).getMessage()).isEqualTo("Secondary location");
  }

  private AstNode createNode(String value) {
    var token = Token.builder()
      .setLine(1).setColumn(0)
      .setValueAndOriginalValue(value)
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
    return new AstNode(token);
  }

  private static class TestSquidCheck extends SquidCheck<Grammar> {
  }

  private static class TestTokenType implements TokenType {
    @Override public String getName() { return "TEST"; }
    @Override public String getValue() { return "test"; }
    @Override public boolean hasToBeSkippedFromAst(AstNode node) { return false; }
  }

  @SuppressWarnings("rawtypes")
  private static class StubContext extends SquidAstVisitorContext<Grammar> {
    @Override public File getFile() { return null; }
    @Override public InputFile getInputFile() { return null; }
    @Override public String getInputFileContent() { return ""; }
    @Override public List<String> getInputFileLines() { return List.of(); }
    @Override public Grammar getGrammar() { return null; }
    @Override public void addSourceCode(SourceCode child) {}
    @Override public void popSourceCode() {}
    @Override public SourceCode peekSourceCode() { return null; }
    @Override public CommentAnalyser getCommentAnalyser() { return null; }
    @Override public void createFileViolation(CodeCheck check, String message, Object... params) {}
    @Override public void createLineViolation(CodeCheck check, String message, AstNode node, Object... params) {}
    @Override public void createLineViolation(CodeCheck check, String message, Token token, Object... params) {}
    @Override public void createLineViolation(CodeCheck check, String message, int line, Object... params) {}
    @Override public void log(CheckMessage message) {}
  }

}
