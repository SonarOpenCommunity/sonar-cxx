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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.CxxSensor;

public class CxxValgrindSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.valgrind.reportPath";
  private static final String DEFAULT_REPORT_PATH = "valgrind-reports/valgrind-result-*.xml";
  
  public CxxValgrindSensor(RuleFinder ruleFinder, Configuration conf) {
    super(ruleFinder, conf);
  }
  
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }
  
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  protected void parseReport(final Project project, final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException
  {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      public void stream(SMHierarchicCursor rootCursor) throws javax.xml.stream.XMLStreamException {
        Set<ValgrindError> valgrindErrors = new HashSet<ValgrindError>();
        
        rootCursor.advance();
        SMInputCursor errorCursor = rootCursor.childElementCursor("error");
        while (errorCursor.getNext() != null) {
          valgrindErrors.add(parseErrorTag(errorCursor));
        }
        
        for (ValgrindError error: valgrindErrors) {
          ValgrindFrame frame = error.getLastOwnFrame(project.getFileSystem().getBasedir().getPath());
          if(frame != null) {
            saveViolation(project, context, CxxValgrindRuleRepository.KEY,
                          frame.getPath(), frame.line, error.kind, error.toString());
          }
        }
      }
    });
    
    parser.parse(report);
  }

  private ValgrindError parseErrorTag(SMInputCursor error)
    throws javax.xml.stream.XMLStreamException
  {
    SMInputCursor child = error.childElementCursor();
    ValgrindError valError = new ValgrindError();
    
    while (child.getNext() != null) {
      String tagName = child.getLocalName();
      if ("kind".equalsIgnoreCase(tagName)) {
        valError.kind = child.getElemStringValue();
      } else if (tagName.matches(".*what.*")) {
        valError.text = child.childElementCursor("text").advance().getElemStringValue();
      } else if ("stack".equalsIgnoreCase(tagName)) {
        valError.stack = parseStackTag(child);
      }
    }

    return valError;
  }

  private ValgrindStack parseStackTag(SMInputCursor child)
    throws javax.xml.stream.XMLStreamException
  {
    ValgrindStack stack = new ValgrindStack();
    SMInputCursor frameCursor = child.childElementCursor("frame");
    while (frameCursor.getNext() != null) {
      
      SMInputCursor frameChild = frameCursor.childElementCursor();
      ValgrindFrame frame = new ValgrindFrame();
      while (frameChild.getNext() != null) {
        String tagName = frameChild.getLocalName();
        
        if ("ip".equalsIgnoreCase(tagName)) {
          frame.ip = frameChild.getElemStringValue();
        } else if ("obj".equalsIgnoreCase(tagName)) {
          frame.obj = frameChild.getElemStringValue();
        } else if ("fn".equalsIgnoreCase(tagName)) {
          frame.fn = frameChild.getElemStringValue();
        } else if ("dir".equalsIgnoreCase(tagName)) {
          frame.dir = frameChild.getElemStringValue();
        } else if ("file".equalsIgnoreCase(tagName)) {
          frame.file = frameChild.getElemStringValue();
        } else if ("line".equalsIgnoreCase(tagName)) {
          frame.line = Integer.parseInt(frameChild.getElemStringValue());
        }
      }
      stack.frames.add(frame);
    }
    
    return stack;
  }

  static class ValgrindError {
    String text = "";
    ValgrindStack stack;
    String kind = "";
    
    @Override
    public String toString() { return text + "\n\n" + stack; }
    
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ValgrindError other = (ValgrindError) o;
      return hashCode() == other.hashCode();
    }

    public int hashCode() {
      return new HashCodeBuilder()
        .append(kind)
        .append(stack)
        .toHashCode();
    }

    ValgrindFrame getLastOwnFrame(String basedir) {
      for(ValgrindFrame frame: stack.frames){
        if (isInside(frame.dir, basedir)){
          return frame;
        }
      }
      return null;
    }
    
    private boolean isInside(String path, String folder) {
      return "".equals(path) ? false : path.startsWith(folder);
    }
  }
  
  static class ValgrindStack {
    List<ValgrindFrame> frames = new ArrayList<ValgrindFrame>();

    @Override
    public String toString() {
      StringBuilder res = new StringBuilder();
      for (ValgrindFrame frame: frames) {
        res.append(frame);
        res.append("\n");
      }
      return res.toString();
    }

    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      for(ValgrindFrame frame: frames) {
        builder.append(frame);
      }
      return builder.toHashCode();
    }
    
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ValgrindStack other = (ValgrindStack) o;
      return hashCode() == other.hashCode();
    }
  }
  
  static class ValgrindFrame extends Object{
    String ip = "?";
    String obj = "";
    String fn = "?";
    String dir = "";
    String file = "";
    int line = -1;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder().append(ip).append(": ").append(fn);
      if(isLocationKnown()){
        builder.append(" (")
          .append("".equals(file) ? ("in " + obj) : (file + ":" + getLine()))
          .append(")");
      }
      
      return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ValgrindFrame other = (ValgrindFrame) o;
      return hashCode() == other.hashCode();
    }

    public int hashCode() {
      return new HashCodeBuilder()
        .append(obj)
        .append(fn)
        .append(dir)
        .append(file)
        .append(line)
        .toHashCode();
    }
    
    String getPath() { return new File(dir, file).getPath(); }
    
    private boolean isLocationKnown() { return !("".equals(file) && "".equals(obj)); }
    
    private String getLine() { return line == -1 ? "" : Integer.toString(line); }
  }
}
