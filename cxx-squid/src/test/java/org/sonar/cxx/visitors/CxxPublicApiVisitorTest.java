/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.groups.Tuple;
import static org.assertj.core.groups.Tuple.tuple;
import org.fest.assertions.Fail;
import org.junit.Test;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTester;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;

public class CxxPublicApiVisitorTest {

  private static final org.sonar.api.utils.log.Logger LOG = Loggers.get(CxxPublicApiVisitorTest.class);

  private static String getFileExtension(String fileName) {
    int lastIndexOf = fileName.lastIndexOf('.');
    if (lastIndexOf == -1) {
      return "";
    }
    return fileName.substring(lastIndexOf);
  }

  @Test
  public void test_no_matching_suffix() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/doxygen_example.h", ".",
                                                                   "");
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.API_FILE_SUFFIXES,
                    new String[]{".hpp"});

    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    assertThat(file.getInt(CxxMetric.PUBLIC_API)).isZero();
    assertThat(file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API)).isZero();
  }

  @Test
  public void doxygen_example() throws IOException {
    assertThat(testFile("src/test/resources/metrics/doxygen_example.h")).isEqualTo(tuple(13, 0));
  }

  @Test
  public void to_delete() throws IOException {
    assertThat(testFile("src/test/resources/metrics/public_api.h")).isEqualTo(tuple(47, 0));

  }

  @Test
  public void no_doc() throws IOException {
    assertThat(testFile("src/test/resources/metrics/no_doc.h")).isEqualTo(tuple(22, 22));
  }

  @Test
  public void template() throws IOException {
    assertThat(testFile("src/test/resources/metrics/template.h")).isEqualTo(tuple(14, 4));
  }

  @Test
  public void alias_function_template() throws IOException {
    assertThat(testFile("src/test/resources/metrics/alias_in_template_func.h")).isEqualTo(tuple(4, 3));
  }

  @Test
  public void unnamed_class() throws IOException {
    assertThat(testFile("src/test/resources/metrics/unnamed_class.h")).isEqualTo(tuple(3, 1));
  }

  @Test
  public void unnamed_enum() throws IOException {
    assertThat(testFile("src/test/resources/metrics/unnamed_enum.h")).isEqualTo(tuple(1, 1));
  }

  @Test
  public void public_api() throws UnsupportedEncodingException, IOException {
    String fileNme = "src/test/resources/metrics/public_api.h";
    TestPublicApiVisitor visitor = new TestPublicApiVisitor(fileNme, true);

    var tester = CxxFileTesterHelper.create(fileNme, ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), visitor);

    var expectedIdCommentMap = new HashMap<String, String>();

    expectedIdCommentMap.put("publicDefinedMethod", "publicDefinedMethod");
    expectedIdCommentMap.put("publicDeclaredMethod", "publicDeclaredMethod");
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
    expectedIdCommentMap.put("globalFuncDef", "globalFuncDef");
    expectedIdCommentMap.put("linkageSpecification1", "linkageSpecification1");
    expectedIdCommentMap.put("linkageSpecification2", "linkageSpecification2");
    expectedIdCommentMap.put("linkageSpecification3", "linkageSpecification3");
    expectedIdCommentMap.put("linkageSpecification4", "linkageSpecification4");

    // check completeness
    for (var id : expectedIdCommentMap.keySet()) {
      LOG.debug("id: {}", id);

      List<Token> comments = visitor.idCommentMap.get(id);

      assertThat(visitor.idCommentMap.keySet())
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
    for (var id : visitor.idCommentMap.keySet()) {
      LOG.debug("id: {}", id);

      List<Token> comments = visitor.idCommentMap.get(id);

      assertThat(comments)
        .overridingErrorMessage("No documentation for " + id)
        .isNotEmpty();
      assertThat(expectedIdCommentMap.keySet())
        .overridingErrorMessage("Should not be part of public API: " + id)
        .contains(id);
    }
  }

  /**
   * Check that CxxPublicApiVisitor correctly counts API for given file.
   *
   * @param fileName the file to use for test
   * @param expectedApi expected number of API
   * @param expectedUndoc expected number of undocumented API
   */
  private Tuple testFile(String fileName)
    throws UnsupportedEncodingException, IOException {

    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.API_FILE_SUFFIXES,
                    new String[]{getFileExtension(fileName)});

    var tester = CxxFileTesterHelper.create(fileName, ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    LOG.debug("#API: {} UNDOC: {}",
              file.getInt(CxxMetric.PUBLIC_API), file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API));

    return new Tuple(file.getInt(CxxMetric.PUBLIC_API), file.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API));
  }

  private class TestPublicApiVisitor extends AbstractCxxPublicApiVisitor<Grammar> {

    final Map<String, List<Token>> idCommentMap = new HashMap<>();
    boolean checkDoubleIDs = false;

    TestPublicApiVisitor(String fileName, boolean checkDoubleIDs) {
      withHeaderFileSuffixes(new String[]{getFileExtension(fileName)});
      this.checkDoubleIDs = checkDoubleIDs;
    }

    @Override
    protected void onPublicApi(AstNode node, String id, List<Token> comments) {
      if (checkDoubleIDs && idCommentMap.containsKey(id)) {
        Fail.fail("DOUBLE ID: " + id);
      }
      idCommentMap.put(id, comments);
    }
  }

}
