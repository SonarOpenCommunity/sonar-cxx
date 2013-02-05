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
package org.sonar.plugins.cxx.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jorge Costa
 */
public class CxxUtilsTest {

  @Test
  public void testIsATest() throws Exception {
    assertEquals(true, CxxUtils.isATestFile("**/EnvironmentTests/**,**/test/**", "EnvironmentTests/file1.cpp"));
    assertEquals(true, CxxUtils.isATestFile("**/EnvironmentTests/**,**/test/**", "src1/test/file2.cpp"));
    assertEquals(true, CxxUtils.isATestFile("**/EnvironmentTests/**,**/test/**", "e:/src1/test/file2.cpp"));
    assertEquals(true, CxxUtils.isATestFile("**/Tests/**,**/test/**", "E:\\SRC\\Tests\\filex.cpp"));
  }
}
