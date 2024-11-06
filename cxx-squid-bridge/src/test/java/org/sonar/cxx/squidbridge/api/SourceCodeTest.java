/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SourceCodeTest {

  private SourceProject sourceProject;
  private SourceFile sourceFile1;
  private SourceFile sourceFile2;
  private SourceCode sourceClass1;
  private SourceCode sourceClass2;

  @BeforeEach
  public void before() {
    sourceProject = new SourceProject("ProjectKey", "Demo");
    sourceFile1 = new SourceFile("src/test/FileName1.cpp", "FileName1.cpp");
    sourceFile2 = new SourceFile("src/test/FileName2.cpp", "FileName2.cpp");
    sourceClass1 = new SourceClass("ClassName1:1", "ClassName1");
    sourceClass2 = new SourceClass("ClassName2:1", "ClassName2");
    sourceProject.addChild(sourceFile1);
    sourceProject.addChild(sourceFile2);
    sourceFile1.addChild(sourceClass1);
    sourceFile1.addChild(sourceClass2);
  }

  @Test
  void testAddChild() {
    sourceProject.addChild(sourceFile1);
    assertThat(sourceFile1.getParent()).isEqualTo(sourceProject);
    assertThat(sourceProject.getChildren()).contains(sourceFile1);
  }

  @Test
  void testEqualsAndHashCode() {
    assertThat(sourceProject).isNotEqualTo(sourceFile1);
    assertThat(sourceProject.hashCode()).isNotEqualTo(sourceFile1.hashCode());
    assertThat(sourceProject).isNotEqualTo(new Object());

    var sameFile = new SourceFile("src/test/FileName1.cpp", "FileName1.cpp");
    assertThat(sourceFile1)
      .isEqualTo(sameFile)
      .hasSameHashCodeAs(sameFile);
  }

  @Test
  void testContains() {
    assertThat(sourceProject.hasChild(sourceFile1)).isTrue();
    assertThat(sourceProject.hasChild(sourceClass1)).isTrue();
    assertThat(sourceProject.hasChild(sourceClass2)).isTrue();
  }

  @Test
  void testIsType() {
    assertThat(sourceFile2)
      .isNotExactlyInstanceOf(SourceCode.class)
      .isNotExactlyInstanceOf(SourceClass.class)
      .isExactlyInstanceOf(SourceFile.class);
  }

  @Test
  void testGetParentByType() {
    var fileFrom = new SourceFile("src/test/FileName4.cpp", "FileName4.cpp");
    var classFrom = new SourceClass("ClassName3:1", "ClassName3");
    var methodFrom = new SourceFunction(classFrom, "methodSignature", null, 1);
    fileFrom.addChild(classFrom);
    classFrom.addChild(methodFrom);
    assertThat(fileFrom).isEqualTo(methodFrom.getParent(SourceFile.class));
  }

  @Test
  void testGetAncestorByType() {
    var file = new SourceFile("src/test/FileName5.cpp", "FileName5.cpp");
    var class1 = new SourceClass("ClassName3:1", "ClassName3");
    var class2 = new SourceClass("ClassName4:1", "ClassName4");
    var method = new SourceFunction(class2, "methodSignature", null, 10);
    file.addChild(class1);
    class1.addChild(class2);
    class2.addChild(method);

    assertThat(file).isEqualTo(class1.getAncestor(SourceFile.class));
    assertThat(class1).isEqualTo(class2.getAncestor(SourceClass.class));
    assertThat(file).isEqualTo(class2.getAncestor(SourceFile.class));
    assertThat(class1).isEqualTo(method.getAncestor(SourceClass.class));
    assertThat(file).isEqualTo(method.getAncestor(SourceFile.class));
  }

  @Test
  void testHasAmongParents() {
    assertThat(sourceClass1.hasAmongParents(sourceProject)).isTrue();
    assertThat(sourceClass1.hasAmongParents(sourceFile1)).isTrue();
    assertThat(sourceProject.hasAmongParents(sourceClass1)).isFalse();
  }

  @Test
  void getCheckMessages() {
    SourceCode foo = new SourceFile("src/test/FileName3.cpp", "FileName3.cpp");
    assertThat(foo.getCheckMessages()).isEmpty();

    foo.log(new CheckMessage(null, "message"));
    assertThat(foo.getCheckMessages()).hasSize(1);
  }

  @Test
  void keyTest() {
    var file1 = new SourceFile("src/test/FileName.cpp", "FileName.cpp");
    var class1 = new SourceClass(file1, "classKey", null, 10);
    file1.addChild(class1);
    var method1 = new SourceFunction(class1, "methodKey", null, 20);
    class1.addChild(method1);
    assertThat(file1.getKey()).isEqualTo("src/test/FileName.cpp");
    assertThat(class1.getKey()).isEqualTo("src/test/FileName.cpp@classKey:10");
    assertThat(method1.getKey()).isEqualTo("src/test/FileName.cpp@classKey:10@methodKey:20");
  }
}
