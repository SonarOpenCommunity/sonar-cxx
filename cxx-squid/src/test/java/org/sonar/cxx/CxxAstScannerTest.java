/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx;

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.squid.AstScanner;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourceProject;
import org.sonar.squid.indexer.QueryByType;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class CxxAstScannerTest {

  @Test
  public void files() {
    AstScanner<CxxGrammar> scanner = CxxAstScanner.create(new CxxConfiguration());
    scanner.scanFiles(ImmutableList.of(new File("src/test/resources/metrics/trivial.cc"),
        new File("src/test/resources/metrics/classes.cc")));
    SourceProject project = (SourceProject) scanner.getIndex().search(new QueryByType(SourceProject.class)).iterator().next();
    assertThat(project.getInt(CxxMetric.FILES)).isEqualTo(2);
  }

  @Test
  public void comments() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/comments.cc"));
    assertThat(file.getInt(CxxMetric.COMMENT_LINES)).isEqualTo(4);
    assertThat(file.getInt(CxxMetric.COMMENT_BLANK_LINES)).isEqualTo(3);
    assertThat(file.getNoSonarTagLines()).contains(8).hasSize(1);
  }

  @Test
  public void lines() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/classes.cc"));
    assertThat(file.getInt(CxxMetric.LINES)).isEqualTo(7);
  }

  @Test
  public void lines_of_code() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/classes.cc"));
    assertThat(file.getInt(CxxMetric.LINES_OF_CODE)).isEqualTo(5);
  }

  @Test
  public void statements() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/statements.cc"));
    assertThat(file.getInt(CxxMetric.STATEMENTS)).isEqualTo(4);
  }

  @Test
  public void functions() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/functions.cc"));
    assertThat(file.getInt(CxxMetric.FUNCTIONS)).isEqualTo(2);
  }

  @Test
  public void classes() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/classes.cc"));
    assertThat(file.getInt(CxxMetric.CLASSES)).isEqualTo(2);
  }

  @Test
  public void complexity() {
    SourceFile file = CxxAstScanner.scanSingleFile(new File("src/test/resources/metrics/complexity.cc"));
    assertThat(file.getInt(CxxMetric.COMPLEXITY)).isEqualTo(14);
  }

}
