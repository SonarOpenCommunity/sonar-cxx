/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
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

package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.util.LinkedList;
import org.junit.Test;

public class CxxUtilsTest {

  @Test
  public void testIsSystemCaseSensitive() {
    boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    boolean result = CxxUtils.isSystemCaseSensitive();

    // Windows OS is case insensitive
    assert (result == !isWindows);
  }
  
  @Test
  public void testCaseSensitiveFileName() {
    try {
      File fileUpperCase = new java.io.File(System.getProperty("java.io.tmpdir") +
                                            java.io.File.separatorChar +
                                            "CXX.TEST.testCaseSensitiveFileName.TXT");
      String realFileName = fileUpperCase.getCanonicalFile().getAbsolutePath();
      fileUpperCase.createNewFile();
      String result = CxxUtils.getCaseSensitiveFileName(realFileName.toLowerCase(),
                                                        new LinkedList<java.io.File>());
      fileUpperCase.delete();
      
      if (CxxUtils.isSystemCaseSensitive()) {
        assert (realFileName == result);
      } else {
        assert (realFileName != result);
      }
      
    } catch (java.io.IOException e) {
      assert (false);
    }
  }
  
  @Test
  public void testCaseSensitiveFileNameWithDirList() {
    try {
      File fileUpperCase = new java.io.File(System.getProperty("java.io.tmpdir") +
                                            java.io.File.separatorChar +
                                            "CXX.TEST.testCaseSensitiveFileNameWithDirList.TXT");
      String realFileName = fileUpperCase.getCanonicalFile().getAbsolutePath();
      fileUpperCase.createNewFile();
      LinkedList<java.io.File> sourceDirs = new LinkedList<java.io.File>();
      sourceDirs.add(new java.io.File(fileUpperCase.getParent().toLowerCase()));
      String result = CxxUtils.getCaseSensitiveFileName(fileUpperCase.getName().toLowerCase(),
                                                        sourceDirs);
      fileUpperCase.delete();
      
      if (CxxUtils.isSystemCaseSensitive()) {
        assert (realFileName == result);
      } else {
        assert (realFileName != result);
      }
      
    } catch (java.io.IOException e) {
      assert (false);
    }
  }
  
}
