/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.sensors.coverage.bullseye;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.PathUtils;
import org.sonar.cxx.sensors.coverage.CoverageMeasures;
import org.sonar.cxx.sensors.coverage.CoverageParser;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class BullseyeParser implements CoverageParser {

  private static final Logger LOG = LoggerFactory.getLogger(BullseyeParser.class);

  private String prevLine = "";
  private int totalconditions = 0;
  private int totalcoveredconditions = 0;

  private static String createRootPath(@Nullable String refPath) {
    if (refPath == null || refPath.isBlank()) {
      return ".";
    }
    return refPath.replace('\\', '/');
  }

  private static String createAbsolutePath(LinkedList<String> paths, String rootPath) {
    var path = String.join("/", paths);
    if (!(new File(path)).isAbsolute()) {
      path = rootPath + "/" + path;
    }
    return PathUtils.sanitize(path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, CoverageMeasures> parse(File report) {
    var coverageData = new HashMap<String, CoverageMeasures>();
    try {
      var topLevelparser = new StaxParser((SMHierarchicCursor rootCursor) -> {
        try {
          rootCursor.advance();
        } catch (com.ctc.wstx.exc.WstxEOFException e) {
          throw new EmptyReportException("Coverage report " + report + " result is empty (parsed by " + this + ")", e);
        }
        collectCoverageLeafNodes(rootCursor.getAttrValue("dir"), rootCursor.childElementCursor("src"), coverageData);
      });

      var parser = new StaxParser((SMHierarchicCursor rootCursor) -> {
        rootCursor.advance();
        collectCoverage2(rootCursor.getAttrValue("dir"), rootCursor.childElementCursor("folder"), coverageData);
      });

      topLevelparser.parse(report);
      parser.parse(report);
    } catch (XMLStreamException e) {
      throw new InvalidReportException("Bullseye coverage report '" + report + "' cannot be parsed.", e);
    }
    return coverageData;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void collectCoverageLeafNodes(String refPath, SMInputCursor folder,
    final Map<String, CoverageMeasures> coverageData) throws XMLStreamException {

    String rootPath = createRootPath(refPath);

    while (folder.getNext() != null) {
      var fileName = new File(rootPath, folder.getAttrValue("name"));
      recTreeTopWalk(fileName, folder, coverageData);
    }
  }

  private void recTreeTopWalk(File fileName, SMInputCursor folder,
    final Map<String, CoverageMeasures> coverageData) throws XMLStreamException {

    SMInputCursor child = folder.childElementCursor();
    while (child.getNext() != null) {
      var fileMeasuresBuilderIn = CoverageMeasures.create();

      funcWalk(child, fileMeasuresBuilderIn);
      coverageData.put(fileName.getPath(), fileMeasuresBuilderIn);
    }
  }

  private void collectCoverage2(String refPath, SMInputCursor folder, final Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {

    String rootPath = createRootPath(refPath);

    var paths = new LinkedList<String>();
    while (folder.getNext() != null) {
      String folderName = folder.getAttrValue("name");
      if (folderName.isBlank()) {
        folderName = ".";
      }
      paths.add(folderName);
      recTreeWalk(rootPath, folder, paths, coverageData);
      paths.removeLast();
    }
  }

  private void probWalk(SMInputCursor prob, CoverageMeasures fileMeasuresBuilderIn) throws XMLStreamException {
    String line = prob.getAttrValue("line");
    String kind = prob.getAttrValue("kind");
    String event = prob.getAttrValue("event");
    if (!line.equals(prevLine)) {
      saveConditions(fileMeasuresBuilderIn);
    }
    updateMeasures(kind, event, line, fileMeasuresBuilderIn);
    prevLine = line;
  }

  private void funcWalk(SMInputCursor func, CoverageMeasures fileMeasuresBuilderIn) throws XMLStreamException {
    SMInputCursor prob = func.childElementCursor("probe");
    while (prob.getNext() != null) {
      probWalk(prob, fileMeasuresBuilderIn);
    }
    saveConditions(fileMeasuresBuilderIn);
  }

  private void fileWalk(SMInputCursor file, CoverageMeasures fileMeasuresBuilderIn) throws XMLStreamException {
    SMInputCursor func = file.childElementCursor("fn");
    while (func.getNext() != null) {
      funcWalk(func, fileMeasuresBuilderIn);
    }
  }

  private void recTreeWalk(String refPath, SMInputCursor folder, LinkedList<String> paths,
    final Map<String, CoverageMeasures> coverageData) throws XMLStreamException {

    String rootPath = createRootPath(refPath);

    SMInputCursor child = folder.childElementCursor();
    while (child.getNext() != null) {
      String folderChildName = child.getLocalName();
      String name = child.getAttrValue("name");
      paths.add(name);
      if ("src".equalsIgnoreCase(folderChildName)) {
        String filePath = createAbsolutePath(paths, rootPath);
        var fileMeasuresBuilderIn = CoverageMeasures.create();
        fileWalk(child, fileMeasuresBuilderIn);
        coverageData.put(filePath, fileMeasuresBuilderIn);
      } else {
        recTreeWalk(rootPath, child, paths, coverageData);
      }
      paths.removeLast();
    }
  }

  private void saveConditions(CoverageMeasures fileMeasuresBuilderIn) {
    if (totalconditions > 0) {
      if (totalcoveredconditions == 0) {
        fileMeasuresBuilderIn.setHits(Integer.parseInt(prevLine), 0);
      } else {
        fileMeasuresBuilderIn.setHits(Integer.parseInt(prevLine), 1);
      }
      fileMeasuresBuilderIn.setConditions(Integer.parseInt(prevLine), totalconditions, totalcoveredconditions);
    }
    totalconditions = 0;
    totalcoveredconditions = 0;
  }

  private void updateMeasures(String kind, String event, String line, CoverageMeasures fileMeasuresBuilderIn) {

    switch (kind.toLowerCase(Locale.ENGLISH)) {
      case "decision", "condition":
        totalconditions += 2;
        setTotalCoveredConditions(event);
        break;
      case "catch", "for-range-body", "switch-label", "try":
        totalconditions++;
        if ("full".equalsIgnoreCase(event)) {
          totalcoveredconditions++;
        }
        break;
      case "function":
        var lineHits = 0;
        if ("full".equalsIgnoreCase(event)) {
          lineHits = 1;
        }
        fileMeasuresBuilderIn.setHits(Integer.parseInt(line), lineHits);
        break;
      case "constant":
        break;
      default:
        LOG.error("BullseyeParser unknown probe kind '{}'", kind);
    }
  }

  /**
   * @param event
   */
  private void setTotalCoveredConditions(String event) {
    switch (event.toLowerCase(Locale.ENGLISH)) {
      case "full":
        totalcoveredconditions += 2;
        break;
      case "true", "false":
        totalcoveredconditions++;
        break;
      case "none":
        // do nothing
        break;
      default:
        LOG.error("BullseyeParser unknown probe event '{}'", event);
    }
  }

}
