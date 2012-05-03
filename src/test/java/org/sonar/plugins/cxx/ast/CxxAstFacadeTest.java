/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.ast;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.InputFile;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxAstFacadeTest {

  static private String TEST_FILE = "/org/sonar/plugins/cxx/ast/ClassTest.cpp";

  private InputFile testFile;

  @Before
  public void setup() {
    testFile = mock(InputFile.class);
    when(testFile.getFile()).thenReturn( TestUtils.loadResource(TEST_FILE) );
  }

  @Test
  public void parseFileTest() {
    CxxAstFacade facade = new CxxAstFacade();
    facade.parseFile();
    facade.getAST();
  }

}
