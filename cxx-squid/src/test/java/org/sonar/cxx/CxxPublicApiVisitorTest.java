/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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
package org.sonar.cxx;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fest.assertions.Fail;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.visitors.CxxPublicApiVisitor;
import org.sonar.cxx.visitors.CxxPublicApiVisitor.PublicApiHandler;
import org.sonar.squidbridge.api.SourceFile;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CxxPublicApiVisitorTest {

  private static final Logger LOG = LoggerFactory
    .getLogger("CxxPublicApiVisitorTest");

  private static String getFileExtension(String fileName) {
    int lastIndexOf = fileName.lastIndexOf(".");
    if (lastIndexOf == -1) {
      return "";
    }
    return fileName.substring(lastIndexOf);
  }

  /**
   * Check that CxxPublicApiVisitor correctly counts API for given file.
   *
   * @param fileName the file to use for test
   * @param expectedApi expected number of API
   * @param expectedUndoc expected number of undocumented API
   * @param checkDouble if true, fails the test if two items with the same id
   * are counted..
   */
  @SuppressWarnings("unchecked")
  private void testFile(String fileName, int expectedApi, int expectedUndoc,
    boolean checkDouble) throws UnsupportedEncodingException, IOException {

    CxxPublicApiVisitor<Grammar> visitor = new CxxPublicApiVisitor<>(
      CxxMetric.PUBLIC_API, CxxMetric.PUBLIC_UNDOCUMENTED_API);

    if (checkDouble) {
      final Map<String, List<Token>> idCommentMap = new HashMap<>();

      visitor.setHandler(new PublicApiHandler() {
        @Override
        public void onPublicApi(AstNode node, String id,
          List<Token> comments) {
          if (idCommentMap.containsKey(id)) {
            Fail.fail("DOUBLE ID: " + id);
          }

          // store and compare later in order to not break the parsing
          idCommentMap.put(id, comments);
        }
      });
    }

    visitor.withHeaderFileSuffixes(Arrays
      .asList(getFileExtension(fileName)));

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester(fileName, ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext,
      visitor);

    if (LOG.isDebugEnabled()) {
      LOG.debug("#API: {} UNDOC: {}",
        file.getInt(CxxMetric.PUBLIC_API), file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API));
    }

    assertThat(file.getInt(CxxMetric.PUBLIC_API)).isEqualTo(expectedApi);
    assertThat(file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API)).isEqualTo(
      expectedUndoc);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_no_matching_suffix() throws IOException {
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/metrics/doxygen_example.h", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext,
      new CxxPublicApiVisitor<>(CxxMetric.PUBLIC_API,
        CxxMetric.PUBLIC_UNDOCUMENTED_API)
      .withHeaderFileSuffixes(Arrays.asList(".hpp")));

    assertThat(file.getInt(CxxMetric.PUBLIC_API)).isEqualTo(0);
    assertThat(file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API)).isEqualTo(0);
  }

  @Test
  public void doxygen_example() throws IOException {
    testFile("src/test/resources/metrics/doxygen_example.h", 13, 0, false);
  }

  @Test
  public void to_delete() throws IOException {
    testFile("src/test/resources/metrics/public_api.h", 41, 0, true);
  }

  @Test
  public void no_doc() throws IOException {
    testFile("src/test/resources/metrics/no_doc.h", 22, 22, true);
  }

  @Test
  public void template() throws IOException {
    testFile("src/test/resources/metrics/template.h", 9, 4, true);
  }

  @Test
  public void alias_function_template() throws IOException {
    testFile("src/test/resources/metrics/alias_in_template_func.h", 4, 3, false);
  }

  @Test
  public void unnamed_class() throws IOException {
    testFile("src/test/resources/metrics/unnamed_class.h", 3, 1, false);
  }

  @Test
  public void unnamed_enum() throws IOException {
    testFile("src/test/resources/metrics/unnamed_enum.h", 1, 1, false);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void public_api() throws UnsupportedEncodingException, IOException {
    CxxPublicApiVisitor<Grammar> visitor = new CxxPublicApiVisitor<>(
      CxxMetric.PUBLIC_API, CxxMetric.PUBLIC_UNDOCUMENTED_API);

    final Map<String, List<Token>> idCommentMap = new HashMap<>();

    visitor.setHandler(new PublicApiHandler() {
      @Override
      public void onPublicApi(AstNode node, String id,
        List<Token> comments) {
        if (idCommentMap.containsKey(id)) {
          Fail.fail("DOUBLE ID: " + id);
        }

        // store and compare later in order to not break the parsing
        idCommentMap.put(id, comments);
      }
    });

    visitor.withHeaderFileSuffixes(Arrays.asList(".h"));

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/metrics/public_api.h", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, visitor); //

    if (LOG.isDebugEnabled()) {
      LOG.debug("DOC: {} UNDOC: {}",
        file.getInt(CxxMetric.PUBLIC_API), file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API));
    }

    final Map<String, String> expectedIdCommentMap = new HashMap<>();

    expectedIdCommentMap.put("publicDefinedMethod", "publicDefinedMethod");
    expectedIdCommentMap.put("aliasDeclaration", "aliasDeclaration");
    expectedIdCommentMap.put("publicMethod", "publicMethod");
    expectedIdCommentMap.put("testStruct", "testStruct");
    expectedIdCommentMap.put("testUnion", "testUnion");
    expectedIdCommentMap.put("inlineCommentedAttr", "inlineCommentedAttr");
    expectedIdCommentMap.put("inlineCommentedLastAttr",
      "inlineCommentedLastAttr");
    expectedIdCommentMap.put("enumVar", "classEnum"); // only one
    // declarator, then
    // doc should precede
    // decl
    expectedIdCommentMap.put("classEnum", "classEnum");
    expectedIdCommentMap.put("classEnumValue", "classEnumValue");
    expectedIdCommentMap.put("protectedMethod", "protectedMethod");
    expectedIdCommentMap.put("testTypeDef", "testTypeDef");
    expectedIdCommentMap.put("testField", "testField");
    expectedIdCommentMap.put("inlinePublicMethod", "inlinePublicMethod");
    expectedIdCommentMap.put("publicAttribute", "publicAttribute");
    expectedIdCommentMap.put("testEnum", "testEnum");
    expectedIdCommentMap.put("testClass", "testClass");
    expectedIdCommentMap.put("enum_val", "enum_val");
    expectedIdCommentMap.put("testFunction", "testFunction");
    expectedIdCommentMap.put("testFunction2", "testFunction2");
    expectedIdCommentMap.put("globalVar", "globalVar");
    expectedIdCommentMap.put("globalVarInline", "globalVarInline");
    expectedIdCommentMap.put("globalVar1", "globalVar1");
    expectedIdCommentMap.put("globalVar2", "globalVar2");
    expectedIdCommentMap.put("globalVar3", "globalVar3");
    expectedIdCommentMap.put("globalAliasDeclaration", "globalAliasDeclaration");
    expectedIdCommentMap.put("testType", "testType");
    expectedIdCommentMap.put("enumVar1", "enumVar1");
    expectedIdCommentMap.put("enumVar2", "enumVar2");
    expectedIdCommentMap.put("attr1", "attr1");
    expectedIdCommentMap.put("attr2", "attr2");
    expectedIdCommentMap.put("lastVar", "lastVar");
    expectedIdCommentMap.put("protectedStruct", "protectedStruct");
    expectedIdCommentMap
      .put("protectedStructField", "protectedStructField");
    expectedIdCommentMap.put("protectedStructField2",
      "protectedStructField2");
    expectedIdCommentMap.put("protectedClass", "protectedClass");
    expectedIdCommentMap.put("operator[]", "operator");
    expectedIdCommentMap.put("bitfield", "bitfield");
    expectedIdCommentMap.put("<unnamed class>", "<unnamed>");
    expectedIdCommentMap.put("testField2", "testField2");
//        expectedIdCommentMap.put("operator=", "operator=");
    expectedIdCommentMap.put("testUnnamedStructVar", "testUnnamedStructVar");

    // check completeness
    for (final String id : expectedIdCommentMap.keySet()) {
      LOG.debug("id: {}", id);

      List<Token> comments = idCommentMap.get(id);

      assertThat(idCommentMap.keySet())
        .overridingErrorMessage("No public API for " + id)
        .contains(id);
      assertThat(comments)
        .overridingErrorMessage("No documentation for " + id)
        .isNotEmpty();
      assertThat(comments.get(0).getValue())
        .overridingErrorMessage("Unexpected documentation for " + id)
        .contains(expectedIdCommentMap.get(id));
    }

    // check correction
    for (final String id : idCommentMap.keySet()) {
      LOG.debug("id: {}", id);

      List<Token> comments = idCommentMap.get(id);

      assertThat(comments)
        .overridingErrorMessage("No documentation for " + id)
        .isNotEmpty();
      assertThat(expectedIdCommentMap.keySet())
        .overridingErrorMessage("Should not be part of public API: " + id)
        .contains(id);
    }

    assertThat(file.getInt(CxxMetric.PUBLIC_API)).isEqualTo(
      expectedIdCommentMap.keySet().size());
    assertThat(file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API)).isEqualTo(0);
  }

}
