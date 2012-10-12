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
package org.sonar.plugins.cxx.ast.visitors.internal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * Writes AST node information in XML format to output file
 * @author Przemyslaw Kociolek
 */
public class XmlNodeWriter {

  private static final int TAB_INCREMENT = 1;
  private static final String QUOTE_CHAR = "\"";
  
  private int tabCount          = 0;
  private BufferedWriter output = null;

  /**
   * Ctor
   * @param fileName  XML output file name
   * @throws IOException  when file could not be created or opened
   */
  public XmlNodeWriter(String fileName) throws IOException {
      output = new BufferedWriter( new FileWriter(fileName) );
      output.write("<?xml version=\"1.0\"?>");
      output.newLine();
  }
  
  /**
   * @return true if output file has been created
   */
  public boolean isValid() {
    return output != null;
  }
  
  public int openNode(IASTNode node, String tokenValue) {
    try {
      String nodeName = getNodeName(node);
      tabCount += TAB_INCREMENT;
      writeTabs(tabCount);
      output.write("<" + nodeName);
      output.write(" token" + "=" + QUOTE_CHAR + tokenValue + QUOTE_CHAR);
      output.write(">");
      output.newLine();
    } catch (IOException e) {
      CxxUtils.LOG.error(e.getMessage());
      return ASTVisitor.PROCESS_ABORT;
    }
        
    return ASTVisitor.PROCESS_CONTINUE;
  }
    
  private String getNodeName(Object node) {    
    return node.getClass().getSimpleName();
  }

  public int closeNode(String nodeName) {
    try {
      writeTabs(tabCount);
      output.write("</" + nodeName + ">");
      output.newLine();
      tabCount -= TAB_INCREMENT;
    } catch (IOException e) {
      CxxUtils.LOG.error(e.getMessage());
      return ASTVisitor.PROCESS_ABORT;
    }
    return ASTVisitor.PROCESS_CONTINUE;
  }
  
  private void writeTabs(int numberOfTabs) throws IOException {
    for(int i = 0; i < numberOfTabs; ++i) {
     output.write("  "); 
    }
  }

  public void saveToFile() throws IOException {
    output.close();
  }

}