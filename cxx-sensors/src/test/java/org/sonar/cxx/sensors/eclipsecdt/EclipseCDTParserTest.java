/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.eclipsecdt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.sonar.cxx.sensors.utils.TestUtils;

public class EclipseCDTParserTest {

  String markToken(List<String> lines, LinebasedTextRange range, boolean asDeclaration) {
    // declarations/references from our test are placed in a single line
    assertThat(range.start().line()).isEqualTo(range.end().line());

    int line = range.start().line();
    int offsetFrom = range.start().lineOffset();
    int offsetTo = range.end().lineOffset();
    assertThat(offsetFrom).isLessThan(offsetTo);

    StringBuilder sb = new StringBuilder();
    sb.append(line).append(": ");

    int prefixLength = sb.length();

    sb.append(lines.get(line - 1)).append("\n");
    for (int i = 0; i < offsetFrom + prefixLength; i++) {
      sb.append(' ');
    }
    for (int i = 0; i < (offsetTo - offsetFrom); i++) {
      if (asDeclaration) {
        sb.append('!');
      } else {
        sb.append('^');
      }
    }
    sb.append("\n");
    return sb.toString();
  }

  private String dumpSymbolTable(File target, Map<LinebasedTextRange, Set<LinebasedTextRange>> table)
      throws IOException {
    StringBuilder actualFullDump = new StringBuilder();
    List<String> content = Files.readAllLines(target.toPath());
    List<LinebasedTextRange> keys = sort(table.keySet());
    for (LinebasedTextRange key : keys) {
      actualFullDump.append("DECLARATION:").append("\n");
      actualFullDump.append(markToken(content, key, true));
      actualFullDump.append("REFERENCES:").append("\n");
      List<LinebasedTextRange> values = sort(table.get(key));
      for (LinebasedTextRange value : values) {
        actualFullDump.append(markToken(content, value, false));
      }
      actualFullDump.append("\n\n");
    }
    return actualFullDump.toString();
  }

  /**
   * sort for reproducable comparison
   */
  static List<LinebasedTextRange> sort(Set<LinebasedTextRange> s) {
    List<LinebasedTextRange> l = new ArrayList<LinebasedTextRange>(s);
    Collections.sort(l);
    return l;
  }

  @Test
  public void testInlineDeclarations() throws IOException, EclipseCDTException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/eclipsecdt");
    File target = new File(baseDir, "symbols_inline.cc");

    File expectedDeclartaions = new File(baseDir, "symbols_inline_declarations.txt");

    List<String> content = Files.readAllLines(target.toPath());

    EclipseCDTParser parser = new EclipseCDTParser(target.getAbsolutePath(), new String[0]);
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = parser.generateSymbolTable();

    StringBuilder actualDeclarationsDump = new StringBuilder();
    List<LinebasedTextRange> keys = sort(table.keySet());
    for (LinebasedTextRange key : keys) {
      actualDeclarationsDump.append(markToken(content, key, true));
    }

    assertThat(actualDeclarationsDump.toString())
        .isEqualTo(new String(Files.readAllBytes(expectedDeclartaions.toPath()), "UTF-8"));

  }

  @Test
  public void testInlineReferences() throws IOException, EclipseCDTException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/eclipsecdt");
    File target = new File(baseDir, "symbols_inline.cc");

    File expectedReferences = new File(baseDir, "symbols_inline_references.txt");

    EclipseCDTParser parser = new EclipseCDTParser(target.getAbsolutePath(), new String[0]);
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = parser.generateSymbolTable();

    String actualFullDump = dumpSymbolTable(target, table);

    assertThat(actualFullDump).isEqualTo(new String(Files.readAllBytes(expectedReferences.toPath()), "UTF-8"));
  }

  @Test
  public void testMissingInclude() throws IOException, EclipseCDTException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/eclipsecdt");
    File target = new File(baseDir, "symbols.cc");

    File expectedReferences = new File(baseDir, "symbols_noinclude_references.txt");
    // symbols_noinclude_references.cc contains a non-trivial include, but the
    // list of include directories is empty
    String[] includePaths = new String[0];
    EclipseCDTParser parser = new EclipseCDTParser(target.getAbsolutePath(), includePaths);
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = parser.generateSymbolTable();

    String actualFullDump = dumpSymbolTable(target, table);

    assertThat(actualFullDump).isEqualTo(new String(Files.readAllBytes(expectedReferences.toPath()), "UTF-8"));
  }

  @Test
  public void testCorrectInclude() throws IOException, EclipseCDTException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/eclipsecdt");
    File target = new File(baseDir, "symbols.cc");

    File expectedReferences = new File(baseDir, "symbols_include_references.txt");
    // symbols_noinclude_references.cc contains a non-trivial include, but the
    // pass a correct (relative) include path
    // now all symbols declared in the header are recognized in the *.cc file
    // too
    String[] includePaths = { "includ3path/" };
    EclipseCDTParser parser = new EclipseCDTParser(target.getAbsolutePath(), includePaths);
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = parser.generateSymbolTable();

    String actualFullDump = dumpSymbolTable(target, table);

    assertThat(actualFullDump).isEqualTo(new String(Files.readAllBytes(expectedReferences.toPath()), "UTF-8"));
  }

  @Test
  public void testErrorRecovery() throws EclipseCDTException, IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/eclipsecdt");
    File target = new File(baseDir, "syntaxerror.cc");

    File expectedReferences = new File(baseDir, "syntaxerror_references.txt");
    EclipseCDTParser parser = new EclipseCDTParser(target.getAbsolutePath(), new String[0]);
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = parser.generateSymbolTable();

    String actualFullDump = dumpSymbolTable(target, table);

    assertThat(actualFullDump).isEqualTo(new String(Files.readAllBytes(expectedReferences.toPath()), "UTF-8"));
  }

}
