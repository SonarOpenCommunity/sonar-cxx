/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SourceCodeTest {

  private SourceProject prj;
  private SourcePackage pac;
  private SourcePackage pac2;
  private SourceCode cla;
  private SourceCode cla2;

  @Before
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
  public void testAddChild() {
    prj.addChild(pac);
    assertEquals(pac.getParent(), prj);
    assertTrue(prj.getChildren().contains(pac));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertThat(prj).isNotEqualTo(pac);
    assertThat(prj.hashCode()).isNotEqualTo(pac.hashCode());
    assertThat(prj).isNotEqualTo(new Object());

    SourceCode samePac = new SourcePackage("org.sonar");
    assertThat(pac).isEqualTo(samePac);
    assertThat(pac.hashCode()).isEqualTo(samePac.hashCode());
  }

  @Test
  public void testContains() {
    assertThat(prj.hasChild(pac), is(true));
    assertThat(prj.hasChild(cla), is(true));
  }

  @Test
  public void testIsType() {
    var pacFrom = new SourcePackage("org.from");
    assertFalse(pacFrom.isType(SourceCode.class));
    assertFalse(pacFrom.isType(SourceClass.class));
    assertTrue(pacFrom.isType(SourcePackage.class));
  }

  @Test
  public void testGetParentByType() {
    var pacFrom = new SourcePackage("org.from");
    var fileFrom = new SourceFile("org.from.From.java", "From.java");
    var classFrom = new SourceClass("org.from.From", "From");
    pacFrom.addChild(fileFrom);
    fileFrom.addChild(classFrom);
    assertEquals(pacFrom, classFrom.getParent(SourcePackage.class));
  }

  @Test
  public void testGetAncestorByType() {
    var file = new SourceFile("org.from.From.java", "From.java");
    var class1 = new SourceClass("org.from.From", "From");
    var class2 = new SourceClass("org.from.From$Foo", "From$Foo");
    var method = new SourceMethod(class2, "foo()", 10);
    file.addChild(class1);
    class1.addChild(class2);
    class2.addChild(method);

    assertEquals(file, class1.getAncestor(SourceFile.class));
    assertEquals(class1, class2.getAncestor(SourceClass.class));
    assertEquals(file, class2.getAncestor(SourceFile.class));
    assertEquals(class1, method.getAncestor(SourceClass.class));
    assertEquals(file, method.getAncestor(SourceFile.class));
  }

  @Test
  public void testHasAmongParents() {
    assertTrue(cla.hasAmongParents(prj));
    assertTrue(cla.hasAmongParents(pac));
    assertFalse(prj.hasAmongParents(cla));
  }

  @Test
  public void getCheckMessages() {
    SourceCode foo = new SourceFile("Foo.java");
    assertThat(foo.getCheckMessages().size(), is(0));

    foo.log(new CheckMessage(null, "message"));
    assertThat(foo.getCheckMessages().size(), is(1));
  }
}
