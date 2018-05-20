/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.eclipsecdt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class EclipseCDTParser {

  private static final Logger LOG = Loggers.get(EclipseCDTParser.class);

  /**
   * Naive implementation for IncludeFileContentProvider, see
   * {@link org.eclipse.cdt.internal.core.parser.EmptyFilesProvider}
   *
   * InernalFile is a source file from the eclipse workspace. Usage of
   * FileContent.createForExternalFileLocation() seems to be inappropropriate.
   */
  private static final IncludeFileContentProvider INCLUDE_FILE_PROVIDER = new InternalFileContentProvider() {
    private InternalFileContent getContentUncached(String path) {
      if (!getInclusionExists(path)) {
        return null;
      }

      char[] contents = CharArrayUtils.EMPTY;
      try {
        contents = FileUtils.readFileToString(new File(path), Charset.defaultCharset()).toCharArray();
      } catch (IOException e) {
        LOG.debug("EclipseCDTParser: Unable to read the content of {}", path);
      }
      return (InternalFileContent) FileContent.create(path, contents);
    }

    @Override
    public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
      return getContentUncached(path);
    }

    @Override
    public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
      return getContentUncached(astPath);
    }
  };

  private static final IParserLogService LOG_ADAPTER = new IParserLogService() {
    @Override
    public boolean isTracing() {
      return LOG.isDebugEnabled();
    }

    @Override
    public void traceLog(String msg) {
      LOG.debug(msg);
    }
  };

  private final String sourcePath;
  private final IASTTranslationUnit translationUnit;
  private final LinebasedOffsetTranslator offsetTranslator;

  public EclipseCDTParser(String path, String[] includePaths) throws EclipseCDTException {
    sourcePath = path;
    try {
      offsetTranslator = new LinebasedOffsetTranslator(path);
    } catch (IOException e) {
      throw new EclipseCDTException("Unable to read file " + path, e);
    }

    final IIndex ignoreIndex = null;
    final Map<String, String> ignoreDefinedMacros = null;
    final int noSpecialParseOptions = 0;
    IScannerInfo scannerInfo = new ScannerInfo(ignoreDefinedMacros, includePaths);
    FileContent fileContent = FileContent.createForExternalFileLocation(path);

    try {
      translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, scannerInfo, INCLUDE_FILE_PROVIDER,
          ignoreIndex, noSpecialParseOptions, LOG_ADAPTER);
    } catch (CoreException e) {
      throw new EclipseCDTException("Unable to parse file " + path, e);
    }

    logPreprocessorProblems();
  }

  private void logPreprocessorProblems() {
    if (LOG.isDebugEnabled()) {
      for (IASTProblem problem : translationUnit.getPreprocessorProblems()) {
        LOG.debug(problem.getMessageWithLocation());
      }
    }
  }

  /**
   * Traverse the given top-level declaration and find all IASTName nodes, which
   * describe this or arbitrary nested declaration
   */
  private Set<IASTName> getDeclarationNameNodes(IASTDeclaration declaration) {
    final Set<IASTName> declarationNameNodes = new HashSet<>();
    declaration.accept(new ASTGenericVisitor(true) {
      {
        includeInactiveNodes = true;
      }

      @Override
      public int visit(IASTName name) {
        if (name.isDeclaration()) {
          declarationNameNodes.add(name);
        }
        return PROCESS_CONTINUE;
      }

    });

    return declarationNameNodes;
  }

  private LinebasedTextRange newRange(IASTFileLocation location) throws EclipseCDTException {
    int globalOffset = location.getNodeOffset();
    int length = location.getNodeLength();
    LinebasedTextPointer start = offsetTranslator.newPointer(globalOffset);
    LinebasedTextPointer end = offsetTranslator.newPointer(globalOffset + length);
    return new LinebasedTextRange(start, end);
  }

  private LinebasedTextRange newRange(IASTName astName) throws EclipseCDTException {
    try {
      return newRange(astName.getFileLocation());
    } catch (EclipseCDTException e) {
      throw new EclipseCDTException("Unable to create LinebasedTextRange for symbol [name=" + astName + ", location="
          + astName.getFileLocation() + ", file=" + sourcePath + ", error=" + e.getMessage() + "]", e);
    }
  }

  private Boolean isLocatedInFile(IASTName name) {
    if (name == null || !name.isPartOfTranslationUnitFile()) {
      return false;
    }
    IASTFileLocation fileLocation = name.getFileLocation();
    return fileLocation != null && sourcePath.equals(fileLocation.getFileName()) && fileLocation.getNodeLength() > 0;
  }

  private Boolean isPartOfSymbolTable(IASTName name) {
    if (!isLocatedInFile(name)) {
      return false;
    }
    IASTImageLocation imageLocation = name.getImageLocation();
    return (imageLocation != null) && (imageLocation.getLocationKind() == IASTImageLocation.REGULAR_CODE);
  }

  public Map<LinebasedTextRange, Set<LinebasedTextRange>> generateSymbolTable() throws EclipseCDTException {
    // collect all declarations from the translation unit
    IASTDeclaration[] declarations = translationUnit.getDeclarations(true);
    final Set<IASTName> declarationNames = new HashSet<>();
    for (IASTDeclaration declaration : declarations) {
      declarationNames.addAll(getDeclarationNameNodes(declaration));
    }

    // collect all references to the declarations
    Map<LinebasedTextRange, Set<LinebasedTextRange>> table = new HashMap<>();
    for (IASTName declarationName : declarationNames) {
      IBinding binding = declarationName.resolveBinding();
      if (binding == null) {
        continue;
      }

      Set<LinebasedTextRange> references = new HashSet<>();
      for (IASTName referenceName : translationUnit.getReferences(binding)) {
        if (referenceName != null && isPartOfSymbolTable(referenceName)) {
          references.add(newRange(referenceName));
        }
      }

      if (isPartOfSymbolTable(declarationName)) {
        table.put(newRange(declarationName), references);
      } else if (!references.isEmpty()) {
        LinebasedTextRange randomReference = references.iterator().next();
        references.remove(randomReference);
        table.put(randomReference, references);
      } else {
        continue;
      }

    }
    return table;
  }
}
