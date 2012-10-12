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
package org.sonar.plugins.cxx.ast.cpp.impl;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.sonar.plugins.cxx.ast.cpp.CxxTranslationUnit;
import org.sonar.plugins.cxx.ast.cpp.impl.internal.ClassHolder;

/**
 * @author Przemyslaw Kociolek
 */
public class CppTranslationUnit extends ClassHolder implements CxxTranslationUnit {

  private String fileName;
  /**
   * @param sourceFile  c++ source file
   */
  public CppTranslationUnit(File sourceFile) {
    this.fileName = validateFile(sourceFile);
  }

  /**
   * Ctor
   * @param fileName  c++ source file name
   */
  public CppTranslationUnit(String fileName) {
    this.fileName = validateFileName(fileName);
  }

  public String getFilename() {
    return fileName;
  }

  private String validateFileName(String fileName) {
    if(StringUtils.isEmpty( StringUtils.trimToEmpty(fileName) )) {
      throw new IllegalArgumentException("C++ source file path not found: " + fileName);
    }
    if(!FileUtils.fileExists(fileName)) {
      throw new IllegalArgumentException("C++ source file path not found: " + fileName);
    }
    return fileName;
  }

  protected final String validateFile(File sourceFile) {
    if(sourceFile == null || !sourceFile.exists()) {
      throw new IllegalArgumentException("C++ source file not found: " + sourceFile.getAbsolutePath());
    }
    return sourceFile.getAbsolutePath();
  }

}
