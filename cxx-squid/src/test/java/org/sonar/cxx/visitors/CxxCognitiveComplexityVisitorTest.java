/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import org.junit.Test;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.CxxFileTester;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.api.SourceFile;

public class CxxCognitiveComplexityVisitorTest {

  private void testFile(String fileName, int expectedCognitiveComplexity) throws UnsupportedEncodingException, IOException {
    CxxCognitiveComplexityVisitor<Grammar> visitor = CxxCognitiveComplexityVisitor.<Grammar>builder()
      .setMetricDef(CxxMetric.COGNITIVE_COMPLEXITY)
      .build();

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester(fileName, ".");
    SourceFile sourceFile = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), visitor);

    assertThat(sourceFile.getInt(CxxMetric.COGNITIVE_COMPLEXITY)).isEqualTo(expectedCognitiveComplexity);
  }

  @Test
  public void if_statement() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/if.cc", 1);
  }

  @Test
  public void if_else() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/if_else.cc", 2);
  }

  @Test
  public void if_else_if() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/if_else_if.cc", 2);
  }

  @Test
  public void if_else_if_else() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/if_else_if_else.cc", 3);
  }

  @Test
  public void ternary() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/ternary.cc", 1);
  }

  @Test
  public void nesting() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/nesting.cc", 20);
  }

  @Test
  public void switch_statement() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/switch.cc", 1);
  }

  @Test
  public void for_statement() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/for.cc", 1);
  }

  @Test
  public void while_statement() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/while.cc", 1);
  }

  @Test
  public void do_while() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/do_while.cc", 1);
  }

  @Test
  public void catch_statement() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/catch.cc", 1);
  }

  @Test
  public void multiple_catch() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/multiple_catch.cc", 3);
  }

  @Test
  public void goto_statement() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/goto.cc", 1);
  }

  @Test
  public void binary_logical() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/binary_logical.cc", 2);
  }

  @Test
  public void sum_of_primes() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/sum_of_primes.cc", 7);
  }

  @Test
  public void get_words() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/get_words.cc", 1);
  }

  @Test
  public void overridden_symbol_from() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/overridden_symbol_from.cc", 19);
  }

  @Test
  public void add_version() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/add_version.cc", 35);
  }

  @Test
  public void to_regexp() throws UnsupportedEncodingException, IOException {
    testFile("src/test/resources/visitors/to_regexp.cc", 20);
  }
}
