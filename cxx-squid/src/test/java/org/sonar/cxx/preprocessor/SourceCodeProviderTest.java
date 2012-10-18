/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
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
package org.sonar.cxx.preprocessor;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SourceCodeProviderTest {
  private SourceCodeProvider codeProvider = new SourceCodeProvider();

  @Test
  public void getting_code_with_abspath() {
    String abspath = new File("src/test/resources/codeprovider/source.hh").getAbsolutePath();
    assertEquals("source code\n", codeProvider.getSourceCode(abspath, null));
  }

  @Test
  public void getting_code_with_relpath_and_cwd() {
    File file = new File("src/test/resources/codeprovider/source.hh");
    assertEquals("source code\n", codeProvider.getSourceCode(file.getName(), file.getParentFile().getAbsolutePath()));
  }

  @Test
  public void getting_code_with_relpath_and_absolute_code_location() {
    String cwd = new File("src/test/resources").getAbsolutePath();
    String file = "source.hh";
    File includeDir = new File(new File("src/test/resources/codeprovider").getAbsolutePath());

    codeProvider.setCodeLocations(Arrays.asList(includeDir));
    assertEquals("source code\n", codeProvider.getSourceCode(file, cwd));
  }

  @Test
  public void getting_code_with_relpath_and_relative_code_location() {
    String cwd = new File("src/test/resources").getAbsolutePath();
    String file = "source.hh";
    File includeDir = new File("codeprovider");

    codeProvider.setCodeLocations(Arrays.asList(includeDir));
    assertEquals("source code\n", codeProvider.getSourceCode(file, cwd));
  }
}
