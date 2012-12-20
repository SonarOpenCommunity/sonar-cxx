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
package org.sonar.plugins.cxx.cppncss;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.plugins.cxx.utils.CxxOsType;

public class CxxCppNcssFileNameTest {

  private static final int FILE_LINE = 16;
  
  private static final String WIN_FULL_NAME = "C:\\src\\utils\\utils.cpp:" + FILE_LINE;
  private static final String WIN_FILE_NAME = "C:\\src\\utils\\utils.cpp";  
  
  private static final String UNIX_FULL_NAME = "src/utils/utils.cpp:" + FILE_LINE;
  private static final String UNIX_FILE_NAME = "src/utils/utils.cpp";
    
  @Test
  public void getFileNameOnWindowsTest() {
    CxxCppNcssFile ncssFileName = new CxxCppNcssFile(WIN_FULL_NAME, CxxOsType.WINDOWS);
   
    assertEquals("Ncss line don't match", FILE_LINE, ncssFileName.getLine());
    assertEquals("Ncss file name don't match", WIN_FILE_NAME, ncssFileName.getFileName());
    assertEquals("Ncss whole line don't match", WIN_FULL_NAME, ncssFileName.getWholeString());
  }
  
  @Test
  public void getFileNameOnUnixTest() {
    CxxCppNcssFile ncssFileName = new CxxCppNcssFile(UNIX_FULL_NAME, CxxOsType.UNIX);
    
    assertEquals("Ncss line don't match", FILE_LINE, ncssFileName.getLine());
    assertEquals("Ncss file name don't match", UNIX_FILE_NAME, ncssFileName.getFileName());
    assertEquals("Ncss whole line don't match", UNIX_FULL_NAME, ncssFileName.getWholeString());
  }
    
}
