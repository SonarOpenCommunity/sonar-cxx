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
package org.sonar.cxx.squidbridge;

import static org.assertj.core.api.Assertions.*;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.squidbridge.test.miniC.MiniCAstScanner;

class AstScannerTest {

  private static final File FILE = FileUtils.toFile(AstScannerTest.class.getResource("/metrics/counter.mc"));

  @Test
  void visitsEachNodeOncePerScan() {
    var visitor = new StatementVisitor();
    var scanner = MiniCAstScanner.create(visitor);

    scanner.scanFile(FILE);
    assertThat(visitor.visitedStatements).isEqualTo(6);

    scanner.scanFile(FILE);
    assertThat(visitor.visitedStatements).isEqualTo(12);
  }

  @Test
  void subscribesToNodeTypeOnce() {
    var visitor = new StatementVisitor();

    visitor.init();
    visitor.init();

    assertThat(visitor.getAstNodeTypesToVisit()).containsExactly(MiniCGrammar.STATEMENT);
  }

  private static class StatementVisitor extends SquidAstVisitor<Grammar> {

    private int visitedStatements;

    @Override
    public void init() {
      subscribeTo(MiniCGrammar.STATEMENT);
    }

    @Override
    public void visitNode(AstNode astNode) {
      visitedStatements++;
    }
  }

}
