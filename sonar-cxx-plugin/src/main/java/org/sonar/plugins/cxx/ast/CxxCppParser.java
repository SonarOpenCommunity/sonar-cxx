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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.sonar.api.resources.InputFile;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * A facade to Eclipse CDT classes. Handles c++ file parsing.
 * @autor Przemyslaw Kociolek
 */
public class CxxCppParser {

  /**
   * Parses a given c++ input file.
   * @param inputFile File to parse
   * @throws CxxCppParserException  throws in several cases:<br>
   * <ul>
   * <li>when inputFile == null</li>
   * <li>when inputFile was not found</li>
   * <li>when file could not be properly parsed</li>
   * </ul>
   */
  public CxxCppParsedFile parseFile(InputFile inputFile) throws CxxCppParserException {
    validateFile(inputFile);
    String fileCode = readFileSourceCode(inputFile);
    IASTTranslationUnit ast = generateAst(inputFile, fileCode);
    return new CxxCppParsedFile(inputFile, ast, fileCode);
  }

  private IASTTranslationUnit generateAst(InputFile inputFile, String fileCode) throws CxxCppParserException {
    String filePath = inputFile.getFile().getAbsolutePath();
    IASTTranslationUnit translationUnit = null;    
    String[] includeDirectories = {"."};

    try {
      IParserLogService log = ParserFactory.createDefaultLogService();
      FileContent fileContent = FileContent.create(filePath, fileCode.toCharArray() );
      IScannerInfo info = new ScannerInfo(new HashMap<String, String>(), includeDirectories);
      IncludeFileContentProvider includeProvider = new CxxCppIncludeFileContentProvider();
      translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info, includeProvider, null, 0, log);
    } catch (CoreException e) {
      throw new CxxCppParserException("Error while parsing " + filePath + ": " + e.getMessage());
    }
    return translationUnit;
  }

  private String readFileSourceCode(InputFile inputFile) throws CxxCppParserException {
    try {
      return FileUtils.readFileToString(inputFile.getFile());
    } catch (IOException e) {
      throw new CxxCppParserException("Could not read file contents: " + CxxUtils.fileToAbsolutePath(inputFile.getFile()));
    }
  }

  private void validateFile(InputFile inputFile) throws CxxCppParserException {
    if(inputFile == null) {
      throw new CxxCppParserException("No input file was provided");
    }

    File file = inputFile.getFile();
    if(file == null || !file.exists()) {
      throw new CxxCppParserException("File not found for parsing: " + CxxUtils.fileToAbsolutePath(file) );
    }
  }

}
