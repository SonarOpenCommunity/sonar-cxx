/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.coverage.vs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.cxx.sensors.coverage.CoverageMeasures;
import org.sonar.cxx.sensors.coverage.CoverageParser;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class VisualStudioParser implements CoverageParser {

  private static void collectModuleMeasures(SMInputCursor module, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    while (module.getNext() != null) {
      handleModuleItems(module, coverageData);
    }
  }

  private static void handleModuleItems(SMInputCursor module, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    SMInputCursor child = module.childElementCursor();
    while (child.getNext() != null) {
      String name = child.getLocalName();
      if ("functions".equalsIgnoreCase(name)) {
        collectFunctionMeasures(child, coverageData);
      } else if ("source_files".equalsIgnoreCase(name)) {
        collectSourceFileMeasures(child, coverageData);
      }
    }
  }

  private static void collectSourceFileMeasures(SMInputCursor sourceFiles, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    SMInputCursor sourceFile = sourceFiles.childElementCursor("source_file");
    while (sourceFile.getNext() != null) {
      String id = sourceFile.getAttrValue("id");
      CoverageMeasures builder = coverageData.remove(id);
      if (builder == null) {
        builder = CoverageMeasures.create();
      }
      // replace id with path
      coverageData.put(sourceFile.getAttrValue("path"), builder);
    }
  }

  private static void collectFunctionMeasures(SMInputCursor functions, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    SMInputCursor function = functions.childElementCursor("function");
    while (function.getNext() != null) {
      collectRangeMeasures(function, coverageData);
    }
  }

  private static void collectRangeMeasures(SMInputCursor function, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    SMInputCursor range = function.childElementCursor("ranges").advance().childElementCursor("range");
    CoverageMeasures builder = null;
    var lastSourceId = "";

    while (range.getNext() != null) {
      String sourceId = range.getAttrValue("source_id");
      var startLine = Integer.parseInt(range.getAttrValue("start_line"));
      var endLine = Integer.parseInt(range.getAttrValue("end_line"));
      // value: yes/no/partial
      int covered = !"no".equalsIgnoreCase(range.getAttrValue("covered")) ? 1 : 0;

      if (!sourceId.equals(lastSourceId) || builder == null) {
        builder = coverageData.computeIfAbsent(sourceId, k -> CoverageMeasures.create());
        lastSourceId = sourceId;
      }

      while (startLine <= endLine) {
        builder.setHits(startLine, covered);
        startLine++;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, CoverageMeasures> parse(File report) {
    var coverageData = new HashMap<String, CoverageMeasures>();
    try {
      var parser = new StaxParser((SMHierarchicCursor rootCursor) -> {
        try {
          rootCursor.advance();
        } catch (com.ctc.wstx.exc.WstxEOFException e) {
          throw new EmptyReportException("Coverage report " + report + " result is empty (parsed by " + this + ")", e);
        }
        collectModuleMeasures(rootCursor.descendantElementCursor("module"), coverageData);
      });
      parser.parse(report);
    } catch (XMLStreamException e) {
      throw new InvalidReportException("Visual Studio coverage report '" + report + "' cannot be parsed.", e);
    }
    return coverageData;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
