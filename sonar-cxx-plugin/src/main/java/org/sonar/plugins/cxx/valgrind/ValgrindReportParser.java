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
package org.sonar.plugins.cxx.valgrind;

import java.io.File;
import java.util.Set;
import java.util.HashSet;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.StaxParser;

class ValgrindReportParser {
  public ValgrindReportParser() {
  }
  
  /**
   * Parses given valgrind report
   */
  public Set<ValgrindError> parseReport(File report)
    throws javax.xml.stream.XMLStreamException
  {
    ValgrindReportStreamHandler streamHandler = new ValgrindReportStreamHandler();
    new StaxParser(streamHandler).parse(report);
    return streamHandler.valgrindErrors;
  }
  
  private class ValgrindReportStreamHandler implements StaxParser.XmlStreamHandler {
    Set<ValgrindError> valgrindErrors = new HashSet<ValgrindError>();
    
    /**
     * {@inheritDoc}
     */
    public void stream(SMHierarchicCursor rootCursor) throws javax.xml.stream.XMLStreamException {
      rootCursor.advance();
      SMInputCursor errorCursor = rootCursor.childElementCursor("error");
      
      while (errorCursor.getNext() != null) {
        valgrindErrors.add(parseErrorTag(errorCursor));
      }
    }
  };

  private ValgrindError parseErrorTag(SMInputCursor error)
    throws javax.xml.stream.XMLStreamException
  {
    SMInputCursor child = error.childElementCursor();
    
    String kind = null;
    String text = null;
    ValgrindStack stack = null;
    while (child.getNext() != null) {
      String tagName = child.getLocalName();
      if ("kind".equalsIgnoreCase(tagName)) {
        kind = child.getElemStringValue();
      } else if ("xwhat".equalsIgnoreCase(tagName)) {
        text = child.childElementCursor("text").advance().getElemStringValue();
      } else if ("what".equalsIgnoreCase(tagName)) {
        text = child.getElemStringValue();
      } else if ("stack".equalsIgnoreCase(tagName)) {
        stack = parseStackTag(child);
      }
    }
    
    if (text == null || kind == null || stack == null) {
      String msg = "Valgrind error is incomplete: we require all of 'kind', '*what.text' and 'stack'";
      child.throwStreamException(msg);
    }
    
    return new ValgrindError(kind, text, stack);
  }

  private ValgrindStack parseStackTag(SMInputCursor child)
    throws javax.xml.stream.XMLStreamException
  {
    ValgrindStack stack = new ValgrindStack();
    SMInputCursor frameCursor = child.childElementCursor("frame");
    while (frameCursor.getNext() != null) {
      
      SMInputCursor frameChild = frameCursor.childElementCursor();
      
      String ip = null;
      String obj = null;
      String fn = null;
      String dir = null;
      String file = null;
      int line = -1;
      
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
          line = Integer.parseInt(frameChild.getElemStringValue());
        }
      }
      stack.addFrame((new ValgrindFrame(ip, obj, fn, dir, file, line)));
    }
    
    return stack;
  } 
}
