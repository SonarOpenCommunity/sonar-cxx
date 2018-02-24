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
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class VisualStudioParser extends CxxCoverageParser {

  private static final Logger LOG = Loggers.get(VisualStudioParser.class);

  public VisualStudioParser() {
    // no operation but necessary for list of coverage parsers 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report, final Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    LOG.debug("Parsing 'Visual Studio' format");
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();
        collectModuleMeasures(rootCursor.descendantElementCursor("module"), coverageData);
      }
    });
    parser.parse(report);
  }

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
    String lastSourceId = "";

    while (range.getNext() != null) {
      String sourceId = range.getAttrValue("source_id");
      int startLine = Integer.parseInt(range.getAttrValue("start_line"));
      int endLine = Integer.parseInt(range.getAttrValue("end_line"));
      // value: yes/no/partial
      int covered = !"no".equalsIgnoreCase(range.getAttrValue("covered")) ? 1 : 0;

      if (!sourceId.equals(lastSourceId) || builder == null) {
        builder = coverageData.get(sourceId);
        if (builder == null) {
          builder = CoverageMeasures.create();
          coverageData.put(sourceId, builder);
        }

        lastSourceId = sourceId;
      }

      while (startLine <= endLine) {
        builder.setHits(startLine, covered);
        startLine++;
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
