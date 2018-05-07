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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;

class ValgrindReportParser {

  public ValgrindReportParser() {
    // do nothing - just for reference
  }

  /**
   * Parses given valgrind report
   *
   * @param report full path of XML report
   * @return Set<ValgrindError>
   * @exception XMLStreamException javax.xml.stream.XMLStreamException
   */
  public Set<ValgrindError> processReport(File report) throws XMLStreamException {
    ValgrindReportStreamHandler streamHandler = new ValgrindReportStreamHandler();
    new StaxParser(streamHandler).parse(report);
    return streamHandler.valgrindErrors;
  }

  private static class ValgrindReportStreamHandler implements StaxParser.XmlStreamHandler {

    private final Set<ValgrindError> valgrindErrors = new HashSet<>();

    private static ValgrindStack parseStackTag(SMInputCursor child) throws XMLStreamException {
      ValgrindStack stack = new ValgrindStack();
      SMInputCursor frameCursor = child.childElementCursor("frame");
      while (frameCursor.getNext() != null) {

        SMInputCursor frameChild = frameCursor.childElementCursor();

        String ip = null;
        String obj = null;
        String fn = null;
        String dir = null;
        String file = null;
        String line = null;

        while (frameChild.getNext() != null) {
          String tagName = frameChild.getLocalName();

          if ("ip".equalsIgnoreCase(tagName)) {
            ip = frameChild.getElemStringValue();
          } else if ("obj".equalsIgnoreCase(tagName)) {
            obj = frameChild.getElemStringValue();
          } else if ("fn".equalsIgnoreCase(tagName)) {
            fn = frameChild.getElemStringValue();
          } else if ("dir".equalsIgnoreCase(tagName)) {
            dir = frameChild.getElemStringValue();
          } else if ("file".equalsIgnoreCase(tagName)) {
            file = frameChild.getElemStringValue();
          } else if ("line".equalsIgnoreCase(tagName)) {
            line = frameChild.getElemStringValue();
          }
        }
        stack.addFrame(new ValgrindFrame(ip, obj, fn, dir, file, line));
      }

      return stack;
    }

    private static ValgrindError parseErrorTag(SMInputCursor error) throws XMLStreamException {
      SMInputCursor child = error.childElementCursor();

      String kind = null;
      String text = null;
      List<String> details = new ArrayList<>();
      List<ValgrindStack> stacks = new ArrayList<>();
      while (child.getNext() != null) {
        String tagName = child.getLocalName();
        if ("kind".equalsIgnoreCase(tagName)) {
          kind = child.getElemStringValue();
        } else if ("xwhat".equalsIgnoreCase(tagName)) {
          text = child.childElementCursor("text").advance().getElemStringValue();
        } else if ("what".equalsIgnoreCase(tagName)) {
          text = child.getElemStringValue();
        } else if ("auxwhat".equalsIgnoreCase(tagName)) {
          details.add(child.getElemStringValue());
        } else if ("stack".equalsIgnoreCase(tagName)) {
          stacks.add(parseStackTag(child));
        }
      }

      if (text == null || kind == null || stacks.isEmpty()) {
        String msg = "Valgrind error is incomplete: we require all of 'kind', '*what.text' and 'stack'";
        child.throwStreamException(msg);
      }

      if (!details.isEmpty()) {
        text = text + ": " + String.join("; ", details);
      }

      return new ValgrindError(kind, text, stacks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
      try {
        rootCursor.advance();
      } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
        throw new EmptyReportException("Cannot read Valgrind report", eofExc);
      }

      SMInputCursor errorCursor = rootCursor.childElementCursor("error");

      while (errorCursor.getNext() != null) {
        valgrindErrors.add(parseErrorTag(errorCursor));
      }
    }
  }
}
