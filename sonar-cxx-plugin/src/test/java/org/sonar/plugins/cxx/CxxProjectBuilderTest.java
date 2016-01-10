/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx;

import static org.fest.assertions.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.bootstrap.internal.ProjectBuilderContext;

public class CxxProjectBuilderTest {

  private final ProjectBuilder projectBuilder = new CxxProjectBuilder();
  private final ProjectDefinition projectDefinition = ProjectDefinition.create();
  private ProjectBuilderContext context;

  @Before
  public void setUp() {
    // setting values to expand
    System.setProperty("cxx.test.key1", "value");
  }

  @Test
  public void replaceString() {
    projectDefinition.setProperty("key1", "${cxx.test.key1}");
    context = new ProjectBuilderContext(new ProjectReactor(projectDefinition));
    projectBuilder.build(context);
    String value = context.projectReactor().getRoot().getProperties().getProperty("key1");
    assertThat(value).isEqualTo("value");
  }

  @Test
  public void replaceStringList1() {
    projectDefinition.setProperty("key2", "${cxx.test.key1}, ${cxx.test.key1}");
    context = new ProjectBuilderContext(new ProjectReactor(projectDefinition));
    projectBuilder.build(context);
    String value = context.projectReactor().getRoot().getProperties().getProperty("key2");
    assertThat(value).isEqualTo("value, value");
  }

  @Test
  public void replaceStringList2() {
    projectDefinition.setProperty("key3", "${cxx.test.key1}, ${undefined}, xxx");
    context = new ProjectBuilderContext(new ProjectReactor(projectDefinition));
    projectBuilder.build(context);
    String value = context.projectReactor().getRoot().getProperties().getProperty("key3");
    assertThat(value).isEqualTo("value, ${undefined}, xxx");
  }

}
