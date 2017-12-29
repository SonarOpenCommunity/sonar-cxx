/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.codehaus.sonarplugins.cxx.cxxlint;

import java.io.File;

import org.junit.Test;
import org.sonar.cxx.cxxlint.CxxLint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jocs
 */
public class CxxLintTest {
//  private static final Logger LOG = Loggers.get(CxxLintTest.class);

  /**
   * Test of main method, of class CxxLint.
   */
  @Test
  public void runsToolWithoutSettingsWithoutExceptions() {
    ClassLoader classLoader = getClass().getClassLoader();
    File fileToAnalyse = new File(classLoader.getResource("PathHandle.cpp").getFile());

    String[] args = new String[2];
    args[0] = "-f";
    args[1] = fileToAnalyse.getAbsolutePath();
    CxxLint.main(args);
    assertThat(true);
  }

  /**
   * Test of main method, of class CxxLint.
   */
  @Test
  public void runsToolWithSettingsWithoutExceptions() {
    ClassLoader classLoader = getClass().getClassLoader();
    File fileToAnalyse = new File(classLoader.getResource("PathHandle.cpp").getFile());
    File settingsFile = new File(classLoader.getResource("4b4b9c5c-05f3-42e1-b94f-4c74b53241e3.json").getFile());

    String[] args = new String[4];
    args[0] = "-f";
    args[1] = fileToAnalyse.getAbsolutePath();
    args[2] = "-s";
    args[3] = settingsFile.getAbsolutePath();

//    try {
    CxxLint.main(args);
    assertThat(true);
//    } catch (Exception ex) {
//      LOG.info("Exception Found: " + ex);
//      assertThat(false);
//    }
  }
}
