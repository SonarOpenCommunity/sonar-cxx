/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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

  private Path baseDir = Paths.get(".");

  public CoberturaParser() {
    // no operation but necessary for list of coverage parsers
  }

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

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(File report, final Map<String, CoverageMeasures> coverageData)
    throws XMLStreamException {
    LOG.debug("Parsing 'Cobertura' format");
    baseDir = Paths.get(".");

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

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void readBaseDir(SMInputCursor source) throws XMLStreamException {
    while (source.getNext() != null) {
      String sourceValue = source.getElemStringValue().trim();
      if (!sourceValue.isEmpty()) {
        // join with . to handle also special cases like drive letter only, e.g. C:
        baseDir = Paths.get(sourceValue, ".").normalize();
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

  /**
   * Join two paths
   *
   * path1    | path2    | result
   * ---------|----------|-------
   * empty    | empty    | empty
   * empty    | absolute | absolute path2
   * empty    | relative | relative path2
   * absolute | empty    | empty
   * relative | empty    | empty
   * absolute | absolute | absolute path2
   * absolute | relative | absolute path1 + relative path2
   * relative | absolute | absolute path2
   * relative | relative | relative path1 + relative path2
   * 
   * @param path1 first path
   * @param path2 second path to be joined to first path
   * @return joined path as string
   */
  public static String join(Path path1, Path path2) {
    if (path2.toString().isEmpty()) {
      return "";
    }
    if (!path1.isAbsolute()) {
      path1 = Paths.get(".", path1.toString());
    }
    if (!path2.isAbsolute()) {
      path2 = Paths.get(".", path2.toString());
    }

    Path result = path1.resolve(path2).normalize();
    if (!result.isAbsolute()) {
      result = Paths.get(".", result.toString());
    }

    return result.toString();
  }

  private void collectFileMeasures(SMInputCursor clazz, Map<String, CoverageMeasures> coverageData)
          throws XMLStreamException {
    while (clazz.getNext() != null) {
      String normalPath = join(baseDir, Paths.get(clazz.getAttrValue("filename")));
      if (!normalPath.isEmpty()) {
        CoverageMeasures builder = coverageData.get(normalPath);
        if (builder == null) {
          builder = CoverageMeasures.create();
          coverageData.put(normalPath, builder);
        }
        collectFileData(clazz, builder);
      }
    }
  }

}
