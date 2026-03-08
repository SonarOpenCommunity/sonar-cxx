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
package org.sonar.cxx.squidbridge;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.CheckMessage;
import org.sonar.cxx.squidbridge.api.CodeCheck;
import org.sonar.cxx.squidbridge.api.PreciseIssue;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceCodeSymbol;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.SymbolTable;

class SquidAstVisitorContextTest {

  @AfterEach
  void cleanup() {
    AstNodeSymbolExtension.clear();
  }

  @Test
  void testGetSymbolTableCreatesIfAbsent() {
    var ctx = new StubContext();
    SymbolTable table = ctx.getSymbolTable();
    assertThat(table).isNotNull();
    // Second call returns same instance
    assertThat(ctx.getSymbolTable()).isSameAs(table);
  }

  @Test
  void testSetAndGetSymbolTable() {
    var ctx = new StubContext();
    var table = new SymbolTable();
    ctx.setSymbolTable(table);
    assertThat(ctx.getSymbolTable()).isSameAs(table);
  }

  @Test
  void testGetSemanticModelDelegatesToSymbolTable() {
    var ctx = new StubContext();
    var table = new SymbolTable();
    ctx.setSymbolTable(table);
    assertThat(ctx.getSemanticModel()).isSameAs(table);
  }

  @Test
  void testSetAndGetSymbol() {
    var ctx = new StubContext();
    var node = mock(AstNode.class);
    var sym = new SourceCodeSymbol("x", Symbol.Kind.VARIABLE, null);
    ctx.setSymbol(node, sym);
    assertThat(ctx.getSymbol(node)).isEqualTo(sym);
  }

  @Test
  void testGetSymbolReturnsNullWhenNotSet() {
    var ctx = new StubContext();
    var node = mock(AstNode.class);
    assertThat(ctx.getSymbol(node)).isNull();
  }

  @Test
  void testSetAndGetTree() {
    var ctx = new StubContext();
    assertThat(ctx.getTree()).isNull();

    var node = mock(AstNode.class);
    ctx.setTree(node);
    assertThat(ctx.getTree()).isSameAs(node);
  }

  @Test
  void testAddIssueAndGetIssues() {
    var ctx = new StubContext();
    assertThat(ctx.getIssues()).isEmpty();

    var node = mock(AstNode.class);
    var issue = new PreciseIssue(null, node, "test message");
    ctx.addIssue(issue);

    assertThat(ctx.getIssues()).containsExactly(issue);
  }

  @Test
  void testAddMultipleIssues() {
    var ctx = new StubContext();
    var node = mock(AstNode.class);
    var check = new Object();

    var issue1 = new PreciseIssue(check, node, "first");
    var issue2 = new PreciseIssue(check, node, "second");
    ctx.addIssue(issue1);
    ctx.addIssue(issue2);

    assertThat(ctx.getIssues()).hasSize(2);
    assertThat(ctx.getIssues().get(0).getMessage()).isEqualTo("first");
    assertThat(ctx.getIssues().get(1).getMessage()).isEqualTo("second");
  }

  @SuppressWarnings({"deprecation", "java:S1874"})
  static class StubContext extends SquidAstVisitorContext<Grammar> {
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
