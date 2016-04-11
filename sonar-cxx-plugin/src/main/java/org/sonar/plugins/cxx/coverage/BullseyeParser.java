/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.coverage;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class BullseyeParser extends CxxCoverageParser {

  private String prevLine;
  private int totaldecisions;
  private int totalcovereddecisions;
  private int totalconditions;
  private int totalcoveredconditions;

  public BullseyeParser(final String baseDir) {
    super(baseDir);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final Project project, final SensorContext context, File report, final Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    CxxUtils.LOG.debug("Parsing 'Bullseye' format");
    StaxParser topLevelparser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();
        collectCoverageLeafNodes(rootCursor.getAttrValue("dir"), rootCursor.childElementCursor("src"), coverageData);
      }
    });

    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();
        collectCoverage2(rootCursor.getAttrValue("dir"), rootCursor.childElementCursor("folder"), coverageData);
      }
    });

    topLevelparser.parse(report);
    parser.parse(report);
  }

  private void collectCoverageLeafNodes(String refPath, SMInputCursor folder, final Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {

    refPath = ensureRefPathIsCorrect(refPath);

    while (folder.getNext() != null) {
      File fileName = new File(refPath, folder.getAttrValue("name"));
      recTreeTopWalk(fileName, folder, coverageData);
    }
  }

  private void recTreeTopWalk(File fileName, SMInputCursor folder, final Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {
    SMInputCursor child = folder.childElementCursor();
    while (child.getNext() != null) {
      CoverageMeasuresBuilder fileMeasuresBuilderIn = CoverageMeasuresBuilder.create();

      funcWalk(child, fileMeasuresBuilderIn);

      String normalPath = CxxUtils.normalizePath(fileName.getPath());
      if (normalPath != null) {
        coverageData.put(normalPath, fileMeasuresBuilderIn);
      }
    }
  }

  private void collectCoverage2(String refPath, SMInputCursor folder, final Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {

    refPath = ensureRefPathIsCorrect(refPath);

    LinkedList<String> path = new LinkedList<>();
    while (folder.getNext() != null) {
      String folderName = folder.getAttrValue("name");
      path.add(folderName);
      recTreeWalk(refPath, folder, path, coverageData);
      path.removeLast();
    }
  }

  private void probWalk(SMInputCursor prob, CoverageMeasuresBuilder fileMeasuresBuilderIn) throws XMLStreamException {
    String line = prob.getAttrValue("line");
    String kind = prob.getAttrValue("kind");
    String event = prob.getAttrValue("event");
    if (!line.equals(prevLine)) {
      saveConditions(fileMeasuresBuilderIn);
    }
    updateMeasures(kind, event, line, fileMeasuresBuilderIn);
    prevLine = line;
  }

  private void funcWalk(SMInputCursor func, CoverageMeasuresBuilder fileMeasuresBuilderIn) throws XMLStreamException {
    SMInputCursor prob = func.childElementCursor();
    while (prob.getNext() != null) {
      probWalk(prob, fileMeasuresBuilderIn);
    }
    saveConditions(fileMeasuresBuilderIn);
  }

  private void fileWalk(SMInputCursor file, CoverageMeasuresBuilder fileMeasuresBuilderIn) throws XMLStreamException {
    SMInputCursor func = file.childElementCursor();
    while (func.getNext() != null) {
      funcWalk(func, fileMeasuresBuilderIn);
    }
  }

  private void recTreeWalk(String refPath, SMInputCursor folder, List<String> path, final Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException {

    refPath = ensureRefPathIsCorrect(refPath);

    SMInputCursor child = folder.childElementCursor();
    while (child.getNext() != null) {
      String folderChildName = child.getLocalName();
      String name = child.getAttrValue("name");
      path.add(name);
      if ("src".equalsIgnoreCase(folderChildName)) {
        String fileName = "";
        Iterator<String> iterator = path.iterator();
        while (iterator.hasNext()) {
          fileName += iterator.next() + File.separator;
        }

        fileName = StringUtils.chop(fileName);

        if ((new File(fileName)).isAbsolute()) {
          refPath = "";
        }
        CoverageMeasuresBuilder fileMeasuresBuilderIn = CoverageMeasuresBuilder.create();
        fileWalk(child, fileMeasuresBuilderIn);
        String normalPath = CxxUtils.normalizePath(refPath + fileName);
        if (normalPath != null) {
          coverageData.put(normalPath, fileMeasuresBuilderIn);
        }

      } else {
        recTreeWalk(refPath, child, path, coverageData);
      }
      path.remove(path.size() - 1);
    }
  }

  private void saveConditions(CoverageMeasuresBuilder fileMeasuresBuilderIn) {
    if (totaldecisions > 0 || totalconditions > 0) {
      if (totalcovereddecisions == 0 && totalcoveredconditions == 0) {
        fileMeasuresBuilderIn.setHits(Integer.parseInt(prevLine), 0);
      } else {
        fileMeasuresBuilderIn.setHits(Integer.parseInt(prevLine), 1);
      }
      if (totalconditions > 0) {
        fileMeasuresBuilderIn.setConditions(Integer.parseInt(prevLine), totalconditions, totalcoveredconditions);
      } else {
        fileMeasuresBuilderIn.setConditions(Integer.parseInt(prevLine), 2, totalcovereddecisions);
      }
    }
    totaldecisions = 0;
    totalcovereddecisions = 0;
    totalconditions = 0;
    totalcoveredconditions = 0;
  }

  private void updateMeasures(String kind, String event, String line, CoverageMeasuresBuilder fileMeasuresBuilderIn) {

    if ("decision".equalsIgnoreCase(kind) || "condition".equalsIgnoreCase(kind)) {
      if ("condition".equalsIgnoreCase(kind)) {
        totalconditions += 2;
        totalcoveredconditions += 1;
        if ("full".equalsIgnoreCase(event)) {
          totalcoveredconditions += 1;
        }
        if ("none".equalsIgnoreCase(event)) {
          totalcoveredconditions -= 1;
        }
      } else {
        totaldecisions += 1;
        totalcovereddecisions = 1;
        if ("full".equalsIgnoreCase(event)) {
          totalcovereddecisions = 2;
        }
        if ("none".equalsIgnoreCase(event)) {
          totalcovereddecisions = 0;
        }
      }
    } else {
      if ("full".equalsIgnoreCase(event)) {
        fileMeasuresBuilderIn.setHits(Integer.parseInt(line), 1);
      } else {
        fileMeasuresBuilderIn.setHits(Integer.parseInt(line), 0);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private String ensureRefPathIsCorrect(String refPath) {
    if (refPath == null || refPath.length() == 0 || refPath.endsWith(File.separator)) {
      return refPath;
    }

    return refPath + File.separatorChar;
  }
}
