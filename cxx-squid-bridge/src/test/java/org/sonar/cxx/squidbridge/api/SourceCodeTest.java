/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2022 SonarOpenCommunity
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

  private SourceProject prj;
  private SourcePackage pac;
  private SourcePackage pac2;
  private SourceCode cla;
  private SourceCode cla2;

  @BeforeEach
  public void before() {
    prj = new SourceProject("dummy project");
    pac = new SourcePackage("org.sonar");
    pac2 = new SourcePackage("org.sonar2");
    pac2 = new SourcePackage("org.sonar2");
    cla = new SourceClass("org.sonar.Toto", "Toto");
    cla2 = new SourceClass("org.sonar2.Tata", "Tata");
    prj.addChild(pac);
    prj.addChild(pac2);
    pac.addChild(cla);
    pac.addChild(cla2);
  }

  @Test
  void testAddChild() {
    prj.addChild(pac);
    assertThat(pac.getParent()).isEqualTo(prj);
    assertThat(prj.getChildren()).contains(pac);
  }

  @Test
  void testEqualsAndHashCode() {
    assertThat(prj).isNotEqualTo(pac);
    assertThat(prj.hashCode()).isNotEqualTo(pac.hashCode());
    assertThat(prj).isNotEqualTo(new Object());

    SourceCode samePac = new SourcePackage("org.sonar");
    assertThat(pac)
      .isEqualTo(samePac)
      .hasSameHashCodeAs(samePac);
  }

  @Test
  void testContains() {
    assertThat(prj.hasChild(pac)).isTrue();
    assertThat(prj.hasChild(cla)).isTrue();
  }

  @Test
  void testIsType() {
    var pacFrom = new SourcePackage("org.from");
    assertThat(pacFrom)
      .isNotExactlyInstanceOf(SourceCode.class)
      .isNotExactlyInstanceOf(SourceClass.class)
      .isExactlyInstanceOf(SourcePackage.class);
  }

  @Test
  void testGetParentByType() {
    var pacFrom = new SourcePackage("org.from");
    var fileFrom = new SourceFile("org.from.From.java", "From.java");
    var classFrom = new SourceClass("org.from.From", "From");
    pacFrom.addChild(fileFrom);
    fileFrom.addChild(classFrom);
    assertThat(pacFrom).isEqualTo(classFrom.getParent(SourcePackage.class));
  }

  @Test
  void testGetAncestorByType() {
    var file = new SourceFile("org.from.From.java", "From.java");
    var class1 = new SourceClass("org.from.From", "From");
    var class2 = new SourceClass("org.from.From$Foo", "From$Foo");
    var method = new SourceMethod(class2, "foo()", 10);
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
    assertThat(cla.hasAmongParents(prj)).isTrue();
    assertThat(cla.hasAmongParents(pac)).isTrue();
    assertThat(prj.hasAmongParents(cla)).isFalse();
  }

  @Test
  void getCheckMessages() {
    SourceCode foo = new SourceFile("Foo.java");
    assertThat(foo.getCheckMessages()).isEmpty();

    foo.log(new CheckMessage(null, "message"));
    assertThat(foo.getCheckMessages()).hasSize(1);
  }
}
