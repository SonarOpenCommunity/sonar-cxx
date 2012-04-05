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

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.valgrind.reportPath";
  private static final String DEFAULT_REPORT_PATH = "valgrind-reports/valgrind-result-*.xml";
  
  /**
   * {@inheritDoc}
   */
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
      /**
       * {@inheritDoc}
       */
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
    
    String kind = null;
    String text = null;
    ValgrindStack stack = null;
    while (child.getNext() != null) {
      String tagName = child.getLocalName();
      if ("kind".equalsIgnoreCase(tagName)) {
        kind = child.getElemStringValue();
      } else if (tagName.matches(".*what.*")) {
        text = child.childElementCursor("text").advance().getElemStringValue();
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
      
      String ip = "?";
      String obj = "";
      String fn = "?";
      String dir = "";
      String file = "";
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

  /**
   * Represents an error found by valgrind. It always has an id,
   * a descriptive text and a stack trace.
   */
  static class ValgrindError {
    private String kind;
    private String text;
    private ValgrindStack stack;
    
    /**
     * Constructs a ValgrindError out of the given attributes
     * @param kind The kind of error, plays the role of an id
     * @param text Description of the error
     * @param stack The associated call stack
     */
    public ValgrindError(String kind, String text, ValgrindStack stack) {
      this.kind = kind;
      this.text = text;
      this.stack = stack;
    }

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

    @Override
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
  
  /** Represents a call stack, consists basically of a list of frames */
  static class ValgrindStack {
    private List<ValgrindFrame> frames = new ArrayList<ValgrindFrame>();
    
    /**
     * Adds a stack frame to this call stack
     * @param frame The frame to add
     */
    public void addFrame(ValgrindFrame frame) { frames.add(frame); }
    
    @Override
    public String toString() {
      StringBuilder res = new StringBuilder();
      for (ValgrindFrame frame: frames) {
        res.append(frame);
        res.append("\n");
      }
      return res.toString();
    }

    @Override
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
  
  /**
   * Represents a stack frame. Overwrites equality. Has a string serialization that
   * resembles the valgrind output in textual mode.
   */
  static class ValgrindFrame{
    private String ip;
    private String obj;
    private String fn;
    private String dir;
    private String file;
    private int line;

    /**
     * Constucts a stack frame with given attributes. Its perfectly valid if some of them
     * are empty or dont carry meaningfull information.
     */
    public ValgrindFrame(String ip, String obj, String fn, String dir, String file, int line){
      this.ip = ip;
      this.obj = obj;
      this.fn = fn;
      this.dir = dir;
      this.file = file;
      this.line = line;
    }
    
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

    @Override
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
