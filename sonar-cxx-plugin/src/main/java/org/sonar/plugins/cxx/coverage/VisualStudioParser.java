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
package org.sonar.plugins.cxx.coverage;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class VisualStudioParser implements CoverageParser {

  private ModuleFileSystem fs;

  public VisualStudioParser(ModuleFileSystem fs) {
    this.fs = fs;
  }

  /**
   * {@inheritDoc}
   */
  public void parseReport(File xmlFile, final Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    CxxUtils.LOG.info("Parsing report '{}'", xmlFile);

    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();
        collectModuleMeasures(rootCursor.descendantElementCursor("module"), coverageData);
      }
    });
    parser.parse(xmlFile);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void collectModuleMeasures(SMInputCursor module, Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    while (module.getNext() != null) {
      handleModuleItems(module, coverageData);
    }
  }

  private void handleModuleItems(SMInputCursor module, Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    SMInputCursor child = module.childElementCursor();
    while (child.getNext() != null) {
      String name = child.getLocalName();
      if (name.equalsIgnoreCase("functions")) {
        collectFunctionMeasures(child, coverageData);
      } else if (name.equalsIgnoreCase("source_files")) {
        collectSourceFileMeasures(child, coverageData);
      }
    }
  }

  private void collectSourceFileMeasures(SMInputCursor sourceFiles, Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    SMInputCursor sourceFile = sourceFiles.childElementCursor("source_file");
    while (sourceFile.getNext() != null) {
      String id = sourceFile.getAttrValue("id");
      String path = sourceFile.getAttrValue("path");
      path = getCaseSensitiveFileName(path, fs.sourceDirs());
      coverageData.put(path, coverageData.remove(id)); // replace key id with path
    }
  }

  private void collectFunctionMeasures(SMInputCursor functions, Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    SMInputCursor function = functions.childElementCursor("function");
    while (function.getNext() != null) {
      int blocksCovered = Integer.parseInt(function.getAttrValue("blocks_covered"));
      int blocksNotCovered = Integer.parseInt(function.getAttrValue("blocks_not_covered"));
      collectRangeMeasures(function, coverageData, blocksCovered + blocksNotCovered, blocksCovered);
    }
  }

  private void collectRangeMeasures(SMInputCursor function, Map<String, CoverageMeasuresBuilder> coverageData, int conditions, int coveredConditions)
    throws XMLStreamException {
    SMInputCursor range = function.childElementCursor("ranges").advance().childElementCursor("range");
    CoverageMeasuresBuilder builder = null;
    String lastSourceId = "";

    while (range.getNext() != null) {
      String sourceId = range.getAttrValue("source_id");
      int startLine = Integer.parseInt(range.getAttrValue("start_line"));
      int endLine = Integer.parseInt(range.getAttrValue("end_line"));
      int covered = !range.getAttrValue("covered").equalsIgnoreCase("no") ? 1 : 0; // value: yes/no/partial

      if (!sourceId.equals(lastSourceId) || builder == null) {
        builder = coverageData.get(sourceId);
        if (builder == null) {
          builder = CoverageMeasuresBuilder.create();
          coverageData.put(sourceId, builder);
        }

        builder.setConditions(startLine - 1, conditions, coveredConditions);
        lastSourceId = sourceId;
      }

      while (startLine <= endLine) {
        builder.setHits(startLine, covered);
        startLine++;
      }
    }
  }

  /**
   * Supports full path and relative path in report.xml file.
   */
  private String getCaseSensitiveFileName(String file, List<java.io.File> sourceDirs) {
    // check whether the report file uses absolute path
    File targetfile = new java.io.File(file);
    if (targetfile.exists()) {
      file = getRealFileName(targetfile);
    } else {
      Iterator<java.io.File> iterator = sourceDirs.iterator();
      while (iterator.hasNext()) {
        targetfile = new java.io.File(iterator.next().getPath() + java.io.File.separatorChar + file);
        if (targetfile.exists()) {
          file = getRealFileName(targetfile);
          break;
        }
      }
    }
    return file;
  }

  /**
   * Find the case sensitive file name - tools might use different naming schema
   * e.g. VC HTML or build log report uses case insensitive file name (lower
   * case on windows)
   */
  private String getRealFileName(File filename) {
    try {
      return filename.getCanonicalFile().getAbsolutePath();
    } catch (java.io.IOException e) {
      CxxUtils.LOG.error("CoverageSensor GetRealFileName failed '{}'", e.toString());
    }
    return filename.getName();
  }

}
