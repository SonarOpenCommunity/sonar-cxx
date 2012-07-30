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
package org.sonar.plugins.cxx.coverage;

import static java.util.Locale.ENGLISH;
import static org.sonar.api.utils.ParsingUtils.parseNumber;

import java.io.File;
import java.text.ParseException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CoberturaParser implements CoverageParser {
  
  public void parseReport(File xmlFile, final Map<String, FileData> dataPerFilename)
    throws XMLStreamException
  {
    CxxUtils.LOG.info("Parsing report '{}'", xmlFile);
    
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
	rootCursor.advance();
	collectPackageMeasures(rootCursor.descendantElementCursor("package"), dataPerFilename);
      }
    });
    parser.parse(xmlFile);
  }

  private void collectPackageMeasures(SMInputCursor pack, Map<String, FileData> dataPerFilename)
    throws XMLStreamException
  {
    while (pack.getNext() != null) {
      collectFileMeasures(pack.descendantElementCursor("class"), dataPerFilename);
    }
  }

  private void collectFileMeasures(SMInputCursor clazz, Map<String, FileData> dataPerFilename)
    throws XMLStreamException
  {
    while (clazz.getNext() != null) {
      String fileName = clazz.getAttrValue("filename");
      FileData data = dataPerFilename.get(fileName);
      if (data == null) {
        data = new FileData(fileName);
        dataPerFilename.put(fileName, data);
      }
      collectFileData(clazz, data);
    }
  }

  private void collectFileData(SMInputCursor clazz, FileData data) throws XMLStreamException {
    SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");
    while (line.getNext() != null) {
      String lineId = line.getAttrValue("number");
      try {
	data.addLine(lineId, (int) parseNumber(line.getAttrValue("hits"), ENGLISH));
      } catch (ParseException e) {
	throw new XMLStreamException(e);
      }

      String isBranch = line.getAttrValue("branch");
      String text = line.getAttrValue("condition-coverage");
      if (StringUtils.equals(isBranch, "true") && StringUtils.isNotBlank(text)) {
        String[] conditions = StringUtils.split(StringUtils.substringBetween(text, "(", ")"), "/");
        data.addConditionLine(lineId, Integer.parseInt(conditions[0]), Integer.parseInt(conditions[1]),
            StringUtils.substringBefore(text, " "));
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
