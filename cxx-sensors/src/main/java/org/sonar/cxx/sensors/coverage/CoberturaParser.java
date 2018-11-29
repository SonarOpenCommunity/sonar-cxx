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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class CoberturaParser extends CxxCoverageParser {

  private static final Logger LOG = Loggers.get(CoberturaParser.class);
  private static final Pattern CONDITION_PATTERN = Pattern.compile("\\((.*?)\\)");

  private static void collectFileData(SMInputCursor clazz, CoverageMeasures builder) throws XMLStreamException {
    SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");

    while (line.getNext() != null) {
      int lineId = Integer.parseInt(line.getAttrValue("number"));
      long noHits = Long.parseLong(line.getAttrValue("hits"));
      if (noHits > Integer.MAX_VALUE) {
        LOG.warn("Truncating the actual number of hits ({}) to the maximum number supported by Sonar ({})",
          noHits, Integer.MAX_VALUE);
        noHits = Integer.MAX_VALUE;
      }
      builder.setHits(lineId, (int) noHits);

      String isBranch = line.getAttrValue("branch");
      String text = line.getAttrValue("condition-coverage");
      if (text != null && "true".equals(isBranch) && !text.trim().isEmpty()) {
        Matcher m = CONDITION_PATTERN.matcher(text);
        if (m.find()) {
          String[] conditions = m.group(1).split("/");
          builder.setConditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
        }
      }
    }
  }
  private Optional<Path> baseDir;

  public CoberturaParser() {
    // no operation but necessary for list of coverage parsers
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(File report, final Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    LOG.debug("Parsing 'Cobertura' format");
    baseDir = Optional.empty();

    StaxParser sourceParser = new StaxParser((SMHierarchicCursor rootCursor) -> {
      rootCursor.advance();
      readBaseDir(rootCursor.descendantElementCursor("source"));
    });
    sourceParser.parse(report);

    StaxParser packageParser = new StaxParser((SMHierarchicCursor rootCursor) -> {
      rootCursor.advance();
      collectPackageMeasures(rootCursor.descendantElementCursor("package"), coverageData);
    });
    packageParser.parse(report);
  }

  private void readBaseDir(SMInputCursor source) throws XMLStreamException {
    while (source.getNext() != null) {
      String sourceValue = source.getElemStringValue().trim();
      if (!sourceValue.isEmpty()) {
        baseDir = Optional.of(Paths.get(sourceValue));
        break;
      }
    }
  }

  private void collectPackageMeasures(SMInputCursor pack, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    while (pack.getNext() != null) {
      collectFileMeasures(pack.descendantElementCursor("class"), coverageData);
    }
  }

  private String buildPath(String filename) {
    Path result = Paths.get(filename);
    if (baseDir.isPresent()) {
      result = baseDir.get().resolve(result);
    }
    return result.normalize().toString();
  }

  private void collectFileMeasures(SMInputCursor clazz, Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    while (clazz.getNext() != null) {
      String normalPath = buildPath(clazz.getAttrValue("filename"));
      CoverageMeasures builder = coverageData.get(normalPath);
      if (builder == null) {
        builder = CoverageMeasures.create();
        coverageData.put(normalPath, builder);
      }
      collectFileData(clazz, builder);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
